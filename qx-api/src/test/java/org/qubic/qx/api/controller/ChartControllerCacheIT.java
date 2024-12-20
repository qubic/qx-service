package org.qubic.qx.api.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.controller.service.ChartService;
import org.qubic.qx.api.redis.QxCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.*;

@SpringBootTest(properties = """
    spring.cache.type=redis
""")
class ChartControllerCacheIT extends AbstractSpringIntegrationTest {

    private static final String ISSUER = "ISSUERISSUERISSUERISSUERISSUERISSUERISSUERISSUERISSUERISSUER";

    @MockitoBean
    private ChartService chartService;

    @Autowired
    private ChartController controller;

    @Autowired
    private QxCacheManager qxCacheManager;

    @Test
    void getAveragePriceForAsset_thenChache() {
        controller.getAveragePriceForAsset(ISSUER, "ASSET");
        controller.getAveragePriceForAsset(ISSUER, "ASSET");
        controller.getAveragePriceForAsset(ISSUER, "ASSET");

        verify(chartService, times(1)).getAveragePriceForAsset(any(), any());
    }

    @Test
    void getAveragePriceForAsset_givenCacheEvicted_thenHitServiceAgain() {
        controller.getAveragePriceForAsset(ISSUER, "ASSET");
        controller.getAveragePriceForAsset(ISSUER, "ASSET");
        qxCacheManager.evictChartCachesForAsset(ISSUER, "ASSET");
        controller.getAveragePriceForAsset(ISSUER, "ASSET");

        verify(chartService, times(2)).getAveragePriceForAsset(any(), any());
    }

    @AfterEach
    void clearCache() {
        evictAllCaches();
    }
  
}