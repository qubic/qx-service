package org.qubic.qx.api.db;

import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.domain.Asset;
import org.qubic.qx.api.db.domain.Entity;
import org.qubic.qx.api.db.domain.Trade;
import org.qubic.qx.api.db.domain.Transaction;
import org.qubic.qx.api.db.dto.AvgPriceData;
import org.qubic.qx.api.db.dto.TradeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/testdata/db/setup-trades-repository-test.sql")
class TradesRepositoryIT extends AbstractPostgresJdbcTest {

    @Autowired
    private AssetsRepository assetsRepository;

    @Autowired
    private TransactionsRepository transactionsRepository;

    @Autowired
    private EntitiesRepository entitiesRepository;

    @Autowired
    private TradesRepository repository;

    @Test
    void saveAndLoad() {

        Asset asset = assetsRepository.findAll().getFirst();
        Transaction tx = transactionsRepository.findAll().iterator().next();

        Trade trade = Trade.builder()
                .transactionId(tx.getId())
                .bid(true)
                .price(1)
                .numberOfShares(2)
                .tickTime(Instant.EPOCH)
                .makerId(tx.getSourceId()) // doesn't make sense but good enough for test
                .assetId(asset.getId())
                .build();

        Trade saved = repository.save(trade);
        assertThat(saved.getId()).isNotNull();

        Trade reloaded = repository.findById(trade.getId()).orElseThrow();
        assertThat(reloaded).isEqualTo(saved);

    }

    @Test
    void findAllOrderedByTickTime() {
        Iterator<Entity> entityIterator = entitiesRepository.findAll().iterator();
        Entity taker = entityIterator.next();
        Entity maker = entityIterator.next();
        Asset asset = assetsRepository.findAll().getFirst();
        Transaction tx = transactionsRepository.findAll().iterator().next();
        assertThat(tx.getSourceId()).isEqualTo(taker.getId());

        Trade trade1 = repository.save(trade(tx, asset, maker, nowPlusSeconds(100)));
        Trade trade2 = repository.save(trade(tx, asset, maker, nowPlusSeconds(-100))); // outside limit
        Trade trade3 = repository.save(trade(tx, asset, maker, nowPlusSeconds(0)));

        assertThat(repository.findAll(0, 2))
                .containsExactly(tradeDto(asset, taker, maker, trade1), tradeDto(asset, taker, maker, trade3));

        assertThat(repository.findAll(1, 2))
                .containsExactly(tradeDto(asset, taker, maker, trade3), tradeDto(asset, taker, maker, trade2));

        assertThat(repository.findAll(1, 1))
                .containsExactly(tradeDto(asset, taker, maker, trade3));
    }

    @Test
    void findByIssuer() {
        Entity entity = entitiesRepository.findAll().iterator().next();
        Transaction tx = transactionsRepository.findAll().iterator().next();
        Iterator<Asset> assetIterator = assetsRepository.findAll().iterator();
        Asset asset1 = assetIterator.next();
        Asset asset2 = findAssetWithIssuerIsNot(assetIterator, asset1.getIssuer());

        Trade trade1 = repository.save(trade(tx, asset1, entity, nowPlusSeconds(-1)));
        Trade trade2 = repository.save(trade(tx, asset2, entity, nowPlusSeconds(-2)));
        Trade trade3 = repository.save(trade(tx, asset1, entity, nowPlusSeconds(-3)));
        Trade trade4 = repository.save(trade(tx, asset2, entity, nowPlusSeconds(-4)));

        assertThat(repository.findByIssuer(asset1.getIssuer(), 0, 10))
                .containsExactly(
                        tradeDto(asset1, entity, entity, trade1),
                        tradeDto(asset1, entity, entity, trade3)
                );

        assertThat(repository.findByIssuer(asset2.getIssuer(), 0, 10))
                .containsExactly(
                        tradeDto(asset2, entity, entity, trade2),
                        tradeDto(asset2, entity, entity, trade4)
                );
    }

