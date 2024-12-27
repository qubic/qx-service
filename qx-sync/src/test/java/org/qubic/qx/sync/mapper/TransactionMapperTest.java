package org.qubic.qx.sync.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.qubic.qx.sync.domain.QxAssetOrderData;
import org.qubic.qx.sync.domain.Transaction;
import org.qubic.qx.sync.domain.TransactionWithTime;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class TransactionMapperTest {

    private final TransactionMapper mapper = Mappers.getMapper(TransactionMapper.class);

    @Test
    void mapTransactionWithTime_thenSetTimestamp() {
        QxAssetOrderData extr = new QxAssetOrderData("issuer", "name", 42, 43);
        Transaction transaction = new Transaction("hash", "source", "destination", 1, 2, 3, 4, extr, true);
        TransactionWithTime mapped = mapper.map(transaction, Instant.EPOCH);
        assertThat(mapped).isEqualTo(new TransactionWithTime(
                "hash",
                "source",
                "destination",
                1, 2, Instant.EPOCH.getEpochSecond(), 3, 4, extr, true
        ));
    }

}