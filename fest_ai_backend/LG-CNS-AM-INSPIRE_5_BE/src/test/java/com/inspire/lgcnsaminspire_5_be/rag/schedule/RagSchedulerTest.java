package com.inspire.lgcnsaminspire_5_be.rag.schedule;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.inspire.lgcnsaminspire_5_be.rag.domain.dto.FestivalSyncResponseDTO;
import com.inspire.lgcnsaminspire_5_be.rag.service.FestivalSyncService;
import com.inspire.lgcnsaminspire_5_be.rag.service.RagIndexService;
import com.inspire.lgcnsaminspire_5_be.rag.store.RagVectorStoreHolder;

class RagSchedulerTest {

    private final FestivalSyncService syncService = mock(FestivalSyncService.class);
    private final RagIndexService indexService = mock(RagIndexService.class);
    private final RagVectorStoreHolder holder = mock(RagVectorStoreHolder.class);
    private final RagScheduler scheduler = new RagScheduler(syncService, indexService, holder, 200);

    @Test
    void 새_축제가_있으면_색인을_다시_만든다() {
        when(syncService.sync("all", 200)).thenReturn(FestivalSyncResponseDTO.builder().created(3).build());

        scheduler.refresh();

        verify(indexService).rebuildIndex();
    }

    @Test
    void 새_축제가_없고_색인이_있으면_색인은_건너뛴다() {
        when(syncService.sync("all", 200)).thenReturn(FestivalSyncResponseDTO.builder().created(0).build());
        when(holder.isEmpty()).thenReturn(false);

        scheduler.refresh();

        verify(indexService, never()).rebuildIndex();
    }

    @Test
    void 새_축제가_없어도_색인이_비어_있으면_만든다() {
        when(syncService.sync("all", 200)).thenReturn(FestivalSyncResponseDTO.builder().created(0).build());
        when(holder.isEmpty()).thenReturn(true);

        scheduler.refresh();

        verify(indexService).rebuildIndex();
    }

    @Test
    void 저장이_실패해도_예외를_밖으로_던지지_않고_색인도_건드리지_않는다() {
        when(syncService.sync(anyString(), anyInt())).thenThrow(new IllegalStateException("TourAPI 실패"));

        assertThatCode(scheduler::refresh).doesNotThrowAnyException();
        verify(indexService, never()).rebuildIndex();
    }
}
