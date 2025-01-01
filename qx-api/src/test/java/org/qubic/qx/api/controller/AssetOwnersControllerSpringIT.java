package org.qubic.qx.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.controller.service.AssetOwnersService;
import org.qubic.qx.api.db.dto.AmountPerEntityDto;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigInteger;
import java.util.List;

import static org.mockito.Mockito.*;

class AssetOwnersControllerSpringIT extends AbstractSpringIntegrationTest {

    private static final String ISSUER = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB";

    @MockitoBean
    private AssetOwnersService service;

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
    void getTopAssetOwners() {
        List<AmountPerEntityDto> expected = List.of(
                new AmountPerEntityDto("id1", BigInteger.TEN),
                new AmountPerEntityDto("id1", BigInteger.ONE)
        );
        when(service.getTopAssetOwners(ISSUER, "ASSET")).thenReturn(expected);

        client.get().uri("/issuer/{:issuer}/asset/ASSET/owners", ISSUER)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AmountPerEntityDto.class)
                .isEqualTo(expected);
    }

}