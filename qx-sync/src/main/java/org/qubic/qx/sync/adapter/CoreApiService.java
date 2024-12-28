package org.qubic.qx.sync.adapter;

import org.qubic.qx.sync.domain.Transaction;
import org.qubic.qx.sync.domain.TickData;
import org.qubic.qx.sync.domain.TickInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CoreApiService {

    Mono<TickInfo> getTickInfo();

    Mono<Long> getCurrentTick();

    Mono<TickData> getTickData(long tickNumber);

    Flux<Transaction> getQxTransactions(long tick);

}
