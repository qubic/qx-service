package org.qubic.qx.api.scheduler.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qubic.qx.api.db.domain.Transaction;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;

import java.time.Instant;

@Mapper(componentModel = "spring", uses = DatabaseMappings.class)
public interface TransactionMapper extends RedisToDomainMapper<Transaction, TransactionRedisDto> {

    @Mapping(target = "sourceId", source = "sourcePublicId", qualifiedBy = EntityMapping.class)
    @Mapping(target = "destinationId", source = "destinationPublicId", qualifiedBy = EntityMapping.class)
    @Mapping(target = "hash", source = "transactionHash")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tickTime", source = "timestamp")
    Transaction map(TransactionRedisDto source);

    default Instant map(long timestamp) {
        return Instant.ofEpochSecond(timestamp);
    }

}
