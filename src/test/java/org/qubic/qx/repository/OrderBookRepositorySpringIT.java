package org.qubic.qx.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.AbstractRedisTest;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.domain.OrderBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

import java.util.List;

import static org.qubic.qx.repository.OrderBookRepository.OBS_MIN_RANK;

@Slf4j
class OrderBookRepositorySpringIT extends AbstractRedisTest {

    @Autowired
    private OrderBookRepository orderBookRepository;

    @Autowired
    private ReactiveStringRedisTemplate redisStringTemplate;

    @BeforeEach
    void init() {
        redisStringTemplate.delete("obs:issuer:asset").block();
    }

    @Test
    void storeOrderBook() {

        List<AssetOrder> bids = List.of(new AssetOrder("identity", 1, 2));
        List<AssetOrder> asks = List.of(new AssetOrder("identity", 3, 4));
        OrderBook orderBook = new OrderBook(123L, "issuer", "asset", asks, bids);

        StepVerifier.create(orderBookRepository.storeOrderBook(orderBook))
                .expectNext(0L)
                .verifyComplete();

        StepVerifier.create(Mono.zip(redisStringTemplate.opsForHash().hasKey("ob:issuer:asset:123", "asks"),
                redisStringTemplate.opsForHash().hasKey("ob:issuer:asset:123", "bids")))
                .expectNext(Tuples.of(true, true))
                .verifyComplete();

    }

    @Test
    void storeOrderBook_givenCapacityReached_thenRemoveOldOnes() {

        List<AssetOrder> bids = List.of(new AssetOrder("identity", 1, 2));
        List<AssetOrder> asks = List.of(new AssetOrder("identity", 3, 4));

        for (int i = 1; i < Math.abs(OBS_MIN_RANK); i++) {
            OrderBook orderBook = new OrderBook(i, "issuer", "asset", asks, bids);
            StepVerifier.create(orderBookRepository.storeOrderBook(orderBook))
                    .expectNext(0L)
                    .verifyComplete();
        }

        OrderBook orderBook = new OrderBook(666L, "issuer", "asset", asks, bids);
        StepVerifier.create(orderBookRepository.storeOrderBook(orderBook))
                .expectNext(1L) // previous one removed
                .verifyComplete();

        StepVerifier.create(redisStringTemplate.opsForZSet().count("obs:issuer:asset", Range.unbounded()))
                .expectNext(Math.abs(OBS_MIN_RANK) - 1)
                .verifyComplete();

    }

    @Test
    void getLatestOrderBook() {

        List<AssetOrder> bids = List.of(new AssetOrder("identity", 1, 2));
        List<AssetOrder> asks = List.of(new AssetOrder("identity", 3, 4));
        OrderBook orderBook = new OrderBook(123L, "issuer", "asset", asks, bids);

        Mono<Long> storeOrderBook = orderBookRepository.storeOrderBook(orderBook);
        Mono<OrderBook> latestOrderBook = orderBookRepository.getLatestOrderBook("issuer", "asset");

        StepVerifier.create(storeOrderBook.then(latestOrderBook))
                .expectNext(orderBook)
                .verifyComplete();

    }

    @Test
    void getPreviousOrderBook() {

        List<AssetOrder> bids = List.of(new AssetOrder("identity", 1, 2));
        List<AssetOrder> asks = List.of(new AssetOrder("identity", 3, 4));
        OrderBook orderBook1 = new OrderBook(123L, "issuer", "asset", asks, bids);
        OrderBook orderBook2 = new OrderBook(124L, "issuer", "asset", asks, bids);
        OrderBook orderBook3 = new OrderBook(125L, "issuer", "asset", asks, bids);

        Mono<Long> storeOrderBook1 = orderBookRepository.storeOrderBook(orderBook1);
        Mono<Long> storeOrderBook2 = orderBookRepository.storeOrderBook(orderBook2);
        Mono<Long> storeOrderBook3 = orderBookRepository.storeOrderBook(orderBook3);
        Mono<OrderBook> previousOrderBook = orderBookRepository.getPreviousOrderBook("issuer", "asset");

        StepVerifier.create(Mono.zip(storeOrderBook1, storeOrderBook2, storeOrderBook3).then(previousOrderBook))
                .expectNext(orderBook2)
                .verifyComplete();

    }

    @Test
    void getLatestOrderBookBefore() {
        List<AssetOrder> bids = List.of(new AssetOrder("identity", 1, 2));
        List<AssetOrder> asks = List.of(new AssetOrder("identity", 3, 4));
        OrderBook orderBook1 = new OrderBook(123L, "issuer", "asset", asks, bids);
        OrderBook orderBook2 = new OrderBook(124L, "issuer", "asset", asks, bids);
        OrderBook orderBook3 = new OrderBook(125L, "issuer", "asset", asks, bids);

        Mono<Long> storeOrderBook1 = orderBookRepository.storeOrderBook(orderBook1);
        Mono<Long> storeOrderBook2 = orderBookRepository.storeOrderBook(orderBook2);
        Mono<Long> storeOrderBook3 = orderBookRepository.storeOrderBook(orderBook3);

        Mono<OrderBook> previousOrderBook1 = orderBookRepository.getPreviousOrderBookBefore("issuer", "asset", 124);

        StepVerifier.create(Mono.zip(storeOrderBook1, storeOrderBook2, storeOrderBook3).then(previousOrderBook1))
                .expectNext(orderBook1)
                .verifyComplete();

        Mono<OrderBook> previousOrderBook2 = orderBookRepository.getPreviousOrderBookBefore("issuer", "asset", 125);
        StepVerifier.create(previousOrderBook2)
                .expectNext(orderBook2)
                .verifyComplete();

        Mono<OrderBook> previousOrderBook3 = orderBookRepository.getPreviousOrderBookBefore("issuer", "asset", 999);
        StepVerifier.create(previousOrderBook3)
                .expectNext(orderBook3)
                .verifyComplete();

        Mono<OrderBook> noPreviousOrderBook = orderBookRepository.getPreviousOrderBookBefore("issuer", "asset", 42);
        StepVerifier.create(noPreviousOrderBook)
                .verifyComplete();
    }




}