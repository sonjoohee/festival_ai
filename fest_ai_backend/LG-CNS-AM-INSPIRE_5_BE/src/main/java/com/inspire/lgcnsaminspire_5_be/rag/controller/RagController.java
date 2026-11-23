package com.inspire.lgcnsaminspire_5_be.rag.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inspire.lgcnsaminspire_5_be.rag.domain.dto.RagAskRequestDTO;
import com.inspire.lgcnsaminspire_5_be.rag.service.FestivalSyncService;
import com.inspire.lgcnsaminspire_5_be.rag.service.RagAskService;
import com.inspire.lgcnsaminspire_5_be.rag.service.RagIndexService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/rag")
@RequiredArgsConstructor
public class RagController {

    private final RagAskService ragAskService;
    private final RagIndexService ragIndexService;
    private final FestivalSyncService festivalSyncService;

    // 질문 답변 (로그인 없이 사용)
    @PostMapping("/ask")
    public ResponseEntity<?> ask(@Valid @RequestBody RagAskRequestDTO request) {
        try {
            return ResponseEntity.ok(ragAskService.ask(request.getQuestion().strip()));
        } catch (RuntimeException e) {
            // 예외 메시지에는 외부 API 응답이 섞일 수 있어 클래스 이름만 남긴다
            log.error("RAG 질문 처리 실패: {}", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "AI 서버 호출에 실패했습니다. 잠시 후 다시 시도해 주세요."));
        }
    }

    // 축제 테이블과 FAQ로 색인을 다시 만든다 (로그인 필요, 임베딩 API 비용이 드는 작업)
    @PostMapping("/index")
    public ResponseEntity<?> rebuildIndex() {
        try {
            return ResponseEntity.ok(ragIndexService.rebuildIndex());
        } catch (RuntimeException e) {
            log.error("RAG 색인 실패: {}", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "임베딩 API 호출에 실패했습니다. API 키와 설정을 확인해 주세요."));
        }
    }

    // TourAPI에서 오늘 이후 열리는 축제를 받아 DB에 없는 것만 저장한다 (로그인 필요)
    // region: 법정동 시도 코드 (예: 11 서울, 26 부산), all이면 전국
    @PostMapping("/festivals/sync")
    public ResponseEntity<?> syncFestivals(
            @RequestParam(name = "region", defaultValue = "all") String region,
            @RequestParam(name = "limit", defaultValue = "100") int limit) {
        return ResponseEntity.ok(festivalSyncService.sync(region, limit));
    }
}
