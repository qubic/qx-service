package org.qubic.qx.repository;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.domain.Trade;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Instant;
import java.util.Objects;

@Slf4j
public class TradeRepository {

    static final String KEY_TRADES = "trades";

    private final ReactiveRedisTemplate<String, Trade> redisTradeTemplate;

    public TradeRepository(ReactiveRedisTemplate<String, Trade> redisTradeTemplate) {
        this.redisTradeTemplate = redisTradeTemplate;
        log.info(redisTradeTemplate.getClass().toString());
    }

    public Mono<Trade> storeTrade(Trade trade, Instant timestamp) {
        return redisTradeTemplate.opsForZSet()
                .add(KEY_TRADES, trade, (double) timestamp.getEpochSecond())
                .doOnNext(b -> log.info("Stored trade with timestamp [{}]: {}", timestamp.getEpochSecond(), trade))
                .map(b -> trade);
    }

    public Flux<Tuple2<Instant, Trade>> findTrades(Instant from, Instant to) {
        return redisTradeTemplate.opsForZSet()
                .rangeByScoreWithScores(KEY_TRADES, Range.closed((double) from.getEpochSecond(), (double) to.getEpochSecond()))
                .map(found -> Tuples.of(Instant.ofEpochSecond(Objects.requireNonNull(found.getScore()).longValue()), Objects.requireNonNull(found.getValue())));
    }

}
