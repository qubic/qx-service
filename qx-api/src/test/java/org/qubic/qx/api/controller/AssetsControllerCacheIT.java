package org.qubic.qx.api.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.controller.service.AssetsService;
import org.qubic.qx.api.db.domain.Asset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.qubic.qx.api.redis.QxCacheManager.CACHE_NAME_ASSETS;

@SpringBootTest(properties = """
    spring.cache.type=redis
""")
class AssetsControllerCacheIT extends AbstractSpringIntegrationTest {

    @MockitoBean
    private AssetsService assetsService;

    @Autowired
    private AssetsController controller;

    @Test
    void getAssets_thenCache() {
        Asset asset1 = Asset.builder()
                .id(42L)
                .issuer("ISSUER1")
                .name("NAME1")
                .build();

        when(assetsService.getVerifiedAssets()).thenReturn(List.of(asset1));

        List<Asset> result1 = controller.getAssets();
        List<Asset> result2 = controller.getAssets();

        verify(assetsService, times(1)).getVerifiedAssets();

        assertThat(result1).contains(asset1);
        assertThat(result1).isEqualTo(result2);
    }

    @BeforeEach
    @AfterEach
    void clearCache() {
        Objects.requireNonNull(cacheManager.getCache(CACHE_NAME_ASSETS)).clear();
    }

}