package org.qubic.qx.api.controller.service;

import org.qubic.qx.api.db.AssetOwnersRepository;
import org.qubic.qx.api.db.dto.AmountPerEntityDto;

import java.util.List;

public class AssetOwnersService {

    private final AssetOwnersRepository assetOwnersRepository;

    public AssetOwnersService(AssetOwnersRepository assetOwnersRepository) {
        this.assetOwnersRepository = assetOwnersRepository;
    }

    public List<AmountPerEntityDto> getTopAssetOwners(String issuer, String assetName) {
        return assetOwnersRepository.findOwnersByAsset(issuer, assetName, 1000);
    }

}
