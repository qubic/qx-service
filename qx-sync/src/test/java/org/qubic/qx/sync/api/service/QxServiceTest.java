package org.qubic.qx.sync.api.service;

import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.QxApiService;
import org.qubic.qx.sync.api.domain.AssetOrder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QxServiceTest {

    private final QxApiService integrationApi = mock();
    private final QxService qxService = new QxService(integrationApi);

    @Test
    void getAssetAskOrders() {
        List<AssetOrder> orders = List.of(new AssetOrder("entity", 1, 2));
        when(integrationApi.getAssetAskOrders("issuer", "asset")).thenReturn(Mono.just(orders));
        StepVerifier.create(qxService.getAssetAskOrders("issuer", "asset"))
                .expectNext(orders)
                .verifyComplete();
    }

    @Test
    void getAssetBidOrders() {
        List<AssetOrder> orders = List.of(new AssetOrder("entity", 1, 2));
        when(integrationApi.getAssetBidOrders("issuer", "asset")).thenReturn(Mono.just(orders));
        StepVerifier.create(qxService.getAssetBidOrders("issuer", "asset"))
                .expectNext(orders)
                .verifyComplete();
    }

}