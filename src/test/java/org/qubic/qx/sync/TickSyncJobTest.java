package org.qubic.qx.sync;

import org.junit.jupiter.api.Test;
import org.mio.qubic.computor.crypto.IdentityUtil;
import org.mio.qubic.computor.domain.std.SignedTransaction;
import org.mio.qubic.computor.domain.std.Transaction;
import org.qubic.qx.adapter.computor.NodeService;
import org.qubic.qx.repository.TickRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TickSyncJobTest {

    private final TickRepository tickRepository = mock();
    private final NodeService nodeService = mock();
    private final IdentityUtil identityUtil = mock();
    private final TickSyncJob tickSync = new TickSyncJob(tickRepository, nodeService, identityUtil);

    @Test
    void sync() {

        SignedTransaction tx = SignedTransaction.builder()
                .transaction(Transaction.builder().build())
                .build();

        when(tickRepository.isProcessedTick(anyLong())).thenReturn(Mono.just(false));
        when(tickRepository.addToProcessedTicks(anyLong())).thenReturn(Mono.just(1L));
        when(tickRepository.getLatestSyncedTick()).thenReturn(Mono.just(2345L));
        when(nodeService.getInitialTick()).thenReturn(Mono.just(3456L));

        when(nodeService.getTickTransactions(3456L)).thenReturn(Flux.just(tx));
        when(nodeService.getTickTransactions(3457L)).thenReturn(Flux.just(tx, tx));
        when(nodeService.getTickTransactions(3458L)).thenReturn(Flux.just(tx, tx, tx));

        StepVerifier.create(tickSync.sync(3459L))
                .expectNextCount(3)
                .verifyComplete();

    }

    @Test
    void sync_givenAlreadyProcessed_thenDoNotProcess() {

        when(tickRepository.isProcessedTick(anyLong())).thenReturn(Mono.just(true));
        when(tickRepository.getLatestSyncedTick()).thenReturn(Mono.just(0L));
        when(nodeService.getInitialTick()).thenReturn(Mono.just(100L));

        StepVerifier.create(tickSync.sync(1000L))
                .expectNextCount(900)
                .verifyComplete();

    }
}