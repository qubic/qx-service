package org.qubic.qx.adapter.il.mapping;

import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qubic.qx.adapter.ExtraDataMapper;
import org.qubic.qx.adapter.il.domain.IlTickData;
import org.qubic.qx.adapter.il.domain.IlTickInfo;
import org.qubic.qx.adapter.il.domain.IlTransaction;
import org.qubic.qx.domain.ExtraData;
import org.qubic.qx.domain.TickData;
import org.qubic.qx.domain.TickInfo;
import org.qubic.qx.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Base64;

@Mapper(componentModel = "spring")
public abstract class IlCoreMapper {

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Setter // needed for test
    @Autowired // needed for mapstruct
    private ExtraDataMapper extraDataMapper;

    @Mapping(target = "transactionHash", source = "source.txId")
    @Mapping(target = "sourcePublicId", source = "source.sourceId")
    @Mapping(target = "destinationPublicId", source = "source.destId")
    @Mapping(target = "extraData", source = "source")
    public abstract Transaction mapTransaction(IlTransaction source, Boolean moneyFlew);

    @Mapping(target = "initialTick", source= "initialTickOfEpoch")
    public abstract TickInfo map(IlTickInfo source);

    public ExtraData mapInput(IlTransaction source) {
        int inputType = source.inputType();
        byte[] input = Base64.getDecoder().decode(source.input());
        assert input != null && input.length == source.inputSize();
        return extraDataMapper.map(inputType, input);
    }

    public abstract TickData map(IlTickData tickData);

}
