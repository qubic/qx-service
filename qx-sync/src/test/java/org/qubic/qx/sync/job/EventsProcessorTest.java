package org.qubic.qx.sync.job;

import at.qubic.api.crypto.IdentityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.domain.*;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventsProcessorTest {

    private final IdentityUtil identityUtil = mock(IdentityUtil.class);
    private final EventsProcessor processor = new EventsProcessor(identityUtil);

    @BeforeEach
    void initMocks() {
        when(identityUtil.getIdentityFromPublicKey(any())).thenReturn("maker");
    }

    @Test
    void calculateTrades() {
        Transaction transaction = new Transaction("hash", "source", "destination", 1, 42, 6, 0, new QxAssetOrderData("issuer", "asset", 2, 3), false);

        List<TransactionEvent> events = List.of(
                new TransactionEvent(mock(), 0, 0, "jo0AwIVOmnDHyw6JCcNWOuWlngm9zxDGrtcyA3WjioYBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEBCDwAAAAA"),
                new TransactionEvent(mock(), 2, 0, "0VGbNisG/WoCJ31lNTqScnrtWbF+ZuwLYqLQJkopJv02EYTUnxm1rKM15TYeDdxsn6lv0WHZd47t7Tzvs+MeIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAAAAABNTE0AAAAAAAAAAAAAAAA="),
                new TransactionEvent(mock(), 6, 0, "AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE1MTQAAAAAAAcLrCwAAAAABAAAAAAAAAA==")
        );
        TransactionEvents transactionEvents = new TransactionEvents("hash", events);

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
}