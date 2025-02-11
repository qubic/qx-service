package org.qubic.qx.api.scheduler;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.db.domain.Trade;
import org.qubic.qx.api.db.domain.Transaction;
import org.qubic.qx.api.redis.dto.TradeRedisDto;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class RedisSyncScheduler {

    private final QueueProcessor<Transaction, TransactionRedisDto> transactionProcessor;
    private final QueueProcessor<Trade, TradeRedisDto> tradeProcessor;

    // export latest processed tick as metric
    private final AtomicLong latestMessageTick = Objects.requireNonNull(Metrics.gauge("tick.latest", Tags.of("source", "message"), new AtomicLong(0)));

    public RedisSyncScheduler(QueueProcessor<Transaction, TransactionRedisDto> transactionProcessor,
                              QueueProcessor<Trade, TradeRedisDto> tradeProcessor) {
        this.transactionProcessor = transactionProcessor;
        this.tradeProcessor = tradeProcessor;
    }

    @Scheduled(cron = "${scheduler.sync.cron}")
    void processTradesAndTransactions() {
        log.debug("Running data synchronization...");
        List<Transaction> transactions = transactionProcessor.process();
        if (!transactions.isEmpty()) {
            log.info("Successfully synced [{}] transactions.", transactions.size());
            transactions.forEach(transaction -> {
                if (latestMessageTick.get() < transaction.getTick()) {
                    latestMessageTick.set(transaction.getTick());
                }
            });
        }
        List<Trade> trades = tradeProcessor.process();
        if (!trades.isEmpty()) {
            log.info("Successfully synced [{}] trades.", trades.size());
        }
    }

}
