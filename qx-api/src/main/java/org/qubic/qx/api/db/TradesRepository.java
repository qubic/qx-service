package org.qubic.qx.api.db;

import org.qubic.qx.api.db.dto.AvgPriceData;
import org.qubic.qx.api.db.dto.TradeDto;
import org.qubic.qx.api.db.domain.Trade;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
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
    offset :offset
    limit :limit
    """)
    List<TradeDto> findAll(long offset, long limit);

    @Query("""
    select t.tick_time, tx.hash as transaction_hash, taker.identity as taker, maker.identity as maker, a.issuer, a.name as asset_name, t.bid, t.price, t.number_of_shares
    from trades t
        join transactions tx on t.transaction_id = tx.id
        join entities taker on tx.source_entity_id = taker.id
        join entities maker on t.maker_id = maker.id
        join assets a on t.asset_id = a.id
    where a.issuer = :issuer
    order by tick_time desc, t.id desc
    offset :offset
    limit :limit
    """)
    List<TradeDto> findByIssuer(String issuer, long offset, long limit);

    @Query("""
    select t.tick_time, tx.hash as transaction_hash, taker.identity as taker, maker.identity as maker, a.issuer, a.name as asset_name, t.bid, t.price, t.number_of_shares
    from trades t
        join transactions tx on t.transaction_id = tx.id
        join entities taker on tx.source_entity_id = taker.id
        join entities maker on t.maker_id = maker.id
        join assets a on t.asset_id = a.id
    where a.issuer != :issuer
    order by tick_time desc, t.id desc
    offset :offset
    limit :limit
    """)
    List<TradeDto> findByIssuerIsNot(String issuer, long offset, long limit);


    @Query("""
    select t.tick_time, tx.hash as transaction_hash, taker.identity as taker, maker.identity as maker, a.issuer, a.name as asset_name, t.bid, t.price, t.number_of_shares
    from trades t
        join transactions tx on t.transaction_id = tx.id
        join entities taker on tx.source_entity_id = taker.id
        join entities maker on t.maker_id = maker.id
        join assets a on t.asset_id = a.id
    where a.issuer = :issuer and a.name = :name
    order by tick_time desc, t.id desc
    offset :offset
    limit :limit
    """)
    List<TradeDto> findByIssuerAndAsset(String issuer, String name, long offset, long limit);

    @Query("""
    select t.tick_time, tx.hash as transaction_hash, taker.identity as taker, maker.identity as maker, a.issuer, a.name as asset_name, t.bid, t.price, t.number_of_shares
    from trades t
        join transactions tx on t.transaction_id = tx.id
        join entities taker on tx.source_entity_id = taker.id
        join entities maker on t.maker_id = maker.id
        join assets a on t.asset_id = a.id
    where taker.identity = :identity or maker.identity = :identity
    order by tick_time desc, t.id desc
    offset :offset
    limit :limit
    """)
    List<TradeDto> findByEntity(String identity, long offset, long limit);


    @Query("""
        select
            t.tick_time::date as time,
            min(t.price) as min,
            max(t.price) as max,
            sum(t.number_of_shares) total_shares,
            sum(t.price * t.number_of_shares) total_amount,
            sum(t.price * t.number_of_shares) / sum(t.number_of_shares) as average_price,
            COUNT(*) AS total_trades
        from trades t
        join assets a on t.asset_id = a.id
        where a.issuer = :issuer and a.name = :name and t.tick_time >= :after
        group by time
        order by time;
    """)
    List<AvgPriceData> findAveragePriceByAssetGroupedByDay(String issuer, String name, Instant after);

    @Query("""
        select
            date_trunc('hour', t.tick_time) as time,
            min(t.price) as min,
            max(t.price) as max,
            sum(t.number_of_shares) total_shares,
            sum(t.price * t.number_of_shares) total_amount,
            sum(t.price * t.number_of_shares) / sum(t.number_of_shares) as average_price,
            COUNT(*) AS total_trades
        from trades t
        join assets a on t.asset_id = a.id
        where a.issuer = :issuer and a.name = :name and t.tick_time >= :after
        group by time
        order by time;
    """)
    List<AvgPriceData> findAveragePriceByAssetGroupedByHour(String issuer, String name, Instant after);

}
