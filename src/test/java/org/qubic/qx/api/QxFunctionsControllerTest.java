package org.qubic.qx.api;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.api.domain.Fees;
import org.qubic.qx.api.service.QxService;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QxFunctionsControllerTest {

    private final QxService qxService = mock();
    private final QxFunctionsController controller = new QxFunctionsController(qxService);

    private final WebTestClient client = WebTestClient
            .bindToController(controller)
            .configureClient()
            .baseUrl("/v1/qx")
            .build();

    @Test
    void getFees() {
        Fees fees = new Fees(1L, 2L, 3L);
        when(qxService.getFees()).thenReturn(Mono.just(fees));

        client.get().uri("/fees")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Fees.class)
                .isEqualTo(fees);
    }

    @Test
    void getAssetAskOrders() {
        AssetOrder assetOrder = new AssetOrder("entity", 1L, 2L);
        List<AssetOrder> assetOrders = List.of(assetOrder);
        when(qxService.getAskOrders("issuerId", "assetName")).thenReturn(Mono.just(assetOrders));

        client.get().uri("/issuer/issuerId/asset/assetName/orders/ask")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AssetOrder.class)
                .isEqualTo(assetOrders);
    }

    @Test
    void getAssetBidOrders() {
        AssetOrder assetOrder = new AssetOrder("entity", 1L, 2L);
        List<AssetOrder> assetOrders = List.of(assetOrder);
        when(qxService.getBidOrders("issuerId", "assetName")).thenReturn(Mono.just(assetOrders));

        client.get().uri("/issuer/issuerId/asset/assetName/orders/bid")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AssetOrder.class)
                .isEqualTo(assetOrders);
    }


}