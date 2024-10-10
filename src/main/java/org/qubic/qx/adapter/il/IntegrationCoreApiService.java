package org.qubic.qx.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.qubic.qx.adapter.CoreApiService;
import org.qubic.qx.adapter.Qx;
import org.qubic.qx.adapter.exception.EmptyResultException;
import org.qubic.qx.adapter.il.domain.IlTickData;
import org.qubic.qx.adapter.il.domain.IlTickInfo;
import org.qubic.qx.adapter.il.domain.IlTransaction;
import org.qubic.qx.adapter.il.domain.IlTransactions;
import org.qubic.qx.adapter.il.mapping.IlCoreMapper;
import org.qubic.qx.domain.TickData;
import org.qubic.qx.domain.TickInfo;
import org.qubic.qx.domain.Transaction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
                .bodyValue(String.format("{\"tick\":%d }", tick))
                .retrieve()
                .bodyToMono(IlTickData.class)
                .retry(NUM_RETRIES)
                .map(mapper::map);
    }

    @Override
    public Flux<Transaction> getQxTransactions(long tick) {
        return webClient.post()
                .uri(CORE_BASE_PATH_V1 + "/getTickTransactions")
                .bodyValue(String.format("{\"tick\":%d }", tick))
                .retrieve()
                .bodyToMono(IlTransactions.class)
                .retry(NUM_RETRIES)
                .flatMapIterable(IlTransactions::transactions)
                .filter(this::isRelevantTransaction)
                .map(mapper::mapTransaction);
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

    private static boolean isSentToQxAddress(IlTransaction transaction) {
        return StringUtils.equals(transaction.destId(), Qx.QX_PUBLIC_ID);
    }

    private static boolean isRelevantInputType(IlTransaction transaction) {
        return Qx.ALL_INPUT_TYPES.contains(transaction.inputType());
    }

}
