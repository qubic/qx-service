package org.qubic.qx.sync.config;

import org.qubic.qx.sync.domain.AssetOrder;
import org.qubic.qx.sync.domain.Trade;
import org.qubic.qx.sync.domain.TransactionWithTime;
import org.qubic.qx.sync.repository.OrderBookRepository;
import org.qubic.qx.sync.repository.TickRepository;
import org.qubic.qx.sync.repository.TradeRepository;
import org.qubic.qx.sync.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public ReactiveRedisTemplate<String, TransactionWithTime> transactionRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<TransactionWithTime> serializer = new Jackson2JsonRedisSerializer<>(TransactionWithTime.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, TransactionWithTime> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
        RedisSerializationContext<String, TransactionWithTime> context = builder
                .value(serializer)
                .hashValue(serializer)
                .build();
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, AssetOrder[]> assetOrderTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<AssetOrder[]> serializer = new Jackson2JsonRedisSerializer<>(AssetOrder[].class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, AssetOrder[]> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
        RedisSerializationContext<String, AssetOrder[]> context = builder
                .hashValue(serializer)
                .build();
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, Trade> tradeRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<Trade> serializer = new Jackson2JsonRedisSerializer<>(Trade.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, Trade> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
        RedisSerializationContext<String, Trade> context = builder
                .value(serializer)
                .hashValue(serializer).build();
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    @Bean
    TransactionRepository transactionRepository(ReactiveRedisTemplate<String, TransactionWithTime> redisTransactionTemplate) {
        return new TransactionRepository(redisTransactionTemplate);
    }

    @Bean
    TickRepository tickRepository(ReactiveStringRedisTemplate redisStringTemplate) {
        return new TickRepository(redisStringTemplate);
    }

    @Bean
    TradeRepository tradeRepository(@Qualifier("tradeRedisTemplate") ReactiveRedisTemplate<String, Trade> redisTradeTemplate) {
        return new TradeRepository(redisTradeTemplate);
    }

    @Bean
    OrderBookRepository orderBookRepository(ReactiveStringRedisTemplate redisStringTemplate, ReactiveRedisTemplate<String, AssetOrder[]> assetOrderRedisTemplate) {
        return new OrderBookRepository(redisStringTemplate, assetOrderRedisTemplate);
    }

}
