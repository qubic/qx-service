package org.qubic.qx.api.controller.service;

import org.qubic.qx.api.db.AssetsRepository;
import org.qubic.qx.api.db.domain.Asset;

import java.util.List;

public class AssetsService {

    private final AssetsRepository assetsRepository;

    public AssetsService(AssetsRepository assetsRepository) {
        this.assetsRepository = assetsRepository;
    }

    public List<Asset> getAssets() {
        return assetsRepository.findByVerifiedIsTrue().stream()
                .toList();
    }

}
