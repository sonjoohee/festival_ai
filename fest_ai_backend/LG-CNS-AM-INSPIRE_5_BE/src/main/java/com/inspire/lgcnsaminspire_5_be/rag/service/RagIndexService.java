package com.inspire.lgcnsaminspire_5_be.rag.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inspire.lgcnsaminspire_5_be.rag.chunk.FestivalDocumentLoader;
import com.inspire.lgcnsaminspire_5_be.rag.domain.dto.RagIndexResponseDTO;
import com.inspire.lgcnsaminspire_5_be.rag.store.RagVectorStoreHolder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagIndexService {

    private final FestivalDocumentLoader documentLoader;
    private final RagVectorStoreHolder vectorStoreHolder;

    /** 축제 테이블과 FAQ를 조각내고 임베딩해서 저장소를 새로 만든다. */
    @Transactional(readOnly = true)
    public RagIndexResponseDTO rebuildIndex() {
        long start = System.currentTimeMillis();

        List<Document> festivalDocs = documentLoader.loadFestivalDocuments();
        List<Document> faqDocs = documentLoader.loadFaqDocuments();

        List<Document> all = new ArrayList<>(festivalDocs);
        all.addAll(faqDocs);
        int total = vectorStoreHolder.rebuild(all);

        long elapsed = System.currentTimeMillis() - start;
        log.info("RAG 색인 완료 - 축제 조각 {}개, FAQ 조각 {}개, {}ms", festivalDocs.size(), faqDocs.size(), elapsed);

        return RagIndexResponseDTO.builder()
                .festivalChunks(festivalDocs.size())
                .faqChunks(faqDocs.size())
                .totalChunks(total)
                .elapsedMillis(elapsed)
                .build();
    }
}
