package org.qubic.qx.api.controller.domain;

import java.io.Serializable;

public record AssetOrder(String entityId, long price, long numberOfShares) implements Serializable {
}
