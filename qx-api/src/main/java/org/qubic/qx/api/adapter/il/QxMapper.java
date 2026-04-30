package org.qubic.qx.api.adapter.il;

import at.qubic.api.domain.qx.response.QxFees;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qubic.qx.api.adapter.il.domain.IlAssetOrder;
import org.qubic.qx.api.adapter.il.domain.IlEntityOrder;
import org.qubic.qx.api.adapter.il.domain.IlFees;
import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;

import java.util.List;

@Mapper(componentModel = "spring", uses = DataTypeTranslator.class)
public interface QxMapper {

    Fees mapFees(IlFees fees);
    Fees mapFees(QxFees fees);

    AssetOrder mapAssetOrder(IlAssetOrder assetOrder);
    @Mapping(target = "entityId", source = "entity", qualifiedBy = PublicKeyMapping.class)
    AssetOrder mapAssetOrder(at.qubic.api.domain.qx.response.AssetOrder assetOrder);

    EntityOrder mapEntityOrder(IlEntityOrder entityOrder);
    @Mapping(target = "issuerId", source = "issuer", qualifiedBy = PublicKeyMapping.class)
    @Mapping(target = "assetName", source = "assetName", qualifiedBy = AssetNameMapping.class)
    EntityOrder mapEntityOrder(at.qubic.api.domain.qx.response.EntityOrder entityOrder);

    List<AssetOrder> mapAssetOrderList(List<IlAssetOrder> assetOrders);
    List<AssetOrder> mapQxAssetOrderList(List<at.qubic.api.domain.qx.response.AssetOrder> assetOrders);

    List<EntityOrder> mapEntityOrderList(List<IlEntityOrder> entityOrders);
    List<EntityOrder> mapQxEntityOrderList(List<at.qubic.api.domain.qx.response.EntityOrder> entityOrders);

}
