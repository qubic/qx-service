package org.qubic.qx.api.db;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.domain.Asset;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetsRepositoryIT extends AbstractPostgresTest{

    @Autowired
    private AssetsRepository assetsRepository;

    @Test
    public void saveEntity() {
        Asset asset = Asset.builder()
                .issuer("FOO")
                .name("BAR")
                .build();
        Asset saved = assetsRepository.save(asset);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getIssuer()).isEqualTo("FOO");
        assertThat(saved.getName()).isEqualTo("BAR");
    }

}
