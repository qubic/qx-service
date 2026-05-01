package org.qubic.qx.sync.adapter.il.mapping;

import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.il.domain.query.IlQueryApiLastProcessedTick;
import org.qubic.qx.sync.adapter.il.domain.query.IlQueryApiTickData;
import org.qubic.qx.sync.adapter.il.domain.query.IlQueryApiTransaction;
import org.qubic.qx.sync.domain.QxAssetOrderData;
import org.qubic.qx.sync.domain.TickData;
import org.qubic.qx.sync.domain.TickInfo;
import org.qubic.qx.sync.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IlQueryApiMapperIT {

    @Autowired
    private IlQueryApiMapper mapper;

    @Test
    void mapTransaction() {
        IlQueryApiTransaction source = new IlQueryApiTransaction(
                "transaction-id",
                42L,
                "source-identity",
                "destination-identity",
                12345L,
                5,
                56,
                "CDC7Y799XhZKyMvThoBjD/dnCh6/OfchC0C83KJT0F9DRkIAAAAAAAMAAAAAAAAACQAAAAAAAAA="
        );
        Transaction target = mapper.mapTransaction(source);

        assertThat(target).isNotNull();
        assertThat(target.transactionHash()).isEqualTo("transaction-id");
        assertThat(target.sourcePublicId()).isEqualTo("source-identity");
        assertThat(target.destinationPublicId()).isEqualTo("destination-identity");
        assertThat(target.amount()).isEqualTo(42L);
        assertThat(target.tick()).isEqualTo(12345L);
        assertThat(target.inputType()).isEqualTo(5);
        assertThat(target.inputSize()).isEqualTo(56);
        assertThat(target.extraData()).isInstanceOf(QxAssetOrderData.class);
        QxAssetOrderData extraData = (QxAssetOrderData) target.extraData();
        assertThat(extraData.name()).isEqualTo("CFB");
        assertThat(extraData.issuer()).isEqualTo("CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL");
        assertThat(extraData.price()).isEqualTo(3);
        assertThat(extraData.numberOfShares()).isEqualTo(9);
    }

    @Test
    void mapLastProcessedTick() {
        IlQueryApiLastProcessedTick source = new IlQueryApiLastProcessedTick(12345L, 129, 12000L, 12346L);
        TickInfo target = mapper.map(source);

        assertThat(target.tick()).isEqualTo(12345L);
        assertThat(target.epoch()).isEqualTo(129);
        assertThat(target.initialTick()).isEqualTo(12000L);
        assertThat(target.logTick()).isEqualTo(12346L);
    }

    @Test
    void mapTickData() {
        IlQueryApiTickData source = new IlQueryApiTickData(12345L, 129, 1714573140000L); // 2024-05-01T14:19:00Z
        TickData target = mapper.map(source);

        assertThat(target.tick()).isEqualTo(12345L);
        assertThat(target.epoch()).isEqualTo(129);
        assertThat(target.timestamp()).isEqualTo(Instant.ofEpochMilli(1714573140000L));
    }

    @Test
    void mapTimestamp() {
        long timestamp = 1714573140000L;
        Instant target = mapper.map(timestamp);
        assertThat(target).isEqualTo(Instant.ofEpochMilli(1714573140000L));
    }
}
