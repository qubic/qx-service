package org.qubic.qx.api.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.qubic.qx.api.redis.QxCacheManager.CACHE_NAME_FEES;

@SpringBootTest(properties = """
    spring.cache.type=redis
""")
class QxFunctionsControllerCacheIT extends AbstractSpringIntegrationTest {

    private static final String TEST_ISSUER = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDVGRF";
    private static final String TEST_ASSET_NAME = "FOO";
    private static final String TEST_IDENTITY = "BCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEOPXN";
    private static final String TEST_IDENTITY_2 = "CDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFOZNK";

    @MockitoBean
    private QxService qxService;

    @Autowired
    private QxFunctionsController controller;

    @Autowired
    private QxCacheManager qxCacheManager;

    @Test
    void getFees_thenHitChache() {
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_FEES)).clear();

        Fees expected = new Fees(1, 2, 3);
        when(qxService.getFees()).thenReturn(expected);

        Fees result = controller.getFees();
        Fees cached = controller.getFees();

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(qxService, times(1)).getFees();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getAssetAskOrders_thenHitChache(boolean aggregated) {
        verifyThatGetAssetAskOrdersAreCached(aggregated);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getAssetAskOrders_givenOtherParams_thenCallService(boolean aggregated) {
        verifyThatGetAssetAskOrdersAreCached(aggregated);
        controller.getAssetAskOrders(TEST_ISSUER, "OTHER", aggregated);
        if (aggregated) {
            verify(qxService).getAggregatedAssetAskOrders(TEST_ISSUER, "OTHER");
        } else {
            verify(qxService).getAssetAskOrders(TEST_ISSUER, "OTHER");
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getAssetAskOrders_givenCacheEvicted_thenCallServiceAgain(boolean aggregated) {
        verifyThatGetAssetAskOrdersAreCached(aggregated);
        qxCacheManager.evictOrderCacheForAsset(TEST_ISSUER, TEST_ASSET_NAME);
        controller.getAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME, aggregated);
        if (aggregated) {
            verify(qxService, times(2)).getAggregatedAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME);
        } else {
            verify(qxService, times(2)).getAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME);
        }
    }

    private void verifyThatGetAssetAskOrdersAreCached(boolean aggregated) {
        List<AssetOrder> expected = List.of(new AssetOrder("entity", 1, 2));
        if (aggregated) {
            when(qxService.getAggregatedAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME)).thenReturn(expected);
        } else {
            when(qxService.getAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME)).thenReturn(expected);
        }

        List<AssetOrder> result = controller.getAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME, aggregated);
        List<AssetOrder> cached = controller.getAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME, aggregated);

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        if (aggregated) {
            verify(qxService, times(1)).getAggregatedAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME);
        } else {
            verify(qxService, times(1)).getAssetAskOrders(TEST_ISSUER, TEST_ASSET_NAME);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getAssetBidOrders_thenHitChache(boolean aggregated) {
        verifyThatGetAssetBidOrdersAreCached(aggregated);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getAssetBidOrders_givenOtherParams_thenCallService(boolean aggregated) {
        verifyThatGetAssetBidOrdersAreCached(aggregated);
        controller.getAssetBidOrders(TEST_ISSUER, "OTHER", aggregated);
        if (aggregated) {
            verify(qxService).getAggregatedAssetBidOrders(TEST_ISSUER, "OTHER");
        } else {
            verify(qxService).getAssetBidOrders(TEST_ISSUER, "OTHER");
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getAssetBidOrders_givenCacheEvicted_thenCallServiceAgain(boolean aggregated) {
        verifyThatGetAssetBidOrdersAreCached(aggregated);
        qxCacheManager.evictOrderCacheForAsset(TEST_ISSUER, TEST_ASSET_NAME);
        controller.getAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME, aggregated);
        if (aggregated) {
            verify(qxService, times(2)).getAggregatedAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME);
        } else {
            verify(qxService, times(2)).getAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME);
        }
    }

    private void verifyThatGetAssetBidOrdersAreCached(boolean aggregated) {
        List<AssetOrder> expected = List.of(new AssetOrder("entity", 1, 2));
        if (aggregated) {
            when(qxService.getAggregatedAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME)).thenReturn(expected);
        } else {
            when(qxService.getAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME)).thenReturn(expected);
        }

        List<AssetOrder> result = controller.getAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME, aggregated);
        List<AssetOrder> cached = controller.getAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME, aggregated);

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        if (aggregated) {
            verify(qxService, times(1)).getAggregatedAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME);
        } else {
            verify(qxService, times(1)).getAssetBidOrders(TEST_ISSUER, TEST_ASSET_NAME);
        }
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