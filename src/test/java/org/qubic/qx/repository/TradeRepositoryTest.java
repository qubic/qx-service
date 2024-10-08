package org.qubic.qx.repository;

import org.junit.jupiter.api.Test;
import org.qubic.qx.AbstractRedisTest;
import org.qubic.qx.domain.Trade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.qubic.qx.repository.TradeRepository.KEY_TRADES;

class TradeRepositoryTest extends AbstractRedisTest {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private ReactiveRedisTemplate<String, Trade> redisTradeTemplate;

    @Test
    void storeTrade() {
        Instant epoch = Instant.EPOCH;
        Trade trade = trade(1);

        StepVerifier.create(tradeRepository.storeTrade(trade, epoch)
                        .then(redisTradeTemplate.opsForZSet().rangeByScore(KEY_TRADES, Range.just((double) epoch.getEpochSecond())).last()))
                .expectNext(trade)
                .verifyComplete();
    }

    @Test
    void findTrades() {
        Instant before = Instant.now().minusSeconds(10);
        Instant now = Instant.now();
        Instant after = Instant.now().plusSeconds(10);
        Trade trade1 = trade(1);
        Trade trade2 = trade(2);
        Trade trade3 = trade(3);

        Mono<Trade> storeTrades = tradeRepository.storeTrade(trade1, before)
                .then(tradeRepository.storeTrade(trade2, now))
                .then(tradeRepository.storeTrade(trade3, after));

        StepVerifier.create(storeTrades.thenMany(tradeRepository.findTrades(before, now)))
                .expectNext(Tuples.of(before.truncatedTo(ChronoUnit.SECONDS), trade1))
                .expectNext(Tuples.of(now.truncatedTo(ChronoUnit.SECONDS), trade2))
                .verifyComplete();
    }

    private static Trade trade(long tick) {
        return new Trade(tick, "hash",  "taker", "maker", true, "issuer", "asset", 2, 3);
    }

}