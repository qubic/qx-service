package org.qubic.qx.sync.job;

import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.EventApiService;
import org.qubic.qx.sync.assets.AssetService;
import org.qubic.qx.sync.domain.TickData;
import org.qubic.qx.sync.domain.TickInfo;
import org.qubic.qx.sync.domain.Transaction;
import org.qubic.qx.sync.repository.TickRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

class TickSyncJobTest {

    private final AssetService assetService = mock();
    private final TickRepository tickRepository = mock();
    private final CoreApiService coreService = mock();
    private final EventApiService eventService = mock();
    private final TransactionProcessor transactionProcessor = mock();

    private final TickSyncJob tickSync = new TickSyncJob(assetService, tickRepository, coreService, eventService, transactionProcessor);

    @Test
    void sync_givenNoNewTick_thenDoNotSync() {
        TickInfo currentTickInfo = new TickInfo(1, 3459, 3456);
        when(coreService.getTickInfo()).thenReturn(Mono.just(currentTickInfo));
        when(tickRepository.getLatestSyncedTick()).thenReturn(Mono.just(3459L));

        StepVerifier.create(tickSync.sync())
                .verifyComplete();
    }

    @Test
    void sync_givenOneNewTick_thenSync() {
        TickInfo currentTickInfo = new TickInfo(1, 3460, 3456);
        when(coreService.getTickInfo()).thenReturn(Mono.just(currentTickInfo));
        when(tickRepository.getLatestSyncedTick()).thenReturn(Mono.just(3458L));

        when(tickRepository.isProcessedTick(anyLong())).thenReturn(Mono.just(false));
        when(tickRepository.addToProcessedTicks(anyLong())).thenReturn(Mono.just(1L));
        when(coreService.getQxTransactions(3459L)).thenReturn(Flux.empty());

        StepVerifier.create(tickSync.sync())
                .expectNext(3459L)
                .verifyComplete();
    }

    @Test
    void sync_givenSeveralTicks_thenSyncAll() {
        Transaction tx = new Transaction("a", "b", "c", 0, 0, 6, 0, null, null);

        when(tickRepository.isProcessedTick(anyLong())).thenReturn(Mono.just(false));
        when(tickRepository.addToProcessedTicks(anyLong())).thenReturn(Mono.just(1L));
        when(tickRepository.getLatestSyncedTick()).thenReturn(Mono.just(2345L));
        TickInfo currentTickInfo = new TickInfo(1, 3459, 3456);
        when(coreService.getTickInfo()).thenReturn(Mono.just(currentTickInfo));

        when(coreService.getQxTransactions(3456L)).thenReturn(Flux.just(tx));
        when(coreService.getQxTransactions(3457L)).thenReturn(Flux.just(tx, tx));
        when(coreService.getQxTransactions(3458L)).thenReturn(Flux.just(tx, tx, tx));
        when(coreService.getTickData(anyLong())).thenReturn(Mono.just(new TickData(1, 1L, Instant.now())));

        when(eventService.getTickEvents(anyLong())).thenReturn(Mono.just(List.of()));
        when(transactionProcessor.processQxTransactions(anyLong(), any(Instant.class), anyList(), anyList())).thenReturn(Mono.empty());

        StepVerifier.create(tickSync.sync().log())
                .expectNext(3458L)
                .verifyComplete();
    }

    @Test
    void sync_givenAlreadyProcessed_thenDoNotProcess() {
        when(tickRepository.isProcessedTick(anyLong())).thenReturn(Mono.just(true));
        when(tickRepository.getLatestSyncedTick()).thenReturn(Mono.just(0L));

        TickInfo currentTickInfo = new TickInfo(1, 100, 90);
        when(coreService.getTickInfo()).thenReturn(Mono.just(currentTickInfo));

        StepVerifier.create(tickSync.sync())
                .expectNext(99L)
                .verifyComplete();

        // include tick 90. exclude tick 100.
        verify(tickRepository, times(10)).isProcessedTick(anyLong());
        verify(coreService, never()).getQxTransactions(anyLong());
        verifyNoInteractions(transactionProcessor);
    }
}