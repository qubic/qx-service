package org.qubic.qx.api.scheduler;

import at.qubic.api.crypto.IdentityUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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

    private final IdentityUtil identityUtil;
    private final AssetsRepository assetsRepository;
    private final QxCacheManager qxCacheManager;

    public TransactionsProcessor(QueueProcessingRepository<TransactionRedisDto> redisRepository, CrudRepository<Transaction, Long> repository, RedisToDomainMapper<Transaction, TransactionRedisDto> mapper, IdentityUtil identityUtil, AssetsRepository assetsRepository, QxCacheManager qxCacheManager) {
        super(redisRepository, repository, mapper);
        this.identityUtil = identityUtil;
        this.assetsRepository = assetsRepository;
        this.qxCacheManager = qxCacheManager;
    }

    @Override
    protected void postProcess(@NonNull Transaction targetDto, @NonNull TransactionRedisDto sourceDto) {
        ExtraData extraData = sourceDto.extraData();
        if (extraData instanceof QxAssetOrderData orderData) {
            // Attention: we don't add assets for simple orders currently.
            log.info("Evicting order caches.");
            qxCacheManager.evictOrdersCache();
            qxCacheManager.evictOrderCacheForAsset(orderData.issuer(), orderData.name());
            qxCacheManager.evictOrderCacheForEntity(sourceDto.sourcePublicId());
        } else if (extraData instanceof QxTransferAssetData transferData) {
            createAssetIfItDoesNotExist(transferData.issuer(), transferData.name(), sourceDto.moneyFlew());
            log.info("Evicting transfer caches.");
            qxCacheManager.evictTransferCache();
            qxCacheManager.evictTransferCacheForAsset(transferData.issuer(), transferData.name());
            qxCacheManager.evictTransferCacheForEntity(sourceDto.sourcePublicId());
            qxCacheManager.evictTransferCacheForEntity(transferData.newOwner());
        } else if (extraData instanceof QxIssueAssetData issueAssetData) {
            createAssetIfItDoesNotExist(sourceDto.sourcePublicId(), issueAssetData.name(), sourceDto.moneyFlew());
            qxCacheManager.evictIssuedAssetsCache();
        }
    }

    private void createAssetIfItDoesNotExist(String issuer, String name, Boolean moneyFlew) {
        try {
            if (BooleanUtils.isTrue(moneyFlew)
                    && identityUtil.isValidIdentity(issuer)
                    && StringUtils.isNotBlank(name)
                    && StringUtils.length(name) < 8) {
                assetsRepository.findByIssuerAndName(issuer, name)
                        .or(() -> createNewAsset(issuer, name));
            }
        } catch (Exception e) {
            String message = String.format("Error verifying asset information issuer [%s] and name [%s].", issuer, name);
            log.error(message, e);
        }
    }

    private Optional<Asset> createNewAsset(String issuer, String name) {
        log.warn("Creating new asset with issuer [{}] and name [{}].", issuer, name);
        return Optional.of(assetsRepository.save(Asset.builder()
                .issuer(issuer)
                .name(name)
                .verified(false)
                .build()));
    }

}