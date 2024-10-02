package org.qubic.qx.adapter.qubicj.mapping;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.domain.std.Transaction;
import org.qubic.qx.adapter.ExtraDataMapper;
import org.qubic.qx.domain.ExtraData;


public class DataTypeTranslator {

    private final IdentityUtil identityUtil;
    private final ExtraDataMapper extraDataMapper;

    public DataTypeTranslator(IdentityUtil identityUtil, ExtraDataMapper extraDataMapper) {
        this.identityUtil = identityUtil;
        this.extraDataMapper = extraDataMapper;
    }

    @PublicKeyMapping
    public String mapPublicKey(byte[] publicKey) {
        return identityUtil.getIdentityFromPublicKey(publicKey);
    }

    @UnsignedIntMapping
    public long mapUnsignedInt(int unsigned) {
        return Integer.toUnsignedLong(unsigned);
    }

    @UnsignedShortMapping
    public int mapUnsignedShort(short unsigned) {
        return Short.toUnsignedInt(unsigned);
    }

    @AssetNameMapping
    public String mapAssetName(byte[] assetName) {
        return new String(assetName).trim();
    }

    @ExtraDataMapping
    public ExtraData mapExtraData(Transaction transaction) {
        int inputType = Short.toUnsignedInt(transaction.getInputType());
        return extraDataMapper.map(inputType, transaction.getExtraData());
    }

}
