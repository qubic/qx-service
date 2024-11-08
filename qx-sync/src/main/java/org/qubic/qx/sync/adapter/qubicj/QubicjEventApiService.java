package org.qubic.qx.sync.adapter.qubicj;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.sync.adapter.EventApiService;
import org.qubic.qx.sync.domain.TransactionEvents;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class QubicjEventApiService implements EventApiService {

    @Override
    public Mono<List<TransactionEvents>> getTickEvents(long tick) {
        log.warn("Event api not supported by qubicj.");
        return Mono.just(List.of());
    }
}
