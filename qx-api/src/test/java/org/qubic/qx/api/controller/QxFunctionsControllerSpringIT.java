package org.qubic.qx.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.time.Duration;

import static org.qubic.qx.api.uitl.FileUtil.readFile;

class QxFunctionsControllerSpringIT extends AbstractSpringIntegrationTest {

    private static final String ISSUER = "ISSUERISSUERISSUERISSUERISSUERISSUERISSUERISSUERISSUERISPXHC";
    private static final String ID = ISSUER;

    private WebTestClient client;

    @BeforeEach
    public void setUpClient(WebApplicationContext context) {
        client = MockMvcWebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .responseTimeout(Duration.ofSeconds(1))
                .baseUrl("/service/v1/qx")
                .build();
    }

    @Test
    void getFees() {
        String responseJson = """
                { "responseData": "AMqaO2QAAADAxi0A" }""";
        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseJson));

        client.get().uri("/fees")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Fees.class)
                .isEqualTo(new Fees(1_000_000_000, 100, 3_000_000));

        assertRequest("/live/v1/querySmartContract");
    }

    @Test
    void getAssetAskOrders() throws IOException {
        String json = readFile("/testdata/db/responses/get-asset-ask-orders-response.json");

        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(json));

        client.get().uri("/issuer/"+ISSUER+"/asset/TEST/asks")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AssetOrder.class)
                .contains(new AssetOrder("BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC", 3, 424634414));

        assertRequest("/live/v1/querySmartContract");
    }

    @Test
    void getAssetBidOrders() throws IOException {
        String json = readFile("/testdata/db/responses/get-asset-bid-orders-response.json");

        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(json));

        client.get().uri("/issuer/"+ISSUER+"/asset/TEST/bids")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AssetOrder.class)
                .contains(new AssetOrder("JHCGKVNUKTBVJGGZTCDBPVSYIOIDFUPCQOVCQFNMEGYHVWMINTEJITLGTXGO", 1, 23116901));

        assertRequest("/live/v1/querySmartContract");
    }

    @Test
    void getEntityAskOrders() throws IOException {
        String json = readFile("/testdata/db/responses/get-entity-ask-orders-response.json");

        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(json));

        client.get().uri("/entity/"+ ID + "/asks")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EntityOrder.class)
                .contains(new EntityOrder("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB", "MLM", 4_500_000_000L, 1));

        assertRequest("/live/v1/querySmartContract");
    }

    @Test
    void getEntityBidOrders() throws IOException {
        String json = readFile("/testdata/db/responses/get-entity-bid-orders-response.json");

        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(json));

        client.get().uri("/entity/" + ID + "/bids")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EntityOrder.class)
                .contains(new EntityOrder("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB", "MLM", 1_900_000_000L, 1));

        assertRequest("/live/v1/querySmartContract");
    }

}