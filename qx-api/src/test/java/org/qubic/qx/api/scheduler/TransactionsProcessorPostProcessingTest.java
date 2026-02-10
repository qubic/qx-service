package org.qubic.qx.api.scheduler;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.AssetsRepository;
import org.qubic.qx.api.db.domain.Asset;
import org.qubic.qx.api.db.domain.QxAssetOrderData;
import org.qubic.qx.api.db.domain.QxIssueAssetData;
import org.qubic.qx.api.db.domain.QxTransferAssetData;
import org.qubic.qx.api.redis.QxCacheManager;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;

import java.util.Optional;

import static org.mockito.Mockito.*;

class TransactionsProcessorPostProcessingTest {

    private final AssetsRepository assetsRepository = mock();
    private final QxCacheManager qxCacheManager = mock();
    private final TransactionsProcessor processor = new TransactionsProcessor(null, null, null,
            assetsRepository, qxCacheManager);

    @Test
    void postProcess_givenAssetOrderData_thenEvictOrderCaches() {
        TransactionRedisDto source = mock();
        QxAssetOrderData orderData = mock();
        when(source.extraData()).thenReturn(orderData);
        when(orderData.issuer()).thenReturn("ISSUER");
        when(orderData.name()).thenReturn("ASSET");
        when(source.sourcePublicId()).thenReturn("SOURCE_IDENTITY");

        processor.postProcess(source);

        qxCacheManager.evictOrderCacheForAsset("ISSUER", "ASSET");
        qxCacheManager.evictOrderCacheForEntity("SOURCE_IDENTITY");
    }

    @Test
    void postProcess_givenTransferAssetData_thenEvictTransferCaches() {
        when(assetsRepository.findByIssuerAndName("ISSUER", "ASSET")).thenReturn(Optional.of(Asset.builder().id(42L).build()));
        TransactionRedisDto source = mock();
        QxTransferAssetData transferAssetData = new QxTransferAssetData(
          "ISSUER", "ASSET", "NEW_OWNER", 42
        );
        when(source.extraData()).thenReturn(transferAssetData);
        when(source.sourcePublicId()).thenReturn("SOURCE_IDENTITY");

        processor.postProcess(source);

        qxCacheManager.evictAssetTransferCache();
        qxCacheManager.evictAssetTransferCacheForAsset("ISSUER", "ASSET");
        qxCacheManager.evictAssetTransferCacheForEntity("SOURCE_IDENTITY");
        qxCacheManager.evictAssetTransferCacheForEntity("NEW_OWNER");
    }

    @Test
    void postProcess_givenTransferWithMissingAsset_thenCreateAsset() {
        when(assetsRepository.save(any())).thenReturn(Asset.builder().id(42L).build()); // ID is needed
        when(assetsRepository.findByIssuerAndName("ISSUER", "ASSET")).thenReturn(Optional.empty());
        TransactionRedisDto source = mock();
        QxTransferAssetData transferAssetData = new QxTransferAssetData(
                "ISSUER", "ASSET", "NEW_OWNER", 42
        );
        when(source.extraData()).thenReturn(transferAssetData);
        when(source.sourcePublicId()).thenReturn("SOURCE_IDENTITY");

        processor.postProcess(source);
        verify(assetsRepository).save(Asset.builder().issuer("ISSUER").name("ASSET").build());
    }


    @Test
    void postProcess_givenIssueAsset_thenCreateAsset() {
        when(assetsRepository.save(any())).thenReturn(Asset.builder().id(42L).build()); // ID is needed
        when(assetsRepository.findByIssuerAndName("ISSUER", "ISSUED")).thenReturn(Optional.empty());
        TransactionRedisDto source = createIssueAssetTransaction();
        processor.postProcess(source);
        verify(assetsRepository).save(Asset.builder().issuer("ISSUER").name("ISSUED").build());
    }

    @Test
    void postProcess_givenIssueAsset_thenEvictCache() {
        when(assetsRepository.findByIssuerAndName("ISSUER", "ISSUED")).thenReturn(Optional.of(Asset.builder().id(42L).build()));
        TransactionRedisDto source = createIssueAssetTransaction();
        processor.postProcess(source);
        verify(qxCacheManager).evictAssetsCaches();
    }

    private static TransactionRedisDto createIssueAssetTransaction() {
        TransactionRedisDto source = mock();
        QxIssueAssetData issueAsset = new QxIssueAssetData(
                "ISSUED", 42, "0000200", (byte) 10
        );
        when(source.extraData()).thenReturn(issueAsset);
        when(source.sourcePublicId()).thenReturn("ISSUER");
        return source;
    }

}