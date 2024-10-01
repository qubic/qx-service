package org.qubic.qx.adapter.il.domain;

public record IlTransaction(String sourceId,
                            String destId,
                            String amount,
                            long tick,
                            int inputType,
                            int inputSize,
                            String input,
                            String txId) {

}
