package org.qubic.qx.api.controller;

import org.qubic.qx.api.controller.service.AssetOwnersService;
import org.qubic.qx.api.db.dto.AmountPerEntityDto;
import org.qubic.qx.api.validation.AssetName;
import org.qubic.qx.api.validation.Identity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.qubic.qx.api.redis.QxCacheManager.CACHE_KEY_ASSET;
import static org.qubic.qx.api.redis.QxCacheManager.CACHE_NAME_ASSET_OWNERS;

@CrossOrigin
@Validated
@RestController
@RequestMapping("/service/v1/qx")
public class AssetOwnersController {

    private final AssetOwnersService assetOwnersService;

    public AssetOwnersController(AssetOwnersService assetOwnersService) {
        this.assetOwnersService = assetOwnersService;
    }

    @Cacheable(cacheNames = CACHE_NAME_ASSET_OWNERS, key = CACHE_KEY_ASSET)
    @GetMapping("/issuer/{issuer}/asset/{asset}/owners")
    public List<AmountPerEntityDto> getTopAssetOwners(@PathVariable("issuer") @Identity String issuer,
                                                      @PathVariable("asset") @AssetName String asset) {
        return assetOwnersService.getTopAssetOwners(issuer, asset);
    }

}
