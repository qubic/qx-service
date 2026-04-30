package org.qubic.qx.api.adapter.il;

import at.qubic.api.crypto.IdentityUtil;

public class DataTypeTranslator {

    private final IdentityUtil identityUtil;

    public DataTypeTranslator(IdentityUtil identityUtil) {
        this.identityUtil = identityUtil;
    }

    @PublicKeyMapping
    public String mapPublicKey(byte[] publicKey) {
        return identityUtil.getIdentityFromPublicKey(publicKey);
    }

    @AssetNameMapping
    public String mapAssetName(byte[] assetName) {
        return new String(assetName).trim();
    }

}
