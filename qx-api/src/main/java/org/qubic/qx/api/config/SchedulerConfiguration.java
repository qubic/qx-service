package org.qubic.qx.api.config;

import org.qubic.qx.api.db.AssetsRepository;
import org.qubic.qx.api.db.EntitiesRepository;
import org.qubic.qx.api.db.TradesRepository;
import org.qubic.qx.api.db.TransactionsRepository;
import org.qubic.qx.api.db.domain.Trade;
import org.qubic.qx.api.db.domain.Transaction;
import org.qubic.qx.api.redis.dto.TradeRedisDto;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;
import org.qubic.qx.api.redis.repository.TradesRedisRepository;
import org.qubic.qx.api.redis.repository.TransactionsRedisRepository;
import org.qubic.qx.api.scheduler.QueueProcessor;
import org.qubic.qx.api.scheduler.RedisSyncScheduler;
import org.qubic.qx.api.scheduler.mapping.DatabaseMappings;
import org.qubic.qx.api.scheduler.mapping.TradeMapper;
import org.qubic.qx.api.scheduler.mapping.TransactionMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration
public class SchedulerConfiguration {

    @Bean
    DatabaseMappings databaseMappings(EntitiesRepository entitiesRepository,
                                      TransactionsRepository transactionsRepository,
                                      AssetsRepository assetsRepository) {

        return new DatabaseMappings(entitiesRepository, transactionsRepository, assetsRepository);

    }

    @Bean
    QueueProcessor<Transaction, TransactionRedisDto> transactionsProcessor(TransactionsRedisRepository transactionsRedisRepository,
                                                                           TransactionsRepository transactionsRepository,
                                                                           TransactionMapper transactionMapper) {
        return new QueueProcessor<>(transactionsRedisRepository, transactionsRepository, transactionMapper);

    }

    @Bean
    QueueProcessor<Trade, TradeRedisDto> tradesProcessor(TradesRedisRepository tradesRedisRepository, TradesRepository tradesRepository, TradeMapper tradeMapper) {
        return new QueueProcessor<>(tradesRedisRepository, tradesRepository, tradeMapper);
    }

    @Bean
    RedisSyncScheduler redisSyncScheduler(QueueProcessor<Transaction, TransactionRedisDto> transactionProcessor, QueueProcessor<Trade, TradeRedisDto> tradesProcessor) {
        return new RedisSyncScheduler(transactionProcessor, tradesProcessor);
    }

}
