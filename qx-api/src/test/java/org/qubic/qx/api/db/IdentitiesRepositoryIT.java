package org.qubic.qx.api.db;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.domain.Identity;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class IdentitiesRepositoryIT extends AbstractPostgresTest{

    @Autowired
    private IdentitiesRepository identitiesRepository;

    @Test
    public void saveEntity() {
        Identity identity = Identity.builder()
                .identity("FOO")
                .build();
        identity = identitiesRepository.save(identity);

        assertThat(identity.getId()).isNotNull();
    }

}