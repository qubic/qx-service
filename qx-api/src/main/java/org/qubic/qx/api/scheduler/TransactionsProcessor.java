package org.qubic.qx.api.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.db.AssetsRepository;
import org.qubic.qx.api.db.domain.*;
import org.qubic.qx.api.redis.QxCacheManager;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;
import org.qubic.qx.api.redis.repository.QueueProcessingRepository;
import org.qubic.qx.api.scheduler.mapping.RedisToDomainMapper;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

@Slf4j
public class TransactionsProcessor extends QueueProcessor<Transaction, TransactionRedisDto> {

    private final AssetsRepository assetsRepository;
    private final QxCacheManager qxCacheManager;

    public TransactionsProcessor(QueueProcessingRepository<TransactionRedisDto> redisRepository, CrudRepository<Transaction,
            Long> repository, RedisToDomainMapper<Transaction, TransactionRedisDto> mapper, AssetsRepository assetsRepository, QxCacheManager qxCacheManager) {
        super(redisRepository, repository, mapper);
        this.assetsRepository = assetsRepository;
        this.qxCacheManager = qxCacheManager;
    }

    @Override
    protected Optional<Transaction> mapAndSave(TransactionRedisDto sourceDto) {
        if (sourceDto.relevantEvents()) { // only relevant for qx orders
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

            createAssetIfNotExists(transferData.issuer(), transferData.name());
            log.info("Evicting transfer caches.");
            qxCacheManager.evictTransferCache();
            qxCacheManager.evictTransferCacheForAsset(transferData.issuer(), transferData.name());
            qxCacheManager.evictTransferCacheForEntity(sourceDto.sourcePublicId());
            qxCacheManager.evictTransferCacheForEntity(transferData.newOwner());

        } else if (extraData instanceof QxIssueAssetData issueData) {

            createAssetIfNotExists(sourceDto.sourcePublicId(), issueData.name());
            qxCacheManager.evictAssetsCaches();

        }
    }

    private void createAssetIfNotExists(String issuer, String assetName) {
        assetsRepository.findByIssuerAndName(issuer, assetName)
                .or(() -> createNewAsset(issuer, assetName))
                .map(Asset::getId)
                .orElseThrow();
    }

    private Optional<Asset> createNewAsset(String issuer, String assetName) {
        log.info("Creating new asset with issuer [{}] and name [{}].",issuer, assetName);
        return Optional.of(assetsRepository.save(Asset.builder().issuer(issuer).name(assetName).verified(false).build()));
    }

}