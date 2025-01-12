package org.qubic.qx.api.controller;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;
import org.qubic.qx.api.controller.service.QxService;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QxFunctionsControllerTest {

    private final QxService qxService = mock();
    private final QxFunctionsController controller = new QxFunctionsController(qxService);

    private final WebTestClient client = WebTestClient
            .bindToController(controller)
            .configureClient()
            .baseUrl("/service/v1/qx")
            .build();

    @Test
    void getFees() {
        Fees expected = new Fees(1, 2, 3);

        when(qxService.getFees()).thenReturn(expected);

        client.get().uri("/fees")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Fees.class)
                .isEqualTo(expected);
    }

    @Test
    void getAssetAskOrders() {
        List<AssetOrder> expected = List.of(new AssetOrder("entity", 1, 2));
        when(qxService.getAssetAskOrders("issuerId", "assetName")).thenReturn(expected);

        client.get().uri("/issuer/issuerId/asset/assetName/asks")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AssetOrder.class)
                .isEqualTo(expected);
    }

    @Test
    void getAggregatedAssetAskOrders() {
        List<AssetOrder> expected = List.of(new AssetOrder("entity", 1, 2));
        when(qxService.getAggregatedAssetAskOrders("issuerId", "assetName")).thenReturn(expected);

        client.get().uri("/issuer/issuerId/asset/assetName/asks?aggregated=true")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AssetOrder.class)
                .isEqualTo(expected);
    }

    @Test
    void getAssetBidOrders() {
        List<AssetOrder> expected = List.of(new AssetOrder("entity", 1, 2));
        when(qxService.getAssetBidOrders("issuerId", "assetName")).thenReturn(expected);

        client.get().uri("/issuer/issuerId/asset/assetName/bids")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AssetOrder.class)
                .isEqualTo(expected);
    }

    @Test
    void getAggregatedAssetBidOrders() {
        List<AssetOrder> expected = List.of(new AssetOrder("entity", 1, 2));
        when(qxService.getAggregatedAssetBidOrders("issuerId", "assetName")).thenReturn(expected);

        client.get().uri("/issuer/issuerId/asset/assetName/bids?aggregated=true")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AssetOrder.class)
                .isEqualTo(expected);
    }

    @Test
    void getEntityAskOrders() {
        List<EntityOrder> expected = List.of(new EntityOrder("issuer", "asset", 1, 2));
        when(qxService.getEntityAskOrders("identity")).thenReturn(expected);

        client.get().uri("/entity/identity/asks")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EntityOrder.class)
                .isEqualTo(expected);
    }

    @Test
    void getEntityBidOrders() {
        List<EntityOrder> expected = List.of(new EntityOrder("issuer", "asset", 1, 2));
        when(qxService.getEntityBidOrders("identity")).thenReturn(expected);

        client.get().uri("/entity/identity/bids")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EntityOrder.class)
                .isEqualTo(expected);
    }


}