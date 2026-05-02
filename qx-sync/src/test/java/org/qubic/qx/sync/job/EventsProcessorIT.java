package org.qubic.qx.sync.job;

import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.domain.*;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventsProcessorIT {

    private final EventsProcessor processor = new EventsProcessor();

    /*

        Real life trade with the following event data (qu transfers are irrelevant in our calculation):

        base64 event data: AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADRUZs2Kwb9agInfWU1OpJyeu1ZsX5m7AtiotAmSikm/T9/lSMAAAAA
        Qu transfer: from [BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARMID] to [BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC]: [596999999]

        base64 event data: 0VGbNisG/WoCJ31lNTqScnrtWbF+ZuwLYqLQJkopJv3RUZs2Kwb9agInfWU1OpJyeu1ZsX5m7AtiotAmSikm/QAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAAAAABNTE0AAAAAAAAAAAAAAAA=
        Asset ownership change: [AAAAAAAAAA]/[MLM] from [BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC] to [BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC]. Number of shares: [1]

        base64 event data: 0VGbNisG/WoCJ31lNTqScnrtWbF+ZuwLYqLQJkopJv3RUZs2Kwb9agInfWU1OpJyeu1ZsX5m7AtiotAmSikm/QAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAAAAABNTE0AAAAAAAAAAAAAAAA=
        Asset possession change: [AAAAAAAAAA]/[MLM] from [BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC] to [BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC]. Number of shares: [1]

        base64 event data: AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE1MTQAAAAAAAEbDIwAAAAABAAAAAAAAAA==
        Qx trade: Asset [AAAAAAAAAA]/[MLM], price: [600000000], shares [1].

        base64 event data: AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADRUZs2Kwb9agInfWU1OpJyeu1ZsX5m7AtiotAmSikm/cB/3AsAAAAA
        Qu transfer: from [BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARMID] to [BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC]: [199000000]

        base64 event data: 0VGbNisG/WoCJ31lNTqScnrtWbF+ZuwLYqLQJkopJv02EYTUnxm1rKM15TYeDdxsn6lv0WHZd47t7Tzvs+MeIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAAAAABNTE0AAAAAAAAAAAAAAAA=
        Asset ownership change: [AAAAAAAAAA]/[MLM] from [BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC] to [UBGIZFTLNCRKAFJBIUUYWTOMEFEDVPTZZZBKNSZODERUXBJPWQZSPGYAMCYH]. Number of shares: [1]

        base64 event data: 0VGbNisG/WoCJ31lNTqScnrtWbF+ZuwLYqLQJkopJv02EYTUnxm1rKM15TYeDdxsn6lv0WHZd47t7Tzvs+MeIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQAAAAAAAABNTE0AAAAAAAAAAAAAAAA=
        Asset possession change: [AAAAAAAAAA]/[MLM] from [BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC] to [UBGIZFTLNCRKAFJBIUUYWTOMEFEDVPTZZZBKNSZODERUXBJPWQZSPGYAMCYH]. Number of shares: [1]

        base64 event data: AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE1MTQAAAAAAAcLrCwAAAAABAAAAAAAAAA==
        Qx trade: Asset [AAAAAAAAAA]/[MLM], price: [200000001], shares [1].
     */
    @Test
    void calculateTrades() {

        String transactionHash = "yakukpaxladuwfcwfndwanqjynxaihgoldglsclmkekbvdurbtddkgrcbxng";
        String seller = "BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC";
        String destinationId = "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARMID";
        String issuer = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB";
        String assetName = "MLM";
        QxAssetOrderData orderData = new QxAssetOrderData(issuer, assetName, 1, 2);
        Transaction tx = new Transaction(transactionHash,
                seller,
                destinationId, 1, 16585576,5, 0,
                orderData
        );

        String buyer1 = "BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC";
        String buyer2 = "UBGIZFTLNCRKAFJBIUUYWTOMEFEDVPTZZZBKNSZODERUXBJPWQZSPGYAMCYH";

        List<TransactionEvent> events = List.of(
                TransactionEvent.builder()
                        .transactionHash(transactionHash)
                        .logType(2)
                        .assetOwnershipChange(new AssetOwnershipChange(seller, buyer1, issuer, assetName, 1))
                        .build(),

                TransactionEvent.builder()
                        .transactionHash(transactionHash)
                        .logType(6)
                        .rawPayload("AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE1MTQAAAAAAAEbDIwAAAAABAAAAAAAAAA==")
                        .smartContractMessage(new SmartContractEvent(1, 0))
                        .build(),

                TransactionEvent.builder()
                        .transactionHash(transactionHash)
                        .logType(2)
                        .assetOwnershipChange(new AssetOwnershipChange(seller, buyer2, issuer, assetName, 1))
                        .build(),

                TransactionEvent.builder()
                        .transactionHash(transactionHash)
                        .logType(6)
                        .rawPayload("AQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAE1MTQAAAAAAAcLrCwAAAAABAAAAAAAAAA==")
                        .smartContractMessage(new SmartContractEvent(1, 0))
                        .build()
        );
        TransactionWithMeta transaction = TransactionWithMeta.builder().transaction(tx).events(events).time(Instant.EPOCH).build();

        List<Trade> trades = processor.calculateTrades(transaction, orderData);
        assertThat(trades.size()).isEqualTo(2);


        Trade trade1 = trades.getFirst();
        assertThat(trade1.transactionHash()).isEqualTo(transactionHash);
        assertThat(trade1.timestamp()).isZero(); // Instant.EPOCH
        assertThat(trade1.tick()).isEqualTo(16585576);

        assertThat(trade1.price()).isEqualTo(600_000_000);
        assertThat(trade1.numberOfShares()).isEqualTo(1);
        assertThat(trade1.taker()).isEqualTo(seller);
        assertThat(trade1.maker()).isEqualTo(buyer1);
        assertThat(trade1.issuer()).isEqualTo(issuer);
        assertThat(trade1.assetName()).isEqualTo("MLM");

        Trade trade2 = trades.getLast();
        assertThat(trade2.transactionHash()).isEqualTo(transactionHash);
        assertThat(trade2.timestamp()).isZero(); // Instant.EPOCH
        assertThat(trade2.tick()).isEqualTo(16585576);

        assertThat(trade2.price()).isEqualTo(200_000_001); // encoded in trade event
        assertThat(trade2.numberOfShares()).isEqualTo(1); // encoded in trade event
        assertThat(trade2.taker()).isEqualTo(seller);
        assertThat(trade2.maker()).isEqualTo(buyer2);
        assertThat(trade2.issuer()).isEqualTo(issuer);
        assertThat(trade2.assetName()).isEqualTo("MLM");
    }

}