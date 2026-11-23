package com.inspire.lgcnsaminspire_5_be.rag.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.inspire.lgcnsaminspire_5_be.festival.domain.dto.FestivalResponseDTO;
import com.inspire.lgcnsaminspire_5_be.festival.service.FestivalService;
import com.inspire.lgcnsaminspire_5_be.rag.domain.dto.FestivalSyncResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * TourAPI에서 축제 목록을 받아 DB에 없는 축제를 한 번에 저장한다.
 * RAG 색인 대상을 "누군가 상세 페이지를 연 축제"에서 "오늘 이후 열리는 축제 전체"로 넓히려는 용도.
 * TourAPI 개발 계정은 하루 호출 한도가 있어 한 번에 저장하는 개수를 제한한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FestivalSyncService {

    static final int MAX_LIMIT = 200;
    private static final long CALL_INTERVAL_MILLIS = 100;

    private final FestivalService festivalService;

    public FestivalSyncResponseDTO sync(String region, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_LIMIT));
        List<FestivalResponseDTO> listed = festivalService.getFestivalsByRegion(region);

        int created = 0, skipped = 0, failed = 0;
        for (FestivalResponseDTO festival : listed.subList(0, Math.min(listed.size(), safeLimit))) {
            try {
                if (festivalService.saveFromTourApiIfAbsent(
                        festival.getContentId(), festival.getStartDate(), festival.getEndDate())) {
                    created++;
                    Thread.sleep(CALL_INTERVAL_MILLIS);
                } else {
                    skipped++;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (RuntimeException e) {
                failed++;
                log.warn("축제 저장 실패 - contentId: {}", festival.getContentId());
            }
        }

        log.info("축제 한 번에 저장 - 목록 {}개, 새로 저장 {}개, 이미 있음 {}개, 실패 {}개",
                listed.size(), created, skipped, failed);
        return FestivalSyncResponseDTO.builder()
                .listed(listed.size())
                .created(created)
                .skipped(skipped)
                .failed(failed)
                .build();
    }
}
