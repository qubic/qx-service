package org.qubic.qx.sync.repository;

import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.AbstractRedisTest;
import org.qubic.qx.sync.domain.Trade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.test.StepVerifier;

import java.time.Instant;

class TradeRepositorySpringIT extends AbstractRedisTest {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private ReactiveRedisTemplate<String, Trade> redisTradeTemplate;

    @Test
    void putTradeIntoQueue_thenPushIntoTradesQueue() {
        Trade trade = new Trade(1, Instant.EPOCH.getEpochSecond(), "hash", true, "taker", "maker", "issuer", "asset", 3, 2);

        StepVerifier.create(tradeRepository.putTradeIntoQueue(trade))
                .expectNext(trade)
                .verifyComplete();

        StepVerifier.create(redisTradeTemplate
                        .opsForList()
                        .rightPop("queue:trades"))
                .expectNext(trade)
                .verifyComplete();

    }

}