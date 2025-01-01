package org.qubic.qx.api.db;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.domain.Entity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EntitiesDbServiceTest {

    private final EntitiesRepository entitiesRepository = mock();
    private final EntitiesDbService dbService = new EntitiesDbService(entitiesRepository);

    @Test
    void getOrCreateAsset_givenAsset_thenReturn() {
        Entity expected = Entity.builder().id(42L).build();
        when(entitiesRepository.findByIdentity("identity")).thenReturn(Optional.of(expected));
        Entity entity = dbService.getOrCreateEntity("identity");
        assertThat(entity).isEqualTo(expected);
    }

    @Test
    void getOrCreateAsset_givenNoAsset_thenCreate() {
        Entity expected = Entity.builder().id(42L).build();
        when(entitiesRepository.save(Entity.builder().identity("identity").build())).thenReturn(expected);
        Entity entity = dbService.getOrCreateEntity("identity");
        assertThat(entity).isEqualTo(expected);
    }

}