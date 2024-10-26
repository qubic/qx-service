package org.qubic.qx.api.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.db.domain.Trade;
import org.qubic.qx.api.db.domain.Transaction;
import org.qubic.qx.api.redis.dto.TradeRedisDto;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Slf4j
public class RedisSyncScheduler {

    private final QueueProcessor<Transaction, TransactionRedisDto> transactionProcessor;
    private final QueueProcessor<Trade, TradeRedisDto> tradeProcessor;

    public RedisSyncScheduler(QueueProcessor<Transaction, TransactionRedisDto> transactionProcessor,
                              QueueProcessor<Trade, TradeRedisDto> tradeProcessor) {
        this.transactionProcessor = transactionProcessor;
        this.tradeProcessor = tradeProcessor;
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 1000)
    void processTradesAndTransactions() {
        log.debug("Running data synchronization...");
        List<Transaction> transactions = transactionProcessor.process();
        if (!transactions.isEmpty()) {
            log.info("Successfully synced [{}] transactions.", transactions.size());
        }
        List<Trade> trades = tradeProcessor.process();
        if (!trades.isEmpty()) {
            log.info("Successfully synced [{}] trades.", trades.size());
        }
    }

}
