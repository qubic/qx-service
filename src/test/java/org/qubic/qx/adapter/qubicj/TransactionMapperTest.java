package org.qubic.qx.adapter.qubicj;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.domain.std.SignedTransaction;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;
import org.qubic.qx.domain.Transaction;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionMapperTest {

    private final IdentityUtil identityUtil = mock();
    private final TransactionMapper mapper = new TransactionMapper(identityUtil);

    @Test
    void mapToTransaction() {
        when(identityUtil.getIdentityFromPublicKey(any())).then(args -> Hex.encodeHexString(args.getArgument(0, byte[].class)));

        SignedTransaction signedTransaction = SignedTransaction.builder()
                .transactionHash("transaction-hash")
                .transaction(at.qubic.api.domain.std.Transaction.builder()
                        .sourcePublicKey(new byte[]{1, 2, 3})
                        .destinationPublicKey(new byte[]{4, 5, 6})
                        .inputType((short) 7)
                        .inputSize((short) 8)
                        .amount(9)
                        .tick(-1) // -> 4294967295L max unsigned int value
                        .extraData(new byte[]{10, 11, 12, 13, 14, 15, 16, 17})
                        .build())
                .build();

        Transaction transaction = mapper.map(signedTransaction);
        assertThat(transaction.sourcePublicId()).isEqualTo("010203");
        assertThat(transaction.destinationPublicId()).isEqualTo("040506");
        assertThat(transaction.inputType()).isEqualTo(7);
        assertThat(transaction.inputSize()).isEqualTo(8);
        assertThat(transaction.amount()).isEqualTo(9);
        assertThat(transaction.tick()).isEqualTo(4294967295L);
        assertThat(transaction.extraData()).isEqualTo("0a0b0c0d0e0f1011");
    }

}