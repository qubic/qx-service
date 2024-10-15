package org.qubic.qx.api.db.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Data
@Table("transactions")
public class Transaction {

    @Id
    private Long id;
    private String hash;
    @Column("source_entity_id")
    private long sourceId;
    @Column("destination_entity_id")
    private long destinationId;
    private long amount;
    private long tick;
    private int inputType;
    private int inputSize;
    private ExtraData extraData;
    private Boolean moneyFlew; // can be null (unknown)

}
