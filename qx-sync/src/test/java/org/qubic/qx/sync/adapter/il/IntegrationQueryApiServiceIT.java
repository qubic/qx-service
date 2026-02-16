package org.qubic.qx.sync.adapter.il;

import io.micrometer.core.instrument.util.IOUtils;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.domain.QxAssetOrderData;
import org.qubic.qx.sync.domain.TickData;
import org.qubic.qx.sync.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

class IntegrationQueryApiServiceIT extends AbstractIntegrationApiTest {

    private static final String TICK_INFO_RESPONSE = """
            {
               "tickNumber": 300,
               "epoch": 200,
               "intervalInitialTick": 100
             }""";

    @Autowired
    private CoreApiService apiService;

    @Test
    void getCurrentTick() {
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(TICK_INFO_RESPONSE));

        StepVerifier.create(apiService.getCurrentTick())
                .expectNext(300L)
                .verifyComplete();

        assertRequest("/query/v1/getLastProcessedTick");
    }

    @Test
    void getTickData() {
        String responseJson = """
                {
                 "tickData": {
                   "tickNumber": 44191622,
                   "epoch": 200,
                   "computorIndex": 150,
                   "timestamp": "1771268260000",
                   "varStruct": "",
                   "timeLock": "Nn5kWGoGuBRevP4sBCT1AXRHVlPkKnSA0o8tPh8uQT0=",
                   "transactionHashes": [
                     "yybxrzmpqjuzydkhdetotswkbvoesnodcknlsonyzbblbixgswjethlhpbmo",
                     "jwrnaatlqedskhqbnzvmzmrwrtpgziczwdkzdjpoxcecebtfwxblrncdwoun",
                     "fwbeecrhkrhbrdmcdzredrohdwldkssiwfgpjsmcbcmfsvbnpxzfjmpgsceb",
                     "sfxtmbnypggbjgmcjrhslpvvkmeboccbvxxhzeytkhvknvnxxavdnfeesnco",
                     "rbndbtfdomuxtgcjvpuabvortkhbqebiugrkcohtvfxprguvluvxuizadirm"
                   ],
                   "contractFees": [],
                   "signature": "9KnMmDfPVG0iI5LK6V06aeIrjQznQiIHWAN5Q5XBgWPIVPEYHyXtwQ+eR6LQkchs/m6KoXBtq2zE3hU7I5AEAA=="
                 }
               }""";

        prepareResponse(response -> response.setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseJson));

        StepVerifier.create(apiService.getTickData(44191622))
                .expectNext(new TickData(200, 44191622, Instant.parse("2026-02-16T18:57:40Z")))
                .verifyComplete();

        assertRequest("/query/v1/getTickData");
    }

    @Test
    void getTickQxTransactions() {
        String body = IOUtils.toString(Objects.requireNonNull(getClass().getResourceAsStream(
                "/testdata/il/query/get-tick-transactions-response.json")), StandardCharsets.UTF_8);

        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body));

        Transaction expected = new Transaction("naadpiyzgqfhdcygtucgiewovdzgqhfulymzvwvblgpaivwgtdkmlggcvngi",
                "EYEFDKGKDIWFUFIVYNXUKJNWWLUAKGYFTXMQDPJEVFIDEOAMNMROUYNAIQDG",
                "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARMID",
                1,
                16206480,
                5,
                56,
                new QxAssetOrderData("CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL",
                        "CFB",
                        3,
                        9));

        StepVerifier.create(apiService.getQxTransactions(123L).log())
                .expectNext(expected)
                .verifyComplete();

        assertRequest("/query/v1/getTransactionsForTick");

    }

    @Test
    void getTickTransactions() {
        String body = IOUtils.toString(Objects.requireNonNull(getClass().getResourceAsStream(
                "/testdata/il/query/get-tick-transactions-response.json")), StandardCharsets.UTF_8);
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body));

        StepVerifier.create(((IntegrationQueryApiService)apiService).getTickTransactions(123L))
                .expectNextCount(2)
                .verifyComplete();

        assertRequest("/query/v1/getTransactionsForTick");
    }

}