package org.qubic.qx.sync.adapter.il.domain.goqubic;

public record IlTransaction(String sourceId,
                            String destId,
                            String amount,
                            long tick,
                            int inputType,
                            int inputSize,
                            String input,
                            String txId) {

}
