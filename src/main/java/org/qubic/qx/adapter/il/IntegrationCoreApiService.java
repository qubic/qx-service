package org.qubic.qx.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.qubic.qx.adapter.CoreApiService;
import org.qubic.qx.adapter.Qx;
import org.qubic.qx.adapter.exception.EmptyResultException;
import org.qubic.qx.adapter.il.domain.*;
import org.qubic.qx.adapter.il.mapping.IlCoreMapper;
import org.qubic.qx.domain.TickData;
import org.qubic.qx.domain.TickInfo;
import org.qubic.qx.domain.TickTransactionsStatus;
import org.qubic.qx.domain.Transaction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Slf4j
public class IntegrationCoreApiService implements CoreApiService {

    private static final String CORE_BASE_PATH_V1 = "/v1/core";
    private static final int NUM_RETRIES = 1;
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
        return Mono.zip(getTransactionsMono(tick), getTickTransactionsStatus(tick))
                .flatMapMany(t2 -> Flux.fromIterable(t2.getT1().transactions())
                        .map(ilt -> Tuples.of(ilt, t2.getT2().statusPerTx().get(ilt.txId()))))
                .filter(t2 -> isRelevantTransaction(t2.getT1()))
                .map(t2 -> mapper.mapTransaction(t2.getT1(), t2.getT2()));
    }

    private Mono<IlTransactions> getTransactionsMono(long tick) {
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

    private static EmptyResultException emptyResult(String action, long tick) {
        return new EmptyResultException(String.format("Could not %s for tick [%d].", action, tick));
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

    private static String tickPayloadBody(long tick) {
        return String.format("{\"tick\":%d }", tick);
    }

    private static boolean isSentToQxAddress(IlTransaction transaction) {
        return StringUtils.equals(transaction.destId(), Qx.QX_PUBLIC_ID);
    }

    private static boolean isRelevantInputType(IlTransaction transaction) {
        return Qx.ALL_INPUT_TYPES.contains(transaction.inputType());
    }

}
