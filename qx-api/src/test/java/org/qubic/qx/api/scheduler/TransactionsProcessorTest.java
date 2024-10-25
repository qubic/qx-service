package org.qubic.qx.api.scheduler;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.domain.QxAssetOrderData;
import org.qubic.qx.api.db.domain.QxTransferAssetData;
import org.qubic.qx.api.redis.QxCacheManager;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;

import static org.mockito.Mockito.*;

class TransactionsProcessorTest {

    private final QxCacheManager qxCacheManager = mock();
    private final TransactionsProcessor processor = new TransactionsProcessor(null, null, null, qxCacheManager);

    @Test
    void postProcess_givenAssetOrderData_thenEvictOrderCaches() {
        TransactionRedisDto source = mock();
        QxAssetOrderData orderData = mock();
        when(source.extraData()).thenReturn(orderData);
        when(orderData.issuer()).thenReturn("ISSUER");
        when(orderData.name()).thenReturn("ASSET_NAME");
        when(source.sourcePublicId()).thenReturn("SOURCE_IDENTITY");

        processor.postProcess(mock(), source);

        qxCacheManager.evictOrdersCache();
        qxCacheManager.evictOrderCacheForAsset("ISSUER", "ASSET_NAME");
        qxCacheManager.evictOrderCacheForEntity("SOURCE_IDENTITY");
    }

    @Test
    void postProcess_givenTransferAssetData_thenEvictTransferCaches() {
        TransactionRedisDto source = mock();
        QxTransferAssetData transferAssetData = mock();
        when(source.extraData()).thenReturn(transferAssetData);
        when(transferAssetData.issuer()).thenReturn("ISSUER");
        when(transferAssetData.name()).thenReturn("ASSET_NAME");
        when(source.sourcePublicId()).thenReturn("SOURCE_IDENTITY");
        when(transferAssetData.newOwner()).thenReturn("NEW_OWNER");

        processor.postProcess(mock(), source);

        qxCacheManager.evictTransferCache();
        qxCacheManager.evictTransferCacheForAsset("ISSUER", "ASSET_NAME");
        qxCacheManager.evictTransferCacheForEntity("SOURCE_IDENTITY");
        qxCacheManager.evictTransferCacheForEntity("NEW_OWNER");
    }

}