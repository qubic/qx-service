package org.qubic.qx.api.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.adapter.QxApiService;
import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.qubic.qx.api.uitl.FileUtil.readFile;

@Slf4j
class IntegrationQxApiServiceIT extends AbstractSpringIntegrationTest {

    private static final String CFB_ISSUER = "CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL";

    @Autowired
    private QxApiService apiClient;

    @Test
    void getFees() {
        String responseJson = """
                { "responseData": "AMqaO2QAAADAxi0A" }""";

        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseJson));

        Fees result = apiClient.getFees();
        assertThat(result).isEqualTo(new Fees(1_000_000_000, 100, 3_000_000));
        assertRequest("/live/v1/querySmartContract");
    }

    @Test
    void getAssetAskOrders() throws IOException {
        String json = readFile("/testdata/db/responses/get-asset-ask-orders-response.json");
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(json));

        List<AssetOrder> orders = apiClient.getAssetAskOrders(CFB_ISSUER, "CFB");
        assertThat(orders).contains(new AssetOrder("BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC", 3, 424634414));
        assertRequest("/live/v1/querySmartContract");
    }

    @Test
    void getAssetBidOrders() throws IOException {
        String json = readFile("/testdata/db/responses/get-asset-bid-orders-response.json");
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(json));

        List<AssetOrder> orders = apiClient.getAssetBidOrders(CFB_ISSUER, "CFB");
        assertThat(orders).containsExactly(new AssetOrder("JHCGKVNUKTBVJGGZTCDBPVSYIOIDFUPCQOVCQFNMEGYHVWMINTEJITLGTXGO", 1, 23116901));
        assertRequest("/live/v1/querySmartContract");
    }

    @Test
    void getEntityAskOrders() throws IOException {
        String json = readFile("/testdata/db/responses/get-entity-ask-orders-response.json");
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(json));

        List<EntityOrder> orders = apiClient.getEntityAskOrders("BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC");
        assertThat(orders).contains(new EntityOrder("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB", "MLM", 4_500_000_000L, 1));
        assertRequest("/live/v1/querySmartContract");
    }

    @Test
    void getEntityBidOrders() throws IOException {
        String json = readFile("/testdata/db/responses/get-entity-bid-orders-response.json");
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(json));

        List<EntityOrder> orders = apiClient.getEntityBidOrders("BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC");
        assertThat(orders).contains(new EntityOrder("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB", "MLM", 1_900_000_000L, 1));
        assertRequest("/live/v1/querySmartContract");
    }

}