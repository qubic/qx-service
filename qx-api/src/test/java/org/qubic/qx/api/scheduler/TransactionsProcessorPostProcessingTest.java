package org.qubic.qx.api.scheduler;

import at.qubic.api.crypto.IdentityUtil;
import org.junit.jupiter.api.Assertions;
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

    private final IdentityUtil identityUtil = mock();
    private final AssetsRepository assetsRepository = mock();
    private final QxCacheManager qxCacheManager = mock();
    private final TransactionsProcessor processor = new TransactionsProcessor(null, null, null,
            identityUtil, assetsRepository, qxCacheManager);

    @Test
    void postProcess_givenAssetOrderData_thenEvictOrderCaches() {
        TransactionRedisDto source = mock();
        QxAssetOrderData orderData = mock();
        when(source.extraData()).thenReturn(orderData);
        when(orderData.issuer()).thenReturn("ISSUER");
        when(orderData.name()).thenReturn("ASSET");
        when(source.sourcePublicId()).thenReturn("SOURCE_IDENTITY");

        processor.postProcess(source);

        qxCacheManager.evictOrdersCache();
        qxCacheManager.evictOrderCacheForAsset("ISSUER", "ASSET");
        qxCacheManager.evictOrderCacheForEntity("SOURCE_IDENTITY");
    }

    @Test
    void postProcess_givenTransferAssetData_thenEvictTransferCaches() {
        TransactionRedisDto source = mock();
        QxTransferAssetData transferAssetData = new QxTransferAssetData(
          "ISSUER", "ASSET", "NEW_OWNER", 42
        );
        when(source.extraData()).thenReturn(transferAssetData);
        when(source.sourcePublicId()).thenReturn("SOURCE_IDENTITY");

        processor.postProcess(source);

        qxCacheManager.evictTransferCache();
        qxCacheManager.evictTransferCacheForAsset("ISSUER", "ASSET");
        qxCacheManager.evictTransferCacheForEntity("SOURCE_IDENTITY");
        qxCacheManager.evictTransferCacheForEntity("NEW_OWNER");
    }

    @Test
    void postProcess_givenAssetExists_thenDoNothing() {
        TransactionRedisDto source = mock();
        QxTransferAssetData transferAssetData = new QxTransferAssetData(
                "ISSUER", "ASSET", "NEW_OWNER", 42
        );
        when(source.extraData()).thenReturn(transferAssetData);
        when(identityUtil.isValidIdentity(anyString())).thenReturn(true);
        when(assetsRepository.findByIssuerAndName("ISSUER", "ASSET")).thenReturn(Optional.of(mock()));

        processor.postProcess(source);
        verify(assetsRepository, never()).save(any(Asset.class));
    }

    @Test
    void postProcess_givenTransferWithNewAsset_thenCreate() {
        TransactionRedisDto source = mock();
        QxTransferAssetData transferAssetData = new QxTransferAssetData(
                "ISSUER", "ASSET", "NEW_OWNER", 42
        );
        when(source.extraData()).thenReturn(transferAssetData);
        when(identityUtil.isValidIdentity(anyString())).thenReturn(true);
        when(assetsRepository.save(any(Asset.class))).then(args -> args.getArgument(0));

        processor.postProcess(source);
        verify(assetsRepository).save(Asset.builder().issuer("ISSUER").name("ASSET").verified(false).build());
    }

    @Test
    void postProcess_givenInvalidAsset_thenDoNotCreate() {
        TransactionRedisDto source = mock();
        QxTransferAssetData transferAssetData = new QxTransferAssetData(
                "ISSUER", "ASSET_NAME_TOO_LONG", "NEW_OWNER", 42
        );
        when(source.extraData()).thenReturn(transferAssetData);
        processor.postProcess(source);
        verifyNoInteractions(assetsRepository);
    }

    @Test
    void postProcess_givenErrorOnAssetCheck_thenDoNotThrow() {
        TransactionRedisDto source = mock();
        QxTransferAssetData transferAssetData = new QxTransferAssetData(
                "ISSUER", "ASSET", "NEW_OWNER", 42
        );
        when(source.extraData()).thenReturn(transferAssetData);
        when(identityUtil.isValidIdentity(anyString())).thenThrow(new RuntimeException("test"));

        Assertions.assertDoesNotThrow(() -> processor.postProcess(source));
        verifyNoInteractions(assetsRepository);
    }


    @Test
    void postProcess_givenIssueAsset_thenCreate() {
        TransactionRedisDto source = createIssueAssetTransaction();
        when(identityUtil.isValidIdentity(anyString())).thenReturn(true);
        when(assetsRepository.save(any(Asset.class))).then(args -> args.getArgument(0));

        processor.postProcess(source);
        verify(assetsRepository).save(Asset.builder().issuer("ISSUER").name("NAME").verified(false).build());
    }

    @Test
    void postProcess_givenIssueAsset_thenEvictCache() {
        TransactionRedisDto source = createIssueAssetTransaction();
        processor.postProcess(source);
        verify(qxCacheManager).evictAssetsCaches();
    }

    private static TransactionRedisDto createIssueAssetTransaction() {
        TransactionRedisDto source = mock();
        QxIssueAssetData issueAsset = new QxIssueAssetData(
                "NAME", 42, "0000200", (byte) 10
        );
        when(source.extraData()).thenReturn(issueAsset);
        when(source.sourcePublicId()).thenReturn("ISSUER");
        return source;
    }

}