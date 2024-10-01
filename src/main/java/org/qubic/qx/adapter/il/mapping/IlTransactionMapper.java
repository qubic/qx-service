package org.qubic.qx.adapter.il.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qubic.qx.adapter.il.domain.IlTransaction;
import org.qubic.qx.domain.Transaction;

@Mapper(componentModel = "spring")
public interface IlTransactionMapper {

    // TODO extra data
    @Mapping(target = "transactionHash", source = "txId")
    @Mapping(target = "sourcePublicId", source = "sourceId")
    @Mapping(target = "destinationPublicId", source = "destId")
    Transaction mapTransaction(IlTransaction source);

}
