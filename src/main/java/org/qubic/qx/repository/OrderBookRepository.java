package org.qubic.qx.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.domain.OrderBook;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class OrderBookRepository {

    private static final String ORDER_BOOK_KEY = "ob:%s:%s:%d"; // hash, ob:issuer:assetName:tick
    private static final String ORDER_BOOKS = "obs:%s:%s"; // set, obs:issuer:assetName
    private static final long OBS_LOWEST_RANK = 0L;
    public static final long OBS_MIN_RANK = -100L; // -1 is highest. -1 means keep none. -10 means keep 9.
    private static final Range<Long> OBS_DISPOSE_RANGE = Range.closed(OBS_LOWEST_RANK, OBS_MIN_RANK);

    private final ReactiveStringRedisTemplate redisStringTemplate;
    private final ReactiveRedisTemplate<String, AssetOrder[]> assetOrderRedisTemplate;

    public OrderBookRepository(ReactiveStringRedisTemplate redisStringTemplate, ReactiveRedisTemplate<String, AssetOrder[]> assetOrderRedisTemplate) {
        this.redisStringTemplate = redisStringTemplate;
        this.assetOrderRedisTemplate = assetOrderRedisTemplate;
    }

    public Mono<Long> storeOrderBook(OrderBook orderBook) {
        String obKey = String.format(ORDER_BOOK_KEY, orderBook.issuer(), orderBook.assetName(), orderBook.tickNumber());
        String obsKey = String.format(ORDER_BOOKS, orderBook.issuer(), orderBook.assetName());
        return putOrderBook(obsKey, obKey, orderBook)
                .then(removeExpiredOrderBooks(obsKey));
    }

    public Mono<OrderBook> getLatestOrderBook(String issuer, String assetName) {
        String obsKey = String.format(ORDER_BOOKS, issuer, assetName);
        return redisStringTemplate.opsForZSet().reverseRange(obsKey, Range.just(0L))
                .next()
                .flatMap(this::getOrderBook);
    }

    public Mono<OrderBook> getPreviousOrderBook(String issuer, String assetName) {
        String obsKey = String.format(ORDER_BOOKS, issuer, assetName);
        return redisStringTemplate.opsForZSet().reverseRange(obsKey, Range.just(1L))
                .next()
                .flatMap(this::getOrderBook);
    }

    private Mono<?> putOrderBook(String indexKey, String entryKey, OrderBook orderBook) {
        return assetOrderRedisTemplate.opsForHash()
                .putAll(entryKey, Map.of("asks", orderBook.asks(), "bids", orderBook.bids()))
                .doOnNext(result -> log.info("Added order book [{}]: [{}].", entryKey, result))
                .then(addToIndex(indexKey, entryKey, orderBook.tickNumber())); // add to index
    }

    private Mono<Boolean> addToIndex(String indexKey, String entryKey, Long score) {
        return redisStringTemplate.opsForZSet().add(indexKey, entryKey, score)
                .doOnNext(result -> log.debug("Added [{}] to index [{}]: [{}].", entryKey, indexKey, result));
    }

    private Mono<OrderBook> getOrderBook(String obKey) {
        return assetOrderRedisTemplate.<String, AssetOrder[]>opsForHash()
                .multiGet(obKey, List.of("asks", "bids"))
                .map(list -> {
                    String[] split = StringUtils.split(obKey, ":");
                    return new OrderBook(Long.parseLong(split[3]),
                            split[1],
                            split[2],
                            Arrays.asList(list.getFirst()),
                            Arrays.asList(list.getLast()));
                })
                .doOnNext(ob -> log.info("Got order book: {}", ob));
    }

    private Mono<Long> removeExpiredOrderBooks(String indexKey) {
        return redisStringTemplate.opsForZSet()
                .range(indexKey, OBS_DISPOSE_RANGE)
                .flatMap(this::deleteHash)
                .then(redisStringTemplate.opsForZSet().removeRange(indexKey, OBS_DISPOSE_RANGE))
                .doOnNext(count -> log.debug("Removed [{}] entries from index [{}].", count, indexKey)); // clean index
    }

    private Mono<Boolean> deleteHash(String toBeRemoved) {
        return redisStringTemplate.opsForHash()
                .delete(toBeRemoved)
                .doOnNext(result -> log.info("Deleted hash [{}].", toBeRemoved));
    }

}
