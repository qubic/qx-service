package org.qubic.qx.api.scheduler.mapping;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.db.AssetsRepository;
import org.qubic.qx.api.db.EntitiesRepository;
import org.qubic.qx.api.db.TransactionsRepository;
import org.qubic.qx.api.db.domain.Asset;
import org.qubic.qx.api.db.domain.Entity;
import org.qubic.qx.api.db.domain.Transaction;
import org.qubic.qx.api.redis.dto.TradeRedisDto;

import java.util.Optional;

@Slf4j
public class DatabaseMappings {

    private final EntitiesRepository entitiesRepository;
    private final TransactionsRepository transactionsRepository;
    private final AssetsRepository assetsRepository;

    public DatabaseMappings(EntitiesRepository entitiesRepository, TransactionsRepository transactionsRepository, AssetsRepository assetsRepository) {
        this.entitiesRepository = entitiesRepository;
        this.transactionsRepository = transactionsRepository;
        this.assetsRepository = assetsRepository;
    }

    @EntityMapping
    public long mapToEntityId(String identity) {
        return entitiesRepository.findByIdentity(identity)
                .or(() -> createNewIdentity(identity))
                .map(Entity::getId)
                .orElseThrow();
    }

    @TransactionMapping
    public long mapHashToTransactionId(String transactionHash) {
        return transactionsRepository.findByHash(transactionHash)
                .map(Transaction::getId)
                .orElseThrow();
    }

    @AssetMapping
    public long mapTradeDtoToAssetId(TradeRedisDto dto) {
        return assetsRepository.findByIssuerAndName(dto.issuer(), dto.assetName())
                .or(() -> createNewAsset(dto))
                .map(Asset::getId)
                .orElseThrow();
    }

    private Optional<Asset> createNewAsset(TradeRedisDto dto) {
        log.warn("Creating new asset with issuer [{}] and name [{}].", dto.issuer(), dto.assetName());
        return Optional.of(assetsRepository.save(Asset.builder().issuer(dto.issuer()).name(dto.assetName()).verified(false).build()));
    }

    private Optional<Entity> createNewIdentity(String identity) {
        log.info("Creating new identity: {}", identity);
        return Optional.of(entitiesRepository.save(Entity.builder().identity(identity).build()));
    }

}
