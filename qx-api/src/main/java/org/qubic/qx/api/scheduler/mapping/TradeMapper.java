package org.qubic.qx.api.scheduler.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qubic.qx.api.db.domain.Trade;
import org.qubic.qx.api.redis.dto.TradeRedisDto;

import java.time.Instant;

@Mapper(componentModel = "spring", uses = DatabaseMappings.class)
public interface TradeMapper extends RedisToDomainMapper<Trade, TradeRedisDto> {

    @Mapping(target = "transactionId", source = "transactionHash", qualifiedBy = TransactionMapping.class)
    @Mapping(target = "tickTime", source = "timestamp")
    @Mapping(target = "makerId", source = "maker", qualifiedBy = EntityMapping.class)
    @Mapping(target = "assetId", source = "source", qualifiedBy = AssetMapping.class)
    @Mapping(target = "id", ignore = true)
    Trade map(TradeRedisDto source);

    default Instant map(long timestamp) {
        return Instant.ofEpochSecond(timestamp);
    }

}
