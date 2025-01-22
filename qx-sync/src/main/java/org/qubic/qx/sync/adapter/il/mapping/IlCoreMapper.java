package org.qubic.qx.sync.adapter.il.mapping;

import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qubic.qx.sync.adapter.ExtraDataMapper;
import org.qubic.qx.sync.adapter.il.domain.IlTickData;
import org.qubic.qx.sync.adapter.il.domain.IlTickInfo;
import org.qubic.qx.sync.adapter.il.domain.IlTransaction;
import org.qubic.qx.sync.domain.ExtraData;
import org.qubic.qx.sync.domain.TickData;
import org.qubic.qx.sync.domain.TickInfo;
import org.qubic.qx.sync.domain.Transaction;
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
    public abstract Transaction mapTransaction(IlTransaction source);

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
