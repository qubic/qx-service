package org.qubic.qx.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.db.dto.TransactionDto;
import org.qubic.qx.api.controller.service.TransactionsService;
import org.qubic.qx.api.redis.QxCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        Pageable pageable = PageRequest.of(0, 50);
        when(service.getTransferTransactions(any(Pageable.class))).thenReturn(List.of(dto));
        controller.getTransferTransactions(pageable);
        controller.getTransferTransactions(pageable);

        verify(service, times(1)).getTransferTransactions(any(Pageable.class));
    }

    @Test
    void getTransferTransactions_givenCacheEvicted_thenHitService() {
        TransactionDto dto = mock();
        Pageable pageable = PageRequest.of(0, 50);
        when(service.getTransferTransactions(any(Pageable.class))).thenReturn(List.of(dto));
        controller.getTransferTransactions(pageable);
        qxCacheManager.evictTransferCache();
        controller.getTransferTransactions(pageable);

        verify(service, times(2)).getTransferTransactions(any(Pageable.class));
    }

    @Test
    void getTransferTransactionsForAsset_thenHitCache() {
        TransactionDto dto = mock();
        Pageable pageable = PageRequest.of(0, 50);
        when(service.getTransferTransactionsForAsset(eq(TEST_ISSUER), eq(TEST_ASSET_NAME), any(Pageable.class))).thenReturn(List.of(dto));
        controller.getTransferTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME, pageable);
        controller.getTransferTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME, pageable);

        verify(service, times(1)).getTransferTransactionsForAsset(eq(TEST_ISSUER), eq(TEST_ASSET_NAME), any(Pageable.class));
    }

    @Test
    void getTransferTransactionsForAsset_givenCacheEvicted_thenHitService() {
        TransactionDto dto = mock();
        Pageable pageable = PageRequest.of(0, 50);
        when(service.getTransferTransactionsForAsset(eq(TEST_ISSUER), eq(TEST_ASSET_NAME), any(Pageable.class))).thenReturn(List.of(dto));
        controller.getTransferTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME, pageable);
        qxCacheManager.evictTransferCacheForAsset(TEST_ISSUER, TEST_ASSET_NAME);
        controller.getTransferTransactionsForAsset(TEST_ISSUER, TEST_ASSET_NAME, pageable);

        verify(service, times(2)).getTransferTransactionsForAsset(eq(TEST_ISSUER), eq(TEST_ASSET_NAME), any(Pageable.class));
    }

    @Test
    void getTransferTransactionsForEntity_thenHitCache() {
        TransactionDto dto = mock();
        Pageable pageable = PageRequest.of(0, 50);
        when(service.getTransferTransactionsForEntity(eq(TEST_IDENTITY), any(Pageable.class))).thenReturn(List.of(dto));
        controller.getTransferTransactionsForEntity(TEST_IDENTITY, pageable);
        controller.getTransferTransactionsForEntity(TEST_IDENTITY, pageable);

        verify(service, times(1)).getTransferTransactionsForEntity(eq(TEST_IDENTITY), any(Pageable.class));
    }

    @Test
    void getTransferTransactionsForEntity_givenCacheEvicted_thenHitService() {
        TransactionDto dto = mock();
        Pageable pageable = PageRequest.of(0, 50);
        when(service.getTransferTransactionsForEntity(eq(TEST_IDENTITY), any(Pageable.class))).thenReturn(List.of(dto));
        controller.getTransferTransactionsForEntity(TEST_IDENTITY, pageable);
        qxCacheManager.evictTransferCacheForEntity(TEST_IDENTITY);
        controller.getTransferTransactionsForEntity(TEST_IDENTITY, pageable);

        verify(service, times(2)).getTransferTransactionsForEntity(eq(TEST_IDENTITY), any(Pageable.class));
    }

    @Test
    void getIssuedAssets_givenCached_thenHitCache() {
        TransactionDto dto = mock();
        Pageable pageable = PageRequest.of(0, 50);
        when(service.getIssuedAssets(any(Pageable.class))).thenReturn(List.of(dto));
        controller.getIssuedAssets(pageable);
        controller.getIssuedAssets(pageable);
        verify(service, times(1)).getIssuedAssets(any(Pageable.class));
    }

    @Test
    void getIssuedAssets_givenCacheEvicted_thenHitServiceAgain() {
        TransactionDto dto = mock();
        Pageable pageable = PageRequest.of(0, 50);
        when(service.getIssuedAssets(any(Pageable.class))).thenReturn(List.of(dto));
        controller.getIssuedAssets(pageable);
        qxCacheManager.evictAssetsCaches();
        controller.getIssuedAssets(pageable);
        verify(service, times(2)).getIssuedAssets(any(Pageable.class));
    }

    @BeforeEach
    void clearCache() {
        evictAllCaches();
    }

}
