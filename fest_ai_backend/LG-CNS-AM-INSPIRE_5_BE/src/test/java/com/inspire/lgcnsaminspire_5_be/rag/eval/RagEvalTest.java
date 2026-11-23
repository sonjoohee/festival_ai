package com.inspire.lgcnsaminspire_5_be.rag.eval;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.retry.support.RetryTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspire.lgcnsaminspire_5_be.rag.domain.dto.RagAskResponseDTO;
import com.inspire.lgcnsaminspire_5_be.rag.service.RagAskService;
import com.inspire.lgcnsaminspire_5_be.rag.store.RagVectorStoreHolder;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * 평가 질문(src/test/resources/rag/eval-questions.json)으로 RAG가 몇 개를 맞히는지 잰다.
 *
 * 질문 종류
 *   - answer: 개요에 답이 있음. 기대한 출처(sourceId)를 찾아 답해야 정답
 *   - no_info: 축제는 맞지만 개요에 답이 없음 (예: 주차장). 검색은 통과해도 되고 LLM이 NO_INFO를 내야 정답
 *   - unrelated: 서비스와 상관없음 (예: 환율). 검색 단계에서 막혀야 정답
 *
 * 1단계: 질문마다 비슷한 조각 4개와 점수를 한 번만 받아 두고, 기준값을 바꿔 가며 answer와 unrelated 정답 수를 센다.
 *        no_info는 검색 점수가 answer와 비슷하게 높아서 기준값으로 거를 대상이 아니므로 세지 않는다.
 *        최고 점수가 여러 기준값에서 같으면 그 구간의 가운데 값을 고른다 (양쪽 질문과 거리를 비슷하게 둔다).
 * 2단계: 고른 기준값으로 실제 질문 답변 흐름(LLM 포함)을 돌려 모든 질문을 채점한다.
 *
 * 실행: ./gradlew evalRag  (서버와 DB는 필요 없음. POST /api/rag/index 로 만든 색인 파일을 읽는다)
 */
@Tag("eval")
class RagEvalTest {

    private static final int TOP_K = 4;
    private static final long LLM_INTERVAL_MILLIS = 4000; // 무료 등급 분당 호출 한도 대비

    record EvalQuestion(String question, String expect, String sourceId) {
        boolean expectsAnswer() {
            return "answer".equals(expect);
        }

        boolean isUnrelated() {
            return "unrelated".equals(expect);
        }
    }

    record Hit(String sourceId, double score) {
    }

