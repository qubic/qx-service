package org.qubic.qx.api.controller;

import at.qubic.api.domain.qx.Qx;
import at.qubic.api.domain.qx.request.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.controller.domain.QxOrderRequest;
import org.qubic.qx.api.controller.domain.QxOrderResponse;
import org.qubic.qx.api.controller.service.QxOrderService;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QxOrderControllerTest {

    private static final BigInteger TICK_NUMBER = BigInteger.valueOf(123456);
    private static final BigInteger PRICE_PER_SHARE = BigInteger.valueOf(42);
    private static final BigInteger NUMBER_OF_SHARES = BigInteger.valueOf(3);
    private static final BigInteger TOTAL_AMOUNT = PRICE_PER_SHARE.multiply(NUMBER_OF_SHARES);
    private static final String FROM_ID = "TESTAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAPQFE";

    private static final QxAssetOrderData TEST_ASSET_ORDER_DATA = QxAssetOrderData.builder()
            .assetIssuer(new byte[32])
            .assetName("ASSET")
            .price(PRICE_PER_SHARE.longValue())
            .numberOfShares(NUMBER_OF_SHARES.longValue())
            .build();

    private final QxOrderService service = mock();
    private final QxOrderController controller = new QxOrderController(service);

    private final WebTestClient client =
    WebTestClient
            .bindToController(controller)
            .configureClient()
            .baseUrl("/service/v1/qx")
            .build();

    @BeforeEach
    void initMocks() {
        when(service.getLatestTick()).thenReturn(TICK_NUMBER);
    }

    @Test
    void addBid() {
        when(service.createAddBidOrder("ISSUER", "ASSET", NUMBER_OF_SHARES, PRICE_PER_SHARE))
                .thenReturn(QxAddBidOrder.builder()
                        .orderData(TEST_ASSET_ORDER_DATA)
                        .build());

        QxOrderResponse expected = new QxOrderResponse(TICK_NUMBER, FROM_ID,  Qx.ADDRESS, 6, TOTAL_AMOUNT,
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABBU1NFVAAAACoAAAAAAAAAAwAAAAAAAAA=");

        client.post().uri("/issuer/ISSUER/asset/ASSET/add-bid")
                .bodyValue(new QxOrderRequest(FROM_ID, NUMBER_OF_SHARES, PRICE_PER_SHARE))
                .exchange()
                .expectStatus().isOk()
                .expectBody(QxOrderResponse.class)
                .isEqualTo(expected);
    }

    @Test
    void addAsk() {
        when(service.createAddAskOrder("ISSUER", "ASSET", NUMBER_OF_SHARES, PRICE_PER_SHARE))
                .thenReturn(QxAddAskOrder.builder()
                        .orderData(TEST_ASSET_ORDER_DATA)
                        .build());

        QxOrderResponse expected = new QxOrderResponse(TICK_NUMBER, FROM_ID,  Qx.ADDRESS, 5, BigInteger.ONE,
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABBU1NFVAAAACoAAAAAAAAAAwAAAAAAAAA=");

        client.post().uri("/issuer/ISSUER/asset/ASSET/add-ask")
                .bodyValue(new QxOrderRequest(FROM_ID, NUMBER_OF_SHARES, PRICE_PER_SHARE))
                .exchange()
                .expectStatus().isOk()
                .expectBody(QxOrderResponse.class)
                .isEqualTo(expected);
    }


    @Test
    void removeBid() {
        when(service.createRemoveBidOrder("ISSUER", "ASSET", NUMBER_OF_SHARES, PRICE_PER_SHARE))
                .thenReturn(QxRemoveBidOrder.builder()
                        .orderData(TEST_ASSET_ORDER_DATA)
                        .build());

        QxOrderResponse expected = new QxOrderResponse(TICK_NUMBER, FROM_ID,  Qx.ADDRESS, 8, BigInteger.ONE,
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABBU1NFVAAAACoAAAAAAAAAAwAAAAAAAAA=");

        client.post().uri("/issuer/ISSUER/asset/ASSET/remove-bid")
                .bodyValue(new QxOrderRequest(FROM_ID, NUMBER_OF_SHARES, PRICE_PER_SHARE))
                .exchange()
                .expectStatus().isOk()
                .expectBody(QxOrderResponse.class)
                .isEqualTo(expected);
    }


    @Test
    void removeAsk() {
        when(service.createRemoveAskOrder("ISSUER", "ASSET", NUMBER_OF_SHARES, PRICE_PER_SHARE))
                .thenReturn(QxRemoveAskOrder.builder()
                        .orderData(TEST_ASSET_ORDER_DATA)
                        .build());

        QxOrderResponse expected = new QxOrderResponse(TICK_NUMBER, FROM_ID,  Qx.ADDRESS, 7, BigInteger.ONE,
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABBU1NFVAAAACoAAAAAAAAAAwAAAAAAAAA=");

        client.post().uri("/issuer/ISSUER/asset/ASSET/remove-ask")
                .bodyValue(new QxOrderRequest(FROM_ID, NUMBER_OF_SHARES, PRICE_PER_SHARE))
                .exchange()
                .expectStatus().isOk()
                .expectBody(QxOrderResponse.class)
                .isEqualTo(expected);
    }

}