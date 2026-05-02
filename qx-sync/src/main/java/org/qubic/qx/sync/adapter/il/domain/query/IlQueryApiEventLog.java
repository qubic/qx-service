package org.qubic.qx.sync.adapter.il.domain.query;

import java.util.List;

public record IlQueryApiEventLog(int epoch,
                                 long tickNumber,
                                 String timestamp,
                                 String transactionHash,
                                 int logType,
                                 String logId,
                                 String logDigest,
                                 List<String> categories,
                                 String rawPayload,
                                 IlQueryApiAssetChangeData assetOwnershipChange,
                                 IlQueryApiSmartContractMessage smartContractMessage,
                                 IlQueryApiAssetIssuanceData assetIssuance) {
}
