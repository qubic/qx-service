package org.qubic.qx.sync.adapter.il.domain.query;

public record IlQueryApiTransaction(String hash,
                                    long amount,
                                    String source,
                                    String destination,
                                    long tickNumber,
                                    int inputType,
                                    int inputSize,
                                    String inputData) {
}
