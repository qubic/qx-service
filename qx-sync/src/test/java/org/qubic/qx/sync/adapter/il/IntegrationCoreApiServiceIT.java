package org.qubic.qx.sync.adapter.il;

import io.micrometer.core.instrument.util.IOUtils;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.il.domain.IlTransaction;
import org.qubic.qx.sync.domain.QxAssetOrderData;
import org.qubic.qx.sync.domain.TickData;
import org.qubic.qx.sync.domain.TickTransactionsStatus;
import org.qubic.qx.sync.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationCoreApiServiceIT extends AbstractIntegrationApiTest {

    private static final String TICK_INFO_RESPONSE = """
            {
              "tick": 123,
              "durationInSeconds": 1,
              "epoch": 456,
              "numberOfAlignedVotes": 2,
              "numberOfMisalignedVotes": 3,
              "initialTickOfEpoch": 99
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
                .expectNext(123L)
                .verifyComplete();

        assertRequest("/v1/core/getTickInfo");

    }

    @Test
    void getInitialTick() {
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(TICK_INFO_RESPONSE));

        StepVerifier.create(apiService.getInitialTick())
                .expectNext(99L)
                .verifyComplete();

        assertRequest("/v1/core/getTickInfo");

    }

    @Test
    void getTickData() {
        String responseJson = """
                {"computorIndex":598,
                "epoch":129,
                "tick":16394274,
                "timestamp":"2024-10-07T11:38:33Z",
                "timeLock":"OX8cA6pGRid4/cQlOvW4bgwm9zwSnPwgxYDwwEbTQHk=",
                "transactionIds":["mmuyrzbufplgvgffxmvelkngezggcliszqlfhulqvclvzlwvfxrpnipdmeee"],
                "contractFees":[],
                "signature":"vRWNCuql1CVDu03euFY+I5/U4v3Kk5UdgXqWD1yd7wlsxRdQAj9UX9bR6lOE9BDS3PR6r1tuXmtrHFAhsR0eAA=="}""";

        prepareResponse(response -> response.setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseJson));

        StepVerifier.create(apiService.getTickData(16394274))
                .expectNext(new TickData(129, 16394274, Instant.parse("2024-10-07T11:38:33Z")))
                .verifyComplete();

        assertRequest("/v1/core/getTickData");

    }

    @Test
    void getTickQxTransactions() {
        mockQxTransactionResponses();

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
                        9)
                ,null);

        StepVerifier.create(apiService.getQxTransactions(123L).log())
                .expectNext(expected)
                .verifyComplete();

    }

    private void mockQxTransactionResponses() {
        String statusBody = IOUtils.toString(Objects.requireNonNull(getClass().getResourceAsStream(
                "/testdata/il/get-tick-transactions-status-response.json"
        )), StandardCharsets.UTF_8);

        String body = IOUtils.toString(Objects.requireNonNull(getClass().getResourceAsStream(
                "/testdata/il/get-tick-transactions-response.json")), StandardCharsets.UTF_8);

        // dispatch responses base on url as we call different urls asynchronously
        final Dispatcher dispatcher = new Dispatcher() {
            @Override
            public MockResponse dispatch (RecordedRequest request) {
                return switch (Objects.requireNonNull(request.getPath())) {
                    case "/v1/core/getTickTransactions" -> new MockResponse()
                            .setResponseCode(HttpStatus.OK.value())
                            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .setBody(body);
                    case "/v1/core/getTickTransactionsStatus"  -> new MockResponse()
                            .setResponseCode(HttpStatus.OK.value())
                            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .setBody(statusBody);
                    default -> new MockResponse().setResponseCode(404);
                };
            }
        };
        integrationLayer.setDispatcher(dispatcher);
    }

    @Test
    void getTickTransactions() {
        String body = IOUtils.toString(Objects.requireNonNull(getClass().getResourceAsStream(
                "/testdata/il/get-tick-transactions-response.json")), StandardCharsets.UTF_8);
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body));

        IlTransaction expected = new IlTransaction(
                "EYEFDKGKDIWFUFIVYNXUKJNWWLUAKGYFTXMQDPJEVFIDEOAMNMROUYNAIQDG",
                "BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARMID",
                "1",
                16206480,
                5,
                56,
                "CDC7Y799XhZKyMvThoBjD/dnCh6/OfchC0C83KJT0F9DRkIAAAAAAAMAAAAAAAAACQAAAAAAAAA=",
                "naadpiyzgqfhdcygtucgiewovdzgqhfulymzvwvblgpaivwgtdkmlggcvngi");

        StepVerifier.create(((IntegrationCoreApiService)apiService).getTickTransactions(123L))
                .assertNext(txs -> assertThat(txs.transactions()).contains(expected))
                .verifyComplete();

        assertRequest("/v1/core/getTickTransactions");
    }

    @Test
    void getTickTransactionsStatus() {

        String body = IOUtils.toString(Objects.requireNonNull(getClass().getResourceAsStream(
                "/testdata/il/get-tick-transactions-status-response.json"
        )), StandardCharsets.UTF_8);
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body));

        Map<String, Boolean> stringBooleanMap = new java.util.HashMap<>();
        stringBooleanMap.put("dlooclqhsdwsidmawyfbjuebpmsddrfrozkrbpswicrjuahmtkbtbibfomva", false);
        stringBooleanMap.put("obzmnvbtyjgnzanwjokywwgzofydfynnlqtoutzwaeacroctebcbkrobawwo", true);
        stringBooleanMap.put("test", null);
        TickTransactionsStatus expected = new TickTransactionsStatus(16510807, 2, stringBooleanMap);

        StepVerifier.create(apiService.getTickTransactionsStatus(16510807))
                .expectNext(expected)
                .verifyComplete();

        assertRequest("/v1/core/getTickTransactionsStatus");
    }

}