package org.qubic.qx.api.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.controller.service.TradesService;
import org.qubic.qx.api.db.domain.Asset;
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
    private static final TradeDto TEST_TRADE = new TradeDto(Instant.now(), "hash", "taker", "maker", "issuer", "tokenAsset", true, 123, 456);

    private static final Pageable DEFAULT_PAGE = PageRequest.of(0, 10);

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
        qxCacheManager.evictTradesCache("foo");
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
    void getSmartContractTrades_thenHitCache() {
        verifyThatGetSmartContractTradesIsCached();
    }

    @Test
    void getSmartContractTrades_givenCacheEvicted_thenCallServiceAgain() {
        verifyThatGetSmartContractTradesIsCached();
        qxCacheManager.evictTradesCache(Asset.SMART_CONTRACT_ISSUER);
        controller.getSmartContractTrades(DEFAULT_PAGE);
        verify(tradesService, times(2)).getSmartContractTrades(DEFAULT_PAGE);
    }

    private void verifyThatGetSmartContractTradesIsCached() {
        List<TradeDto> expected = List.of(TEST_TRADE, TEST_TRADE);
        when(tradesService.getSmartContractTrades(DEFAULT_PAGE)).thenReturn(expected);

        List<TradeDto> result = controller.getSmartContractTrades(DEFAULT_PAGE); // not cached
        List<TradeDto> cached = controller.getSmartContractTrades(DEFAULT_PAGE); // cached

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(tradesService, times(1)).getSmartContractTrades(DEFAULT_PAGE);
    }

    @Test
    void getTokenTrades_thenHitCache() {
        verifyThatGetTokenTradesIsCached();
    }

    @Test
    void getTokenTrades_givenCacheEvicted_thenCallServiceAgain() {
        verifyThatGetTokenTradesIsCached();
        qxCacheManager.evictTradesCache("SOME_TOKEN_ISSUER");
        controller.getTokenTrades(DEFAULT_PAGE);
        verify(tradesService, times(2)).getTokenTrades(DEFAULT_PAGE);
    }

    private void verifyThatGetTokenTradesIsCached() {
        List<TradeDto> expected = List.of(TEST_TRADE, TEST_TRADE);
        when(tradesService.getTokenTrades(DEFAULT_PAGE)).thenReturn(expected);

        List<TradeDto> result = controller.getTokenTrades(DEFAULT_PAGE); // not cached
        List<TradeDto> cached = controller.getTokenTrades(DEFAULT_PAGE); // cached

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(tradesService, times(1)).getTokenTrades(DEFAULT_PAGE);
    }

    @Test
    void getAssetTrades_thenHitCache() {
        verifyThatGetAssetTradesIsCached();
    }

    @Test
    void getAssetTrades_givenCacheEvicted_thenCallServiceAgain() {
        verifyThatGetAssetTradesIsCached();
        qxCacheManager.evictTradeCacheForAsset(TEST_ISSUER, TEST_ASSET_NAME);
        controller.getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME, DEFAULT_PAGE);
        verify(tradesService, times(2)).getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME, DEFAULT_PAGE);
    }

    private void verifyThatGetAssetTradesIsCached() {
        List<TradeDto> expected = List.of(TEST_TRADE, TEST_TRADE);
        when(tradesService.getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME, TradesControllerCacheIT.DEFAULT_PAGE)).thenReturn(expected);

        List<TradeDto> result = controller.getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME, TradesControllerCacheIT.DEFAULT_PAGE); // not cached
        List<TradeDto> cached = controller.getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME, TradesControllerCacheIT.DEFAULT_PAGE); // cached
        controller.getAssetTrades(TEST_IDENTITY, TEST_ASSET_NAME, TradesControllerCacheIT.DEFAULT_PAGE);

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(tradesService, times(1)).getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME, TradesControllerCacheIT.DEFAULT_PAGE);
        verify(tradesService, times(1)).getAssetTrades(TEST_IDENTITY, TEST_ASSET_NAME, TradesControllerCacheIT.DEFAULT_PAGE);
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

    @BeforeEach
    @AfterEach
    void clearCache() {
        evictAllCaches();
    }

}