    @Test
    void 기준값별_정답_수() throws Exception {
        Dotenv env = Dotenv.configure().ignoreIfMissing().load();
        String apiKey = env.get("OPEN_AI_KEY", "");
        Path storePath = Path.of(env.get("RAG_STORE_PATH", "rag-data/vector-store.json"));
        Assumptions.assumeTrue(!apiKey.isBlank(), "OPEN_AI_KEY가 없어 평가를 건너뜁니다.");
        Assumptions.assumeTrue(Files.exists(storePath), "색인 파일이 없습니다. 먼저 POST /api/rag/index 를 실행하세요.");

        OpenAiApi api = OpenAiApi.builder()
                .baseUrl(env.get("OPEN_AI_BASE_URL", "https://generativelanguage.googleapis.com/v1beta/openai"))
                .completionsPath(env.get("OPEN_AI_CHAT_PATH", "/chat/completions"))
                .embeddingsPath(env.get("OPEN_AI_EMBEDDING_PATH", "/embeddings"))
                .apiKey(apiKey)
                .build();
        // 평가는 기다려도 되므로 모델이 바쁠 때(503) 서버보다 오래 다시 시도한다
        RetryTemplate retry = RetryTemplate.builder().maxAttempts(5).exponentialBackoff(2000, 2, 20000).build();
        OpenAiEmbeddingModel embeddingModel = new OpenAiEmbeddingModel(api, MetadataMode.EMBED,
                OpenAiEmbeddingOptions.builder().model(env.get("OPEN_AI_EMBEDDING_MODEL", "gemini-embedding-001")).build(),
                retry);
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(OpenAiChatOptions.builder().model(env.get("OPEN_AI_MODEL", "gemini-3.1-flash-lite")).build())
                .retryTemplate(retry)
                .build();

        RagVectorStoreHolder holder = new RagVectorStoreHolder(embeddingModel, storePath.toString());
        List<EvalQuestion> questions = loadQuestions();

        // 1단계: 질문마다 조각 점수를 한 번만 받아 둔다
        List<List<Hit>> hitsPerQuestion = new ArrayList<>();
        for (EvalQuestion q : questions) {
            List<Document> docs = holder.get().similaritySearch(SearchRequest.builder()
                    .query(q.question()).topK(TOP_K).similarityThreshold(0).build());
            hitsPerQuestion.add(docs.stream()
                    .map(d -> new Hit(String.valueOf(d.getMetadata().get("sourceId")), d.getScore()))
                    .toList());
        }

        StringBuilder report = new StringBuilder();
        long answerCount = questions.stream().filter(EvalQuestion::expectsAnswer).count();
        long unrelatedCount = questions.stream().filter(EvalQuestion::isUnrelated).count();
        report.append("## 질문별 1순위 점수\n\n| 기대 | 1순위 점수 | 1순위 출처 | 기대 출처 | 질문 |\n| --- | --- | --- | --- | --- |\n");
        for (int i = 0; i < questions.size(); i++) {
            EvalQuestion q = questions.get(i);
            Hit top = hitsPerQuestion.get(i).isEmpty() ? new Hit("-", 0) : hitsPerQuestion.get(i).get(0);
            report.append("| %s | %.3f | %s | %s | %s |\n".formatted(q.expect(), top.score(), top.sourceId(),
                    q.sourceId() == null ? "-" : q.sourceId(), q.question()));
        }

        report.append("\n## 1단계: 검색 단계 기준값별 정답 수\n\n")
                .append("| 기준값 | answer 찾음 (/%d) | unrelated 막힘 (/%d) | 합계 |\n| --- | --- | --- | --- |\n"
                        .formatted(answerCount, unrelatedCount));
        List<Double> bestThresholds = new ArrayList<>();
        int bestTotal = -1;
        for (int step = 50; step <= 80; step += 2) {
            double t = step / 100.0;
            int answerOk = 0, unrelatedOk = 0;
            for (int i = 0; i < questions.size(); i++) {
                EvalQuestion q = questions.get(i);
                List<Hit> passed = hitsPerQuestion.get(i).stream().filter(h -> h.score() >= t).toList();
                if (q.expectsAnswer()) {
                    if (passed.stream().anyMatch(h -> h.sourceId().equals(q.sourceId()))) {
                        answerOk++;
                    }
                } else if (q.isUnrelated() && passed.isEmpty()) {
                    unrelatedOk++;
                }
            }
            int total = answerOk + unrelatedOk;
            report.append("| %.2f | %d | %d | %d |\n".formatted(t, answerOk, unrelatedOk, total));
            if (total > bestTotal) {
                bestTotal = total;
                bestThresholds.clear();
            }
            if (total == bestTotal) {
                bestThresholds.add(t);
            }
        }
        double bestThreshold = bestThresholds.get((bestThresholds.size() - 1) / 2);

        // 2단계: 고른 기준값으로 LLM까지 포함해 실제 답변 흐름을 돌린다
        double finalThreshold = Double.parseDouble(env.get("RAG_EVAL_THRESHOLD", String.valueOf(bestThreshold)));
        RagAskService askService = new RagAskService(ChatClient.builder(chatModel).build(), holder, TOP_K, finalThreshold);
        report.append("\n## 2단계: 기준값 %.2f로 LLM까지 돌린 결과\n\n| 기대 | 결과 | 맞음 | 질문 | 답변 |\n| --- | --- | --- | --- | --- |\n"
                .formatted(finalThreshold));
        int finalOk = 0, errors = 0;
        for (EvalQuestion q : questions) {
            RagAskResponseDTO res;
            try {
                res = askService.ask(q.question());
            } catch (RuntimeException e) {
                // 재시도해도 모델이 응답하지 않으면 이 질문만 에러로 적고 넘어간다
                errors++;
                report.append("| %s | 에러 | X | %s | %s |\n".formatted(q.expect(), q.question(), e.getClass().getSimpleName()));
                continue;
            }
            boolean ok = q.expectsAnswer()
                    ? res.isFound() && res.getSources().stream().anyMatch(s -> s.getSourceId().equals(q.sourceId()))
                    : !res.isFound();
            if (ok) {
                finalOk++;
            }
            String answer = res.getAnswer().replace("\n", " ").replace("|", "/");
            report.append("| %s | %s | %s | %s | %s |\n".formatted(q.expect(), res.isFound() ? "답변" : "정보 없음",
                    ok ? "O" : "X", q.question(), answer.length() > 80 ? answer.substring(0, 80) + "..." : answer));
            if (res.isFound()) {
                Thread.sleep(LLM_INTERVAL_MILLIS);
            }
        }
        report.append("\n**최종: 질문 %d개 중 %d개 정답 (기준값 %.2f, 모델 응답 에러 %d개)**\n"
                .formatted(questions.size(), finalOk, finalThreshold, errors));

        Path out = Path.of("build/reports/rag-eval.md");
        Files.createDirectories(out.getParent());
        Files.writeString(out, report.toString());
        System.out.println(report);
        System.out.println("결과 파일: " + out.toAbsolutePath());
    }

    private List<EvalQuestion> loadQuestions() throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/rag/eval-questions.json")) {
            return new ObjectMapper().readValue(in, new TypeReference<>() {
            });
        }
    }
}
