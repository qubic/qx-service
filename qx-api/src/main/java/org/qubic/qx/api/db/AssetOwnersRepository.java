package org.qubic.qx.api.db;

import org.qubic.qx.api.db.domain.AssetOwner;
import org.qubic.qx.api.db.dto.AmountPerEntityDto;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AssetOwnersRepository extends CrudRepository<AssetOwner, Long> {


    @Query("""
    select e.identity, ao.amount from asset_owners ao
        join assets a on a.id = ao.asset_id
        join entities e on e.id = ao.entity_id
        where a.issuer = :issuer and a.name = :name
        order by ao.amount desc
        limit :limit
    """)
    List<AmountPerEntityDto> findOwnersByAsset(String issuer, String name, long limit);

    Optional<AssetOwner> findByAssetIdAndEntityId(long assetId, long entityId);

}
