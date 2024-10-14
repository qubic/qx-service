package org.qubic.qx.sync.adapter.qubicj.mapping;

import at.qubic.api.domain.qx.response.QxFees;
import at.qubic.api.domain.std.SignedTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qubic.qx.sync.api.domain.AssetOrder;
import org.qubic.qx.sync.api.domain.EntityOrder;
import org.qubic.qx.sync.api.domain.Fees;
import org.qubic.qx.sync.domain.TickData;
import org.qubic.qx.sync.domain.TickInfo;
import org.qubic.qx.sync.domain.Transaction;

import java.util.List;

@Mapper(componentModel = "spring", uses = DataTypeTranslator.class)
public interface QubicjMapper {

    @Mapping(target = "assetIssuanceFee", qualifiedBy = UnsignedIntMapping.class)
    @Mapping(target = "transferFee", qualifiedBy = UnsignedIntMapping.class)
    @Mapping(target = "tradeFee", qualifiedBy = UnsignedIntMapping.class)
    Fees map(QxFees source);

    @Mapping(target = "entityId", source = "entity", qualifiedBy = PublicKeyMapping.class)
    AssetOrder map(at.qubic.api.domain.qx.response.AssetOrder source);

    @Mapping(target = "issuerId", source = "issuer", qualifiedBy = PublicKeyMapping.class)
    @Mapping(target = "assetName", qualifiedBy = AssetNameMapping.class)
    EntityOrder map(at.qubic.api.domain.qx.response.EntityOrder source);

    List<AssetOrder> mapAssetOrders(List<at.qubic.api.domain.qx.response.AssetOrder> source);
    List<EntityOrder> mapEntityOrders(List<at.qubic.api.domain.qx.response.EntityOrder> source);

    @Mapping(target = "sourcePublicId", source = "transaction.sourcePublicKey", qualifiedBy = PublicKeyMapping.class)
    @Mapping(target = "destinationPublicId", source = "transaction.destinationPublicKey", qualifiedBy = PublicKeyMapping.class)
    @Mapping(target = "amount", source = "transaction.amount")
    @Mapping(target = "tick", source = "transaction.tick", qualifiedBy = UnsignedIntMapping.class)
    @Mapping(target = "inputType", source = "transaction.inputType", qualifiedBy = UnsignedShortMapping.class)
    @Mapping(target = "inputSize", source = "transaction.inputSize", qualifiedBy = UnsignedShortMapping.class)
    @Mapping(target = "extraData", source = "transaction", qualifiedBy = ExtraDataMapping.class)
    @Mapping(target = "moneyFlew", ignore = true)
    Transaction map(SignedTransaction source);

    @Mapping(target = "epoch", qualifiedBy = UnsignedShortMapping.class)
    @Mapping(target = "tick", qualifiedBy = UnsignedIntMapping.class)
    @Mapping(target = "timestamp", source = "source.timestamp")
    TickData map(at.qubic.api.domain.std.response.TickData source);

    @Mapping(target = "epoch", qualifiedBy = UnsignedShortMapping.class)
    @Mapping(target = "tick", qualifiedBy = UnsignedIntMapping.class)
    @Mapping(target = "initialTick", qualifiedBy = UnsignedIntMapping.class)
    TickInfo map(at.qubic.api.domain.std.response.TickInfo source);

}
