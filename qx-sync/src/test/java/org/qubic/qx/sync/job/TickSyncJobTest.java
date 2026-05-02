package org.qubic.qx.sync.job;

import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.exception.EmptyResultException;
import org.qubic.qx.sync.domain.TickData;
import org.qubic.qx.sync.domain.TickInfo;
import org.qubic.qx.sync.domain.Transaction;
import org.qubic.qx.sync.repository.TickRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.Mockito.*;

class TickSyncJobTest {

    private final TickRepository tickRepository = mock();
    private final CoreApiService coreService = mock();
    private final TransactionProcessor transactionProcessor = mock();

    private final TickSyncJob tickSync = new TickSyncJob(tickRepository, coreService, transactionProcessor);

    @Test
    void sync_givenNoNewTick_thenDoNotSync() {
        TickInfo currentTickInfo = new TickInfo(1, 3459, 3456, 9999);
        when(coreService.getTickInfo()).thenReturn(Mono.just(currentTickInfo));
        when(tickRepository.getLatestSyncedTick()).thenReturn(Mono.just(3459L));

        StepVerifier.create(tickSync.sync())
                .verifyComplete();

        verify(coreService).getTickInfo();
        verify(tickRepository).getLatestSyncedTick();
        verifyNoMoreInteractions(tickRepository, coreService);
        verifyNoInteractions(transactionProcessor);
    }

    @Test
    void sync_givenOneNewTick_thenProcessTick() {
        TickInfo currentTickInfo = new TickInfo(1, 3460, 3456, 9999);
        when(coreService.getTickInfo()).thenReturn(Mono.just(currentTickInfo));
        when(tickRepository.getLatestSyncedTick()).thenReturn(Mono.just(3458L));

        when(tickRepository.isProcessedTick(anyLong())).thenReturn(Mono.just(false));
        when(tickRepository.addToProcessedTicks(anyLong())).thenReturn(Mono.just(1L));

        TickData tickData = new TickData(1, 3459, Instant.now());
        when(coreService.getQxTransactions(3459L)).thenReturn(Flux.empty());
        when(coreService.getTickData(3459L)).thenReturn(Mono.just(tickData));
        when(coreService.getAssetEventLogs(3459L)).thenReturn(Flux.empty());


        StepVerifier.create(tickSync.sync())
                .expectNext(3459L)
                .verifyComplete();
    }

    @Test
    void sync_givenEmptyTickEventsOrNoTickData_thenError() {

        TickInfo currentTickInfo = new TickInfo(1, 3458, 1000, 9999);
        when(coreService.getTickInfo()).thenReturn(Mono.just(currentTickInfo));
        when(tickRepository.getLatestSyncedTick()).thenReturn(Mono.just(3456L));
        when(tickRepository.isProcessedTick(anyLong())).thenReturn(Mono.just(false));
        Transaction tx = new Transaction("tx-hash", "b", "c", 0, 0, 6, 0, null);
        when(coreService.getQxTransactions(3457L)).thenReturn(Flux.just(tx));
        when(coreService.getAssetEventLogs(3457)).thenReturn(Flux.empty());
        when(coreService.getTickData(3457)).thenReturn(Mono.empty()); // must not be empty

        StepVerifier.create(tickSync.sync())
                .verifyErrorMatches(err -> err instanceof EmptyResultException && err.getMessage().contains("getting tick data, transactions and/or events"));

        verify(tickRepository, never()).addToProcessedTicks(anyLong());
    }

    @Test
    void sync_givenEventsNotAvailable_thenSyncUntilFullyAvailableTickMinusOne() {
        // when(eventService.getLastProcessedTick()).thenReturn(Mono.just(new EpochAndTick(1, 3458)));

        Transaction tx = new Transaction("a", "b", "c", 0, 0, 6, 0, null);

        when(tickRepository.isProcessedTick(anyLong())).thenReturn(Mono.just(false));
        when(tickRepository.addToProcessedTicks(anyLong())).thenReturn(Mono.just(1L));
        when(tickRepository.getLatestSyncedTick()).thenReturn(Mono.just(2345L));
        TickInfo currentTickInfo = new TickInfo(1, 3459, 3456, 3458);
        when(coreService.getTickInfo()).thenReturn(Mono.just(currentTickInfo));

        when(coreService.getQxTransactions(3456L)).thenReturn(Flux.just(tx));
        when(coreService.getQxTransactions(3457L)).thenReturn(Flux.just(tx, tx));
        when(coreService.getTickData(anyLong())).thenReturn(Mono.just(new TickData(1, 1L, Instant.now())));
        when(coreService.getAssetEventLogs(anyLong())).thenReturn(Flux.empty());
        when(transactionProcessor.processQxTransaction(any())).thenReturn(Mono.empty());

        StepVerifier.create(tickSync.sync())
                .expectNext(3456L)
                .expectNext(3457L)
                .verifyComplete();
    }

    @Test
    void sync_givenSeveralTicks_thenProcessAll() {
        Transaction tx = new Transaction("a", "b", "c", 0, 0, 6, 0, null);

        when(tickRepository.isProcessedTick(anyLong())).thenReturn(Mono.just(false));
        when(tickRepository.addToProcessedTicks(anyLong())).thenReturn(Mono.just(1L));
        when(tickRepository.getLatestSyncedTick()).thenReturn(Mono.just(2345L));
        TickInfo currentTickInfo = new TickInfo(1, 3459, 3456, 9999);
        when(coreService.getTickInfo()).thenReturn(Mono.just(currentTickInfo));

        when(coreService.getQxTransactions(3456L)).thenReturn(Flux.just(tx));
        when(coreService.getQxTransactions(3457L)).thenReturn(Flux.just(tx, tx));
        when(coreService.getQxTransactions(3458L)).thenReturn(Flux.just(tx, tx, tx));
        when(coreService.getTickData(anyLong())).thenReturn(Mono.just(new TickData(1, 1L, Instant.now())));
        when(coreService.getAssetEventLogs(anyLong())).thenReturn(Flux.empty());
        when(transactionProcessor.processQxTransaction(any())).thenReturn(Mono.empty());

        StepVerifier.create(tickSync.sync().log())
                .expectNext(3456L)
                .expectNext(3457L)
                .expectNext(3458L)
                .verifyComplete();
    }

    @Test
    void sync_givenAlreadyProcessed_thenDoNotProcess() {
        when(tickRepository.isProcessedTick(anyLong())).thenReturn(Mono.just(true));
        when(tickRepository.getLatestSyncedTick()).thenReturn(Mono.just(0L));

        TickInfo currentTickInfo = new TickInfo(1, 100, 95, 9999);
        when(coreService.getTickInfo()).thenReturn(Mono.just(currentTickInfo));

        StepVerifier.create(tickSync.sync())
                .expectNext(95L)
                .expectNext(96L)
                .expectNext(97L)
                .expectNext(98L)
                .expectNext(99L)
                .verifyComplete();

        // include tick 90. exclude tick 100.
        verify(tickRepository, times(5)).isProcessedTick(anyLong());
        verify(coreService, never()).getQxTransactions(anyLong());
        verifyNoInteractions(transactionProcessor);
    }
}