package com.inspire.lgcnsaminspire_5_be.review.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.inspire.lgcnsaminspire_5_be.review.domain.entity.ReviewEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private Long reviewId;
    private String author;
    private int rating;
    private String comment;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public static ReviewResponseDTO fromEntity(ReviewEntity review) {
        return ReviewResponseDTO.builder()
                .reviewId(review.getId())
                .author(review.getUser().getLoginId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}