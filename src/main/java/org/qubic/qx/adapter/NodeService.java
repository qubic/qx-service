package org.qubic.qx.adapter;

import org.qubic.qx.domain.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NodeService {
    
    byte[] QX_PUBLIC_KEY = new byte[]{
            1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    String QX_PUBLIC_ID = "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARMID";

    Mono<Long> getCurrentTick();

    Mono<Long> getInitialTick();

    Flux<Transaction> getQxTransactions(long tick);

}
