package org.qubic.qx.sync;

import org.junit.jupiter.api.Test;
import org.qubic.qx.adapter.CoreApiService;
import org.qubic.qx.domain.Transaction;
import org.qubic.qx.repository.TickRepository;
import org.qubic.qx.repository.TransactionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TickSyncJobTest {

    private final TickRepository tickRepository = mock();
    private final TransactionRepository transactionRepository = mock();
    private final CoreApiService coreService = mock();
    private final TickSyncJob tickSync = new TickSyncJob(tickRepository, transactionRepository, coreService);

    @Test
    void sync() {
        Transaction tx = new Transaction("a", "b", "c", 0, 0, 0, 0, null);

        when(tickRepository.isProcessedTick(anyLong())).thenReturn(Mono.just(false));
        when(tickRepository.addToProcessedTicks(anyLong())).thenReturn(Mono.just(1L));
        when(tickRepository.getLatestSyncedTick()).thenReturn(Mono.just(2345L));
        when(coreService.getInitialTick()).thenReturn(Mono.just(3456L));

        when(coreService.getQxTransactions(3456L)).thenReturn(Flux.just(tx));
        when(coreService.getQxTransactions(3457L)).thenReturn(Flux.just(tx, tx));
        when(coreService.getQxTransactions(3458L)).thenReturn(Flux.just(tx, tx, tx));

        when(transactionRepository.putTransaction(any())).thenReturn(Mono.just(tx));
        when(tickRepository.setTickTransactions(anyLong(), anyList())).thenReturn(Mono.just(true));
        when(tickRepository.addToQxTicks(anyLong())).thenReturn(Mono.just(1L));

        StepVerifier.create(tickSync.sync(3459L).log())
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void sync_givenAlreadyProcessed_thenDoNotProcess() {
        when(tickRepository.isProcessedTick(anyLong())).thenReturn(Mono.just(true));
        when(tickRepository.getLatestSyncedTick()).thenReturn(Mono.just(0L));
        when(coreService.getInitialTick()).thenReturn(Mono.just(100L));

        StepVerifier.create(tickSync.sync(1000L))
                .expectNextCount(900)
                .verifyComplete();
    }
}