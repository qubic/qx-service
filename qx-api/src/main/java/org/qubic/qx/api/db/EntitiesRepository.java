package org.qubic.qx.api.db;

import org.qubic.qx.api.db.domain.Entity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EntitiesRepository extends CrudRepository<Entity, Long> {

    Optional<Entity> findByIdentity(String identity);

}
