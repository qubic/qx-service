package org.qubic.qx.api.db;

import org.qubic.qx.api.db.domain.Identity;
import org.springframework.data.repository.CrudRepository;

public interface IdentitiesRepository extends CrudRepository<Identity, Long> {
}
