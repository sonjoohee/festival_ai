package com.inspire.lgcnsaminspire_5_be.rag.chunk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.inspire.lgcnsaminspire_5_be.festival.domain.entity.FestivalEntity;
import com.inspire.lgcnsaminspire_5_be.festival.repository.FestivalRepository;

/**
 * 축제 테이블과 FAQ 파일을 검색용 조각(Document)으로 바꾼다.
 * aiInfo 컬럼은 LLM이 만든 추측 섞인 글이라 근거 문서에서 뺀다.
 */
@Component
public class FestivalDocumentLoader {

    public static final String SOURCE_FESTIVAL = "festival";
    public static final String SOURCE_FAQ = "faq";

    private final FestivalRepository festivalRepository;
    private final ResourcePatternResolver resourceResolver;
    private final TextChunker chunker;

    public FestivalDocumentLoader(FestivalRepository festivalRepository,
            ResourcePatternResolver resourceResolver,
            @Value("${rag.chunk-size:500}") int chunkSize,
            @Value("${rag.chunk-overlap:50}") int chunkOverlap) {
        this.festivalRepository = festivalRepository;
        this.resourceResolver = resourceResolver;
        this.chunker = new TextChunker(chunkSize, chunkOverlap);
    }

    public List<Document> loadFestivalDocuments() {
        List<Document> documents = new ArrayList<>();
        for (FestivalEntity festival : festivalRepository.findAll()) {
            documents.addAll(toDocuments(festival));
        }
        return documents;
    }

    List<Document> toDocuments(FestivalEntity festival) {
        String title = nullToEmpty(festival.getTitle());
        // 조각마다 축제 이름, 기간, 장소를 붙여야 "OO축제 언제야?" 같은 질문에서 그 축제 조각이 걸린다
        String header = "[%s] 기간: %s ~ %s | 장소: %s".formatted(
                title,
                formatDate(festival.getStartDate()),
                formatDate(festival.getEndDate()),
                nullToEmpty(festival.getAddr()));

        List<String> bodies = chunker.split(stripHtml(festival.getContent()));
        if (bodies.isEmpty()) {
            bodies = List.of("");
        }

        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < bodies.size(); i++) {
            String text = bodies.get(i).isEmpty() ? header : header + "\n" + bodies.get(i);
            documents.add(new Document(text, metadata(SOURCE_FESTIVAL, title, festival.getContentId(), i)));
        }
        return documents;
    }

    public List<Document> loadFaqDocuments() {
        List<Document> documents = new ArrayList<>();
        try {
            for (Resource resource : resourceResolver.getResources("classpath:rag/faq/*.md")) {
                String markdown = resource.getContentAsString(StandardCharsets.UTF_8);
                documents.addAll(faqToDocuments(resource.getFilename(), markdown));
            }
        } catch (IOException e) {
            throw new IllegalStateException("FAQ 파일을 읽지 못했습니다.", e);
        }
        return documents;
    }

    /** "## 질문" 제목 하나를 FAQ 한 건으로 본다. */
    List<Document> faqToDocuments(String fileName, String markdown) {
        List<Document> documents = new ArrayList<>();
        for (String section : markdown.split("(?m)^## ")) {
            int lineEnd = section.indexOf('\n');
            if (section.startsWith("#") || lineEnd < 0) {
                continue; // 파일 맨 위 "# 제목" 부분
            }
            String question = section.substring(0, lineEnd).strip();
            List<String> bodies = chunker.split(section.substring(lineEnd + 1));
            for (int i = 0; i < bodies.size(); i++) {
                String text = "[FAQ] " + question + "\n" + bodies.get(i);
                documents.add(new Document(text, metadata(SOURCE_FAQ, question, nullToEmpty(fileName), i)));
            }
        }
        return documents;
    }

    private Map<String, Object> metadata(String sourceType, String title, String sourceId, int chunkIndex) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceType", sourceType);
        metadata.put("title", title);
        metadata.put("sourceId", nullToEmpty(sourceId));
        metadata.put("chunkIndex", chunkIndex);
        return metadata;
    }

    static String stripHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("<[^>]+>", "")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&");
    }

    private static String formatDate(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.length() != 8) {
            return nullToEmpty(yyyymmdd);
        }
        return yyyymmdd.substring(0, 4) + "." + yyyymmdd.substring(4, 6) + "." + yyyymmdd.substring(6);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
