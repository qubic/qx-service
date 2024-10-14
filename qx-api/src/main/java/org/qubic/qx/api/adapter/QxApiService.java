package org.qubic.qx.api.adapter;

import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;

import java.util.List;

public interface QxApiService {

    Fees getFees();

    List<AssetOrder> getAssetAskOrders(String issuer, String asset);

    List<AssetOrder> getAssetBidOrders(String issuer, String asset);

    List<EntityOrder> getEntityAskOrders(String identity);

    List<EntityOrder> getEntityBidOrders(String identity);
}
