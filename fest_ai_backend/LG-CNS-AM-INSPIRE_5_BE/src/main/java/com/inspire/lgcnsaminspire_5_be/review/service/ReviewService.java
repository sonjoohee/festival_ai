package com.inspire.lgcnsaminspire_5_be.review.service;

import com.inspire.lgcnsaminspire_5_be.auth.domain.entity.UserEntity;
import com.inspire.lgcnsaminspire_5_be.auth.repository.UserRepository;
import com.inspire.lgcnsaminspire_5_be.festival.domain.entity.FestivalEntity;
import com.inspire.lgcnsaminspire_5_be.festival.repository.FestivalRepository;
import com.inspire.lgcnsaminspire_5_be.festival.service.FestivalService;
import com.inspire.lgcnsaminspire_5_be.review.domain.dto.ReviewRequestDTO;
import com.inspire.lgcnsaminspire_5_be.review.domain.dto.ReviewResponseDTO;
import com.inspire.lgcnsaminspire_5_be.review.domain.entity.ReviewEntity;
import com.inspire.lgcnsaminspire_5_be.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final FestivalRepository festivalRepository;
    private final FestivalService festivalService;

    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getReviews(String contentId, Pageable pageable) {
        Page<ReviewEntity> reviews = reviewRepository.findByFestivalEntity_ContentId(contentId, pageable);
        return reviews.map(ReviewResponseDTO::fromEntity);
    }

    @Transactional
    public ReviewResponseDTO createReview(String contentId, ReviewRequestDTO requestDto, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        FestivalEntity festival = festivalRepository.findByContentId(contentId)
                .orElseGet(() -> festivalService.findOrCreateFestival(contentId));

        if (festival == null) {
            throw new RuntimeException("축제 정보를 가져올 수 없습니다. 외부 API 상태를 확인해주세요.");
        }

        if (reviewRepository.existsByFestivalEntity_ContentIdAndUser_UserId(contentId, user.getUserId())) {
            throw new IllegalStateException("이미 해당 축제에 대한 리뷰를 작성했습니다.");
        }

        ReviewEntity review = ReviewEntity.builder()
                .user(user)
                .festivalEntity(festival)
                .rating(requestDto.getRating())
                .comment(requestDto.getComment())
                .build();

        ReviewEntity savedReview = reviewRepository.save(review);
        return ReviewResponseDTO.fromEntity(savedReview);
    }

    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, ReviewRequestDTO requestDto, Long userId) {
        log.debug(">>>> ReviewService updateReview - reviewId: {}, userId: {}", reviewId, userId);
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("ID " + reviewId + "에 해당하는 리뷰를 찾을 수 없습니다."));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        log.debug(">>>> review author userId: {}", review.getUser().getUserId());
        if (!review.getUser().getUserId().equals(userId)) {
            log.warn(">>>> review update failed: permission denied. reviewId: {}, userId: {}", reviewId, userId);
            throw new AccessDeniedException("리뷰를 수정할 권한이 없습니다.");
        }

        review.update(requestDto.getRating(), requestDto.getComment());
        log.info(">>>> review update success. reviewId: {}", reviewId);
        return ReviewResponseDTO.fromEntity(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("ID " + reviewId + "에 해당하는 리뷰를 찾을 수 없습니다."));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        
        if (!review.getUser().getUserId().equals(userId) && !isAdmin) {
            throw new AccessDeniedException("리뷰를 삭제할 권한이 없습니다.");
        }

        reviewRepository.delete(review);
    }
}