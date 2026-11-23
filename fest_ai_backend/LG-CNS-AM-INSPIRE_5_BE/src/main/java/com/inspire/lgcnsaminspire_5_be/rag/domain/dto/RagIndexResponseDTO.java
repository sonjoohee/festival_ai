package com.inspire.lgcnsaminspire_5_be.rag.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagIndexResponseDTO {
    private int festivalChunks;
    private int faqChunks;
    private int totalChunks;
    private long elapsedMillis;
}
