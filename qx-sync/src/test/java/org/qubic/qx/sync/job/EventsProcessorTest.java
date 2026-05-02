package org.qubic.qx.sync.job;

import at.qubic.api.domain.event.EventType;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.domain.*;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpellCheckingInspection")
class EventsProcessorTest {

    private final EventsProcessor processor = new EventsProcessor();

    @Test
    void calculateTrades() {
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 2, 3);
        Transaction transaction = new Transaction("hash", "source", "destination", 1, 42,6, 0, orderData);
        List<TransactionEvent> events = List.of(
                TransactionEvent.builder().logType(2)
                        .assetOwnershipChange(new AssetOwnershipChange("maker", "taker-not-used", "issuer-not-used", "asset-not-used", 1))
                        .rawPayload("0VGbNisG/WoCJ31lNTqScnrtWbF+ZuwLYqLQJkopJv02EYTUnxm1rKM15TYeDdxsn6lv0WHZd47t7Tzvs+MeIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAqAAAAAAAAAE1MTQAAAAAAAAAAAAAAAA==").build(),
                TransactionEvent.builder().logType(6)
                        .smartContractMessage(new SmartContractEvent(1, 0))
                        .rawPayload("AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE1MTQAAAAAAAcLrCwAAAAABAAAAAAAAAA==").build()
        );

