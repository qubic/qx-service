package org.qubic.qx.sync.api.service;

import org.qubic.qx.sync.adapter.QxApiService;
import org.qubic.qx.sync.api.domain.AssetOrder;
import reactor.core.publisher.Mono;

import java.util.List;

public class QxService {

    private final QxApiService integrationApi;

    public QxService(QxApiService integrationApiService) {
        this.integrationApi = integrationApiService;
    }

    public Mono<List<AssetOrder>> getAssetAskOrders(String issuer, String asset) {
        return integrationApi.getAssetAskOrders(issuer, asset);
    }

    public Mono<List<AssetOrder>> getAssetBidOrders(String issuer, String asset) {
        return integrationApi.getAssetBidOrders(issuer, asset);
    }

}
