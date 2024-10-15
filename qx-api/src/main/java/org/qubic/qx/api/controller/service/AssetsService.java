package org.qubic.qx.api.controller.service;

import org.qubic.qx.api.controller.domain.Asset;
import org.qubic.qx.api.controller.mapping.AssetsMapper;
import org.qubic.qx.api.db.AssetsRepository;

import java.util.List;

public class AssetsService {

    private final AssetsRepository assetsRepository;
    private final AssetsMapper assetsMapper;

    public AssetsService(AssetsRepository assetsRepository, AssetsMapper assetsMapper) {
        this.assetsRepository = assetsRepository;
        this.assetsMapper = assetsMapper;
    }

    public List<Asset> getAssets() {
        return assetsRepository.findByVerifiedIsTrue().stream()
                .map(assetsMapper::map)
                .toList();
    }

}
