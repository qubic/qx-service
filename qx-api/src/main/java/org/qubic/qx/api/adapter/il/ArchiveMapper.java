package org.qubic.qx.api.adapter.il;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qubic.qx.api.adapter.domain.TickData;
import org.qubic.qx.api.adapter.il.domain.IlTickData;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface ArchiveMapper {

    @Mapping(target = "tick", source = "tickNumber")
    @Mapping(target = "timestamp", source = "timestamp")
    TickData map(IlTickData source);

    default Instant map(String timestamp) {
        return Instant.ofEpochMilli(Long.parseLong(timestamp));
    }

}
