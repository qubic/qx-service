package org.qubic.qx.api.db;

import org.qubic.qx.api.db.domain.Trade;
import org.springframework.data.repository.CrudRepository;

public interface TradesRepository extends CrudRepository<Trade, Long> {
}
