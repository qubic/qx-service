package org.qubic.qx.sync.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
@Builder
@JsonDeserialize(builder = TransactionEvent.TransactionEventBuilder.class)
public class TransactionEvent {

    private final EventHeader header;
    private final int eventType;
    private final long eventSize;
    private final String eventData;

    private final String transactionHash;
    private final AssetOwnershipChange assetOwnershipChange;
    private final SmartContractEvent smartContractMessage;

    @JsonPOJOBuilder(withPrefix = "")
    public static class TransactionEventBuilder {}

}
