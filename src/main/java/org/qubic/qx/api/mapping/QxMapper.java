package org.qubic.qx.api.mapping;

import org.mapstruct.Mapper;
import org.qubic.qx.adapter.il.qx.domain.QxAssetOrder;
import org.qubic.qx.adapter.il.qx.domain.QxEntityOrder;
import org.qubic.qx.adapter.il.qx.domain.QxFees;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.api.domain.EntityOrder;
import org.qubic.qx.api.domain.Fees;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QxMapper {

    Fees mapFees(QxFees fees);
    AssetOrder mapAssetOrder(QxAssetOrder assetOrder);
    EntityOrder mapEntityOrder(QxEntityOrder entityOrder);

    List<AssetOrder> mapAssetOrderList(List<QxAssetOrder> assetOrders);
    List<EntityOrder> mapEntityOrderList(List<QxEntityOrder> entityOrders);

}
