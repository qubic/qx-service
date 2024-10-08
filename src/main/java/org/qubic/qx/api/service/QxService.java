package org.qubic.qx.api.service;

import org.qubic.qx.adapter.QxApiService;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.api.domain.EntityOrder;
import org.qubic.qx.api.domain.Fees;
import reactor.core.publisher.Mono;

import java.util.List;

public class QxService {

    private final QxApiService integrationApi;

    public QxService(QxApiService integrationApiService) {
        this.integrationApi = integrationApiService;
    }

    public Mono<Fees> getFees() {
        return integrationApi.getFees();
    }

    public Mono<List<AssetOrder>> getAssetAskOrders(String issuer, String asset) {
        return integrationApi.getAssetAskOrders(issuer, asset);
    }

    public Mono<List<AssetOrder>> getAssetBidOrders(String issuer, String asset) {
        return integrationApi.getAssetBidOrders(issuer, asset);
    }

    public Mono<List<EntityOrder>> getEntityAskOrders(String identity) {
        return integrationApi.getEntityAskOrders(identity);
    }

    public Mono<List<EntityOrder>> getEntityBidOrders(String identity) {
        return integrationApi.getEntityBidOrders(identity);
    }

}
