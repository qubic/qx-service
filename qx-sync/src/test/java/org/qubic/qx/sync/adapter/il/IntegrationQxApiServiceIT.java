package org.qubic.qx.sync.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.QxApiService;
import org.qubic.qx.sync.domain.AssetOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import reactor.test.StepVerifier;

import java.util.List;

@Slf4j
class IntegrationQxApiServiceIT extends AbstractIntegrationApiTest {

    private static final String TEST_ID = "TESTRAIJSNPOJAKARTQNQVRROKWBKLHXIBEYMYKVIGTWYXLDKFMEAFMDRJIC";
    private static final String CFB_ISSUER = "CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL";

    @Autowired
    private QxApiService apiClient;

    @Test
    void getAssetAskOrders() {
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                         {"orders":[ {"entityId":"%s","price":"3","numberOfShares":"100"} ]}""".formatted(TEST_ID)));

        StepVerifier.create(apiClient.getAssetAskOrders(CFB_ISSUER, "CFB")
                        .doOnNext(l -> log.info("{}", l)))
                .expectNext(List.of(new AssetOrder(TEST_ID, 3, 100)))
                .verifyComplete();

        assertRequest(String.format("/v1/qx/getAssetAskOrders?issuerId=%s&assetName=CFB&offset=0", CFB_ISSUER));
    }

    @Test
    void getAssetBidOrders() {
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                         {"orders":[ {"entityId":"%s","price":"3","numberOfShares":"100"} ]}""".formatted(TEST_ID)));

        StepVerifier.create(apiClient.getAssetBidOrders(CFB_ISSUER, "CFB")
                        .doOnNext(l -> log.info("{}", l)))
                .expectNext(List.of(new AssetOrder(TEST_ID, 3, 100)))
                .verifyComplete();

        assertRequest(String.format("/v1/qx/getAssetBidOrders?issuerId=%s&assetName=CFB&offset=0", CFB_ISSUER));
    }

}