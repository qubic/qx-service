package org.qubic.qx.sync.repository;

import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.AbstractRedisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TickRepositorySpringIT extends AbstractRedisTest {

    @Autowired
    private TickRepository tickRepository;

    @Autowired
    private ReactiveStringRedisTemplate redisStringTemplate;

    @Test
    void setCurrentTick() {
        StepVerifier.create(tickRepository.setLatestSyncedTick(42))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void getCurrentTick() {
        Mono<Long> latestTick = tickRepository.setLatestSyncedTick(42)
                .then(tickRepository.getLatestSyncedTick());

        StepVerifier.create(latestTick)
                .expectNext(42L)
                .verifyComplete();
    }

    @Test
    void addToProcessedTicks() {
        StepVerifier.create(tickRepository.addToProcessedTicks(123))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(tickRepository.addToProcessedTicks(123))
                .expectNext(0L)
                .verifyComplete();

        StepVerifier.create(redisStringTemplate.opsForSet().isMember("ticks:processed", "123"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void isProcessedTick() {
        tickRepository.addToProcessedTicks(42L)
                .then(tickRepository.addToProcessedTicks(43L))
                .block();

        StepVerifier.create(tickRepository.isProcessedTick(42L))
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(tickRepository.isProcessedTick(43L))
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(tickRepository.isProcessedTick(666L))
                .expectNext(false)
                .verifyComplete();
    }

}