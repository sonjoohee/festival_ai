package com.inspire.lgcnsaminspire_5_be.rag.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.inspire.lgcnsaminspire_5_be.rag.domain.dto.RagAskResponseDTO;
import com.inspire.lgcnsaminspire_5_be.rag.domain.dto.RagSourceDTO;
import com.inspire.lgcnsaminspire_5_be.rag.store.RagVectorStoreHolder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RagAskService {

    public static final String NO_INFO_ANSWER = "등록된 축제 정보에서 관련 내용을 찾지 못해 답할 수 없습니다.";
    static final String NO_INFO_MARKER = "NO_INFO";
    private static final int SNIPPET_LENGTH = 120;
    private static final Pattern CITATION = Pattern.compile("\\[(\\d+)]");

    private static final String SYSTEM_PROMPT = """
            너는 축제 정보 서비스의 안내 도우미다.
            반드시 [참고 문서]에 적힌 내용만 근거로 한국어로 답한다.
            - 참고 문서에 없는 일정, 장소, 요금, 시설 정보는 추측하거나 지어내지 않는다.
            - 참고 문서로 답할 수 없으면 다른 말 없이 NO_INFO 한 단어만 출력한다.
            - 답한 문장 끝에는 근거가 된 문서 번호를 [1], [2]처럼 붙인다.
            - 서론이나 인사말 없이 3~5문장 안에서 답한다.
            """;

    private final ChatClient chatClient;
    private final RagVectorStoreHolder vectorStoreHolder;
    private final int topK;
    private final double similarityThreshold;

    public RagAskService(ChatClient chatClient,
            RagVectorStoreHolder vectorStoreHolder,
            @Value("${rag.top-k:4}") int topK,
            @Value("${rag.similarity-threshold:0.68}") double similarityThreshold) {
        this.chatClient = chatClient;
        this.vectorStoreHolder = vectorStoreHolder;
        this.topK = topK;
        this.similarityThreshold = similarityThreshold;
    }

    public RagAskResponseDTO ask(String question) {
        return ask(question, null, null);
    }

    /**
     * @param contentId     있으면 그 축제 조각과 FAQ 안에서만 찾는다 (상세 페이지 질문창용). 없으면 전체에서 찾는다.
     * @param festivalTitle contentId 축제의 이름. 상세 페이지에서는 "언제까지 해?"처럼 이름 없이 묻기 때문에
     *                      검색어와 프롬프트에 이름을 붙여 그 축제 얘기로 알아듣게 한다.
     */
    public RagAskResponseDTO ask(String question, String contentId, String festivalTitle) {
        if (vectorStoreHolder.isEmpty()) {
            log.info("RAG 질문 - 색인된 문서가 없어 바로 정보 없음으로 답함");
            return noInfo();
        }

        // 1. 질문과 비슷한 조각 찾기 (기준값 미만은 버림)
        boolean hasTitle = festivalTitle != null && !festivalTitle.isBlank();
        SearchRequest.Builder search = SearchRequest.builder()
                .query(hasTitle ? festivalTitle.strip() + " " + question : question)
                .topK(topK)
                .similarityThreshold(similarityThreshold);
        if (contentId != null && !contentId.isBlank()) {
            // 문자열을 이어 붙이지 않고 빌더로 만들어 contentId 값이 조건식을 깨지 못하게 한다
            FilterExpressionBuilder b = new FilterExpressionBuilder();
            search.filterExpression(b.or(
                    b.eq("sourceId", contentId.strip()),
                    b.eq("sourceType", "faq")).build());
        }
        List<Document> hits = vectorStoreHolder.get().similaritySearch(search.build());
        log.info("RAG 질문 - 질문 {}자, 검색된 조각 {}개, 점수 {}", question.length(), hits.size(),
                hits.stream().map(d -> "%.3f".formatted(d.getScore())).toList());

        // 2. 비슷한 조각이 없으면 LLM을 부르지 않는다
        if (hits.isEmpty()) {
            return noInfo();
        }

        // 3. 찾은 조각을 번호를 붙여 프롬프트에 넣는다
        StringBuilder context = new StringBuilder();
        List<RagSourceDTO> sources = new ArrayList<>();
        for (int i = 0; i < hits.size(); i++) {
            Document doc = hits.get(i);
            int number = i + 1;
            context.append("[").append(number).append("] ").append(doc.getText()).append("\n\n");
            sources.add(toSource(number, doc));
        }

        String answer = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user("""
                        [참고 문서]
                        %s
                        [질문]
                        %s%s
                        """.formatted(context, question,
                        hasTitle ? "\n(이 질문은 '" + festivalTitle.strip() + "' 상세 페이지에서 나왔다)" : ""))
                .options(ChatOptions.builder().temperature(0.2).build())
                .call()
                .content();

        // 4. 모델이 문서로 답할 수 없다고 하면 정보 없음으로 돌려준다
        if (answer == null || answer.isBlank() || answer.contains(NO_INFO_MARKER)) {
            log.info("RAG 질문 - 조각은 찾았지만 모델이 답할 근거가 없다고 판단함");
            return noInfo();
        }

        return RagAskResponseDTO.builder()
                .answer(answer.strip())
                .found(true)
                .sources(citedOnly(answer, sources))
                .build();
    }

    /** 답변에 [1], [2]로 인용된 조각만 출처로 남긴다. 인용 표시가 없으면 검색된 조각을 모두 보여 준다. */
    static List<RagSourceDTO> citedOnly(String answer, List<RagSourceDTO> sources) {
        Matcher m = CITATION.matcher(answer);
        Set<Integer> cited = new java.util.HashSet<>();
        while (m.find()) {
            cited.add(Integer.parseInt(m.group(1)));
        }
        List<RagSourceDTO> filtered = sources.stream()
                .filter(s -> cited.contains(s.getNumber()))
                .collect(Collectors.toList());
        return filtered.isEmpty() ? sources : filtered;
    }

    private RagAskResponseDTO noInfo() {
        return RagAskResponseDTO.builder()
                .answer(NO_INFO_ANSWER)
                .found(false)
                .sources(List.of())
                .build();
    }

    private RagSourceDTO toSource(int number, Document doc) {
        Map<String, Object> meta = doc.getMetadata();
        String text = doc.getText() == null ? "" : doc.getText();
        return RagSourceDTO.builder()
                .number(number)
                .sourceType(String.valueOf(meta.getOrDefault("sourceType", "")))
                .title(String.valueOf(meta.getOrDefault("title", "")))
                .sourceId(String.valueOf(meta.getOrDefault("sourceId", "")))
                .score(doc.getScore() == null ? 0 : Math.round(doc.getScore() * 1000) / 1000.0)
                .snippet(text.length() > SNIPPET_LENGTH ? text.substring(0, SNIPPET_LENGTH) + "..." : text)
                .build();
    }
}
