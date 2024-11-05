package org.qubic.qx.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.controller.domain.TransactionDto;
import org.qubic.qx.api.controller.service.TransactionsService;
import org.qubic.qx.api.redis.QxCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest(properties = """
    spring.cache.type=redis
""")
class TransactionsControllerCacheIT extends AbstractSpringIntegrationTest {

    private static final String TEST_ISSUER = "ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGH";
    private static final String TEST_ASSET_NAME = "FOO";
    private static final String TEST_IDENTITY = "BCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHI";

    @Autowired
    private QxCacheManager qxCacheManager;

    @Autowired
    private TransactionsController controller;

    @MockBean
    private TransactionsService service;

    @Test
    void getTransferTransactions_thenHitCache() {
        TransactionDto dto = mock();
        when(service.getTransferTransactions()).thenReturn(List.of(dto));
        controller.getTransferTransactions();
        controller.getTransferTransactions();

        verify(service, times(1)).getTransferTransactions();
    }

    @Test
    void getTransferTransactions_givenCacheEvicted_thenHitService() {
        TransactionDto dto = mock();
        when(service.getTransferTransactions()).thenReturn(List.of(dto));
        controller.getTransferTransactions();
        qxCacheManager.evictTransferCache();
        controller.getTransferTransactions();

        verify(service, times(2)).getTransferTransactions();
    }

    @Test
    void getTransferTransactionsForAsset_thenHitCache() {
        TransactionDto dto = mock();
        when(service.getTransferTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME)).thenReturn(List.of(dto));
        controller.getTransferTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME);
        controller.getTransferTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME);

        verify(service, times(1)).getTransferTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME);
    }

    @Test
    void getTransferTransactionsForAsset_givenCacheEvicted_thenHitService() {
        TransactionDto dto = mock();
        when(service.getTransferTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME)).thenReturn(List.of(dto));
        controller.getTransferTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME);
        qxCacheManager.evictTransferCacheForAsset(TEST_ISSUER, TEST_ASSET_NAME);
        controller.getTransferTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME);

        verify(service, times(2)).getTransferTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME);
    }

    @Test
    void getTransferTransactionsForEntity_thenHitCache() {
        TransactionDto dto = mock();
        when(service.getTransferTransactionsForEntity(TEST_IDENTITY)).thenReturn(List.of(dto));
        controller.getTransferTransactionsForEntity(TEST_IDENTITY);
        controller.getTransferTransactionsForEntity(TEST_IDENTITY);

        verify(service, times(1)).getTransferTransactionsForEntity(TEST_IDENTITY);
    }

    @Test
    void getTransferTransactionsForEntity_givenCacheEvicted_thenHitService() {
        TransactionDto dto = mock();
        when(service.getTransferTransactionsForEntity(TEST_IDENTITY)).thenReturn(List.of(dto));
        controller.getTransferTransactionsForEntity(TEST_IDENTITY);
        qxCacheManager.evictTransferCacheForEntity(TEST_IDENTITY);
        controller.getTransferTransactionsForEntity(TEST_IDENTITY);

        verify(service, times(2)).getTransferTransactionsForEntity(TEST_IDENTITY);
    }

    @Test
    void getOrderTransactions_thenHitCache() {
        TransactionDto dto = mock();
        when(service.getOrderTransactions()).thenReturn(List.of(dto));

        controller.getOrderTransactions();
        controller.getOrderTransactions();

        verify(service, times(1)).getOrderTransactions();
    }

    @Test
    void getOrderTransactions_givenCacheEvicted_thenHitService() {
        TransactionDto dto = mock();
        when(service.getOrderTransactions()).thenReturn(List.of(dto));

        controller.getOrderTransactions();
        qxCacheManager.evictOrdersCache();
        controller.getOrderTransactions();

        verify(service, times(2)).getOrderTransactions();
    }

    @Test
    void getOrderTransactionsForAsset_thenHitCache() {
        TransactionDto dto = mock();
        when(service.getOrderTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME)).thenReturn(List.of(dto));

        controller.getOrderTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME);
        controller.getOrderTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME);

        verify(service, times(1)).getOrderTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME);
    }

    @Test
    void getOrderTransactionsForAsset_givenCacheEvicted_thenHitService() {
        TransactionDto dto = mock();
        when(service.getOrderTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME)).thenReturn(List.of(dto));

        controller.getOrderTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME);
        qxCacheManager.evictOrderCacheForAsset(TEST_ISSUER, TEST_ASSET_NAME);
        controller.getOrderTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME);

        verify(service, times(2)).getOrderTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME);
    }

    @Test
    void getOrderTransactionsForEntity_thenHitCache() {
        TransactionDto dto = mock();
        when(service.getOrderTransactionsForEntity(TEST_IDENTITY)).thenReturn(List.of(dto));

        controller.getOrderTransactionsForEntity(TEST_IDENTITY);
        controller.getOrderTransactionsForEntity(TEST_IDENTITY);

        verify(service, times(1)).getOrderTransactionsForEntity(TEST_IDENTITY);
    }

    @Test
    void getOrderTransactionsForEntity_givenCacheEvicted_thenHitService() {
        TransactionDto dto = mock();
        when(service.getOrderTransactionsForEntity(TEST_IDENTITY)).thenReturn(List.of(dto));

        controller.getOrderTransactionsForEntity(TEST_IDENTITY);
        qxCacheManager.evictOrderCacheForEntity(TEST_IDENTITY);
        controller.getOrderTransactionsForEntity(TEST_IDENTITY);

        verify(service, times(2)).getOrderTransactionsForEntity(TEST_IDENTITY);
    }

    @Test
    void getIssuedAssets_givenCached_thenHitCache() {
        TransactionDto dto = mock();
        when(service.getIssuedAssets()).thenReturn(List.of(dto));
        controller.getIssuedAssets();
        controller.getIssuedAssets();
        verify(service, times(1)).getIssuedAssets();
    }

    @Test
    void getIssuedAssets_givenCacheEvicted_thenHitServiceAgain() {
        TransactionDto dto = mock();
        when(service.getIssuedAssets()).thenReturn(List.of(dto));
        controller.getIssuedAssets();
        qxCacheManager.evictAssetsCaches();
        controller.getIssuedAssets();
        verify(service, times(2)).getIssuedAssets();
    }

    @BeforeEach
    void clearCache() {
        evictAllCaches();
    }

}