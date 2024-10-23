package org.qubic.qx.api.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.controller.service.AssetsService;
import org.qubic.qx.api.db.domain.Asset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = """
    spring.cache.type=redis
""")
class AssetsControllerCacheIT extends AbstractSpringIntegrationTest {

    @MockBean
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

        when(assetsService.getAssets()).thenReturn(List.of(asset1));

        List<Asset> result1 = controller.getAssets();
        List<Asset> result2 = controller.getAssets();

        verify(assetsService, times(1)).getAssets();

        assertThat(result1).contains(asset1);
        assertThat(result1).isEqualTo(result2);
    }

    @AfterEach
    void clearCache() {
        evictAllCaches();
    }

}