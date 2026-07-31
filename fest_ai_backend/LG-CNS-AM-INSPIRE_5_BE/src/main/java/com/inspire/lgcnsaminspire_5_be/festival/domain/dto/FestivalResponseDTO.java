package com.inspire.lgcnsaminspire_5_be.festival.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class FestivalResponseDTO {
    // 축제 필드
    private String contentId; // TourAPI 고유 ID
    private String title; // 축제명
    private String region; // 시도명 코드
    private String imageUrl; // 이미지 링크
    private String addr; // 주소
    private String content; // TourAPI 개요 항목 (상세 내용)
    private String aiInfo; // ChatGPT API가 응답한 AI 답변 (유모차/주차장 정보)
    private String startDate; // 축제 시작일
    private String endDate; // 축제 종료일

    // 찜 연동 필드
    private boolean isBookmarked; // 찜한 경우 true, 안 했으면 false
    private Long bookmarkId; // 찜한 경우에만 PK 값 존재, 안 했으면 null
}
