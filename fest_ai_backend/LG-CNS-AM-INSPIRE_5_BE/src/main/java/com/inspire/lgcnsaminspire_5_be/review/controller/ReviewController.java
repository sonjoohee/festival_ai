package com.inspire.lgcnsaminspire_5_be.review.controller;

import com.inspire.lgcnsaminspire_5_be.review.domain.dto.ReviewRequestDTO;
import com.inspire.lgcnsaminspire_5_be.review.domain.dto.ReviewResponseDTO;
import com.inspire.lgcnsaminspire_5_be.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/festivals/{contentId}/reviews")
    public ResponseEntity<Page<ReviewResponseDTO>> getReviews(
            @PathVariable("contentId") String contentId, // 👈 "contentId" 명시
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt,desc") String sortBy) {

        String[] sortParams = sortBy.split(",");
        Sort sort = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(page, limit, sort);

        Page<ReviewResponseDTO> reviews = reviewService.getReviews(contentId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping(value = "/festivals/{contentId}/reviews")
    public ResponseEntity<ReviewResponseDTO> createReview(
            @PathVariable("contentId") String contentId, // 👈 "contentId" 명시 (핵심 해결 부분!)
            @Valid @RequestBody ReviewRequestDTO requestDto,
            @AuthenticationPrincipal Long userId) {
        ReviewResponseDTO responseDto = reviewService.createReview(contentId, requestDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable("reviewId") Long reviewId, // 👈 "reviewId" 명시
            @Valid @RequestBody ReviewRequestDTO requestDto,
            @AuthenticationPrincipal Long userId) {
        ReviewResponseDTO responseDto = reviewService.updateReview(reviewId, requestDto, userId);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable("reviewId") Long reviewId, // 👈 "reviewId" 명시
            @AuthenticationPrincipal Long userId) {
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }
}