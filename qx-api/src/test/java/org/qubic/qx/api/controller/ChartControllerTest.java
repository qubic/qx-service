package org.qubic.qx.api.controller;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.controller.service.ChartService;
import org.qubic.qx.api.db.dto.AvgPriceData;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChartControllerTest {

    private final ChartService chartService = mock();
    private final ChartController controller = new ChartController(chartService);

    private final WebTestClient client = WebTestClient
            .bindToController(controller)
            .configureClient()
            .baseUrl("/service/v1/qx")
            .build();

    @Test
    void getAveragePriceForAsset() {
        AvgPriceData priceData = new AvgPriceData(Instant.EPOCH, 1, 3, 5, 7, 13.13, 17);
        List<AvgPriceData> expected = List.of(priceData);
        when(chartService.getAveragePriceForAssetPerDay("ISSUER", "ASSET")).thenReturn(expected);

        // needs interval required = false without default value as conversion is not registered in test
        client.get().uri("/issuer/ISSUER/asset/ASSET/chart/average-price")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AvgPriceData.class)
                .contains(priceData);

    }
  
}