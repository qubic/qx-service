package org.qubic.qx.api.service;

import org.qubic.qx.adapter.il.qx.QxApiClient;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.api.domain.Fees;
import org.qubic.qx.api.mapping.QxMapper;
import reactor.core.publisher.Mono;

import java.util.List;

public class QxService {

    private final QxApiClient qxApiClient;
    private final QxMapper qxMapper;

    public QxService(QxApiClient qxApiClient, QxMapper qxMapper) {
        this.qxApiClient = qxApiClient;
        this.qxMapper = qxMapper;
    }

    public Mono<Fees> getFees() {
        return qxApiClient.getFees()
                .map(qxMapper::mapFees);
    }

    public Mono<List<AssetOrder>> getAskOrders(String issuer, String asset) {
        return qxApiClient.getAskOrders(issuer, asset)
                .map(qxMapper::mapAssetOrderList);
    }

    public Mono<List<AssetOrder>> getBidOrders(String issuer, String asset) {
        return qxApiClient.getBidOrders(issuer, asset)
                .map(qxMapper::mapAssetOrderList);
    }

}
