package org.qubic.qx.adapter.qubicj;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.domain.qx.Qx;
import at.qubic.api.domain.std.SignedTransaction;
import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.domain.*;
import org.qubic.qx.util.AssetUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Slf4j
public class TransactionMapper {

    protected final IdentityUtil identityUtil;

    public TransactionMapper(IdentityUtil identityUtil) {
        this.identityUtil = identityUtil;
    }

    public Transaction map(SignedTransaction source) {
        at.qubic.api.domain.std.Transaction sourceTransaction = source.getTransaction();
        int inputType = Short.toUnsignedInt(sourceTransaction.getInputType());

        ExtraData extraData;
        if (inputType > 4 && inputType < 9) {
            extraData = mapQxOrderData(sourceTransaction.getExtraData());
        } else if (inputType == Qx.Procedure.QX_TRANSFER_SHARE.getCode()) {
            extraData = mapQxTransferAssetData(sourceTransaction.getExtraData());
        } else if (inputType == Qx.Procedure.QX_ISSUE_ASSET.getCode()) {
            extraData = mapQxIssueAssetData(sourceTransaction.getExtraData());
        } else {
            // FIXME to support issue and transfer asset transactions
            String msg = String.format("Unsupported transaction input type: %s.", inputType);
            log.error(msg);
            throw new IllegalStateException(msg);
        }

        Transaction tx = new Transaction(
                source.getTransactionHash(),
                identityUtil.getIdentityFromPublicKey(sourceTransaction.getSourcePublicKey()),
                identityUtil.getIdentityFromPublicKey(sourceTransaction.getDestinationPublicKey()),
                sourceTransaction.getAmount(),
                Integer.toUnsignedLong(sourceTransaction.getTick()),
                inputType,
                Short.toUnsignedInt(sourceTransaction.getInputSize()),
                extraData
        );
        log.info("Mapped transaction: {}.", tx);
        return tx;
    }

    private QxAssetOrderData mapQxOrderData(byte[] extraData) {
        ByteBuffer buffer = ByteBuffer.wrap(extraData)
                .order(ByteOrder.LITTLE_ENDIAN);
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

    private QxTransferAssetData mapQxTransferAssetData(byte[] extraData) {
        ByteBuffer buffer = ByteBuffer.wrap(extraData)
                .order(ByteOrder.LITTLE_ENDIAN);
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

    private QxIssueAssetData mapQxIssueAssetData(byte[] extraData) {
        ByteBuffer buffer = ByteBuffer.wrap(extraData)
                .order(ByteOrder.LITTLE_ENDIAN);

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
