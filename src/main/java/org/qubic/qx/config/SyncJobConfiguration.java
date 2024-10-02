package org.qubic.qx.config;

import org.qubic.qx.adapter.CoreApiService;
import org.qubic.qx.adapter.il.IntegrationCoreApiService;
import org.qubic.qx.adapter.il.mapping.IlTransactionMapper;
import org.qubic.qx.domain.Transaction;
import org.qubic.qx.repository.TickRepository;
import org.qubic.qx.repository.TransactionRepository;
import org.qubic.qx.sync.TickSyncJob;
import org.qubic.qx.sync.TickSyncJobRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class SyncJobConfiguration {

    @Bean
    public ReactiveRedisTemplate<String, Transaction> redisTransactionOperations(ReactiveRedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<Transaction> serializer = new Jackson2JsonRedisSerializer<>(Transaction.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, Transaction> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
        RedisSerializationContext<String, Transaction> context = builder.hashValue(serializer).build();
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    @Bean
    TransactionRepository transactionRepository(ReactiveRedisTemplate<String, Transaction> redisTransactionOperations) {
        return new TransactionRepository(redisTransactionOperations);
    }

    @Bean
    TickRepository tickRepository(ReactiveStringRedisTemplate redisStringTemplate) {
        return new TickRepository(redisStringTemplate);
    }

    @ConditionalOnProperty(value = "backend", havingValue = "integration", matchIfMissing = true)
    @Bean
    CoreApiService integrationCoreApiService(WebClient integrationApiWebClient, IlTransactionMapper transactionMapper) {
        return new IntegrationCoreApiService(integrationApiWebClient, transactionMapper);
    }

    @Bean
    TickSyncJob tickSyncJob(TickRepository tickRepository, TransactionRepository transactionRepository, CoreApiService coreService) {
        return new TickSyncJob(tickRepository, transactionRepository, coreService);
    }

    @Bean
    TickSyncJobRunner tickSyncJobRunner(TickSyncJob tickSyncJob, @Value("${sync.interval}") Duration syncInterval) {
        return new TickSyncJobRunner(tickSyncJob, syncInterval);
    }

}
