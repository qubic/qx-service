package org.qubic.qx.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.db.dto.TransactionDto;
import org.qubic.qx.api.controller.service.TransactionsService;
import org.qubic.qx.api.redis.QxCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest(properties = """
    spring.cache.type=redis
""")
class TransactionsControllerCacheIT extends AbstractSpringIntegrationTest {

    private static final String TEST_ISSUER = "ISSUERISSUERISSUERISSUERISSUERISSUERISSUERISSUERISSUERISPXHC";
    private static final String TEST_ASSET_NAME = "FOO";
    private static final String TEST_IDENTITY = "BCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEOPXN";

    @Autowired
    private QxCacheManager qxCacheManager;

    @Autowired
    private TransactionsController controller;

    @MockitoBean
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