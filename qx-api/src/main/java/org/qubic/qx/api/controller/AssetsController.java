package org.qubic.qx.api.controller;

import org.qubic.qx.api.controller.service.AssetsService;
import org.qubic.qx.api.db.domain.Asset;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.qubic.qx.api.redis.QxCacheManager.CACHE_NAME_ASSETS;

@CrossOrigin
@RestController
@RequestMapping("/service/v1/qx")
public class AssetsController {

    private final AssetsService assetsService;

    public AssetsController(AssetsService assetsService) {
        this.assetsService = assetsService;
    }

    @Cacheable(CACHE_NAME_ASSETS)
    @GetMapping("/assets")
    public List<Asset> getAssets() {
        return assetsService.getVerifiedAssets();
    }

}

