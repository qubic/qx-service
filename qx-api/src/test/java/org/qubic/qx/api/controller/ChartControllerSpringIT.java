package org.qubic.qx.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.controller.domain.AvgPriceData;
import org.qubic.qx.api.controller.service.ChartService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;

class ChartControllerSpringIT extends AbstractSpringIntegrationTest  {

    private static final String ISSUER = "ISSUERISSUERISSUERISSUERISSUERISSUERISSUERISSUERISSUERISSUER";

    @MockBean
    private ChartService chartService;

    private WebTestClient client;

    @BeforeEach
    public void setUpClient(WebApplicationContext context) {
        client = MockMvcWebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .baseUrl("/service/v1/qx")
                .build();
    }

    @Test
    void getAveragePriceForAsset() {
        AvgPriceData priceData = new AvgPriceData(LocalDate.EPOCH, 1, 3, 5, 7, 13.13, 17);
        List<AvgPriceData> expected = List.of(priceData);
        when(chartService.getAveragePriceForAsset(ISSUER, "ASSET")).thenReturn(expected);

        client.get().uri("/issuer/{:issuer}/asset/ASSET/chart/average-price", ISSUER)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AvgPriceData.class)
                .contains(priceData);
    }
  
}