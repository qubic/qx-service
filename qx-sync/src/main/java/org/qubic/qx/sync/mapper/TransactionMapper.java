package org.qubic.qx.sync.mapper;

import org.mapstruct.Mapper;
import org.qubic.qx.sync.domain.Transaction;
import org.qubic.qx.sync.domain.TransactionWithTime;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionWithTime map(Transaction transaction, Instant timestamp);
    default long map(Instant instant) { return instant.getEpochSecond(); }

}
