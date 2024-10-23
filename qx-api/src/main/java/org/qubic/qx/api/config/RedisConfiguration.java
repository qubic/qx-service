package org.qubic.qx.api.config;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.redis.QxCacheManager;
import org.qubic.qx.api.redis.dto.TradeRedisDto;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;
import org.qubic.qx.api.redis.repository.TradesRedisRepository;
import org.qubic.qx.api.redis.repository.TransactionsRedisRepository;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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

    @Bean
    RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory, Environment environment) {

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        String[] caches = environment.getProperty("qx.caches", String[].class, new String[0]);
        for (String cache : caches) {
            Duration ttl = environment.getProperty(String.format("qx.cache.%s.ttl", cache), Duration.class);
            if (ttl == null) {
                log.warn("Missing cache configuration for [{}]", cache);
            } else {
                log.info("Overriding defaults for cache [{}]. TTL: [{}]", cache, ttl);
                cacheConfigurations.put(cache, RedisCacheConfiguration.defaultCacheConfig().entryTtl(ttl));
            }
        }

        Duration defaultTtl = environment.getRequiredProperty("qx.cache.default.ttl", Duration.class);
        log.info("Default cache ttl: [{}]", defaultTtl);
        return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(connectionFactory)
                .withInitialCacheConfigurations(cacheConfigurations)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(defaultTtl)
                        .disableCachingNullValues())
                .build();

    }

    @Bean
    QxCacheManager qxCacheManager(RedisCacheManager cacheManager) {
        return new QxCacheManager(cacheManager);
    }

}

