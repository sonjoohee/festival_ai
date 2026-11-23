package com.inspire.lgcnsaminspire_5_be.rag.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

import com.inspire.lgcnsaminspire_5_be.rag.domain.dto.RagAskResponseDTO;
import com.inspire.lgcnsaminspire_5_be.rag.store.RagVectorStoreHolder;

/**
 * 외부 API 없이 검색 → 프롬프트 → 답변 → 출처 흐름을 끝까지 확인한다.
 * 임베딩은 두 글자씩 묶은 해시 벡터로, LLM은 정해진 답을 돌려주는 가짜로 대신한다.
 */
class RagAskServiceTest {

    @TempDir
    Path tempDir;

    private final FakeChatModel chatModel = new FakeChatModel();
    private RagVectorStoreHolder holder;
    private RagAskService service;

    @BeforeEach
    void setUp() {
        holder = new RagVectorStoreHolder(new BigramEmbeddingModel(), tempDir.resolve("store.json").toString());
        service = new RagAskService(ChatClient.builder(chatModel).build(), holder, 2, 0.3);
    }

    @Test
    void 색인이_비어_있으면_LLM을_부르지_않고_정보_없음() {
        RagAskResponseDTO res = service.ask("보령 머드축제 언제 열려?");

        assertThat(res.isFound()).isFalse();
        assertThat(res.getAnswer()).isEqualTo(RagAskService.NO_INFO_ANSWER);
        assertThat(chatModel.prompts).isEmpty();
    }

    @Test
    void 비슷한_조각을_프롬프트에_넣고_출처와_함께_답한다() {
        indexSamples();
        chatModel.reply = "보령머드축제는 2026.07.18부터 열립니다 [1]";

        RagAskResponseDTO res = service.ask("보령머드축제 기간 알려줘");

        assertThat(res.isFound()).isTrue();
        assertThat(res.getAnswer()).isEqualTo("보령머드축제는 2026.07.18부터 열립니다 [1]");
        assertThat(res.getSources()).isNotEmpty();
        assertThat(res.getSources().get(0).getTitle()).isEqualTo("보령머드축제");
        assertThat(res.getSources().get(0).getSourceId()).isEqualTo("100");
        // 본문이 없는 조각은 머리말을 그대로 미리보기로 쓴다
        assertThat(res.getSources().get(0).getSnippet()).startsWith("[보령머드축제]");
        assertThat(chatModel.prompts).hasSize(1);
        assertThat(chatModel.prompts.get(0)).contains("[1] [보령머드축제]", "[질문]", "보령머드축제 기간 알려줘");
    }

    @Test
    void 관련_조각이_없으면_LLM을_부르지_않는다() {
        indexSamples();

        RagAskResponseDTO res = service.ask("환율 얼마야");

        assertThat(res.isFound()).isFalse();
        assertThat(res.getSources()).isEmpty();
        assertThat(chatModel.prompts).isEmpty();
    }

    @Test
    void 모델이_NO_INFO라고_하면_정보_없음으로_바꾼다() {
        indexSamples();
        chatModel.reply = "NO_INFO";

        RagAskResponseDTO res = service.ask("보령머드축제 입장료 얼마야");

        assertThat(res.isFound()).isFalse();
        assertThat(res.getAnswer()).isEqualTo(RagAskService.NO_INFO_ANSWER);
        assertThat(res.getSources()).isEmpty();
    }

    @Test
    void contentId를_주면_그_축제_조각만_찾는다() {
        indexSamples();
        chatModel.reply = "답변 [1]";

        RagAskResponseDTO other = service.ask("보령머드축제 기간 알려줘", "200", null);
        RagAskResponseDTO same = service.ask("보령머드축제 기간 알려줘", "100", null);

        assertThat(other.isFound()).isFalse(); // 부산불꽃축제(200) 범위에는 머드축제 조각이 없다
        assertThat(same.getSources()).extracting(s -> s.getSourceId()).containsOnly("100");
    }

