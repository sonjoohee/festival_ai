package com.inspire.lgcnsaminspire_5_be.rag.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inspire.lgcnsaminspire_5_be.rag.domain.dto.RagAskRequestDTO;
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
}
