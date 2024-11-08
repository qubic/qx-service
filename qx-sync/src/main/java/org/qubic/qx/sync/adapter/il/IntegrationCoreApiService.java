package org.qubic.qx.sync.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.Qx;
import org.qubic.qx.sync.adapter.exception.EmptyResultException;
import org.qubic.qx.sync.adapter.il.domain.*;
import org.qubic.qx.sync.adapter.il.mapping.IlCoreMapper;
import org.qubic.qx.sync.domain.TickData;
import org.qubic.qx.sync.domain.TickInfo;
import org.qubic.qx.sync.domain.TickTransactionsStatus;
import org.qubic.qx.sync.domain.Transaction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Optional;

@Slf4j
public class IntegrationCoreApiService implements CoreApiService {

    private static final String CORE_BASE_PATH_V1 = "/v1/core";
    private static final int NUM_RETRIES = 3;
    private final WebClient webClient;
    private final IlCoreMapper mapper;

    public IntegrationCoreApiService(WebClient webClient, IlCoreMapper mapper) {
        this.webClient = webClient;
        this.mapper = mapper;
    }

    @Override
    public Mono<Long> getCurrentTick() {
        return getTickInfo()
                .map(TickInfo::tick)
                .doOnNext(tick -> log.debug("Current tick: [{}]", tick));
    }

    @Override
    public Mono<Long> getInitialTick() {
        return getTickInfo()
                .map(TickInfo::initialTick)
                .doOnNext(tick -> log.debug("Initial epoch tick: [{}]", tick));
    }

    @Override
    public Mono<TickData> getTickData(long tick) {
        return webClient.post()
                .uri(CORE_BASE_PATH_V1 + "/getTickData")
                .bodyValue(tickPayloadBody(tick))
                .retrieve()
                .bodyToMono(IlTickData.class)
                .retry(NUM_RETRIES)
                .map(mapper::map);
    }

    @Override
    public Flux<Transaction> getQxTransactions(long tick) {
        return Mono.zip(getTickTransactions(tick), getTickTransactionsStatus(tick))
                .flatMapMany(tuple -> Flux.fromIterable(tuple.getT1().transactions())
                        .map(ilt -> Tuples.of(ilt, getMoneyFlewStatus(tuple, ilt))))
                .filter(tuple -> isRelevantTransaction(tuple.getT1()))
                .map(tuple -> mapper.mapTransaction(tuple.getT1(), tuple.getT2().orElse(null)));
    }

    Mono<IlTransactions> getTickTransactions(long tick) {
        return webClient.post()
                .uri(CORE_BASE_PATH_V1 + "/getTickTransactions")
                .bodyValue(tickPayloadBody(tick))
                .retrieve()
                .bodyToMono(IlTransactions.class)
                .switchIfEmpty(Mono.error(emptyResult("get tick transactions", tick)))
                .retry(NUM_RETRIES);
    }

    @Override
    public Mono<TickTransactionsStatus> getTickTransactionsStatus(long tick) {
        return webClient.post()
                .uri(CORE_BASE_PATH_V1 + "/getTickTransactionsStatus")
                .bodyValue(tickPayloadBody(tick))
                .retrieve()
                .bodyToMono(TickTransactionsStatus.class)
                .switchIfEmpty(Mono.error(emptyResult("get tick transactions status", tick)))
                .retry(NUM_RETRIES);
    }

    @Override
    public Mono<TickInfo> getTickInfo() {
        return webClient.get()
                .uri(CORE_BASE_PATH_V1 + "/getTickInfo")
                .retrieve()
                .bodyToMono(IlTickInfo.class)
                .retry(NUM_RETRIES)
                .map(mapper::map)
                .switchIfEmpty(Mono.error(new EmptyResultException("Could not get tick info.")));
    }

    private boolean isRelevantTransaction(IlTransaction transaction) {
        if (isRelevantInputType(transaction) && isSentToQxAddress(transaction)) {
            log.debug("[{}]: [{}].", Qx.OrderType.fromCode(transaction.inputType()), transaction.txId());
            return true;
        } else {
            return false;
        }
    }

    private static Optional<Boolean> getMoneyFlewStatus(Tuple2<IlTransactions, TickTransactionsStatus> t2, IlTransaction ilt) {
        return t2.getT2().statusPerTx() == null ? Optional.empty() : Optional.ofNullable(t2.getT2().statusPerTx().get(ilt.txId()));
    }

    private static String tickPayloadBody(long tick) {
        return String.format("{\"tick\":%d}", tick);
    }

    private static boolean isSentToQxAddress(IlTransaction transaction) {
        return StringUtils.equals(transaction.destId(), Qx.QX_PUBLIC_ID);
    }

    private static boolean isRelevantInputType(IlTransaction transaction) {
        return Qx.ALL_INPUT_TYPES.contains(transaction.inputType());
    }

    private static EmptyResultException emptyResult(String action, long tick) {
        return new EmptyResultException(String.format("Could not %s for tick [%d].", action, tick));
    }

}
