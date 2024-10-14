package org.qubic.qx.sync.adapter;

import org.qubic.qx.sync.api.domain.AssetOrder;
import org.qubic.qx.sync.api.domain.EntityOrder;
import org.qubic.qx.sync.api.domain.Fees;
import reactor.core.publisher.Mono;

import java.util.List;

public interface QxApiService {
    Mono<Fees> getFees();

    Mono<List<AssetOrder>> getAssetAskOrders(String issuer, String asset);

    Mono<List<AssetOrder>> getAssetBidOrders(String issuer, String asset);

    Mono<List<EntityOrder>> getEntityAskOrders(String identity);

    Mono<List<EntityOrder>> getEntityBidOrders(String identity);
}
