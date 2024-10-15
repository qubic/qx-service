package org.qubic.qx.api.controller.mapping;

import org.qubic.qx.api.controller.domain.Asset;

import java.util.Objects;

public class AssetsMapper {

    public Asset map(org.qubic.qx.api.db.domain.Asset source) {
        Objects.requireNonNull(source);
        assert source.getIssuer() != null : "Issuer is null";
        assert source.getName() != null : "Name is null";
        return new Asset(source.getIssuer(), source.getName());
    }

}
