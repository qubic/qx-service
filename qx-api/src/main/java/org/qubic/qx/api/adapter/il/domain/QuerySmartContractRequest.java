package org.qubic.qx.api.adapter.il.domain;

/**
 * Request for a smart contract call via the live API.
 * @param contractIndex 0-based index of the contract.
 * @param inputType The number of the function to call.
 * @param inputSize The size of the input data.
 * @param requestData Base64 encoded input data.
 */
public record QuerySmartContractRequest(int contractIndex, short inputType, long inputSize, String requestData) {
}
