package org.qubic.qx.api.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.db.domain.Trade;
import org.qubic.qx.api.redis.QxCacheManager;
import org.qubic.qx.api.redis.dto.TradeRedisDto;
import org.qubic.qx.api.redis.repository.QueueProcessingRepository;
import org.qubic.qx.api.richlist.TransferAssetService;
import org.qubic.qx.api.scheduler.mapping.RedisToDomainMapper;
import org.springframework.data.repository.CrudRepository;

@Slf4j
public class TradesProcessor extends QueueProcessor<Trade, TradeRedisDto> {

    private final TransferAssetService transferAssetService;
    private final QxCacheManager qxCacheManager;

    public TradesProcessor(QueueProcessingRepository<TradeRedisDto> redisRepository, CrudRepository<Trade, Long> repository, RedisToDomainMapper<Trade, TradeRedisDto> mapper, TransferAssetService transferAssetService, QxCacheManager qxCacheManager) {
        super(redisRepository, repository, mapper);
        this.transferAssetService = transferAssetService;
        this.qxCacheManager = qxCacheManager;
    }

    @Override
    protected void postProcess(TradeRedisDto sourceDto) {
        if (sourceDto != null) {

            String issuer = sourceDto.issuer();
            String assetName = sourceDto.assetName();
            String from = sourceDto.bid() ? sourceDto.maker() : sourceDto.taker();
            String to = sourceDto.bid() ? sourceDto.taker() : sourceDto.maker();
            transferAssetService.transfer(from, to, issuer, assetName, sourceDto.numberOfShares());

            log.info("Evicting caches.");
            qxCacheManager.evictTradesCache();
            qxCacheManager.evictTradeCacheForAsset(issuer, assetName);
            qxCacheManager.evictChartCachesForAsset(issuer, assetName);
            qxCacheManager.evictTradeCacheForEntity(sourceDto.maker());
            qxCacheManager.evictTradeCacheForEntity(sourceDto.taker());

        }
    }

}
