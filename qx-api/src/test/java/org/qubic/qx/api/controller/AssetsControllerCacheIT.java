package org.qubic.qx.api.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.db.AssetsRepository;
import org.qubic.qx.api.db.domain.Asset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.qubic.qx.api.redis.QxCacheManager.CACHE_NAME_ASSETS;
import static org.qubic.qx.api.redis.QxCacheManager.CACHE_NAME_ASSETS_VERIFIED;

@SpringBootTest(properties = """
    spring.cache.type=redis
""")
class AssetsControllerCacheIT extends AbstractSpringIntegrationTest {

    @Autowired
    private AssetsController controller;

    @MockitoBean
    private AssetsRepository repository;

    @Test
    void getAssets_givenAll_thenCache() {
        Asset asset1 = Asset.builder()
                .id(42L)
                .issuer("ISSUER1")
                .name("NAME1")
                .build();

        when(repository.findAll()).thenReturn(List.of(asset1));

        List<Asset> result1 = controller.getAssets(true);
        List<Asset> result2 = controller.getAssets(true);

        verify(repository, times(1)).findAll();

        assertThat(result1).contains(asset1);
        assertThat(result1).isEqualTo(result2);
    }

    @Test
    void getAssets_givenVerifiedOnly_thenCache() {
        Asset asset1 = Asset.builder()
                .id(42L)
                .issuer("ISSUER1")
                .name("NAME1")
                .build();

        when(repository.findByVerifiedIsTrue()).thenReturn(List.of(asset1));

        List<Asset> result1 = controller.getAssets(false);
        List<Asset> result2 = controller.getAssets(false);

        verify(repository, times(1)).findByVerifiedIsTrue();

        assertThat(result1).contains(asset1);
        assertThat(result1).isEqualTo(result2);
    }

    @BeforeEach
    @AfterEach
    void clearCache() {
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ASSETS)).clear();
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ASSETS_VERIFIED)).clear();
    }

}