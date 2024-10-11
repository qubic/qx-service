package org.qubic.qx.repository;

import org.junit.jupiter.api.Test;
import org.qubic.qx.AbstractRedisTest;
import org.qubic.qx.domain.Trade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.qubic.qx.repository.TradeRepository.KEY_TRADES;

class TradeRepositorySpringIT extends AbstractRedisTest {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private ReactiveRedisTemplate<String, Trade> redisTradeTemplate;

    @Test
    void storeTrade() {
        Trade trade = trade(1, Instant.EPOCH);

        StepVerifier.create(tradeRepository.storeTrade(trade)
                        .then(redisTradeTemplate.opsForZSet().rangeByScore(KEY_TRADES, Range.just((double) Instant.EPOCH.getEpochSecond())).last()))
                .expectNext(trade)
                .verifyComplete();
    }

    @Test
    void findTrades() {
        Instant before = Instant.now().minusSeconds(10);
        Instant now = Instant.now();
        Instant after = Instant.now().plusSeconds(10);
        Trade trade1 = trade(1, before);
        Trade trade2 = trade(2, now);
        Trade trade3 = trade(3, after);

        Mono<Trade> storeTrades = tradeRepository.storeTrade(trade1)
                .then(tradeRepository.storeTrade(trade2))
                .then(tradeRepository.storeTrade(trade3));

        StepVerifier.create(storeTrades.thenMany(tradeRepository.findTrades(before, now)))
                .expectNext(trade1)
                .expectNext(trade2)
                .verifyComplete();
    }

    private static Trade trade(long tick, Instant timestamp) {
        return new Trade(tick, timestamp.getEpochSecond(), "hash", true, "taker", "maker", "issuer", "asset", 3, 2);
    }

}