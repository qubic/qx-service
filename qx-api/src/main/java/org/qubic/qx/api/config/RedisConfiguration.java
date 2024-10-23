package org.qubic.qx.api.config;

import org.qubic.qx.api.redis.dto.TradeRedisDto;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;
import org.qubic.qx.api.redis.repository.TradesRedisRepository;
import org.qubic.qx.api.redis.repository.TransactionsRedisRepository;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@EnableCaching
@Configuration
public class RedisConfiguration {

    @Bean
    RedisTemplate<String, TransactionRedisDto> transactionRedisTemplate(RedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<TransactionRedisDto> jsonSerializer = new Jackson2JsonRedisSerializer<>(TransactionRedisDto.class);
        RedisTemplate<String, TransactionRedisDto> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(jsonSerializer);
        return template;
    }

    @Bean
    RedisTemplate<String, TradeRedisDto> tradeRedisTemplate(RedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<TradeRedisDto> jsonSerializer = new Jackson2JsonRedisSerializer<>(TradeRedisDto.class);
        RedisTemplate<String, TradeRedisDto> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(jsonSerializer);
        return template;
    }

    @Bean
    TradesRedisRepository tradesRedisRepository(RedisTemplate<String, TradeRedisDto> tradeRedisTemplate) {
        return new TradesRedisRepository(tradeRedisTemplate);
    }

    @Bean
    TransactionsRedisRepository transactionsRedisRepository(RedisTemplate<String, TransactionRedisDto> transactionRedisTemplate) {
        return new TransactionsRedisRepository(transactionRedisTemplate);
    }

}

