package org.qubic.qx.api.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.db.domain.ExtraData;
import org.qubic.qx.api.db.domain.QxAssetOrderData;
import org.qubic.qx.api.db.domain.QxTransferAssetData;
import org.qubic.qx.api.db.domain.Transaction;
import org.qubic.qx.api.redis.QxCacheManager;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;
import org.qubic.qx.api.redis.repository.QueueProcessingRepository;
import org.qubic.qx.api.scheduler.mapping.RedisToDomainMapper;
import org.springframework.data.repository.CrudRepository;

@Slf4j
public class TransactionsProcessor extends QueueProcessor<Transaction, TransactionRedisDto> {

    private final  QxCacheManager qxCacheManager;

    public TransactionsProcessor(QueueProcessingRepository<TransactionRedisDto> redisRepository, CrudRepository<Transaction, Long> repository, RedisToDomainMapper<Transaction, TransactionRedisDto> mapper, QxCacheManager qxCacheManager) {
        super(redisRepository, repository, mapper);
        this.qxCacheManager = qxCacheManager;
    }

    @Override
    protected void postProcess(Transaction targetDto, TransactionRedisDto sourceDto) {
        if (targetDto != null && sourceDto != null) {
            ExtraData extraData = sourceDto.extraData();
            if (extraData instanceof QxAssetOrderData orderData) {
                log.info("Evicting caches.");
                qxCacheManager.evictOrdersCache();
                qxCacheManager.evictOrderCacheForAsset(orderData.issuer(), orderData.name());
                qxCacheManager.evictOrderCacheForEntity(sourceDto.sourcePublicId());
            } else if (extraData instanceof QxTransferAssetData transferData) {
                qxCacheManager.evictTransferCache();
                qxCacheManager.evictTransferCacheForAsset(transferData.issuer(), transferData.name());
                qxCacheManager.evictTransferCacheForEntity(sourceDto.sourcePublicId());
                qxCacheManager.evictTransferCacheForEntity(transferData.newOwner());

            }
        }
    }
}
