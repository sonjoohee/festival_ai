package com.inspire.lgcnsaminspire_5_be.rag.domain.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagAskResponseDTO {
    private String answer;
    private boolean found;          // false면 관련 문서가 없어 답하지 못한 것
    private List<RagSourceDTO> sources;
}
