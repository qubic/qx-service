package org.qubic.qx.api.config;

import org.qubic.qx.api.adapter.CoreArchiveApiService;
import org.qubic.qx.api.db.AssetsRepository;
import org.qubic.qx.api.db.EntitiesRepository;
import org.qubic.qx.api.db.TradesRepository;
import org.qubic.qx.api.db.TransactionsRepository;
import org.qubic.qx.api.db.domain.Trade;
import org.qubic.qx.api.db.domain.Transaction;
import org.qubic.qx.api.redis.QxCacheManager;
import org.qubic.qx.api.redis.dto.TradeRedisDto;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;
import org.qubic.qx.api.redis.repository.TradesRedisRepository;
import org.qubic.qx.api.redis.repository.TransactionsRedisRepository;
import org.qubic.qx.api.scheduler.*;
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
    TransactionsProcessor transactionsProcessor(TransactionsRedisRepository transactionsRedisRepository,
                                                TransactionsRepository transactionsRepository,
                                                TransactionMapper transactionMapper,
                                                AssetsRepository assetsRepository,
                                                QxCacheManager qxCacheManager) {
        return new TransactionsProcessor(transactionsRedisRepository, transactionsRepository, transactionMapper,
                assetsRepository, qxCacheManager);
    }

    @Bean
    TradesProcessor tradesProcessor(TradesRedisRepository tradesRedisRepository, TradesRepository tradesRepository,
                                    TradeMapper tradeMapper, QxCacheManager qxCacheManager) {
        return new TradesProcessor(tradesRedisRepository, tradesRepository, tradeMapper, qxCacheManager);
    }

    @Bean
    RedisSyncScheduler redisSyncScheduler(QueueProcessor<Transaction, TransactionRedisDto> transactionProcessor, QueueProcessor<Trade, TradeRedisDto> tradesProcessor) {
        return new RedisSyncScheduler(transactionProcessor, tradesProcessor);
    }

    @Bean
    TransactionMigrationScheduler transactionMigrationScheduler(TransactionsRepository transactionsRepository, CoreArchiveApiService coreArchiveApiService) {
        return new TransactionMigrationScheduler(coreArchiveApiService, transactionsRepository);
    }

}
