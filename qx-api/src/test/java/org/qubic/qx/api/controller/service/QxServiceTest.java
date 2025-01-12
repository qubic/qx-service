package org.qubic.qx.api.controller.service;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.adapter.QxApiService;
import org.qubic.qx.api.controller.domain.AssetOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QxServiceTest {

    private final QxApiService qxApiService = mock();
    private final QxService service = new QxService(qxApiService);

    @Test
    void getAggregatedAskOrders_thenGroupByPriceDescending() {
        when(qxApiService.getAssetAskOrders("issuer", "asset")).thenReturn(List.of(
                new AssetOrder("entity1", 1, 3),
                new AssetOrder("entity2", 1, 4),
                new AssetOrder("entity1", 2, 5)
        ));

        List<AssetOrder> result = service.getAggregatedAssetAskOrders("issuer", "asset");

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(
                new AssetOrder(null, 1, 7),
                new AssetOrder(null, 2, 5));
    }

    @Test
    void getAggregatedAskOrders_thenGroupByPriceAscending() {
        when(qxApiService.getAssetBidOrders("issuer", "asset")).thenReturn(List.of(
                new AssetOrder("entity1", 1, 3),
                new AssetOrder("entity2", 1, 4),
                new AssetOrder("entity1", 2, 5)
        ));

        List<AssetOrder> result = service.getAggregatedAssetBidOrders("issuer", "asset");

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(
                new AssetOrder(null, 2, 5),
                new AssetOrder(null, 1, 7)
        );
    }

}