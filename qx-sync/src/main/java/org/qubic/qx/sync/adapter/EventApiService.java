package org.qubic.qx.sync.adapter;

import org.qubic.qx.sync.domain.EpochAndTick;
import org.qubic.qx.sync.domain.TransactionEvents;
import reactor.core.publisher.Mono;

import java.util.List;

public interface EventApiService {

    Mono<List<TransactionEvents>> getTickEvents(long tick);

    Mono<EpochAndTick> getLastProcessedTick();
}
