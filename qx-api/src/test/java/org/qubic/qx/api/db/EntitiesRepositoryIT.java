package org.qubic.qx.api.db;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.domain.Entity;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class EntitiesRepositoryIT extends AbstractPostgresJdbcTest {

    @Autowired
    private EntitiesRepository repository;

    @Test
    public void saveAndLoad() {
        Entity entity = Entity.builder()
                .identity("FOO")
                .build();

        Entity saved = repository.save(entity);
        assertThat(saved.getId()).isNotNull();

        Entity reloaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(saved).isEqualTo(reloaded);
    }

    @Test
    public void findByIdentity() {
        Entity entity = Entity.builder()
                .identity("SOME")
                .build();

        repository.save(entity);

        Entity reloaded = repository.findByIdentity("SOME").orElseThrow();
        assertThat(reloaded).isEqualTo(entity);

    }

}