package org.qubic.qx.api.service;

import org.junit.jupiter.api.Test;
import org.qubic.qx.adapter.QxApiService;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.api.domain.EntityOrder;
import org.qubic.qx.api.domain.Fees;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

class QxServiceTest {

    private final QxApiService integrationApi = mock();
    private final QxService qxService = new QxService(integrationApi);

    @Test
    void getFees() {
        Fees fees = new Fees(1, 2, 3);
        when(integrationApi.getFees()).thenReturn(Mono.just(fees));

        StepVerifier.create(qxService.getFees())
                .expectNext(fees)
                .verifyComplete();
    }

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

    @Test
    void getEntityAskOrders() {
        List<EntityOrder> orders = List.of(new EntityOrder("issuer", "asset", 1, 2));
        when(integrationApi.getEntityAskOrders("identity")).thenReturn(Mono.just(orders));
        StepVerifier.create(qxService.getEntityAskOrders("identity"))
                .expectNext(orders)
                .verifyComplete();
    }

    @Test
    void getEntityBidOrders() {
        List<EntityOrder> orders = List.of(new EntityOrder("issuer", "asset", 1, 2));
        when(integrationApi.getEntityBidOrders("identity")).thenReturn(Mono.just(orders));
        StepVerifier.create(qxService.getEntityBidOrders("identity"))
                .expectNext(orders)
                .verifyComplete();
    }

}