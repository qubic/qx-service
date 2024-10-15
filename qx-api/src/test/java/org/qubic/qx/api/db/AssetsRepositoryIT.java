package org.qubic.qx.api.db;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.domain.Asset;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetsRepositoryIT extends AbstractPostgresTest{

    @Autowired
    private AssetsRepository repository;

    @Test
    public void saveAndLoad() {
        Asset asset = Asset.builder()
                .issuer("FOO")
                .name("BAR")
                .verified(true)
                .build();

        Asset saved = repository.save(asset);
        assertThat(saved.getId()).isNotNull();

        Asset reloaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded).isEqualTo(saved);
    }

}
