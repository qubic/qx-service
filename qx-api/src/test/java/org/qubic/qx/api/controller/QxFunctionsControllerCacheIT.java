package org.qubic.qx.api.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;
import org.qubic.qx.api.controller.service.QxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = """
    spring.cache.type=redis
""")
class QxFunctionsControllerCacheIT extends AbstractSpringIntegrationTest {

    private static final String TEST_ISSUER = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGH";
    private static final String TEST_ASSET_NAME = "FOO";
    private static final String TEST_IDENTITY = "BCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHI";

    @MockBean
    private QxService qxService;

    @Autowired
    private QxFunctionsController controller;

    @Test
    void getFees() {
        Fees expected = new Fees(1, 2, 3);
        when(qxService.getFees()).thenReturn(expected);

        Fees result = controller.getFees();
        Fees cached = controller.getFees();

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(qxService, times(1)).getFees();
    }

    @Test
    void getAssetAskOrders() {
        List<AssetOrder> expected = List.of(new AssetOrder("entity", 1, 2));
        when(qxService.getAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME)).thenReturn(expected);

        List<AssetOrder> result = controller.getAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME);
        List<AssetOrder> cached = controller.getAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME);
        controller.getAssetAskOrders(TEST_ISSUER, "OTHER");

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(qxService, times(1)).getAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME);
        verify(qxService, times(1)).getAssetAskOrders(TEST_ISSUER, "OTHER");
    }

    @Test
    void getAssetBidOrders() {
        List<AssetOrder> expected = List.of(new AssetOrder("entity", 1, 2));
        when(qxService.getAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME)).thenReturn(expected);

        List<AssetOrder> result = controller.getAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME);
        List<AssetOrder> cached = controller.getAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME);
        controller.getAssetBidOrders(TEST_ISSUER, "OTHER");

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(qxService, times(1)).getAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME);
        verify(qxService, times(1)).getAssetBidOrders(TEST_ISSUER, "OTHER");
    }

    @Test
    void getEntityAskOrders() {
        List<EntityOrder> expected = List.of(new EntityOrder(TEST_ISSUER, TEST_ASSET_NAME, 1, 2));
        when(qxService.getEntityAskOrders(TEST_IDENTITY)).thenReturn(expected);

        List<EntityOrder> result = controller.getEntityAskOrders(TEST_IDENTITY);
        List<EntityOrder> cached = controller.getEntityAskOrders(TEST_IDENTITY);
        controller.getEntityAskOrders(TEST_ISSUER); // not TEST_IDENTITY

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(qxService, times(1)).getEntityAskOrders(TEST_IDENTITY);
        verify(qxService, times(1)).getEntityAskOrders(TEST_ISSUER);
    }

    @Test
    void getEntityBidOrders() {
        List<EntityOrder> expected = List.of(new EntityOrder(TEST_ISSUER, TEST_ASSET_NAME, 1, 2));
        when(qxService.getEntityBidOrders(TEST_IDENTITY)).thenReturn(expected);

        List<EntityOrder> result = controller.getEntityBidOrders(TEST_IDENTITY);
        List<EntityOrder> cached = controller.getEntityBidOrders(TEST_IDENTITY);
        controller.getEntityBidOrders(TEST_ISSUER); // not TEST_IDENTITY

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(qxService, times(1)).getEntityBidOrders(TEST_IDENTITY);
        verify(qxService, times(1)).getEntityBidOrders(TEST_ISSUER);
    }

    @AfterEach
    void clearCache() {
        evictAllCaches();
    }

}