        TransactionWithMeta transactionWithMeta = TransactionWithMeta.builder().transaction(transaction).events(events).time(Instant.EPOCH).build();
        List<Trade> trades = processor.calculateTrades(transactionWithMeta, orderData);
        assertThat(trades.size()).isOne();
        Trade trade = trades.getFirst();
        assertThat(trade.transactionHash()).isEqualTo("hash");
        assertThat(trade.timestamp()).isZero(); // epoch seconds == 0
        assertThat(trade.tick()).isEqualTo(42);
        assertThat(trade.price()).isEqualTo(200_000_001); // encoded in trade event
        assertThat(trade.numberOfShares()).isEqualTo(1); // encoded in trade event
        assertThat(trade.taker()).isEqualTo("source"); // taker is always source id of the transaction
        assertThat(trade.maker()).isEqualTo("maker");
        assertThat(trade.issuer()).isEqualTo("issuer");
        assertThat(trade.assetName()).isEqualTo("asset");
    }

    @Test
    void calculateTrades_givenBid_thenSellerIsMaker() {

        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 3, 69899); // price and number of shares is not relevant here but in qx trade event
        Transaction transaction = new Transaction("hash", "buyer", "destination", 1, 2, 6, 0, orderData); // add bid
        List<TransactionEvent> events = List.of(
                TransactionEvent.builder().logType(2)
                        .assetOwnershipChange(new AssetOwnershipChange("seller", "buyer-not-used", "issuer-not-used", "asset-not-used", 69899))
                        .rawPayload("8pqyme6pv9eYVA7tXXvID9V/RXwyvzSidxS1cI/m3EQkyu/rLunTsnDqQGiDV8IY6YIOHMO23xJHUhHjapBDQQgwu2O/fV4WSsjL04aAYw/3Zwoevzn3IQtAvNyiU9BfCxEBAAAAAAAqAAAAAAAAAENGQgAAAAAAANAA0CMYFQ==").build(),
                TransactionEvent.builder().logType(6)
                        .smartContractMessage(new SmartContractEvent(1, 0))
                        .rawPayload("AQAAAAAAAAAIMLtjv31eFkrIy9OGgGMP92cKHr859yELQLzcolPQX0NGQgAAAAAAAwAAAAAAAAALEQEAAAAAAA==").build()
        );
        TransactionWithMeta transactionWithMeta = TransactionWithMeta.builder().transaction(transaction).events(events).time(Instant.EPOCH).build();

        List<Trade> trades = processor.calculateTrades(transactionWithMeta, orderData);
        assertThat(trades.size()).isOne();
        Trade trade = trades.getFirst();
        assertThat(trade.transactionHash()).isEqualTo("hash");
        assertThat(trade.price()).isEqualTo(3);
        assertThat(trade.maker()).isEqualTo("seller");
        assertThat(trade.taker()).isEqualTo("buyer");

    }

    @Test
    void calculateTrades_givenAsk_thenBuyerIsMaker() {

        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 3, 69899); // price and shares are used from qx trade event not from here
        Transaction transaction = new Transaction("hash", "seller", "destination", 1, 2, 5, 0, orderData); // add bid

        List<TransactionEvent> events = List.of(
                // the type 0 events are irrelevant
                TransactionEvent.builder().logType(0).rawPayload("JMrv6y7p07Jw6kBog1fCGOmCDhzDtt8SR1IR42qQQ0EBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACEzAwAAAAAA").build(),
                TransactionEvent.builder().logType(0).rawPayload("AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADymrKZ7qm/15hUDu1de8gP1X9FfDK/NKJ3FLVwj+bcRAgvAwAAAAAA").build(),
                TransactionEvent.builder().logType(2)
                        .assetOwnershipChange(new AssetOwnershipChange("seller", "buyer", "issuer-not-used", "asset-not-used", 69899))
                        .rawPayload("8pqyme6pv9eYVA7tXXvID9V/RXwyvzSidxS1cI/m3EQkyu/rLunTsnDqQGiDV8IY6YIOHMO23xJHUhHjapBDQQgwu2O/fV4WSsjL04aAYw/3Zwoevzn3IQtAvNyiU9BfCxEBAAAAAAAqAAAAAAAAAENGQgAAAAAAANAA0CMYFQ==").build(),
                TransactionEvent.builder().logType(6)
                        .smartContractMessage(new SmartContractEvent(1, 0))
                        .rawPayload("AQAAAAAAAAAIMLtjv31eFkrIy9OGgGMP92cKHr859yELQLzcolPQX0NGQgAAAAAAAwAAAAAAAAALEQEAAAAAAA==").build()
        );
        TransactionWithMeta transactionWithMeta = TransactionWithMeta.builder().transaction(transaction).events(events).time(Instant.EPOCH).build();

        List<Trade> trades = processor.calculateTrades(transactionWithMeta, orderData);
        assertThat(trades.size()).isOne();
        Trade trade = trades.getFirst();
        assertThat(trade.transactionHash()).isEqualTo("hash");
        assertThat(trade.price()).isEqualTo(3);
        assertThat(trade.maker()).isEqualTo("buyer");
        assertThat(trade.taker()).isEqualTo("seller");
    }

    @Test
    void isAssetIssued_givenAssetIssuanceEvent_thenTrue() {
        List<TransactionEvent> events = List.of(
                TransactionEvent.builder().logType(EventType.ASSET_ISSUANCE.getCode()).rawPayload("foo").build()
        );
        boolean issued = processor.isAssetIssued(events);
        assertThat(issued).isTrue();
    }

    @Test
    void isAssetIssued_givenOtherEvent_thenFalse() {
        List<TransactionEvent> events = List.of(
                TransactionEvent.builder().logType(EventType.ASSET_OWNERSHIP_CHANGE.getCode()).rawPayload("foo").build()
        );
        boolean issued = processor.isAssetIssued(events);
        assertThat(issued).isFalse();
    }

    @Test
    void isAssetIssued_givenNoEvent_thenFalse() {
        boolean issued = processor.isAssetIssued(List.of());
        assertThat(issued).isFalse();
    }

    @Test
    void isAssetTransferred_givenOwnershipChange_thenTrue() {
        List<TransactionEvent> events = List.of(
                TransactionEvent.builder().logType(EventType.ASSET_OWNERSHIP_CHANGE.getCode()).rawPayload("foo").build()
        );
        boolean issued = processor.isAssetTransferred(events);
        assertThat(issued).isTrue();
    }

    @Test
    void isAssetTransferred_givenOtherEvent_thenFalse() {
        List<TransactionEvent> events = List.of(
                TransactionEvent.builder().logType(EventType.QU_TRANSFER.getCode()).rawPayload("foo").build()
        );
        boolean issued = processor.isAssetTransferred(events);
        assertThat(issued).isFalse();
    }

    @Test
    void isAssetTransferred_givenNoEvent_thenFalse() {
        boolean issued = processor.isAssetTransferred(List.of());
        assertThat(issued).isFalse();
    }

}