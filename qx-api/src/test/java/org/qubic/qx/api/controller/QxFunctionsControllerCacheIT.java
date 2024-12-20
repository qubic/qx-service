package org.qubic.qx.api.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;
import org.qubic.qx.api.controller.service.QxService;
import org.qubic.qx.api.redis.QxCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
    private static final String TEST_IDENTITY_2 = "CDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJ";

    @MockitoBean
    private QxService qxService;

    @Autowired
    private QxFunctionsController controller;

    @Autowired
    private QxCacheManager qxCacheManager;

    @Test
    void getFees_thenHitChache() {
        Fees expected = new Fees(1, 2, 3);
        when(qxService.getFees()).thenReturn(expected);

        Fees result = controller.getFees();
        Fees cached = controller.getFees();

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(qxService, times(1)).getFees();
    }

    @Test
    void getAssetAskOrders_thenHitChache() {
        verifyThatGetAssetAskOrdersAreCached();
    }

    @Test
    void getAssetAskOrders_givenOtherParams_thenCallService() {
        verifyThatGetAssetAskOrdersAreCached();
        controller.getAssetAskOrders(TEST_ISSUER, "OTHER");
        verify(qxService).getAssetAskOrders(TEST_ISSUER, "OTHER");
    }

    @Test
    void getAssetAskOrders_givenCacheEvicted_thenCallServiceAgain() {
        verifyThatGetAssetAskOrdersAreCached();
        qxCacheManager.evictOrderCacheForAsset(TEST_ISSUER, TEST_ASSET_NAME);
        controller.getAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME);
        verify(qxService, times(2)).getAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME);
    }

    private void verifyThatGetAssetAskOrdersAreCached() {
        List<AssetOrder> expected = List.of(new AssetOrder("entity", 1, 2));
        when(qxService.getAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME)).thenReturn(expected);

        List<AssetOrder> result = controller.getAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME);
        List<AssetOrder> cached = controller.getAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME);

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(qxService, times(1)).getAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME);
    }

    @Test
    void getAssetBidOrders_thenHitChache() {
        verifyThatGetAssetBidOrdersAreCached();
    }

    @Test
    void getAssetBidOrders_givenOtherParams_thenCallService() {
        verifyThatGetAssetBidOrdersAreCached();
        controller.getAssetBidOrders(TEST_ISSUER, "OTHER");
        verify(qxService).getAssetBidOrders(TEST_ISSUER, "OTHER");
    }

    @Test
    void getAssetBidOrders_givenCacheEvicted_thenCallServiceAgain() {
        verifyThatGetAssetBidOrdersAreCached();
        qxCacheManager.evictOrderCacheForAsset(TEST_ISSUER, TEST_ASSET_NAME);
        controller.getAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME);
        verify(qxService, times(2)).getAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME);
    }

    private void verifyThatGetAssetBidOrdersAreCached() {
        List<AssetOrder> expected = List.of(new AssetOrder("entity", 1, 2));
        when(qxService.getAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME)).thenReturn(expected);

        List<AssetOrder> result = controller.getAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME);
        List<AssetOrder> cached = controller.getAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME);

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(qxService, times(1)).getAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME);
    }

    @Test
    void getEntityAskOrders_thenHitChache() {
        verifyThatGetEntityAskOrdersAreCached();
    }

    @Test
    void getEntityAskOrders_givenOtherParams_thenCallService() {
        verifyThatGetEntityAskOrdersAreCached();

        controller.getEntityAskOrders(TEST_IDENTITY_2);
        verify(qxService).getEntityAskOrders(TEST_IDENTITY_2);
    }

    @Test
    void getEntityAskOrders_givenCacheEvicted_thenCallServiceAgain() {
        verifyThatGetEntityAskOrdersAreCached();
        qxCacheManager.evictOrderCacheForEntity(TEST_IDENTITY);
        controller.getEntityAskOrders(TEST_IDENTITY);
        verify(qxService, times(2)).getEntityAskOrders(TEST_IDENTITY);
    }

    private void verifyThatGetEntityAskOrdersAreCached() {
        List<EntityOrder> expected = List.of(new EntityOrder(TEST_ISSUER, TEST_ASSET_NAME, 1, 2));
        when(qxService.getEntityAskOrders(TEST_IDENTITY)).thenReturn(expected);

        List<EntityOrder> result = controller.getEntityAskOrders(TEST_IDENTITY);
        List<EntityOrder> cached = controller.getEntityAskOrders(TEST_IDENTITY);

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(qxService, times(1)).getEntityAskOrders(TEST_IDENTITY);
    }

    @Test
    void getEntityBidOrders_thenHitChache() {
        verifyThatGetEntityBidOrdersAreCached();
    }

    @Test
    void getEntityBidOrders_givenOtherParams_thenCallService() {
        verifyThatGetEntityBidOrdersAreCached();
        controller.getEntityBidOrders(TEST_IDENTITY_2);
        verify(qxService, times(1)).getEntityBidOrders(TEST_IDENTITY_2);
    }

    @Test
    void getEntityBidOrders_givenCacheEvicted_thenCallServiceAgain() {
        verifyThatGetEntityBidOrdersAreCached();
        qxCacheManager.evictOrderCacheForEntity(TEST_IDENTITY);
        controller.getEntityBidOrders(TEST_IDENTITY);
        verify(qxService, times(2)).getEntityBidOrders(TEST_IDENTITY);
    }

    private void verifyThatGetEntityBidOrdersAreCached() {
        List<EntityOrder> expected = List.of(new EntityOrder(TEST_ISSUER, TEST_ASSET_NAME, 1, 2));
        when(qxService.getEntityBidOrders(TEST_IDENTITY)).thenReturn(expected);

        List<EntityOrder> result = controller.getEntityBidOrders(TEST_IDENTITY);
        List<EntityOrder> cached = controller.getEntityBidOrders(TEST_IDENTITY);

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(qxService, times(1)).getEntityBidOrders(TEST_IDENTITY);
    }

    @AfterEach
    void clearCache() {
        evictAllCaches();
    }

}