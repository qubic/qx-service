package org.qubic.qx.api.scheduler;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.redis.QxCacheManager;
import org.qubic.qx.api.redis.dto.TradeRedisDto;

import static org.mockito.Mockito.*;

class TradesProcessorPostProcessingTest {

    private final QxCacheManager qxCacheManager = mock();
    private final TradesProcessor processor = new TradesProcessor(null, null, null, qxCacheManager);

    @Test
    void postProcess() {
        TradeRedisDto sourceDto = mock();
        when(sourceDto.issuer()).thenReturn("ISSUER");
        when(sourceDto.assetName()).thenReturn("ASSET");
        when(sourceDto.maker()).thenReturn("MAKER");
        when(sourceDto.taker()).thenReturn("TAKER");
        processor.postProcess(sourceDto);

        verify(qxCacheManager).evictTradesCache();
        verify(qxCacheManager).evictTradeCacheForAsset("ISSUER", "ASSET");
        verify(qxCacheManager).evictChartCachesForAsset("ISSUER", "ASSET");
        verify(qxCacheManager).evictTradeCacheForEntity("MAKER");
        verify(qxCacheManager).evictTradeCacheForEntity("TAKER");
    }

}