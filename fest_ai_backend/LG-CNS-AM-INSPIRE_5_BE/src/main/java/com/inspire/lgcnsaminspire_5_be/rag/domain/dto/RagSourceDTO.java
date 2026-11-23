package com.inspire.lgcnsaminspire_5_be.rag.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 답변에 쓴 문서 조각. number는 답변 안의 [1], [2] 표시와 같다. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagSourceDTO {
    private int number;
    private String sourceType; // festival 또는 faq
    private String title;
    private String sourceId;   // 축제면 contentId, FAQ면 파일 이름
    private double score;      // 질문과 비슷한 정도 (코사인 유사도)
    private String snippet;
}
