package org.qubic.qx.api.db;

import org.qubic.qx.api.controller.domain.TransactionDto;
import org.qubic.qx.api.db.domain.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionsRepository extends CrudRepository<Transaction, Long>  {

    Optional<Transaction> findByHash(String transactionHash);

    List<Transaction> findByTickTimeIsNull(Pageable pageable);

    @Query("""
    select t.tick_time, t.hash, src.identity as source, t.amount, t.tick, t.input_type, t.extra_data, t.money_flew
    from transactions t
    join entities src on t.source_entity_id = src.id
    order by t.tick desc, t.id desc
    limit :limit
    """)
    List<TransactionDto> findOrdered(long limit);

    @Query("""
    select t.tick_time, t.hash, src.identity as source, t.amount, t.tick, t.input_type, t.extra_data, t.money_flew
    from transactions t
    join entities src on t.source_entity_id = src.id
    where t.input_type in (:inputTypes)
    order by t.tick desc, t.id desc
    limit :limit
    """)
    List<TransactionDto> findByInputTypesOrdered(List<Integer> inputTypes, long limit);

    @Query("""
    select t.tick_time, t.hash, src.identity as source, t.amount, t.tick, t.input_type, t.extra_data, t.money_flew
    from transactions t
    join entities src on t.source_entity_id = src.id
    join assets a on t.extra_data->>'issuer' = a.issuer and t.extra_data->>'name' = a.name
    where a.issuer = :issuer
    and a.name = :asset
    and t.input_type in (:inputTypes)
    order by t.tick desc, t.id desc
    limit :limit
    """)
    List<TransactionDto> findByAssetOrdered(String issuer, String asset, List<Integer> inputTypes, long limit); // inputType 1 does not work here

    @Query("""
    select t.tick_time, t.hash, src.identity as source, t.amount, t.tick, t.input_type, t.extra_data, t.money_flew
    from transactions t
    join entities src on t.source_entity_id = src.id
    where src.identity = :identity
    and t.input_type in (:inputTypes)
    order by t.tick desc, t.id desc
    limit :limit
    """)
    List<TransactionDto> findBySourceEntityOrdered(String identity, List<Integer> inputTypes, long limit);

    @Query("""
    select t.tick_time, t.hash, src.identity as source, t.amount, t.tick, t.input_type, t.extra_data, t.money_flew
    from transactions t
    join entities src on t.source_entity_id = src.id
    where (src.identity = :identity or t.extra_data->>'newOwner' = :identity)
    and t.input_type = 2
    order by t.tick desc, t.id desc
    limit :limit
    """)
    List<TransactionDto> findTransfersByEntityOrdered(String identity, long limit);

}
