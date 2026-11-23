package com.inspire.lgcnsaminspire_5_be.rag.store;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 임베딩 저장소를 들고 있는 곳.
 * 서버가 뜰 때 저장 파일이 있으면 불러오고, 색인을 다시 만들면 새 저장소로 통째로 바꾼다.
 * 밖에서는 VectorStore 인터페이스로만 쓰기 때문에 나중에 pgvector 등으로 바꿔도 검색 코드는 그대로다.
 */
@Slf4j
@Component
public class RagVectorStoreHolder {

    private static final int EMBEDDING_BATCH_SIZE = 20;

    private final EmbeddingModel embeddingModel;
    private final Path storePath;
    private final AtomicReference<SimpleVectorStore> current = new AtomicReference<>();
    private volatile int documentCount = -1; // -1: 파일에서 불러와서 개수를 모름

    public RagVectorStoreHolder(EmbeddingModel embeddingModel,
            @Value("${rag.store-path}") String storePath) {
        this.embeddingModel = embeddingModel;
        this.storePath = Path.of(storePath);
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        if (Files.exists(this.storePath)) {
            store.load(this.storePath.toFile());
            log.info("RAG 저장 파일을 불러왔습니다: {}", this.storePath);
        } else {
            documentCount = 0;
            log.info("RAG 저장 파일이 없습니다. POST /api/rag/index 로 색인을 만들어 주세요.");
        }
        current.set(store);
    }

    public VectorStore get() {
        return current.get();
    }

    public boolean isEmpty() {
        return documentCount == 0;
    }

    /** 문서 조각을 모두 임베딩해서 새 저장소를 만들고, 파일에 저장한 뒤 교체한다. */
    public synchronized int rebuild(List<Document> documents) {
        SimpleVectorStore next = SimpleVectorStore.builder(embeddingModel).build();
        for (int from = 0; from < documents.size(); from += EMBEDDING_BATCH_SIZE) {
            next.add(documents.subList(from, Math.min(documents.size(), from + EMBEDDING_BATCH_SIZE)));
        }

        try {
            Path parent = storePath.toAbsolutePath().getParent();
            Files.createDirectories(parent);
            File temp = Files.createTempFile(parent, "vector-store", ".json").toFile();
            next.save(temp);
            Files.move(temp.toPath(), storePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("RAG 저장 파일을 쓰지 못했습니다: " + storePath, e);
        }

        current.set(next);
        documentCount = documents.size();
        return documents.size();
    }
}
