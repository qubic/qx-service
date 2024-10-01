package org.qubic.qx.adapter.qubicj;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.domain.std.SignedTransaction;
import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.adapter.ExtraDataMapper;
import org.qubic.qx.domain.ExtraData;
import org.qubic.qx.domain.Transaction;

@Slf4j
public class TransactionMapper {

    private final IdentityUtil identityUtil;
    private final ExtraDataMapper extraDataMapper;

    public TransactionMapper(ExtraDataMapper extraDataMapper, IdentityUtil identityUtil) {
        this.identityUtil = identityUtil;
        this.extraDataMapper = extraDataMapper;
    }

    public Transaction map(SignedTransaction source) {
        at.qubic.api.domain.std.Transaction sourceTransaction = source.getTransaction();
        int inputType = Short.toUnsignedInt(sourceTransaction.getInputType());

        ExtraData extraData = extraDataMapper.map(inputType, sourceTransaction.getExtraData());
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

}
