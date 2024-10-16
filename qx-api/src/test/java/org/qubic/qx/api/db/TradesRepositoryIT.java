package org.qubic.qx.api.db;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractPostgresTest;
import org.qubic.qx.api.db.domain.Asset;
import org.qubic.qx.api.db.domain.Trade;
import org.qubic.qx.api.db.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
class TradesRepositoryIT extends AbstractPostgresTest {

    @Autowired
    private AssetsRepository assetsRepository;

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private TradesRepository repository;

    @Sql(scripts = "/testdata/db/setup-trades-repository-test.sql")
    @Test
    void saveAndLoad() {
        Asset asset = assetsRepository.findAll().iterator().next();
        Transaction transaction = transactionsRepository.findAll().iterator().next();

        Trade trade = Trade.builder()
                .transactionId(transaction.getId())
                .bid(true)
                .price(1)
                .numberOfShares(2)
                .tickTime(Instant.EPOCH)
                .makerId(transaction.getSourceId()) // doesn't make sense but good enough for test
                .assetId(asset.getId())
                .build();

        Trade saved = repository.save(trade);
        assertThat(saved.getId()).isNotNull();

        Trade reloaded = repository.findById(trade.getId()).orElseThrow();
        assertThat(reloaded).isEqualTo(saved);

    }

}