package org.qubic.qx.api.db;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.controller.domain.TransactionDto;
import org.qubic.qx.api.db.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/testdata/db/setup-transactions-repository-test.sql")
class TransactionsRepositoryIT extends AbstractPostgresJdbcTest {

    @Autowired
    private EntitiesRepository entitiesRepository;

    @Autowired
    private TransactionsRepository repository;

    @Test
    void saveAndLoad_givenAssetOrderData() {
        QxAssetOrderData extraData = new QxAssetOrderData("ISSUER", "ASSET", 123, 456);

        Transaction transaction = Transaction.builder()
                .tick(1)
                .hash("hash")
                .sourceId(getEntity("ID1").getId())
                .destinationId(getEntity("ID2").getId())
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

    @Test
    void saveAndLoad_givenTransferAssetData() {
        QxTransferAssetData extraData = new QxTransferAssetData("ISSUER", "ASSET", "NEW_OWNER", 123);

        Transaction transaction = Transaction.builder()
                .hash("hash")
                .sourceId(getEntity("ID1").getId())
                .destinationId(getEntity("ID2").getId())
                .extraData(extraData)
                .build();

        // check serialization
        Transaction saved = repository.save(transaction);
        assertThat(saved.getId()).isNotNull();

        // check deserialization
        Transaction reloaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded).isEqualTo(saved);
        assertThat(reloaded.getExtraData()).isEqualTo(extraData);
    }

    @Test
    void saveAndLoad_givenIssueAssetData() {
        QxIssueAssetData extraData = new QxIssueAssetData("ASSET", 12345, "00002000", (byte) 12);

        Transaction transaction = Transaction.builder()
                .hash("hash")
                .sourceId(getEntity("ID1").getId())
                .destinationId(getEntity("ID2").getId())
                .extraData(extraData)
                .build();

        // check serialization
        Transaction saved = repository.save(transaction);
        assertThat(saved.getId()).isNotNull();

        // check deserialization
        Transaction reloaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded).isEqualTo(saved);
        assertThat(reloaded.getExtraData()).isEqualTo(extraData);
    }

    @Test
    void testTransactionDtoMapping() {
        List<TransactionDto> result = repository.findOrdered(1);
        assertThat(result).isNotEmpty();
        assertThat(result).containsExactly(
                new TransactionDto("hash6",
                        "ID1", 6,
                        5, 8,
                        new QxAssetOrderData("ISSUER1", "ASSET1", 123, 456),
                        true
                )
        );
    }

    @Test
    void findOrdered() {
        List<TransactionDto> result = repository.findOrdered(5);
        assertThat(result).isNotEmpty();
        assertThat(result.stream().map(TransactionDto::hash)).containsExactly(
                "hash6", "hash5", "hash4", "hash3", "hash2"
        );
    }

    @Test
    void findByAssetOrdered() {
        List<TransactionDto> result = repository.findByAssetOrdered("ISSUER1", "ASSET1", 3);
        assertThat(result).isNotEmpty();
        assertThat(result.stream().map(TransactionDto::hash)).containsExactly(
                "hash6", "hash4", "hash3"
        );
    }

    @Test
    void findByInputTypesOrdered() {
        List<TransactionDto> result = repository.findByInputTypesOrdered(List.of(6,7,5), 10);
        assertThat(result).isNotEmpty();
        assertThat(result.stream().map(TransactionDto::hash)).containsExactly(
                "hash5", "hash4", "hash1"
        );
    }

    @Test
    void findByEntityOrdered() {
        List<TransactionDto> result = repository.findByEntityOrdered("ID1", 5);
        assertThat(result).isNotEmpty();
        assertThat(result.stream().map(TransactionDto::hash)).containsExactly(
                "hash6", "hash5", "hash4", "hash3", "hash1"
        );
    }

    private Entity getEntity(String identity) {
        return entitiesRepository.findByIdentity(identity).orElseThrow();
    }

}