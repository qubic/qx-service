package org.qubic.qx.api.db;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.db.domain.Entity;

@Slf4j
public class EntitiesDbService {

    private final EntitiesRepository entitiesRepository;

    public EntitiesDbService(EntitiesRepository entitiesRepository) {
        this.entitiesRepository = entitiesRepository;
    }

    public Entity getOrCreateEntity(String identity) {
        return entitiesRepository.findByIdentity(identity)
                .orElseGet(() -> {
                    log.info("Creating entity with identity [{}].", identity);
                    return entitiesRepository.save(Entity.builder().identity(identity).build());
                });
    }

}
