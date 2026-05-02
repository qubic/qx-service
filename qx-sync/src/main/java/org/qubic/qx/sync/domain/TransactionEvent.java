package org.qubic.qx.sync.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
@Builder
public class TransactionEvent {

    private final long tick;
    private final String logId;
    private final String logDigest;
    private final int logType;
    private final String transactionHash;
    private final String rawPayload;

    private final AssetIssuance assetIssuance;
    private final AssetOwnershipChange assetOwnershipChange;
    private final SmartContractEvent smartContractMessage;

}
