package org.qubic.qx.api.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.db.dto.TradeDto;
import org.qubic.qx.api.controller.service.TradesService;
import org.qubic.qx.api.redis.QxCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = """
    spring.cache.type=redis
""")
class TradesControllerCacheIT extends AbstractSpringIntegrationTest {

    private static final String TEST_ISSUER = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGH";
    private static final String TEST_ASSET_NAME = "FOO";
    private static final String TEST_IDENTITY = "BCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHI";
    private static final TradeDto TEST_TRADE = new TradeDto(Instant.now(), "hash", "taker", "maker", "issuer", "assetName", true, 123, 456);

    @MockitoBean
    private TradesService tradesService;

    @Autowired
    private TradesController controller;

    @Autowired
    private QxCacheManager qxCacheManager;

    @Test
    void getTrades_thenHitCache() {
        verifyThatGetTradesIsCached();
    }

    @Test
    void getTrades_givenCacheEvicted_thenCallServiceAgain() {
        verifyThatGetTradesIsCached();
        qxCacheManager.evictTradesCache();
        controller.getTrades();
        verify(tradesService, times(2)).getTrades();
    }

    private void verifyThatGetTradesIsCached() {
        List<TradeDto> expected = List.of(TEST_TRADE, TEST_TRADE);
        when(tradesService.getTrades()).thenReturn(expected);

        List<TradeDto> result = controller.getTrades(); // not cached
        List<TradeDto> cached = controller.getTrades(); // cached

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(tradesService, times(1)).getTrades();
    }

    @Test
    void getAssetTrades_thenHitCache() {
        verifyThatGetAssetTradesIsCached();
    }

    @Test
    void getAssetTrades_givenCacheEvicted_thenCallServiceAgain() {
        verifyThatGetAssetTradesIsCached();
        qxCacheManager.evictTradeCacheForAsset(TEST_ISSUER, TEST_ASSET_NAME);
        controller.getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME);
        verify(tradesService, times(2)).getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME);
    }

    private void verifyThatGetAssetTradesIsCached() {
        List<TradeDto> expected = List.of(TEST_TRADE, TEST_TRADE);
        when(tradesService.getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME)).thenReturn(expected);

        List<TradeDto> result = controller.getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME); // not cached
        List<TradeDto> cached = controller.getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME); // cached
        controller.getAssetTrades(TEST_IDENTITY, TEST_ASSET_NAME);

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(tradesService, times(1)).getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME);
        verify(tradesService, times(1)).getAssetTrades(TEST_IDENTITY, TEST_ASSET_NAME);
    }

    @Test
    void getEntityTrades_thenHitCache() {
        verfiyThatGetEntityTradesIsCached();
    }

    @Test
    void getEntityTrades_givenCacheEvicted_thenCallServiceAgain() {
        verfiyThatGetEntityTradesIsCached();
        qxCacheManager.evictTradeCacheForEntity(TEST_IDENTITY);
        controller.getEntityTrades(TEST_IDENTITY);
        verify(tradesService, times(2)).getEntityTrades(TEST_IDENTITY);
    }

    private void verfiyThatGetEntityTradesIsCached() {
        List<TradeDto> expected = List.of(TEST_TRADE, TEST_TRADE);
        when(tradesService.getEntityTrades(TEST_IDENTITY)).thenReturn(expected);

        List<TradeDto> result = controller.getEntityTrades(TEST_IDENTITY); // not cached
        List<TradeDto> cached = controller.getEntityTrades(TEST_IDENTITY); // cached
        controller.getEntityTrades(TEST_ISSUER);

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(tradesService, times(1)).getEntityTrades(TEST_IDENTITY);
        verify(tradesService, times(1)).getEntityTrades(TEST_ISSUER);
    }

    @AfterEach
    void clearCache() {
        evictAllCaches();
    }

}