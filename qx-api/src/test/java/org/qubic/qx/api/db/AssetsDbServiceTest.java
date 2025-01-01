package org.qubic.qx.api.db;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.domain.Asset;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssetsDbServiceTest {

    private final AssetsRepository assetsRepository = mock();
    private final AssetsDbService assetsDbService = new AssetsDbService(assetsRepository);

    @Test
    void getOrCreateAsset_givenAsset_thenReturn() {
        Asset expected = Asset.builder().id(42L).build();
        when(assetsRepository.findByIssuerAndName("issuer", "name")).thenReturn(Optional.of(expected));
        Asset asset = assetsDbService.getOrCreateAsset("issuer", "name");
        assertThat(asset).isEqualTo(expected);
    }

    @Test
    void getOrCreateAsset_givenNoAsset_thenCreate() {
        Asset expected = Asset.builder().id(42L).build();
        when(assetsRepository.save(Asset.builder().issuer("issuer").name("name").build())).thenReturn(expected);
        Asset asset = assetsDbService.getOrCreateAsset("issuer", "name");
        assertThat(asset).isEqualTo(expected);
    }

}