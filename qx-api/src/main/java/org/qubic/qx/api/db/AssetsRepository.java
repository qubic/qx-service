package org.qubic.qx.api.db;

import org.qubic.qx.api.db.domain.Asset;
import org.springframework.data.repository.CrudRepository;

public interface AssetsRepository extends CrudRepository<Asset, Long> {
}
