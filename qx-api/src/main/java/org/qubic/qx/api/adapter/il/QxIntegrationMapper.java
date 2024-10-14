package org.qubic.qx.api.adapter.il;

import org.mapstruct.Mapper;
import org.qubic.qx.api.adapter.il.domain.IlAssetOrder;
import org.qubic.qx.api.adapter.il.domain.IlEntityOrder;
import org.qubic.qx.api.adapter.il.domain.IlFees;
import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QxIntegrationMapper {

    Fees mapFees(IlFees fees);
    AssetOrder mapAssetOrder(IlAssetOrder assetOrder);
    EntityOrder mapEntityOrder(IlEntityOrder entityOrder);

    List<AssetOrder> mapAssetOrderList(List<IlAssetOrder> assetOrders);
    List<EntityOrder> mapEntityOrderList(List<IlEntityOrder> entityOrders);

}
