package org.qubic.qx.api.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.db.domain.Trade;
import org.qubic.qx.api.redis.QxCacheManager;
import org.qubic.qx.api.redis.dto.TradeRedisDto;
import org.qubic.qx.api.redis.repository.QueueProcessingRepository;
import org.qubic.qx.api.scheduler.mapping.RedisToDomainMapper;
import org.springframework.data.repository.CrudRepository;

@Slf4j
public class TradesProcessor extends QueueProcessor<Trade, TradeRedisDto> {

    private final QxCacheManager qxCacheManager;

    public TradesProcessor(QueueProcessingRepository<TradeRedisDto> redisRepository, CrudRepository<Trade, Long> repository, RedisToDomainMapper<Trade, TradeRedisDto> mapper, QxCacheManager qxCacheManager) {
        super(redisRepository, repository, mapper);
        this.qxCacheManager = qxCacheManager;
    }

    @Override
    protected void postProcess(TradeRedisDto sourceDto) {
        if (sourceDto != null) {
            log.info("Evicting caches.");
            qxCacheManager.evictTradesCache();
            qxCacheManager.evictTradeCacheForAsset(sourceDto.issuer(), sourceDto.assetName());
            qxCacheManager.evictChartCachesForAsset(sourceDto.issuer(), sourceDto.assetName());
            qxCacheManager.evictTradeCacheForEntity(sourceDto.maker());
            qxCacheManager.evictTradeCacheForEntity(sourceDto.taker());
        }
    }

}
