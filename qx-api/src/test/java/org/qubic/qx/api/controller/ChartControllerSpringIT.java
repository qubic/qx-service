package org.qubic.qx.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.db.dto.AvgPriceData;
import org.qubic.qx.api.controller.service.ChartService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

class ChartControllerSpringIT extends AbstractSpringIntegrationTest  {

    private static final String ISSUER = "ISSUERISSUERISSUERISSUERISSUERISSUERISSUERISSUERISSUERISPXHC";

    @MockitoBean
    private ChartService chartService;

    private WebTestClient client;

    @BeforeEach
    public void setUpClient(WebApplicationContext context) {
        client = MockMvcWebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .baseUrl("/service/v1/qx")
                .build();
        evictAllCaches();
    }

    @Test
    void getAveragePriceForAssetDefault() {
        AvgPriceData priceData = new AvgPriceData(Instant.EPOCH, 1, 3, 5, 7, 13.13, 17);
        List<AvgPriceData> expected = List.of(priceData);
        when(chartService.getAveragePriceForAssetPerDay(ISSUER, "ASSET")).thenReturn(expected);

        client.get().uri("/issuer/{:issuer}/asset/ASSET/chart/average-price", ISSUER)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AvgPriceData.class)
                .contains(priceData);
    }

    @Test
    void getAveragePriceForAssetPerDay() {
        AvgPriceData priceData = new AvgPriceData(Instant.EPOCH, 1, 3, 5, 7, 13.13, 17);
        List<AvgPriceData> expected = List.of(priceData);
        when(chartService.getAveragePriceForAssetPerDay(ISSUER, "ASSET")).thenReturn(expected);

        client.get().uri("/issuer/{:issuer}/asset/ASSET/chart/average-price?interval={:interval}", ISSUER, "1d")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AvgPriceData.class)
                .contains(priceData);
    }

    @Test
    void getAveragePriceForAssetPerHour() {
        AvgPriceData priceData = new AvgPriceData(Instant.EPOCH, 1, 3, 5, 7, 13.13, 17);
        List<AvgPriceData> expected = List.of(priceData);
        when(chartService.getAveragePriceForAssetPerHour(ISSUER, "ASSET")).thenReturn(expected);

        client.get().uri("/issuer/{:issuer}/asset/ASSET/chart/average-price?interval={:interval}", ISSUER, "1h")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AvgPriceData.class)
                .contains(priceData);
    }
  
}