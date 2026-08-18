package com.inspire.lgcnsaminspire_5_be.review.repository;

import com.inspire.lgcnsaminspire_5_be.review.domain.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    Page<ReviewEntity> findByFestivalEntity_ContentId(String contentId, Pageable pageable);

    boolean existsByFestivalEntity_ContentIdAndUser_UserId(String contentId, Long userId);

}