package com.inspire.lgcnsaminspire_5_be.rag.schedule;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.inspire.lgcnsaminspire_5_be.rag.domain.dto.FestivalSyncResponseDTO;
import com.inspire.lgcnsaminspire_5_be.rag.service.FestivalSyncService;
import com.inspire.lgcnsaminspire_5_be.rag.service.RagIndexService;
import com.inspire.lgcnsaminspire_5_be.rag.store.RagVectorStoreHolder;

import lombok.extern.slf4j.Slf4j;

/**
 * 하루 한 번 축제를 받아 오고, 바뀐 게 있을 때만 색인을 다시 만든다.
 * 색인은 조각 전체를 임베딩해서 Gemini 호출이 많으므로, 새 축제가 없으면 건너뛴다.
 * 끄려면 .env에 RAG_SCHEDULE_ENABLED=false
 */
@Slf4j
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "rag.schedule.enabled", havingValue = "true", matchIfMissing = true)
public class RagScheduler {

    private final FestivalSyncService festivalSyncService;
    private final RagIndexService ragIndexService;
    private final RagVectorStoreHolder vectorStoreHolder;
    private final int syncLimit;

    public RagScheduler(FestivalSyncService festivalSyncService,
            RagIndexService ragIndexService,
            RagVectorStoreHolder vectorStoreHolder,
            @Value("${rag.schedule.sync-limit:200}") int syncLimit) {
        this.festivalSyncService = festivalSyncService;
        this.ragIndexService = ragIndexService;
        this.vectorStoreHolder = vectorStoreHolder;
        this.syncLimit = syncLimit;
    }

    @Scheduled(cron = "${rag.schedule.cron:0 10 4 * * *}", zone = "Asia/Seoul")
    public void refresh() {
        try {
            FestivalSyncResponseDTO sync = festivalSyncService.sync("all", syncLimit);
            if (sync.getCreated() == 0 && !vectorStoreHolder.isEmpty()) {
                log.info("RAG 자동 갱신 - 새 축제가 없어 색인은 그대로 둡니다");
                return;
            }
            ragIndexService.rebuildIndex();
            log.info("RAG 자동 갱신 완료 - 새 축제 {}개", sync.getCreated());
        } catch (RuntimeException e) {
            // 실패해도 서버는 계속 돌고, 다음 날 다시 시도한다. 예외 메시지에는 외부 응답이 섞일 수 있어 이름만 남긴다
            log.error("RAG 자동 갱신 실패: {}", e.getClass().getSimpleName());
        }
    }
}