    @Test
    void findByIssuerIsNot() {
        Entity entity = entitiesRepository.findAll().iterator().next();
        Transaction tx = transactionsRepository.findAll().iterator().next();
        Iterator<Asset> assetIterator = assetsRepository.findAll().iterator();
        Asset asset1 = assetIterator.next();
        Asset asset2 = findAssetWithIssuerIsNot(assetIterator, asset1.getIssuer());

        Trade trade1 = repository.save(trade(tx, asset1, entity, nowPlusSeconds(-1)));
        Trade trade2 = repository.save(trade(tx, asset2, entity, nowPlusSeconds(-2)));
        Trade trade3 = repository.save(trade(tx, asset1, entity, nowPlusSeconds(-3)));
        Trade trade4 = repository.save(trade(tx, asset2, entity, nowPlusSeconds(-4)));

        assertThat(repository.findByIssuerIsNot(asset2.getIssuer(), 0, 10))
                .containsExactly(
                        tradeDto(asset1, entity, entity, trade1),
                        tradeDto(asset1, entity, entity, trade3)
                );

        assertThat(repository.findByIssuerIsNot(asset1.getIssuer(), 0, 10))
                .containsExactly(
                        tradeDto(asset2, entity, entity, trade2),
                        tradeDto(asset2, entity, entity, trade4)
                );
    }


    @Test
    void findByIssuerAndAssetOrderedByTickTime() {
        Entity entity = entitiesRepository.findAll().iterator().next();
        Iterator<Asset> assetIterator = assetsRepository.findAll().iterator();
        Asset asset1 = assetIterator.next();
        Asset asset2 = assetIterator.next();
        Transaction tx = transactionsRepository.findAll().iterator().next();

        Trade trade1 = repository.save(trade(tx, asset1, entity, nowPlusSeconds(100)));
        Trade trade2 = repository.save(trade(tx, asset1, entity, nowPlusSeconds(-100)));
        Trade trade3 = repository.save(trade(tx, asset1, entity, nowPlusSeconds(-101))); // outside limit
        repository.save(trade(tx, asset2, entity, nowPlusSeconds(0))); // another asset

        assertThat(repository.findByIssuerAndAsset(asset1.getIssuer(), asset1.getName(), 0, 2))
                .containsExactly(
                        tradeDto(asset1, entity, entity, trade1),
                        tradeDto(asset1, entity, entity, trade2)
                );

        assertThat(repository.findByIssuerAndAsset(asset1.getIssuer(), asset1.getName(), 1, 2))
                .containsExactly(
                        tradeDto(asset1, entity, entity, trade2),
                        tradeDto(asset1, entity, entity, trade3)
                );

        assertThat(repository.findByIssuerAndAsset(asset1.getIssuer(), asset1.getName(), 1, 1))
                .containsExactly(
                        tradeDto(asset1, entity, entity, trade2)
                );
    }


    @Test
    void findByEntityOrderedByTickTimeDesc() {
        Iterator<Entity> entityIterator = entitiesRepository.findAll().iterator();
        Entity entity1 = entityIterator.next();
        Entity entity2 = entityIterator.next();
        Asset asset = assetsRepository.findAll().getFirst();
        Transaction tx1 = transactionsRepository.findAll().iterator().next();
        Transaction tx2 = transactionsRepository.save(Transaction.builder()
                        .hash("hash2")
                        .sourceId(entity2.getId())
                        .destinationId(entity1.getId()) // not relevant
                        .extraData(tx1.getExtraData())
                .build());
        assertThat(tx1.getSourceId()).isEqualTo(entity1.getId());

        Trade trade1 = repository.save(trade(tx1, asset, entity1, nowPlusSeconds(100))); // only entity 1
        Trade trade2 = repository.save(trade(tx1, asset, entity1, nowPlusSeconds(-100))); // only entity 1 // out of limit
        Trade trade3 = repository.save(trade(tx1, asset, entity2, nowPlusSeconds(0))); // entity 1 and 2
        Trade trade4 = repository.save(trade(tx2, asset, entity1, nowPlusSeconds(-1000))); // only entity 2

        assertThat(repository.findByEntity(entity2.getIdentity(), 0, 3))
                .containsExactly(
                        tradeDto(asset, entity1, entity2, trade3),
                        tradeDto(tx2.getHash(), asset, entity2, entity1, trade4)
                );

        assertThat(repository.findByEntity(entity1.getIdentity(), 0, 2))
                .containsExactly(
                        tradeDto(asset, entity1, entity1, trade1),
                        tradeDto(asset, entity1, entity2, trade3)
                );

        assertThat(repository.findByEntity(entity1.getIdentity(), 1, 2))
                .containsExactly(
                        tradeDto(asset, entity1, entity2, trade3),
                        tradeDto(asset, entity1, entity1, trade2)
                        );

        assertThat(repository.findByEntity(entity1.getIdentity(), 1, 1))
                .containsExactly(
                        tradeDto(asset, entity1, entity2, trade3)
                );
    }

