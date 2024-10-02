package org.qubic.qx.adapter.qubicj.mapping;

import at.qubic.api.domain.qx.response.QxFees;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.api.domain.EntityOrder;
import org.qubic.qx.api.domain.Fees;

import java.util.List;

@Mapper(componentModel = "spring", uses = DataTypeTranslator.class)
public interface QubicjOxMapper {

    @Mapping(target = "assetIssuanceFee", qualifiedBy = UnsignedIntMapping.class)
    @Mapping(target = "transferFee", qualifiedBy = UnsignedIntMapping.class)
    @Mapping(target = "tradeFee", qualifiedBy = UnsignedIntMapping.class)
    Fees mapFees(QxFees source);

    @Mapping(target = "entityId", source = "entity", qualifiedBy = PublicKeyMapping.class)
    AssetOrder mapAssetOrder(at.qubic.api.domain.qx.response.AssetOrder source);

    @Mapping(target = "issuerId", source = "issuer", qualifiedBy = PublicKeyMapping.class)
    @Mapping(target = "assetName", qualifiedBy = AssetNameMapping.class)
    EntityOrder mapEntityOrder(at.qubic.api.domain.qx.response.EntityOrder source);

    List<AssetOrder> mapAssetOrders(List<at.qubic.api.domain.qx.response.AssetOrder> source);
    List<EntityOrder> mapEntityOrders(List<at.qubic.api.domain.qx.response.EntityOrder> source);

}
