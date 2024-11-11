package org.qubic.qx.sync.job;

import at.qubic.api.crypto.IdentityUtil;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.domain.*;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventsProcessorTest {

    private final IdentityUtil identityUtil = mock(IdentityUtil.class);
    private final EventsProcessor processor = new EventsProcessor(identityUtil);

    @Test
    void calculateTrades() {
        Transaction transaction = new Transaction("hash", "source", "destination", 1, 42, 6, 0, new QxAssetOrderData("issuer", "asset", 2, 3), false);

        List<TransactionEvent> events = List.of(
                new TransactionEvent(mock(), 2, 0, "0VGbNisG/WoCJ31lNTqScnrtWbF+ZuwLYqLQJkopJv02EYTUnxm1rKM15TYeDdxsn6lv0WHZd47t7Tzvs+MeIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAAAAABNTE0AAAAAAAAAAAAAAAA="),
                new TransactionEvent(mock(), 6, 0, "AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE1MTQAAAAAAAcLrCwAAAAABAAAAAAAAAA==")
        );
        TransactionEvents transactionEvents = new TransactionEvents("hash", events);

        when(identityUtil.getIdentityFromPublicKey(Base64.decode("0VGbNisG/WoCJ31lNTqScnrtWbF+ZuwLYqLQJkopJv0="))).thenReturn("maker");
        List<Trade> trades = processor.calculateTrades(42, Instant.EPOCH, List.of(transactionEvents), List.of(transaction));
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
    void inferMaker_givenBid_thenSellerIsMaker() {

        Transaction transaction = new Transaction("hash", "buyer", "destination", 1, 2, 6, 0, new QxAssetOrderData("issuer", "asset", 2, 3), false); // add bid

        List<TransactionEvent> events = List.of(
                new TransactionEvent(mock(), 2, 0, "8pqyme6pv9eYVA7tXXvID9V/RXwyvzSidxS1cI/m3EQkyu/rLunTsnDqQGiDV8IY6YIOHMO23xJHUhHjapBDQQgwu2O/fV4WSsjL04aAYw/3Zwoevzn3IQtAvNyiU9BfCxEBAAAAAABDRkIAAAAAAADQANAjGBU="),
                new TransactionEvent(mock(), 6, 0, "AQAAAAAAAAAIMLtjv31eFkrIy9OGgGMP92cKHr859yELQLzcolPQX0NGQgAAAAAAAwAAAAAAAAALEQEAAAAAAA==")
        );
        TransactionEvents transactionEvents = new TransactionEvents("hash", events);
        when(identityUtil.getIdentityFromPublicKey(Base64.decode("8pqyme6pv9eYVA7tXXvID9V/RXwyvzSidxS1cI/m3EQ="))).thenReturn("seller");

        List<Trade> trades = processor.calculateTrades(42, Instant.EPOCH, List.of(transactionEvents), List.of(transaction));
        assertThat(trades.size()).isOne();
        Trade trade = trades.getFirst();
        assertThat(trade.transactionHash()).isEqualTo("hash");
        assertThat(trade.price()).isEqualTo(3);
        assertThat(trade.maker()).isEqualTo("seller");
        assertThat(trade.taker()).isEqualTo("buyer");

    }

    @Test
    void inferMaker_givenAsk_thenBuyerIsMaker() {

        Transaction transaction = new Transaction("hash", "seller", "destination", 1, 2, 5, 0, new QxAssetOrderData("issuer", "asset", 2, 3), false); // add bid

        List<TransactionEvent> events = List.of(
                new TransactionEvent(mock(), 0, 0, "JMrv6y7p07Jw6kBog1fCGOmCDhzDtt8SR1IR42qQQ0EBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACEzAwAAAAAA"),
                new TransactionEvent(mock(), 0, 0, "AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADymrKZ7qm/15hUDu1de8gP1X9FfDK/NKJ3FLVwj+bcRAgvAwAAAAAA"),
                new TransactionEvent(mock(), 2, 0, "8pqyme6pv9eYVA7tXXvID9V/RXwyvzSidxS1cI/m3EQkyu/rLunTsnDqQGiDV8IY6YIOHMO23xJHUhHjapBDQQgwu2O/fV4WSsjL04aAYw/3Zwoevzn3IQtAvNyiU9BfCxEBAAAAAABDRkIAAAAAAADQANAjGBU="),
                new TransactionEvent(mock(), 6, 0, "AQAAAAAAAAAIMLtjv31eFkrIy9OGgGMP92cKHr859yELQLzcolPQX0NGQgAAAAAAAwAAAAAAAAALEQEAAAAAAA==")
        );
        TransactionEvents transactionEvents = new TransactionEvents("hash", events);
        when(identityUtil.getIdentityFromPublicKey(Base64.decode("JMrv6y7p07Jw6kBog1fCGOmCDhzDtt8SR1IR42qQQ0E="))).thenReturn("buyer");

        List<Trade> trades = processor.calculateTrades(42, Instant.EPOCH, List.of(transactionEvents), List.of(transaction));
        assertThat(trades.size()).isOne();
        Trade trade = trades.getFirst();
        assertThat(trade.transactionHash()).isEqualTo("hash");
        assertThat(trade.price()).isEqualTo(3);
        assertThat(trade.maker()).isEqualTo("buyer");
        assertThat(trade.taker()).isEqualTo("seller");

    }

}