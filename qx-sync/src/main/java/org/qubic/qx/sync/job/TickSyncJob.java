package org.qubic.qx.sync.job;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.EventApiService;
import org.qubic.qx.sync.adapter.Qx;
import org.qubic.qx.sync.adapter.exception.EmptyResultException;
import org.qubic.qx.sync.domain.*;
import org.qubic.qx.sync.repository.TickRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class TickSyncJob {

    public static final String TAG_KEY_SOURCE = "source";
    public static final String TAG_VALUE_SYNCED = "sync";
    public static final String TAG_VALUE_EVENTS = "events";
    public static final String TAG_VALUE_LIVE = "live";
    public static final String METRIC_LATEST_TICK = "tick.latest";

    private final TickRepository tickRepository;
    private final CoreApiService coreService;
    private final EventApiService eventService;
    private final TransactionProcessor transactionProcessor;

    // export tick numbers as metric
    private final AtomicLong latestSyncedTick = Objects.requireNonNull(
            Metrics.gauge(METRIC_LATEST_TICK, Tags.of(TAG_KEY_SOURCE, TAG_VALUE_SYNCED), new AtomicLong(0)));
    private final AtomicLong latestEventTick = Objects.requireNonNull(
            Metrics.gauge(METRIC_LATEST_TICK, Tags.of(TAG_KEY_SOURCE, TAG_VALUE_EVENTS), new AtomicLong(0)));
    private final AtomicLong latestLiveTick = Objects.requireNonNull(
            Metrics.gauge(METRIC_LATEST_TICK, Tags.of(TAG_KEY_SOURCE, TAG_VALUE_LIVE), new AtomicLong(0)));

    public TickSyncJob(TickRepository tickRepository, CoreApiService coreService, EventApiService eventService, TransactionProcessor transactionProcessor) {
        this.tickRepository = tickRepository;
        this.coreService = coreService;
        this.eventService = eventService;
        this.transactionProcessor = transactionProcessor;
    }

    public Flux<Long> sync() {
        // get highes available start block (start from scratch or last db state)
        return getLatestAvailableTick()
                .flatMap(this::calculateStartAndEndTick)
                .flatMapMany(this::calculateSyncRange)
                .doOnNext(tickNumber -> log.debug("Syncing tick [{}].", tickNumber))
                .concatMap(this::processTick);
    }

    public Mono<Long> updateLatestSyncedTick(long syncedTick) {
        // only update if it increased
        return tickRepository.getLatestSyncedTick()
                .flatMap(latest -> latest >= syncedTick
                        ? Mono.just(false)
                        : tickRepository.setLatestSyncedTick(syncedTick))
                .then(Mono.just(syncedTick));
    }

    private Mono<Tuple2<TickInfo, EpochAndTick>> getLatestAvailableTick() {
        return Mono.zip(coreService.getTickInfo(), eventService.getLastProcessedTick())
                .doOnNext(tuple -> {
                    latestLiveTick.set(tuple.getT1().tick());
                    latestEventTick.set(tuple.getT2().tickNumber());
                    // log if there is a 'larger' gap between current tick and event service
                    if (Math.abs(tuple.getT1().tick() - tuple.getT2().tickNumber()) > 10) {
                        log.info("Current tick: [{}]. Events are available until tick [{}].",
                                tuple.getT1().tick(), tuple.getT2().tickNumber());
                    }
                });
    }

    private Flux<Long> calculateSyncRange(Tuple2<Long, Long> startAndEndTick) {
        long startTick = startAndEndTick.getT1();
        long endTick = startAndEndTick.getT2(); // we could do +1 here because end tick is exclusive but we better wait one tick
        int numberOfTicks = Math.min(1_000, (int) (endTick - startTick)); // we don't sync the latest tick (integration api might still be behind)
        if (numberOfTicks > 0) {
            if (numberOfTicks > 1) {
                if (numberOfTicks > 5) {
                    log.info("Syncing range from tick [{}] (incl) to [{}] (excl). Number of ticks: [{}].", startTick, endTick, numberOfTicks);
                }
                return Flux.range(0, numberOfTicks).map(counter -> startTick + counter);
            } else {
                return Flux.just(startTick);
            }
        } else if (numberOfTicks < -3) { // don't log minor fluctuations
            log.warn("Not syncing. Invalid sync range. From tick [{}] to tick [{}].", startTick, endTick);
            return Flux.empty();
        } else {
            log.debug("Nothing to sync... start [{}], end [{}]", startTick, endTick);
            return Flux.empty();
        }
    }

    private Mono<Tuple2<Long, Long>> calculateStartAndEndTick(Tuple2<TickInfo, EpochAndTick> tuple) {
        return tickRepository.getLatestSyncedTick()
                .doOnNext(latestSyncedTick::set)
                // take latest stored tick + 1 as next tick or initial tick if no old ticks are available
                .map(latestStoredTick -> latestStoredTick < tuple.getT1().initialTick()
                        ? tuple.getT1().initialTick()
                        : latestStoredTick + 1)
                // take the lowest common tick as end tick from normal nodes and event nodes
                .map(startTick -> Tuples.of(startTick, Math.min(tuple.getT1().tick(), tuple.getT2().tickNumber())));
    }

    private Mono<Long> processTick(Long tickNumber) {
        return tickRepository.isProcessedTick(tickNumber)
                .flatMap(alreadyProcessed -> alreadyProcessed
                        ? Mono.just(tickNumber).doOnNext(n -> log.debug("Skipping already stored tick [{}].", n))
                        : processNewTick(tickNumber));
    }

    private Mono<Long> processNewTick(Long tickNumber) {
        return queryTransactionsWithMetadata(tickNumber)
                .flatMap(list -> processTransactions(tickNumber, list))
                .map(x -> tickNumber)
                .doOnNext(tno -> log.debug("Synced tick [{}].", tno))
                .doOnError(err -> log.error("Error processing tick [{}]: {}", tickNumber, err.getMessage()));
    }

    private Mono<List<TransactionWithMeta>> queryTransactionsWithMetadata(long tickNumber) {
        Mono<List<TransactionEvents>> tickEventsMono = eventService.getTickEvents(tickNumber); // never empty
        Mono<TickData> tickDataMono = coreService.getTickData(tickNumber); // for time stamp // never empty
        Mono<List<Transaction>> qxTransactionsListMono = coreService.getQxTransactions(tickNumber)
                .doOnNext(tx -> log.info("[{}] Received [{}] transaction.", tx.transactionHash(), Qx.OrderType.fromCode(tx.inputType())))
                .collectList(); // empty list, if empty flux
        return Mono.zip(tickDataMono, qxTransactionsListMono, tickEventsMono)
                .switchIfEmpty(emptyResult(String.format("Error getting tick data, transactions and/or events for tick [%s].", tickNumber)))
                .map(tuple -> mapToTransactionsWithMetadata(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    private static List<TransactionWithMeta> mapToTransactionsWithMetadata(TickData tickData, List<Transaction> qxTransactions, List<TransactionEvents> events) {
        List<TransactionWithMeta> meta = new ArrayList<>();
        for (Transaction tx : qxTransactions) {
            meta.add(TransactionWithMeta.builder()
                    .transaction(tx)
                    .time(tickData.timestamp())
                    .events(getEventsForTransaction(events, tx.transactionHash()))
                    .build());
        }
        return meta;
    }

    private static List<TransactionEvent> getEventsForTransaction(List<TransactionEvents> events, String transactionHash) {
        return events.stream()
                .filter(txe -> StringUtils.equals(transactionHash, txe.txId()))
                .findAny() // there should only be one
                .orElse(new TransactionEvents(transactionHash, List.of())) // empty
                .events();
    }

    private Mono<?> processTransactions(Long tickNumber, List<TransactionWithMeta> txs) {
        Mono<Long> storeTickNumberMono = Mono.defer(() -> tickRepository.addToProcessedTicks(tickNumber));
        if (CollectionUtils.isEmpty(txs)) {
            return storeTickNumberMono
                    .then(Mono.just(false));
        } else {
            return Flux.fromIterable(txs)
                    .flatMap(transactionProcessor::processQxTransaction)
                    .then(storeTickNumberMono);
        }
    }

    private <T> Mono<T> emptyResult(String message) {
        return Mono.error(new EmptyResultException(message));
    }

}
