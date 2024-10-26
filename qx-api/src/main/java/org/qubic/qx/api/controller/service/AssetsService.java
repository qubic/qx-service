package org.qubic.qx.api.controller.service;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.db.AssetsRepository;
import org.qubic.qx.api.db.domain.Asset;

import java.util.List;

@Slf4j
public class AssetsService {

    private final AssetsRepository assetsRepository;

    public AssetsService(AssetsRepository assetsRepository) {
        this.assetsRepository = assetsRepository;
    }

    public List<Asset> getVerifiedAssets() {
        return assetsRepository.findByVerifiedIsTrue().stream()
                .toList();
    }

}
