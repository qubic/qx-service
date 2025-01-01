package org.qubic.qx.api.controller;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.controller.service.AssetOwnersService;
import org.qubic.qx.api.db.dto.AmountPerEntityDto;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigInteger;
import java.util.List;

import static org.mockito.Mockito.*;

class AssetOwnersControllerTest {

    private final AssetOwnersService service = mock();
    private final AssetOwnersController controller = new AssetOwnersController(service);

    private final WebTestClient client = WebTestClient
            .bindToController(controller)
            .configureClient()
            .baseUrl("/service/v1/qx")
            .build();

    @Test
    void getTopAssetOwners() {
        List<AmountPerEntityDto> expected = List.of(
                new AmountPerEntityDto("id1", BigInteger.TEN),
                new AmountPerEntityDto("id1", BigInteger.ONE)
        );
        when(service.getTopAssetOwners("ISSUER", "ASSET")).thenReturn(expected);

        client.get().uri("/issuer/ISSUER/asset/ASSET/owners")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AmountPerEntityDto.class)
                .isEqualTo(expected);
    }
}