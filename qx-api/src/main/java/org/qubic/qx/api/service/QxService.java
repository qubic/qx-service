package org.qubic.qx.api.service;

import org.qubic.qx.api.adapter.QxApiService;
import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;

import java.util.List;

public class QxService {

    private final QxApiService integrationApi;

    public QxService(QxApiService integrationApiService) {
        this.integrationApi = integrationApiService;
    }

    public Fees getFees() {
        return integrationApi.getFees();
    }

    public List<AssetOrder> getAssetAskOrders(String issuer, String asset) {
        return integrationApi.getAssetAskOrders(issuer, asset);
    }

    public List<AssetOrder> getAssetBidOrders(String issuer, String asset) {
        return integrationApi.getAssetBidOrders(issuer, asset);
    }

    public List<EntityOrder> getEntityAskOrders(String identity) {
        return integrationApi.getEntityAskOrders(identity);
    }

    public List<EntityOrder> getEntityBidOrders(String identity) {
        return integrationApi.getEntityBidOrders(identity);
    }

}