    @Test
    void 상세_페이지에서는_축제_이름을_붙여_검색하고_프롬프트에도_알린다() {
        indexSamples();
        chatModel.reply = "7월 27일까지 합니다 [1]";

        // 가짜 임베딩은 짧은 질문의 점수가 낮아서 이 테스트만 기준값을 낮춘다
        RagAskService lowThreshold = new RagAskService(ChatClient.builder(chatModel).build(), holder, 2, 0.15);
        RagAskResponseDTO withoutTitle = lowThreshold.ask("언제까지 해?", "100", null);
        RagAskResponseDTO withTitle = lowThreshold.ask("언제까지 해?", "100", "보령머드축제");

        assertThat(withoutTitle.isFound()).isFalse(); // 이름 없이는 조각과 겹치는 글자가 없다
        assertThat(withTitle.isFound()).isTrue();
        assertThat(chatModel.prompts.get(chatModel.prompts.size() - 1)).contains("'보령머드축제' 상세 페이지에서 나왔다");
    }

    @Test
    void contentId에_조건식_문자를_넣어도_다른_축제가_섞이지_않는다() {
        indexSamples();
        chatModel.reply = "답변 [1]";

        RagAskResponseDTO res = service.ask("보령머드축제 기간 알려줘", "200' || sourceId == '100", null);

        assertThat(res.isFound()).isFalse();
    }

    @Test
    void 답변에_인용된_조각만_출처로_남긴다() {
        List<com.inspire.lgcnsaminspire_5_be.rag.domain.dto.RagSourceDTO> sources = List.of(
                com.inspire.lgcnsaminspire_5_be.rag.domain.dto.RagSourceDTO.builder().number(1).build(),
                com.inspire.lgcnsaminspire_5_be.rag.domain.dto.RagSourceDTO.builder().number(2).build(),
                com.inspire.lgcnsaminspire_5_be.rag.domain.dto.RagSourceDTO.builder().number(3).build());

        assertThat(RagAskService.citedOnly("답 [2]. 또 답 [3]", sources)).extracting(s -> s.getNumber()).containsExactly(2, 3);
        assertThat(RagAskService.citedOnly("인용 표시 없는 답", sources)).hasSize(3);
    }

    @Test
    void 색인_파일을_저장하고_다시_불러온다() {
        indexSamples();
        chatModel.reply = "7월 18일부터 열립니다 [1]";

        RagVectorStoreHolder reloaded = new RagVectorStoreHolder(new BigramEmbeddingModel(),
                tempDir.resolve("store.json").toString());

        assertThat(reloaded.isEmpty()).isFalse();
        assertThat(new RagAskService(ChatClient.builder(chatModel).build(), reloaded, 2, 0.3)
                .ask("보령머드축제 기간").isFound()).isTrue();
    }

    private void indexSamples() {
        holder.rebuild(List.of(
                new Document("[보령머드축제] 기간: 2026.07.18 ~ 2026.07.27 | 장소: 충남 보령시 대천해수욕장",
                        Map.of("sourceType", "festival", "title", "보령머드축제", "sourceId", "100", "chunkIndex", 0)),
                new Document("[부산불꽃축제] 기간: 2026.11.07 ~ 2026.11.07 | 장소: 부산 광안리해수욕장",
                        Map.of("sourceType", "festival", "title", "부산불꽃축제", "sourceId", "200", "chunkIndex", 0))));
    }

    /** 두 글자 묶음을 해시해서 만든 벡터. 글자가 많이 겹칠수록 코사인 유사도가 높다. */
    static class BigramEmbeddingModel implements EmbeddingModel {
        private static final int DIM = 512;

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            List<Embedding> embeddings = new ArrayList<>();
            for (int i = 0; i < request.getInstructions().size(); i++) {
                embeddings.add(new Embedding(vector(request.getInstructions().get(i)), i));
            }
            return new EmbeddingResponse(embeddings);
        }

        @Override
        public float[] embed(Document document) {
            return vector(document.getText());
        }

        private float[] vector(String text) {
            float[] v = new float[DIM];
            String t = text.replaceAll("\\s+", "");
            for (int i = 0; i + 1 < t.length(); i++) {
                v[Math.floorMod(t.substring(i, i + 2).hashCode(), DIM)] += 1;
            }
            return v;
        }
    }

    static class FakeChatModel implements ChatModel {
        final List<String> prompts = new ArrayList<>();
        String reply = "";

        @Override
        public ChatResponse call(Prompt prompt) {
            prompts.add(prompt.getContents());
            return new ChatResponse(List.of(new Generation(new AssistantMessage(reply))));
        }
    }
}
