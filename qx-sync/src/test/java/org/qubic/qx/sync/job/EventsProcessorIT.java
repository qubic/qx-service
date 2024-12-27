package org.qubic.qx.sync.job;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.crypto.NoCrypto;
import io.micrometer.core.instrument.util.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.domain.*;
import org.qubic.qx.sync.util.JsonUtil;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class EventsProcessorIT {

    private final IdentityUtil identityUtil = new IdentityUtil(true, new NoCrypto());
    private final EventsProcessor processor = new EventsProcessor(identityUtil);

    /*
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

        String responseJson = IOUtils.toString(Objects.requireNonNull(getClass().getResourceAsStream(
                "/testdata/il/get-tick-events-1-response.json"
        )), StandardCharsets.UTF_8);

        String transactionHash = "yakukpaxladuwfcwfndwanqjynxaihgoldglsclmkekbvdurbtddkgrcbxng";
        String sourceId = "BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC";
        String destinationId = "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARMID";
        String issuer = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB";
        QxAssetOrderData orderData = new QxAssetOrderData(issuer, "MLM", 1, 2);
        TransactionWithTime transaction = new TransactionWithTime(transactionHash,
                sourceId,
                destinationId, 1, 16585576, Instant.EPOCH.getEpochSecond(), 5, 0,
                orderData,
                null);

        List<TransactionEvents> transactionEventList = Arrays.asList(JsonUtil.fromJson(responseJson, TransactionEvents[].class));
        List<TransactionEvent> events = transactionEventList.stream().filter(te -> StringUtils.equals(te.txId(), transactionHash)).findAny().orElseThrow().events();

        List<Trade> trades = processor.calculateTrades(transaction, events, orderData);
        assertThat(trades.size()).isEqualTo(2);


        Trade trade1 = trades.getFirst();
        assertThat(trade1.transactionHash()).isEqualTo(transactionHash);
        assertThat(trade1.timestamp()).isZero(); // Instant.EPOCH
        assertThat(trade1.tick()).isEqualTo(16585576);

        assertThat(trade1.price()).isEqualTo(600_000_000);
        assertThat(trade1.numberOfShares()).isEqualTo(1);
        assertThat(trade1.taker()).isEqualTo(sourceId);
        assertThat(trade1.maker()).isEqualTo("BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC");
        assertThat(trade1.issuer()).isEqualTo(issuer);
        assertThat(trade1.assetName()).isEqualTo("MLM");

        Trade trade2 = trades.getLast();
        assertThat(trade2.transactionHash()).isEqualTo(transactionHash);
        assertThat(trade2.timestamp()).isZero(); // Instant.EPOCH
        assertThat(trade2.tick()).isEqualTo(16585576);

        assertThat(trade2.price()).isEqualTo(200_000_001); // encoded in trade event
        assertThat(trade2.numberOfShares()).isEqualTo(1); // encoded in trade event
        assertThat(trade2.taker()).isEqualTo(sourceId);
        assertThat(trade2.maker()).isEqualTo("UBGIZFTLNCRKAFJBIUUYWTOMEFEDVPTZZZBKNSZODERUXBJPWQZSPGYAMCYH");
        assertThat(trade2.issuer()).isEqualTo(issuer);
        assertThat(trade2.assetName()).isEqualTo("MLM");
    }

}