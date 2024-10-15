package org.qubic.qx.api.db.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Builder
@Data
@Table("trades")
public class Trade {

    @Id
    private Long id;
    private Instant tickTime;
    private long transactionId; // transactions.id
    private boolean bid;
    private long makerId; // identities.id
    private long assetId; // assets.id
    private long price;
    private long numberOfShares;

}
