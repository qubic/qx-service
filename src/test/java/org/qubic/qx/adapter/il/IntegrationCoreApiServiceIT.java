package org.qubic.qx.adapter.il;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.crypto.NoCrypto;
import io.micrometer.core.instrument.util.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.qubic.qx.adapter.ExtraDataMapper;
import org.qubic.qx.adapter.il.mapping.IlTransactionMapper;
import org.qubic.qx.domain.QxAssetOrderData;
import org.qubic.qx.domain.Transaction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

class IntegrationCoreApiServiceIT extends AbstractIntegrationApiTest {

    private final WebClient webClient = createWebClient("http://localhost:1234");
    private final IlTransactionMapper transactionMapper = Mappers.getMapper(IlTransactionMapper.class);
    private final IntegrationCoreApiService apiService = new IntegrationCoreApiService(webClient, transactionMapper);
    private static final String TICK_INFO_RESPONSE = """
            {
              "tick": 123,
              "durationInSeconds": 1,
              "epoch": 456,
              "numberOfAlignedVotes": 2,
              "numberOfMisalignedVotes": 3,
              "initialTickOfEpoch": 99
            }""";

    @BeforeEach
    void initMapper() {
        IdentityUtil identityUtil = new IdentityUtil(true, new NoCrypto());
        ExtraDataMapper extraDataMapper = new ExtraDataMapper(identityUtil);
        transactionMapper.setExtraDataMapper(extraDataMapper);
    }

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
    void getQxTransactions() {
        String body = IOUtils.toString(Objects.requireNonNull(getClass().getResourceAsStream(
                "/testdata/il/get-tick-transactions-response.json")), StandardCharsets.UTF_8);
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
                        9)
                );

        StepVerifier.create(apiService.getQxTransactions(123L))
                .expectNext(expected)
                .verifyComplete();
    }

}