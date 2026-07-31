package com.inspire.lgcnsaminspire_5_be.festival.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FestivalRequestDTO {
    private String contentId; // TourAPI 고유 ID
    private String title; // 축제명
    private String region; // 시도명 코드
    private String imageUrl; // 이미지 링크
}
