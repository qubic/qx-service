package org.qubic.qx.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class TickRepository {

    public static final String KEY_TICK_SYNCED_LATEST = "tick:synced:latest"; // key value
    public static final String KEY_TICKS_PROCESSED = "ticks:processed"; // set
    public static final String KEY_TICK_TRANSACTIONS = "tick:%d:transactions"; // list
    // TODO public static final String KEY_TICKS_RELEVANT -> set with tick numbers that have qx transactions

    private final ReactiveStringRedisTemplate redisStringTemplate;

    public TickRepository(ReactiveStringRedisTemplate redisStringTemplate) {
        this.redisStringTemplate = redisStringTemplate;
    }

    public Mono<Long> getLatestSyncedTick() {
        return getLongValue(KEY_TICK_SYNCED_LATEST);
    }

    public Mono<Boolean> setLatestSyncedTick(long latestSyncedBlock) {
        return setValue(KEY_TICK_SYNCED_LATEST, String.valueOf(latestSyncedBlock));
    }

    public Mono<Long> addToProcessedTicks(long tickNumber) {
        return addToSet(KEY_TICKS_PROCESSED, String.valueOf(tickNumber));
    }

    public Mono<Boolean> isProcessedTick(long tickNumber) {
        return redisStringTemplate.opsForSet()
                .isMember(KEY_TICKS_PROCESSED, String.valueOf(tickNumber));
    }

    private Mono<Long> addToSet(String key, String value) {
        return redisStringTemplate
                .opsForSet()
                .add(key, value)
                .doOnNext(count -> log.info("Added [{}] to set [{}].", value, key));
    }

    public Mono<Long> setTickTransactions(long tickNumber, List<String> transactionHashes) {
        return redisStringTemplate.opsForList()
                .rightPushAll(String.format(KEY_TICK_TRANSACTIONS, tickNumber), transactionHashes)
                .doOnSuccess(success -> log.debug("Set transactions for tick [{}]: {}", tickNumber, transactionHashes))
                .doOnError(t -> log.error("Error setting transactions {} for tick [{}]: {}", transactionHashes, tickNumber, t.toString()));
    }

    public Flux<String> getTickTransactions(long tickNumber) {
        return redisStringTemplate.opsForList()
                .range(String.format(KEY_TICK_TRANSACTIONS, tickNumber), 0, -1)
                .doOnEach(hash -> log.debug("Retrieved transaction hash [{}] for tick [{}].", hash, tickNumber))
                .doOnError(t -> log.error("Error getting transactions for tick [{}]: {}", tickNumber, t.toString()));
    }

    private Mono<Boolean> setValue(String key, String value) {
        return redisStringTemplate
                .opsForValue()
                .set(key, value)
                .doOnSuccess(success -> log.debug("Set key [{}] to [{}]: [{}]", key, value, success))
                .doOnError(t -> log.error("Updating key [{}] to [{}] failed: {}", key, value, t.toString()));
    }

    private Mono<Long> getLongValue(String key) {
        return redisStringTemplate
                .opsForValue()
                .get(key)
                .doOnNext(value -> log.debug("Retrieved [{}]: [{}].", key, value))
                .defaultIfEmpty("0")
                .map(this::toLongValue);
    }

    private Long toLongValue(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException nfe) {
            log.error(nfe.toString(), nfe);
            return 0L;
        }
    }

}
