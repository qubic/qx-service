package org.qubic.qx.api.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.controller.domain.TradeDto;
import org.qubic.qx.api.controller.service.TradesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

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

    @MockBean
    private TradesService tradesService;

    @Autowired
    private TradesController controller;

    @Test
    void getTrades() {
        List<TradeDto> expected = List.of(TEST_TRADE, TEST_TRADE);
        when(tradesService.getTrades()).thenReturn(expected);

        List<TradeDto> result = controller.getTrades();
        List<TradeDto> cached = controller.getTrades();

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(tradesService, times(1)).getTrades();
    }

    @Test
    void getAssetTrades() {
        List<TradeDto> expected = List.of(TEST_TRADE, TEST_TRADE);
        when(tradesService.getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME)).thenReturn(expected);

        List<TradeDto> result = controller.getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME);
        List<TradeDto> cached = controller.getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME);
        controller.getAssetTrades(TEST_IDENTITY, TEST_ASSET_NAME);

        assertThat(result).isEqualTo(expected);
        assertThat(cached).isEqualTo(result);

        verify(tradesService, times(1)).getAssetTrades(TEST_ISSUER, TEST_ASSET_NAME);
        verify(tradesService, times(1)).getAssetTrades(TEST_IDENTITY, TEST_ASSET_NAME);
    }

    @Test
    void getEntityTrades() {
        List<TradeDto> expected = List.of(TEST_TRADE, TEST_TRADE);
        when(tradesService.getEntityTrades(TEST_IDENTITY)).thenReturn(expected);

        List<TradeDto> result = controller.getEntityTrades(TEST_IDENTITY);
        List<TradeDto> cached = controller.getEntityTrades(TEST_IDENTITY);
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