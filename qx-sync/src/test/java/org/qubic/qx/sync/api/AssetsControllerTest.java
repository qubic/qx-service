package org.qubic.qx.sync.api;

import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.assets.Asset;
import org.qubic.qx.sync.assets.Assets;
import org.springframework.test.web.reactive.server.WebTestClient;

class AssetsControllerTest {

    private final Assets assets = new Assets();
    private final AssetsController controller = new AssetsController(assets);

    private final WebTestClient client = WebTestClient
            .bindToController(controller)
            .configureClient()
            .baseUrl("/v1/qx")
            .build();

    @Test
    void getAssets() {
        Asset a = new Asset("issuerA", "nameA");
        Asset b = new Asset("issuerB", "nameA");
        Asset c = new Asset("issuerB", "nameB");
        assets.add(a);
        assets.add(b);
        assets.add(c);

        client.get().uri("/assets")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Asset.class)
                .contains(a, b, c);
    }

}