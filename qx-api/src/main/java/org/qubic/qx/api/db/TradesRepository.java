package org.qubic.qx.api.db;

import org.qubic.qx.api.controller.domain.TradeDto;
import org.qubic.qx.api.db.domain.Trade;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TradesRepository extends CrudRepository<Trade, Long> {

    @Query("""
    select t.tick_time, tx.hash as transaction_hash, taker.identity as taker, maker.identity as maker, a.issuer, a.name as asset_name, t.bid, t.price, t.number_of_shares
    from trades t
        join transactions tx on t.transaction_id = tx.id
        join entities taker on tx.source_entity_id = taker.id
        join entities maker on t.maker_id = maker.id
        join assets a on t.asset_id = a.id
    order by tick_time desc, t.id desc
    limit :limit
    """)
    List<TradeDto> findOrderedByTickTimeDesc(long limit);

    @Query("""
    select t.tick_time, tx.hash as transaction_hash, taker.identity as taker, maker.identity as maker, a.issuer, a.name as asset_name, t.bid, t.price, t.number_of_shares
    from trades t
        join transactions tx on t.transaction_id = tx.id
        join entities taker on tx.source_entity_id = taker.id
        join entities maker on t.maker_id = maker.id
        join assets a on t.asset_id = a.id
    where a.issuer = :issuer and a.name = :name
    order by tick_time desc, t.id desc
    limit :limit
    """)
    List<TradeDto> findByAssetOrderedByTickTimeDesc(String issuer, String name, long limit);

    @Query("""
    select t.tick_time, tx.hash as transaction_hash, taker.identity as taker, maker.identity as maker, a.issuer, a.name as asset_name, t.bid, t.price, t.number_of_shares
    from trades t
        join transactions tx on t.transaction_id = tx.id
        join entities taker on tx.source_entity_id = taker.id
        join entities maker on t.maker_id = maker.id
        join assets a on t.asset_id = a.id
    where taker.identity = :identity or maker.identity = :identity
    order by tick_time desc, t.id desc
    limit :limit
    """)
    List<TradeDto> findByEntityOrderedByTickTimeDesc(String identity, long limit);

}
