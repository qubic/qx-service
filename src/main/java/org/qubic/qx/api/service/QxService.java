package org.qubic.qx.api.service;

import org.qubic.qx.adapter.il.qx.QxIntegrationApiClient;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.api.domain.Fees;
import reactor.core.publisher.Mono;

import java.util.List;

public class QxService {

    private final QxIntegrationApiClient qxApiClient;

    public QxService(QxIntegrationApiClient qxApiClient) {
        this.qxApiClient = qxApiClient;
    }

    public Mono<Fees> getFees() {
        return qxApiClient.getFees();
    }

    public Mono<List<AssetOrder>> getAskOrders(String issuer, String asset) {
        return qxApiClient.getAskOrders(issuer, asset);
    }

    public Mono<List<AssetOrder>> getBidOrders(String issuer, String asset) {
        return qxApiClient.getBidOrders(issuer, asset);
    }

}
