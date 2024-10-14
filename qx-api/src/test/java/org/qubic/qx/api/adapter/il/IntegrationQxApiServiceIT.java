package org.qubic.qx.api.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.adapter.QxApiService;
import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class IntegrationQxApiServiceIT extends AbstractIntegrationApiTest {

    private static final String TEST_ID = "TESTRAIJSNPOJAKARTQNQVRROKWBKLHXIBEYMYKVIGTWYXLDKFMEAFMDRJIC";
    private static final String CFB_ISSUER = "CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL";

    @Autowired
    private QxApiService apiClient;

    @Test
    void getFees() {
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                        { "assetIssuanceFee": 1000000000, "transferFee": 1000000, "tradeFee": 5000000 }"""));

        Fees result = apiClient.getFees();
        assertThat(result).isEqualTo(new Fees(1_000_000_000, 1_000_000, 5_000_000));
        assertRequest("/v1/qx/getFees");
    }

    @Test
    void getAssetAskOrders() {
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                         {"orders":[ {"entityId":"%s","price":"3","numberOfShares":"100"} ]}""".formatted(TEST_ID)));

        List<AssetOrder> orders = apiClient.getAssetAskOrders(CFB_ISSUER, "CFB");
        assertThat(orders).containsExactly(new AssetOrder(TEST_ID, 3, 100));
        assertRequest(String.format("/v1/qx/getAssetAskOrders?issuerId=%s&assetName=CFB&offset=0", CFB_ISSUER));
    }

    @Test
    void getAssetBidOrders() {
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                         {"orders":[ {"entityId":"%s","price":"3","numberOfShares":"100"} ]}""".formatted(TEST_ID)));

        List<AssetOrder> orders = apiClient.getAssetBidOrders(CFB_ISSUER, "CFB");
        assertThat(orders).containsExactly(new AssetOrder(TEST_ID, 3, 100));
        assertRequest(String.format("/v1/qx/getAssetBidOrders?issuerId=%s&assetName=CFB&offset=0", CFB_ISSUER));
    }

    @Test
    void getEntityAskOrders() {
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                         {"orders":[
                           {"issuerId":"issuer","assetName":"asset","price":"42","numberOfShares": "666"}
                         ]}"""));

        List<EntityOrder> orders = apiClient.getEntityAskOrders(TEST_ID);
        assertThat(orders).containsExactly(new EntityOrder("issuer", "asset", 42, 666));
        assertRequest(String.format("/v1/qx/getEntityAskOrders?entityId=%s&offset=0", TEST_ID));
    }

    @Test
    void getEntityBidOrders() {
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("""
                         {"orders":[
                           {"issuerId":"issuer","assetName":"asset","price":"42","numberOfShares": "666"}
                         ]}"""));

        List<EntityOrder> orders = apiClient.getEntityBidOrders(TEST_ID);
        assertThat(orders).containsExactly(new EntityOrder("issuer", "asset", 42, 666));
        assertRequest(String.format("/v1/qx/getEntityBidOrders?entityId=%s&offset=0", TEST_ID));
    }

}