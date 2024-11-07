package org.qubic.qx.api.adapter.il;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.qubic.qx.api.adapter.domain.TickData;
import org.qubic.qx.api.adapter.il.domain.IlTickData;
import org.qubic.qx.api.adapter.il.domain.IlTickDataResponse;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ArchiveMapperIT {

    private final ArchiveMapper archiveMapper = Mappers.getMapper(ArchiveMapper.class);

    @Test()
    void mapTickData() {
        Instant now = Instant.now();
        IlTickData tickData = new IlTickData(42, 43, String.valueOf(now.toEpochMilli()));
        IlTickDataResponse tickDataResponse = new IlTickDataResponse(tickData);

        TickData mapped = archiveMapper.map(tickDataResponse.tickData());
        assertThat(mapped).isNotNull();
        assertThat(mapped.tick()).isEqualTo(43);
        assertThat(mapped.timestamp()).isEqualTo(now.truncatedTo(ChronoUnit.MILLIS));
        assertThat(mapped.epoch()).isEqualTo(42);
    }

}