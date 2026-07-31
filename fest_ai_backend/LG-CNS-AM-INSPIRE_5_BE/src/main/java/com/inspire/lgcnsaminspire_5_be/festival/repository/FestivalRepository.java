package com.inspire.lgcnsaminspire_5_be.festival.repository;

import com.inspire.lgcnsaminspire_5_be.festival.domain.entity.FestivalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FestivalRepository extends JpaRepository<FestivalEntity, Long> {
    Optional<FestivalEntity> findByContentId(String contentId);
}
