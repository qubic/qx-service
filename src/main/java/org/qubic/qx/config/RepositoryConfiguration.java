package org.qubic.qx.config;

import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.domain.Transaction;
import org.qubic.qx.repository.OrderBookRepository;
import org.qubic.qx.repository.TickRepository;
import org.qubic.qx.repository.TransactionRepository;
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
    public ReactiveRedisTemplate<String, Transaction> transactionRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<Transaction> serializer = new Jackson2JsonRedisSerializer<>(Transaction.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, Transaction> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
        RedisSerializationContext<String, Transaction> context = builder.hashValue(serializer).build();
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, AssetOrder[]> assetOrderTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<AssetOrder[]> serializer = new Jackson2JsonRedisSerializer<>(AssetOrder[].class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, AssetOrder[]> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
        RedisSerializationContext<String, AssetOrder[]> context = builder.hashValue(serializer).build();
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    @Bean
    TransactionRepository transactionRepository(ReactiveRedisTemplate<String, Transaction> redisTransactionTemplate) {
        return new TransactionRepository(redisTransactionTemplate);
    }

    @Bean
    TickRepository tickRepository(ReactiveStringRedisTemplate redisStringTemplate) {
        return new TickRepository(redisStringTemplate);
    }

    @Bean
    OrderBookRepository orderBookRepository(ReactiveStringRedisTemplate redisStringTemplate, ReactiveRedisTemplate<String, AssetOrder[]> assetOrderRedisTemplate) {
        return new OrderBookRepository(redisStringTemplate, assetOrderRedisTemplate);
    }

}
