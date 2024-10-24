package org.qubic.qx.api.db;

import org.qubic.qx.api.controller.domain.TransactionDto;
import org.qubic.qx.api.db.domain.Transaction;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionsRepository extends CrudRepository<Transaction, Long>  {

    Optional<Transaction> findByHash(String transactionHash);

    @Query("""
    select t.hash, src.identity as source, t.amount, t.tick, t.input_type, t.extra_data, t.money_flew
    from transactions t
    join entities src on t.source_entity_id = src.id
    order by t.tick desc, t.id desc
    limit :limit
    """)
    List<TransactionDto> findOrdered(long limit);

    @Query("""
    select t.hash, src.identity as source, t.amount, t.tick, t.input_type, t.extra_data, t.money_flew
    from transactions t
    join entities src on t.source_entity_id = src.id
    join assets a on t.extra_data->>'issuer' = a.issuer and t.extra_data->>'name' = a.name
    where a.issuer = :issuer
    and a.name = :asset
    and t.input_type in (2,5,6,7,8) -- add/remove ask/bid
    order by t.tick desc, t.id desc
    limit :limit
    """)
    List<TransactionDto> findByAssetOrdered(String issuer, String asset, long limit);

    @Query("""
    select t.hash, src.identity as source, t.amount, t.tick, t.input_type, t.extra_data, t.money_flew
    from transactions t
    join entities src on t.source_entity_id = src.id
    where t.input_type in (:inputTypes)
    order by t.tick desc, t.id desc
    limit :limit
    """)
    List<TransactionDto> findByInputTypesOrdered(List<Integer> inputTypes, long limit);

    @Query("""
    select t.hash, src.identity as source, t.amount, t.tick, t.input_type, t.extra_data, t.money_flew
    from transactions t
    join entities src on t.source_entity_id = src.id
    where src.identity = :identity
    order by t.tick desc, t.id desc
    limit :limit
    """)
    List<TransactionDto> findByEntityOrdered(String identity, long limit);

}
