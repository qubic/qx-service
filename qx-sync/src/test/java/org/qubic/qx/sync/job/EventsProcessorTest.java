package org.qubic.qx.sync.job;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.domain.event.EventType;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.domain.*;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("SpellCheckingInspection")
class EventsProcessorTest {

    private final IdentityUtil identityUtil = mock(IdentityUtil.class);
    private final EventsProcessor processor = new EventsProcessor(identityUtil);

    @Test
    void calculateTrades() {
        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 2, 3);
        Transaction transaction = new Transaction("hash", "source", "destination", 1, 42,6, 0, orderData);
        List<TransactionEvent> events = List.of(
                new TransactionEvent(mock(), 2, 0, "0VGbNisG/WoCJ31lNTqScnrtWbF+ZuwLYqLQJkopJv02EYTUnxm1rKM15TYeDdxsn6lv0WHZd47t7Tzvs+MeIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAqAAAAAAAAAE1MTQAAAAAAAAAAAAAAAA=="),
                new TransactionEvent(mock(), 6, 0, "AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE1MTQAAAAAAAcLrCwAAAAABAAAAAAAAAA==")
        );
        when(identityUtil.getIdentityFromPublicKey(Base64.decode("0VGbNisG/WoCJ31lNTqScnrtWbF+ZuwLYqLQJkopJv0="))).thenReturn("maker");

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

        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 2, 3);
        Transaction transaction = new Transaction("hash", "buyer", "destination", 1, 2, 6, 0, orderData); // add bid
        List<TransactionEvent> events = List.of(
                new TransactionEvent(mock(), 2, 0, "8pqyme6pv9eYVA7tXXvID9V/RXwyvzSidxS1cI/m3EQkyu/rLunTsnDqQGiDV8IY6YIOHMO23xJHUhHjapBDQQgwu2O/fV4WSsjL04aAYw/3Zwoevzn3IQtAvNyiU9BfCxEBAAAAAAAqAAAAAAAAAENGQgAAAAAAANAA0CMYFQ=="),
                new TransactionEvent(mock(), 6, 0, "AQAAAAAAAAAIMLtjv31eFkrIy9OGgGMP92cKHr859yELQLzcolPQX0NGQgAAAAAAAwAAAAAAAAALEQEAAAAAAA==")
        );
        when(identityUtil.getIdentityFromPublicKey(Base64.decode("8pqyme6pv9eYVA7tXXvID9V/RXwyvzSidxS1cI/m3EQ="))).thenReturn("seller");
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

        QxAssetOrderData orderData = new QxAssetOrderData("issuer", "asset", 2, 3);
        Transaction transaction = new Transaction("hash", "seller", "destination", 1, 2, 5, 0, orderData); // add bid

        List<TransactionEvent> events = List.of(
                new TransactionEvent(mock(), 0, 0, "JMrv6y7p07Jw6kBog1fCGOmCDhzDtt8SR1IR42qQQ0EBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACEzAwAAAAAA"),
                new TransactionEvent(mock(), 0, 0, "AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADymrKZ7qm/15hUDu1de8gP1X9FfDK/NKJ3FLVwj+bcRAgvAwAAAAAA"),
                new TransactionEvent(mock(), 2, 0, "8pqyme6pv9eYVA7tXXvID9V/RXwyvzSidxS1cI/m3EQkyu/rLunTsnDqQGiDV8IY6YIOHMO23xJHUhHjapBDQQgwu2O/fV4WSsjL04aAYw/3Zwoevzn3IQtAvNyiU9BfCxEBAAAAAAAqAAAAAAAAAENGQgAAAAAAANAA0CMYFQ=="),
                new TransactionEvent(mock(), 6, 0, "AQAAAAAAAAAIMLtjv31eFkrIy9OGgGMP92cKHr859yELQLzcolPQX0NGQgAAAAAAAwAAAAAAAAALEQEAAAAAAA==")
        );
        when(identityUtil.getIdentityFromPublicKey(Base64.decode("JMrv6y7p07Jw6kBog1fCGOmCDhzDtt8SR1IR42qQQ0E="))).thenReturn("buyer");
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
                new TransactionEvent(mock(), EventType.ASSET_ISSUANCE.getCode(), 123, "foo")
        );
        boolean issued = processor.isAssetIssued(events);
        assertThat(issued).isTrue();
    }

    @Test
    void isAssetIssued_givenOtherEvent_thenFalse() {
        List<TransactionEvent> events = List.of(
                new TransactionEvent(mock(), EventType.ASSET_OWNERSHIP_CHANGE.getCode(), 123, "foo")
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
                new TransactionEvent(mock(), EventType.ASSET_OWNERSHIP_CHANGE.getCode(), 123, "foo")
        );
        boolean issued = processor.isAssetTransferred(events);
        assertThat(issued).isTrue();
    }

    @Test
    void isAssetTransferred_givenOtherEvent_thenFalse() {
        List<TransactionEvent> events = List.of(
                new TransactionEvent(mock(), EventType.QU_TRANSFER.getCode(), 123, "foo")
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