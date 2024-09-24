package org.qubic.qx.adapter.qubicj;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.domain.std.SignedTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.qubic.qx.domain.Transaction;

@Slf4j
public class TransactionMapper {

    protected final IdentityUtil identityUtil;

    public TransactionMapper(IdentityUtil identityUtil) {
        this.identityUtil = identityUtil;
    }

    public Transaction map(SignedTransaction source) {
        at.qubic.api.domain.std.Transaction sourceTransaction = source.getTransaction();
        Transaction tx = new Transaction(
                source.getTransactionHash(),
                identityUtil.getIdentityFromPublicKey(sourceTransaction.getSourcePublicKey()),
                identityUtil.getIdentityFromPublicKey(sourceTransaction.getDestinationPublicKey()),
                sourceTransaction.getAmount(),
                Integer.toUnsignedLong(sourceTransaction.getTick()),
                Short.toUnsignedInt(sourceTransaction.getInputType()),
                Short.toUnsignedInt(sourceTransaction.getInputSize()),
                Hex.encodeHexString(sourceTransaction.getExtraData())
        );
        log.info("Mapped transaction {} to {}.", source, tx);
        return tx;
    }

}
