package org.qubic.qx.api.scheduler;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.adapter.CoreArchiveApiService;
import org.qubic.qx.api.adapter.domain.TickData;
import org.qubic.qx.api.db.TransactionsRepository;
import org.qubic.qx.api.db.domain.Transaction;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;

class TransactionMigrationSchedulerTest {

    private final CoreArchiveApiService coreArchiveApiService = mock();
    private final TransactionsRepository repository = mock();
    private final TransactionMigrationScheduler scheduler = new TransactionMigrationScheduler(coreArchiveApiService, repository);

    @Test
    void fixTickTime() {

        Transaction transaction = Transaction.builder()
                .tick(42)
                .build();
        when(repository.findByTickTimeIsNull(any(Pageable.class))).thenReturn(List.of(transaction));
        when(coreArchiveApiService.getTickData(42)).thenReturn(new TickData(1, 2, Instant.EPOCH));

        scheduler.processTransactionsWithoutTickTime();

        verify(repository).save(Transaction.builder()
                .tick(42)
                .tickTime(Instant.EPOCH)
                .build());


    }


}