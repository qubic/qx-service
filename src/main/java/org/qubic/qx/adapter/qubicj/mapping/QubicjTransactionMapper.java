package org.qubic.qx.adapter.qubicj.mapping;

import at.qubic.api.domain.std.SignedTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qubic.qx.domain.Transaction;

@Mapper(componentModel = "spring", uses = DataTypeTranslator.class)
public interface QubicjTransactionMapper {

    @Mapping(target = "sourcePublicId", source = "transaction.sourcePublicKey", qualifiedBy = PublicKeyMapping.class)
    @Mapping(target = "destinationPublicId", source = "transaction.destinationPublicKey", qualifiedBy = PublicKeyMapping.class)
    @Mapping(target = "amount", source = "transaction.amount")
    @Mapping(target = "tick", source = "transaction.tick", qualifiedBy = UnsignedIntMapping.class)
    @Mapping(target = "inputType", source = "transaction.inputType", qualifiedBy = UnsignedShortMapping.class)
    @Mapping(target = "inputSize", source = "transaction.inputSize", qualifiedBy = UnsignedShortMapping.class)
    @Mapping(target = "extraData", source = "transaction", qualifiedBy = ExtraDataMapping.class)
    Transaction map(SignedTransaction source);

}
