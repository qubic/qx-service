package org.qubic.qx.api.scheduler;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.domain.QxAssetOrderData;
import org.qubic.qx.api.db.domain.QxIssueAssetData;
import org.qubic.qx.api.db.domain.QxTransferAssetData;
import org.qubic.qx.api.redis.QxCacheManager;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;

import static org.mockito.Mockito.*;

class TransactionsProcessorPostProcessingTest {

    private final QxCacheManager qxCacheManager = mock();
    private final TransactionsProcessor processor = new TransactionsProcessor(null, null, null,
            qxCacheManager);

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