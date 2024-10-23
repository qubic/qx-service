package org.qubic.qx.api.scheduler;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.domain.QxAssetOrderData;
import org.qubic.qx.api.redis.QxCacheManager;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;

import static org.mockito.Mockito.*;

class TransactionsProcessorTest {

    private final QxCacheManager qxCacheManager = mock();
    private final TransactionsProcessor processor = new TransactionsProcessor(null, null, null, qxCacheManager);

    @Test
    void postProcess() {
        TransactionRedisDto source = mock();
        QxAssetOrderData orderData = mock();
        when(source.extraData()).thenReturn(orderData);
        when(orderData.issuer()).thenReturn("ISSUER");
        when(orderData.name()).thenReturn("ASSET_NAME");
        when(source.sourcePublicId()).thenReturn("SOURCE_IDENTITY");

        processor.postProcess(mock(), source);

        qxCacheManager.evictOrderCachesForAsset("ISSUER", "ASSET_NAME");
        qxCacheManager.evictOrderCachesForEntity("SOURCE_IDENTITY");
    }

}