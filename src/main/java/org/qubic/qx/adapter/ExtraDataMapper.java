package org.qubic.qx.adapter;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.domain.qx.Qx;
import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.domain.ExtraData;
import org.qubic.qx.domain.QxAssetOrderData;
import org.qubic.qx.domain.QxIssueAssetData;
import org.qubic.qx.domain.QxTransferAssetData;
import org.qubic.qx.util.AssetUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Slf4j
public class ExtraDataMapper {

    private final IdentityUtil identityUtil;

    public ExtraDataMapper(IdentityUtil identityUtil) {
        this.identityUtil = identityUtil;
    }

    public ExtraData map(int inputType, byte[] input) {
        return map(inputType, ByteBuffer.wrap(input).order(ByteOrder.LITTLE_ENDIAN));
    }

    private ExtraData map(int inputType, ByteBuffer input) {
        ExtraData extraData;
        if (inputType > 4 && inputType < 9) {
            return mapQxOrderData(input);
        } else if (inputType == Qx.Procedure.QX_TRANSFER_SHARE.getCode()) {
            extraData = mapQxTransferAssetData(input);
        } else if (inputType == Qx.Procedure.QX_ISSUE_ASSET.getCode()) {
            extraData = mapQxIssueAssetData(input);
        } else {
            String msg = String.format("Unsupported transaction input type: %s.", inputType);
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return extraData;
    }

    private QxAssetOrderData mapQxOrderData(ByteBuffer buffer) {
        byte[] issuer = new byte[32];
        buffer.get(issuer);
        String issuerId = identityUtil.getIdentityFromPublicKey(issuer);
        byte[] assetName = new byte[8];
        buffer.get(assetName);
        String name = AssetUtil.getAssetNameString(assetName);
        long price = buffer.getLong();
        long quantity = buffer.getLong();

        return new QxAssetOrderData(
                issuerId,
                name,
                price,
                quantity
        );
    }

    private QxTransferAssetData mapQxTransferAssetData(ByteBuffer buffer) {
        byte[] issuer = new byte[32];
        buffer.get(issuer);
        String issuerId = identityUtil.getIdentityFromPublicKey(issuer);
        byte[] newOwner = new byte[32];
        buffer.get(newOwner);
        String newOwnerId = identityUtil.getIdentityFromPublicKey(newOwner);
        byte[] name = new byte[8];
        buffer.get(name);
        String assetName = AssetUtil.getAssetNameString(name);

        return new QxTransferAssetData(
                issuerId,
                newOwnerId,
                assetName,
                buffer.getLong()
        );
    }

    private QxIssueAssetData mapQxIssueAssetData(ByteBuffer buffer) {
        byte[] name = new byte[8];
        buffer.get(name);
        String assetName = AssetUtil.getAssetNameString(name);
        long numberOfUnits = buffer.getLong();
        byte[] units = new byte[8];
        buffer.get(units);
        String unitOfMeasurement = AssetUtil.getUnitOfMeasurementString(units);

        return new QxIssueAssetData(
                assetName,
                numberOfUnits,
                unitOfMeasurement,
                buffer.get()
        );
    }

}
