package com.inspire.lgcnsaminspire_5_be.rag.chunk;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import com.inspire.lgcnsaminspire_5_be.festival.domain.entity.FestivalEntity;

class FestivalDocumentLoaderTest {

    // DB와 파일을 읽지 않는 메서드만 테스트하므로 repository, resolver는 null로 둔다
    private final FestivalDocumentLoader loader = new FestivalDocumentLoader(null, null, 100, 10);

    @Test
    void 축제_조각마다_이름_기간_장소가_붙고_aiInfo는_빠진다() {
        FestivalEntity festival = FestivalEntity.builder()
                .contentId("123")
                .title("테스트 불꽃축제")
                .addr("부산 수영구")
                .startDate("20261101")
                .endDate("20261102")
                .content("첫 문단입니다.<br>둘째 문단입니다.")
                .aiInfo("AI가 만든 추측 정보")
                .build();

        List<Document> docs = loader.toDocuments(festival);

        assertThat(docs).hasSize(1);
        Document doc = docs.get(0);
        assertThat(doc.getText()).startsWith("[테스트 불꽃축제] 기간: 2026.11.01 ~ 2026.11.02 | 장소: 부산 수영구");
        assertThat(doc.getText()).contains("첫 문단입니다.\n둘째 문단입니다.").doesNotContain("<br>", "AI가 만든");
        assertThat(doc.getMetadata()).containsEntry("sourceType", "festival").containsEntry("sourceId", "123");
    }

    @Test
    void 개요가_없어도_기본_정보_조각은_만든다() {
        FestivalEntity festival = FestivalEntity.builder().contentId("9").title("빈 축제").build();

        List<Document> docs = loader.toDocuments(festival);

        assertThat(docs).hasSize(1);
        assertThat(docs.get(0).getText()).startsWith("[빈 축제]");
    }

    @Test
    void FAQ는_소제목_하나가_한_건이다() {
        String md = "# 제목\n\n## 찜하기는 어떻게 하나요?\n로그인 후 누릅니다.\n\n## 후기는 누가 쓰나요?\n로그인한 사용자입니다.\n";

        List<Document> docs = loader.faqToDocuments("faq.md", md);

        assertThat(docs).extracting(Document::getText).containsExactly(
                "[FAQ] 찜하기는 어떻게 하나요?\n로그인 후 누릅니다.",
                "[FAQ] 후기는 누가 쓰나요?\n로그인한 사용자입니다.");
        assertThat(docs.get(0).getMetadata()).containsEntry("sourceType", "faq").containsEntry("title", "찜하기는 어떻게 하나요?");
    }
}
