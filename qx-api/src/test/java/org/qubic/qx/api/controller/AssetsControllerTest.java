package org.qubic.qx.api.controller;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.controller.service.AssetsService;
import org.qubic.qx.api.db.domain.Asset;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.mockito.Mockito.*;

class AssetsControllerTest {

    private final AssetsService assetsService = mock();
    private final AssetsController controller = new AssetsController(assetsService);

    private final WebTestClient client = WebTestClient
            .bindToController(controller)
            .configureClient()
            .baseUrl("/service/v1/qx")
            .build();

    @Test
    void getAssets() {
        Asset a = Asset.builder().issuer("issuerA").name("nameA").build();
        Asset b = Asset.builder().issuer("issuerB").name("nameA").build();
        Asset c = Asset.builder().issuer("issuerB").name("nameB").build();

        when(assetsService.getAssets()).thenReturn(List.of(a, b, c));

        client.get().uri("/assets")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Asset.class)
                .contains(a, b, c);
    }



}