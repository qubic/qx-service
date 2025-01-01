package org.qubic.qx.api.controller;

import jakarta.validation.constraints.Size;
import org.qubic.qx.api.controller.service.AssetOwnersService;
import org.qubic.qx.api.db.dto.AmountPerEntityDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.qubic.qx.api.redis.QxCacheManager.*;

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
    public List<AmountPerEntityDto> getTopAssetOwners(@PathVariable("issuer") @Size(min = 60, max = 60) String issuer,
                                                      @PathVariable("asset") @Size(min = 1, max = 7) String asset) {
        return assetOwnersService.getTopAssetOwners(issuer, asset);
    }

}
