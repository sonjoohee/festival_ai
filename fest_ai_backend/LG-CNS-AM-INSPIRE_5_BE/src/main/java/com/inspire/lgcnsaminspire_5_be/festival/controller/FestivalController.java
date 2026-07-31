package com.inspire.lgcnsaminspire_5_be.festival.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inspire.lgcnsaminspire_5_be.festival.domain.dto.FestivalResponseDTO;
import com.inspire.lgcnsaminspire_5_be.festival.service.FestivalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/festivals")
@RequiredArgsConstructor
public class FestivalController {
    private final FestivalService festivalService;

    // 지역기반 축제목록 조회 API
    @GetMapping
    public ResponseEntity<?> getFestivalList(
            @RequestParam(name = "region", required = false) String region) {
        System.out.println(">>>> debug festival controller getFestivalList");
        System.out.println(">>>> debug request param region : " + region);

        // 초기 진입 시(region 파라미터가 없거나 빈 값일 때) 외부 API 호출 안 함
        if (region == null || region.isBlank()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<FestivalResponseDTO> response = festivalService.getFestivalsByRegion(region);
        return ResponseEntity.ok(response);
    }

    // 축제상세 조회 API
    // Header: Authorization (선택/필수 여부에 따라 시큐리티 컨텍스트 파싱)
    @GetMapping("/detail")
    public ResponseEntity<?> getFestivalDetail(
            @RequestParam(name = "contentId") String contentId,
            Authentication authentication) {
        System.out.println(">>>> debug festival controller - getFestivalDetail");
        System.out.println(">>>> debug request param contentId : " + contentId);

        Long userId = null;
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            userId = (Long) authentication.getPrincipal();
        }

        // 정제된 userId와 contentId를 넘겨 상세 정보 조회
        FestivalResponseDTO response = festivalService.getFestivalDetail(contentId, userId);
        return ResponseEntity.ok(response);
    }
}
