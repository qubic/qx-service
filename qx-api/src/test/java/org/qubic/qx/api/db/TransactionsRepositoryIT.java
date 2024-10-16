package org.qubic.qx.api.db;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.domain.Entity;
import org.qubic.qx.api.db.domain.QxAssetOrderData;
import org.qubic.qx.api.db.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionsRepositoryIT extends AbstractPostgresJdbcTest {

    @Autowired
    private EntitiesRepository entitiesRepository;

    @Autowired
    private TransactionsRepository repository;

    @Test
    void saveAndLoad() {

        Entity source = entitiesRepository.save(Entity.builder()
                .identity("SOURCE")
                .build());

        Entity destination = entitiesRepository.save(Entity.builder()
                .identity("DESTINATION")
                .build());

        QxAssetOrderData extraData = new QxAssetOrderData("ISSUER", "ASSET", 123, 456);

        Transaction transaction = Transaction.builder()
                .tick(1)
                .hash("hash")
                .sourceId(source.getId())
                .destinationId(destination.getId())
                .amount(2)
                .inputType(3)
                .inputSize(4)
                .extraData(extraData)
                .moneyFlew(true)
                .build();

        // check serialization
        Transaction saved = repository.save(transaction);
        assertThat(saved.getId()).isNotNull();

        // check deserialization
        Transaction reloaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded).isEqualTo(saved);
        assertThat(reloaded.getExtraData()).isEqualTo(extraData);
    }

}