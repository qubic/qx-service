package org.qubic.qx.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class TickRepository {

    public static final String KEY_TICK_SYNCED_LATEST = "tick:synced:latest"; // key value
    public static final String KEY_TICKS_PROCESSED = "ticks:processed"; // set
    public static final String KEY_TICKS_WITH_QX_TRANSACTIONS = "ticks:qx"; // set
    public static final String KEY_TRANSACTIONS_IN_TICK = "txs:%d"; // list
    public static final String LIST_SEPARATOR = ",";

    private final ReactiveStringRedisTemplate redisStringTemplate;

    public TickRepository(ReactiveStringRedisTemplate redisStringTemplate) {
        this.redisStringTemplate = redisStringTemplate;
    }

    public Mono<Long> getLatestSyncedTick() {
        return getValue(KEY_TICK_SYNCED_LATEST)
                .defaultIfEmpty("0")
                .map(this::toLongValue);
    }

    public Mono<Boolean> setLatestSyncedTick(long latestSyncedBlock) {
        return setValue(KEY_TICK_SYNCED_LATEST, String.valueOf(latestSyncedBlock));
    }

    public Mono<Long> addToProcessedTicks(long tickNumber) {
        return addToSet(KEY_TICKS_PROCESSED, String.valueOf(tickNumber));
    }

    public Mono<Long> addToQxTicks(long tickNumber) {
        return addToSet(KEY_TICKS_WITH_QX_TRANSACTIONS, String.valueOf(tickNumber));
    }

    public Mono<Boolean> isProcessedTick(long tickNumber) {
        return redisStringTemplate.opsForSet()
                .isMember(KEY_TICKS_PROCESSED, String.valueOf(tickNumber));
    }

    private Mono<Long> addToSet(String key, String value) {
        return redisStringTemplate
                .opsForSet()
                .add(key, value)
                .doOnNext(count -> {
                    if (count > 0) {
                        log.info("Added [{}] to set [{}].", value, key);
                    } else {
                        log.debug("[{}] is already member of set [{}].", value, key);
                    }
                });
    }

    public Mono<Boolean> setTickTransactions(long tickNumber, List<String> transactionHashes) {
        return setValue(String.format(KEY_TRANSACTIONS_IN_TICK, tickNumber), StringUtils.join(transactionHashes, LIST_SEPARATOR));
    }

    public Flux<String> getTickTransactions(long tickNumber) {
        return getValue(String.format(KEY_TRANSACTIONS_IN_TICK, tickNumber))
                .switchIfEmpty(Mono.just(StringUtils.EMPTY)
                        .doOnNext(x -> log.warn("Could not get transactions. There are no transactions for tick: [{}].", tickNumber)))
                .map(val -> Arrays.stream(StringUtils.split(val, LIST_SEPARATOR)).toList())
                .flatMapIterable(val -> val);
    }

    private Mono<Boolean> setValue(String key, String value) {
        return redisStringTemplate
                .opsForValue()
                .set(key, value)
                .doOnSuccess(success -> log.debug("Set key [{}] to [{}].", key, value))
                .doOnError(t -> log.error("Updating key [{}] to [{}] failed: {}", key, value, t.toString()));
    }

    private Mono<String> getValue(String key) {
        return redisStringTemplate
                .opsForValue()
                .get(key)
                .doOnNext(value -> log.debug("Retrieved [{}]: [{}].", key, value));
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
