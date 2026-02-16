package org.qubic.qx.sync.adapter.il.mapping;

import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qubic.qx.sync.adapter.ExtraDataMapper;
import org.qubic.qx.sync.adapter.il.domain.query.IlQueryApiLastProcessedTick;
import org.qubic.qx.sync.adapter.il.domain.query.IlQueryApiTickData;
import org.qubic.qx.sync.adapter.il.domain.query.IlQueryApiTransaction;
import org.qubic.qx.sync.domain.ExtraData;
import org.qubic.qx.sync.domain.TickData;
import org.qubic.qx.sync.domain.TickInfo;
import org.qubic.qx.sync.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Base64;

@Mapper(componentModel = "spring")
public abstract class IlQueryApiMapper {

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Setter // needed for test
    @Autowired // needed for mapstruct
    private ExtraDataMapper extraDataMapper;

    @Mapping(target = "transactionHash", source = "transaction.hash")
    @Mapping(target = "sourcePublicId", source = "transaction.source")
    @Mapping(target = "destinationPublicId", source = "transaction.destination")
    @Mapping(target = "tick", source = "transaction.tickNumber")
    @Mapping(target = "extraData", source = "transaction")
    public abstract Transaction mapTransaction(IlQueryApiTransaction transaction);

    @Mapping(target = "tick", source = "tickNumber")
    @Mapping(target = "initialTick", source = "intervalInitialTick")
    public abstract TickInfo map(IlQueryApiLastProcessedTick source);

    @Mapping(target = "tick", source = "tickNumber")
    public abstract TickData map(IlQueryApiTickData tickData);

    public ExtraData mapInput(IlQueryApiTransaction source) {
        int inputType = source.inputType();
        byte[] input = Base64.getDecoder().decode(source.inputData());
        assert input != null && input.length == source.inputSize();
        return extraDataMapper.map(inputType, input);
    }

    public Instant map(long timestamp) {
        return Instant.ofEpochMilli(timestamp);
    }

}
