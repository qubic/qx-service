package org.qubic.qx.api.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.controller.service.TradesService;
import org.qubic.qx.api.db.dto.TradeDto;
import org.qubic.qx.api.redis.QxCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = """
    spring.cache.type=redis
""")
class TradesControllerCacheIT extends AbstractSpringIntegrationTest {

    private static final String TEST_ISSUER = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDVGRF";
    private static final String TEST_ASSET_NAME = "FOO";
    private static final String TEST_IDENTITY = "BCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEOPXN";
    private static final TradeDto TEST_TRADE = new TradeDto(Instant.now(), "hash", "taker", "maker", "issuer", "assetName", true, 123, 456);

    private static final Pageable DEFAULT_PAGE = PageRequest.of(0, 100);

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
        controller.getTrades(DEFAULT_PAGE);
        verify(tradesService, times(2)).getTrades(DEFAULT_PAGE);
    }

    private void verifyThatGetTradesIsCached() {
        List<TradeDto> expected = List.of(TEST_TRADE, TEST_TRADE);
        when(tradesService.getTrades(DEFAULT_PAGE)).thenReturn(expected);

        List<TradeDto> result = controller.getTrades(DEFAULT_PAGE); // not cached
        List<TradeDto> cached = controller.getTrades(DEFAULT_PAGE); // cached

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(tradesService, times(1)).getTrades(DEFAULT_PAGE);
    }

    @Test
    void getAssetTrades_thenHitCache() {
        verifyThatGetAssetTradesIsCached(DEFAULT_PAGE);
    }

    @Test
    void getAssetTrades_givenCacheEvicted_thenCallServiceAgain() {
        verifyThatGetAssetTradesIsCached(DEFAULT_PAGE);
        qxCacheManager.evictTradeCacheForAsset(TEST_ISSUER, TEST_ASSET_NAME);
        controller.getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME, DEFAULT_PAGE);
        verify(tradesService, times(2)).getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME, DEFAULT_PAGE);
    }

    private void verifyThatGetAssetTradesIsCached(Pageable pageable) {
        List<TradeDto> expected = List.of(TEST_TRADE, TEST_TRADE);
        when(tradesService.getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME, pageable)).thenReturn(expected);

        List<TradeDto> result = controller.getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME, pageable); // not cached
        List<TradeDto> cached = controller.getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME, pageable); // cached
        controller.getAssetTrades(TEST_IDENTITY, TEST_ASSET_NAME, pageable);

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(tradesService, times(1)).getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME, pageable);
        verify(tradesService, times(1)).getAssetTrades(TEST_IDENTITY, TEST_ASSET_NAME, pageable);
    }

    @Test
    void getEntityTrades_thenHitCache() {
        verfiyThatGetEntityTradesIsCached();
    }

    @Test
    void getEntityTrades_givenCacheEvicted_thenCallServiceAgain() {
        verfiyThatGetEntityTradesIsCached();
        qxCacheManager.evictTradeCacheForEntity(TEST_IDENTITY);
        controller.getEntityTrades(TEST_IDENTITY, DEFAULT_PAGE);
        verify(tradesService, times(2)).getEntityTrades(TEST_IDENTITY, DEFAULT_PAGE);
    }

    private void verfiyThatGetEntityTradesIsCached() {
        List<TradeDto> expected = List.of(TEST_TRADE, TEST_TRADE);
        when(tradesService.getEntityTrades(TEST_IDENTITY, DEFAULT_PAGE)).thenReturn(expected);

        List<TradeDto> result = controller.getEntityTrades(TEST_IDENTITY, DEFAULT_PAGE); // not cached
        List<TradeDto> cached = controller.getEntityTrades(TEST_IDENTITY, DEFAULT_PAGE); // cached
        controller.getEntityTrades(TEST_ISSUER, DEFAULT_PAGE);

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(tradesService, times(1)).getEntityTrades(TEST_IDENTITY, DEFAULT_PAGE);
        verify(tradesService, times(1)).getEntityTrades(TEST_ISSUER, DEFAULT_PAGE);
    }

    @AfterEach
    void clearCache() {
        evictAllCaches();
    }

}