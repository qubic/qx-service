package org.qubic.qx.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.qubic.qx.adapter.CoreApiService;
import org.qubic.qx.adapter.QxSpecs;
import org.qubic.qx.adapter.exception.EmptyResultException;
import org.qubic.qx.adapter.il.domain.IlTransaction;
import org.qubic.qx.adapter.il.domain.IlTransactions;
import org.qubic.qx.adapter.il.mapping.IlTransactionMapper;
import org.qubic.qx.domain.TickInfo;
import org.qubic.qx.domain.Transaction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class IntegrationCoreApiService implements CoreApiService {

    private static final String CORE_BASE_PATH_V1 = "/v1/core";
    private final WebClient webClient;
    private final IlTransactionMapper transactionMapper;

    public IntegrationCoreApiService(WebClient webClient, IlTransactionMapper transactionMapper) {
        this.webClient = webClient;
        this.transactionMapper = transactionMapper;
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
                .map(TickInfo::initialTickOfEpoch)
                .doOnNext(tick -> log.debug("Initial epoch tick: [{}]", tick));
    }

    @Override
    public Flux<Transaction> getQxTransactions(long tick) {
        return webClient.get()
                .uri(CORE_BASE_PATH_V1 + "/getTickTransactions")
                .retrieve()
                .bodyToMono(IlTransactions.class)
                .flatMapIterable(IlTransactions::transactions)
                .filter(this::isRelevantTransaction)
                .map(transactionMapper::mapTransaction);
    }

    private Mono<TickInfo> getTickInfo() {
        return webClient.get()
                .uri(CORE_BASE_PATH_V1 + "/getTickInfo")
                .retrieve()
                .bodyToMono(TickInfo.class)
                .switchIfEmpty(Mono.error(new EmptyResultException("Could not get tick info.")));
    }

    private boolean isRelevantTransaction(IlTransaction transaction) {
        String relevantQxOperation = QxSpecs.INPUT_TYPES.get(transaction.inputType());
        if (StringUtils.equals(transaction.destId(), QxSpecs.QX_PUBLIC_ID)
                && StringUtils.isNotBlank(relevantQxOperation)) {
            log.debug("[{}]: [{}].", relevantQxOperation, transaction.txId());
            return true;
        } else {
            return false;
        }
    }

}
