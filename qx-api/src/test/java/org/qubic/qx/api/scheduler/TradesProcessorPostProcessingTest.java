package org.qubic.qx.api.scheduler;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.redis.QxCacheManager;
import org.qubic.qx.api.redis.dto.TradeRedisDto;
import org.qubic.qx.api.richlist.TransferAssetService;

import static org.mockito.Mockito.*;

class TradesProcessorPostProcessingTest {

    private final TransferAssetService transferAssetService = mock();
    private final QxCacheManager qxCacheManager = mock();
    private final TradesProcessor processor = new TradesProcessor(null, null, null, transferAssetService, qxCacheManager);

    @Test
    void postProcess_thenEvictCaches() {
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

    @Test
    void postProcess_givenBid_thenTransferAssets() {
        TradeRedisDto sourceDto = new TradeRedisDto(1, 2, "hash", true, "taker", "maker", "issuer", "assetName", 3, 4);
        processor.postProcess(sourceDto);
        verify(transferAssetService).transfer("maker", "taker", "issuer", "assetName", 4);
    }

    @Test
    void postProcess_givenAsk_thenTransferAssets() {
        TradeRedisDto sourceDto = new TradeRedisDto(1, 2, "hash", false, "taker", "maker", "issuer", "assetName", 3, 4);
        processor.postProcess(sourceDto);
        verify(transferAssetService).transfer("taker", "maker", "issuer", "assetName", 4);
    }


}