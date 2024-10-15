package org.qubic.qx.api.db;

import org.qubic.qx.api.db.domain.Transaction;
import org.springframework.data.repository.CrudRepository;

public interface TransactionsRepository extends CrudRepository<Transaction, Long>  {
}
