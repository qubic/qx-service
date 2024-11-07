package org.qubic.qx.api.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.adapter.CoreArchiveApiService;
import org.qubic.qx.api.adapter.domain.TickData;
import org.qubic.qx.api.db.TransactionsRepository;
import org.qubic.qx.api.db.domain.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Slf4j
public class TransactionMigrationScheduler {

    private final CoreArchiveApiService coreArchiveApiService;
    private final TransactionsRepository transactionsRepository;

    public TransactionMigrationScheduler(CoreArchiveApiService coreArchiveApiService, TransactionsRepository transactionsRepository) {
        this.coreArchiveApiService = coreArchiveApiService;
        this.transactionsRepository = transactionsRepository;
    }

    @Scheduled(cron = "${scheduler.migrate.cron}")
    void processTransactionsWithoutTickTime() {
        log.debug("Running job to fix missing transaction tick times...");

        Pageable pageable = Pageable.ofSize(20);
        List<Transaction> transactions = transactionsRepository.findByTickTimeIsNull(pageable);

        for (Transaction transaction : transactions) {
            TickData tickData = coreArchiveApiService.getTickData(transaction.getTick());
            if (tickData != null && tickData.epoch() > 0 && tickData.tick() > 0 && tickData.timestamp() != null) {
                transaction.setTickTime(tickData.timestamp());
                log.info("Set tick time [{}] for transaction with id [{}], tick[{}] and hash [{}].",
                        transaction.getTickTime(),
                        transaction.getId(),
                        transaction.getTick(),
                        transaction.getHash());
                transactionsRepository.save(transaction);
            } else {
                log.warn("Invalid tick data queried for transaction with id [{}] and tick [{}]", transaction.getId(), transaction.getTick());
            }
        }

    }

}
