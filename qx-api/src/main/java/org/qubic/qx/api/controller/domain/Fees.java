package org.qubic.qx.api.controller.domain;

import java.io.Serializable;

public record Fees(long assetIssuanceFee, long transferFee, long tradeFee) implements Serializable {

}
