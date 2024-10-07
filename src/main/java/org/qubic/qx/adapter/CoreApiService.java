package org.qubic.qx.adapter;

import org.qubic.qx.domain.TickData;
import org.qubic.qx.domain.TickInfo;
import org.qubic.qx.domain.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CoreApiService {

    Mono<TickInfo> getTickInfo();

    Mono<Long> getCurrentTick();

    Mono<Long> getInitialTick();

    Mono<TickData> getTickData(long tickNumber);

    Flux<Transaction> getQxTransactions(long tick);

}
