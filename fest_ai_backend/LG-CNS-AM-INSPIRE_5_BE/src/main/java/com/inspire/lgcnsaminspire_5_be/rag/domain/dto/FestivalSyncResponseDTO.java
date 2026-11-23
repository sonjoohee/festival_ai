package com.inspire.lgcnsaminspire_5_be.rag.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FestivalSyncResponseDTO {
    private int listed;   // TourAPI 목록에서 받은 축제 수
    private int created;  // 새로 저장한 축제 수
    private int skipped;  // 이미 DB에 있던 축제 수
    private int failed;   // 상세 정보를 못 받은 축제 수
}