    @Test
    void findAveragePriceByAssetGroupedByDay() {
        Asset asset = assetsRepository.findAll().getFirst();
        Transaction tx = transactionsRepository.findAll().iterator().next();
        Instant now = Instant.now();

        repository.save(trade(tx, asset, 100, 1, now.minus(Duration.ofDays(7))));

        repository.save(trade(tx, asset, 1, 4, now.minus(Duration.ofDays(6))));
        repository.save(trade(tx, asset, 2, 3, now.minus(Duration.ofDays(6))));
        repository.save(trade(tx, asset, 3, 1, now.minus(Duration.ofDays(6))));

        repository.save(trade(tx, asset, 4, 2, now.minus(Duration.ofDays(4))));
        repository.save(trade(tx, asset, 8, 1, now.minus(Duration.ofDays(4))));

        repository.save(trade(tx, asset, 16, 2, now.minus(Duration.ofDays(1))));

        repository.save(trade(tx, asset, 32, 3, now));

        List<AvgPriceData> priceData = repository.findAveragePriceByAssetGroupedByDay(asset.getIssuer(), asset.getName(), now.minus(Duration.ofDays(6)));
        assertThat(priceData.size()).isEqualTo(4);

        ZoneId UTC = ZoneId.of("UTC");
        assertThat(priceData).containsExactly(
                new AvgPriceData(LocalDate.ofInstant(now.minus(Duration.ofDays(6)), UTC), 1, 3, 8, 13, (double) 13 / 8, 3),
                new AvgPriceData(LocalDate.ofInstant(now.minus(Duration.ofDays(4)), UTC), 4, 8, 3, 16, (double) 16 / 3, 2),
                new AvgPriceData(LocalDate.ofInstant(now.minus(Duration.ofDays(1)), UTC), 16, 16, 2, 32, 16, 1),
                new AvgPriceData(LocalDate.ofInstant(now, UTC), 32, 32, 3, 96, 32, 1)
        );

    }

    private static Asset findAssetWithIssuerIsNot(Iterator<Asset> assetIterator, String issuer) {
        while (assetIterator.hasNext()) {
            Asset nextAsset = assetIterator.next();
            if (!Strings.CS.equals(nextAsset.getIssuer(), issuer)) { // find asset with different issuer
                return nextAsset;
            }
        }
        throw new IllegalStateException("No asset found with issuer different to " + issuer);
    }

    private Instant nowPlusSeconds(long seconds) {
        return Instant.now().plusSeconds(seconds).truncatedTo(ChronoUnit.MILLIS);
    }

    private static TradeDto tradeDto(Asset asset, Entity taker, Entity maker, Trade trade) {
        return tradeDto("hash", asset, taker, maker, trade);
    }

    private static TradeDto tradeDto(String hash, Asset asset, Entity taker, Entity maker, Trade trade) {
        return new TradeDto(
                trade.getTickTime().truncatedTo(ChronoUnit.MILLIS),
                hash,
                taker.getIdentity(),
                maker.getIdentity(),
                asset.getIssuer(),
                asset.getName(),
                trade.isBid(),
                trade.getPrice(),
                trade.getNumberOfShares()
        );
    }

    private static Trade trade(Transaction transaction, Asset asset, long price, long shares, Instant tickTime) {
        return trade(transaction.getId(), asset.getId(), transaction.getSourceId(), price, shares, tickTime);
    }


    private static Trade trade(Transaction transaction, Asset asset, Entity maker, Instant tickTime) {
        return trade(transaction.getId(), asset.getId(), maker.getId(), 1, 2, tickTime);
    }

    private static Trade trade(long transactionId, long assetId, long makerId, long price, long shares, Instant tickTime) {
        return Trade.builder()
                .transactionId(transactionId)
                .bid(true)
                .price(price)
                .numberOfShares(shares)
                .tickTime(tickTime)
                .makerId(makerId) // doesn't make sense but good enough for test
                .assetId(assetId)
                .build();
    }

}