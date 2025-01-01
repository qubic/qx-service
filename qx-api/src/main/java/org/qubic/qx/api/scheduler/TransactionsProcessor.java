package org.qubic.qx.api.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.db.domain.*;
import org.qubic.qx.api.redis.QxCacheManager;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;
import org.qubic.qx.api.redis.repository.QueueProcessingRepository;
import org.qubic.qx.api.richlist.TransferAssetService;
import org.qubic.qx.api.scheduler.mapping.RedisToDomainMapper;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

@Slf4j
public class TransactionsProcessor extends QueueProcessor<Transaction, TransactionRedisDto> {

    private final TransferAssetService transferAssetService;
    private final QxCacheManager qxCacheManager;

    public TransactionsProcessor(QueueProcessingRepository<TransactionRedisDto> redisRepository, CrudRepository<Transaction, Long> repository, RedisToDomainMapper<Transaction, TransactionRedisDto> mapper, TransferAssetService transferAssetService, QxCacheManager qxCacheManager) {
        super(redisRepository, repository, mapper);
        this.transferAssetService = transferAssetService;
        this.qxCacheManager = qxCacheManager;
    }

    @Override
    protected Optional<Transaction> mapAndSave(TransactionRedisDto sourceDto) {
        if (sourceDto.relevantEvents()) {
            return super.mapAndSave(sourceDto);
        } else {
            log.info("Not storing transaction [{}]. No relevant events.", sourceDto.transactionHash());
            return Optional.empty();
        }
    }

    @Override
    protected void postProcess(TransactionRedisDto sourceDto) {
        ExtraData extraData = sourceDto.extraData();
        if (extraData instanceof QxAssetOrderData orderData) {

            // we ignore unknown assets here
            log.info("Evicting order caches.");
            qxCacheManager.evictOrderCacheForAsset(orderData.issuer(), orderData.name());
            qxCacheManager.evictOrderCacheForEntity(sourceDto.sourcePublicId());

        } else if (extraData instanceof QxTransferAssetData transferData) {

            String issuer = transferData.issuer();
            String assetName = transferData.name();
            String from = sourceDto.sourcePublicId();
            String to = transferData.newOwner();
            transferAssetService.transfer(from, to, issuer, assetName, transferData.numberOfShares());

            log.info("Evicting transfer caches.");
            qxCacheManager.evictTransferCache();
            qxCacheManager.evictTransferCacheForAsset(issuer, assetName);
            qxCacheManager.evictTransferCacheForEntity(from);
            qxCacheManager.evictTransferCacheForEntity(to);

        } else if (extraData instanceof QxIssueAssetData issueAssetData) {

            String issuer = sourceDto.sourcePublicId();
            transferAssetService.issueAsset(issuer, issueAssetData.name(), issueAssetData.numberOfShares());
            qxCacheManager.evictAssetsCaches();

        }
    }

}