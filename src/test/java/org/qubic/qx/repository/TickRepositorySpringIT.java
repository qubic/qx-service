package org.qubic.qx.repository;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.util.List;

@SpringBootTest(properties = {"spring.data.redis.port=16379"})
class TickRepositorySpringIT {

    private static final RedisServer REDIS_SERVER = createRedis();

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

    @Test
    void setTickTransactions() {
        StepVerifier.create(tickRepository.setTickTransactions(123, List.of("a", "b", "c")))
                .expectNext(3L)
                .verifyComplete();

        // it doesn't make sense to add the same transactions again, but we need a list to preserve the order
        StepVerifier.create(tickRepository.setTickTransactions(123, List.of("a", "b", "c")))
                .expectNext(6L)
                .verifyComplete();

        StepVerifier.create(redisStringTemplate.opsForList().size("tick:123:transactions"))
                .expectNext(6L)
                .verifyComplete();

    }

    @Test
    void getTickTransactions() {
        tickRepository.setTickTransactions(42L, List.of("a", "b", "c"))
                .then(tickRepository.setTickTransactions(43L, List.of("x", "y", "z")))
                .block();

        StepVerifier.create(tickRepository.getTickTransactions(42L))
                .expectNext("a", "b", "c")
                .verifyComplete();

        StepVerifier.create(tickRepository.getTickTransactions(43L))
                .expectNext("x", "y", "z")
                .verifyComplete();

        StepVerifier.create(tickRepository.getTickTransactions(666L))
                .verifyComplete();
    }

    @BeforeAll
    public static void startRedis() throws IOException {
        REDIS_SERVER.start();
    }

    @AfterAll
    public static void stopRedis() throws IOException {
        REDIS_SERVER.stop();
    }

    @SneakyThrows
    private static RedisServer createRedis() {
        return RedisServer.newRedisServer()
                .port(16379)
                .build();
    }

}