package org.qubic.qx.api.controller.service;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.db.AssetsRepository;
import org.qubic.qx.api.db.domain.Asset;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

import static org.qubic.qx.api.redis.QxCacheManager.CACHE_NAME_ASSETS;
import static org.qubic.qx.api.redis.QxCacheManager.CACHE_NAME_ASSETS_VERIFIED;

@Slf4j
public class AssetsService {

    private final AssetsRepository assetsRepository;

    public AssetsService(AssetsRepository assetsRepository) {
        this.assetsRepository = assetsRepository;
    }

    @Cacheable(CACHE_NAME_ASSETS_VERIFIED)
    public List<Asset> getVerifiedAssets() {
        return assetsRepository.findByVerifiedIsTrue().stream().toList();
    }

    @Cacheable(CACHE_NAME_ASSETS)
    public List<Asset> getAllAssets() {
        return assetsRepository.findAll().stream().toList();
    }

}
