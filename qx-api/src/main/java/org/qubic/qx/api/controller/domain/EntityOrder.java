package org.qubic.qx.api.controller.domain;

import java.io.Serializable;

public record EntityOrder(String issuerId, String assetName, long price, long numberOfShares) implements Serializable {
}
