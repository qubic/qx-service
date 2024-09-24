package org.qubic.qx.config;

import org.qubic.qx.repository.TickRepository;
import org.qubic.qx.repository.TransactionRepository;
import org.qubic.qx.domain.Transaction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RepositoryConfiguration {

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

}
