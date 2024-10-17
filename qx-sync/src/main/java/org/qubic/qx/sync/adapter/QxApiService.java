package org.qubic.qx.sync.adapter;

import org.qubic.qx.sync.api.domain.AssetOrder;
import reactor.core.publisher.Mono;

import java.util.List;

public interface QxApiService {

    Mono<List<AssetOrder>> getAssetAskOrders(String issuer, String asset);

    Mono<List<AssetOrder>> getAssetBidOrders(String issuer, String asset);

}
