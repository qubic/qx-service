package org.qubic.qx.api.db.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigInteger;

@Builder
@Data
@Table("asset_owners")
public class AssetOwner {

    @Id
    private Long id;
    private long entityId;
    private long assetId;
    private BigInteger amount;

}
