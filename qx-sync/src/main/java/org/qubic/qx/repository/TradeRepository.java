package org.qubic.qx.repository;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.domain.Trade;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
public class TradeRepository {

    static final String KEY_TRADES = "trades";

    private final ReactiveRedisTemplate<String, Trade> redisTradeTemplate;

    public TradeRepository(ReactiveRedisTemplate<String, Trade> redisTradeTemplate) {
        this.redisTradeTemplate = redisTradeTemplate;
        log.info(redisTradeTemplate.getClass().toString());
    }

    public Mono<Trade> storeTrade(Trade trade) {
        return redisTradeTemplate.opsForZSet()
                .add(KEY_TRADES, trade, (double) trade.timestamp())
                .doOnNext(b -> log.info("Stored trade with timestamp [{}]: {}", trade.timestamp(), trade))
                .map(b -> trade);
    }

    public Flux<Trade> findTrades(Instant from, Instant to) {
        return redisTradeTemplate.opsForZSet()
                .rangeByScore(KEY_TRADES, Range.closed((double) from.getEpochSecond(), (double) to.getEpochSecond()));
    }

}
