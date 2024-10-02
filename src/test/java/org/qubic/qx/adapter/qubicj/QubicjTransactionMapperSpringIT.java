package org.qubic.qx.adapter.qubicj;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.domain.qx.Qx;
import at.qubic.api.domain.std.SignedTransaction;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.adapter.ExtraDataMapper;
import org.qubic.qx.adapter.qubicj.mapping.QubicjTransactionMapper;
import org.qubic.qx.domain.ExtraData;
import org.qubic.qx.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = """
backend=qubicj
""")
class QubicjTransactionMapperSpringIT {

    private static final byte[] SOURCE_PUBLIC_KEY = {1, 2, 3};
    private static final byte[] DESTINATION_PUBLIC_KEY = {4, 5, 6};
    private static final short INPUT_SIZE = (short) 42;
    private static final int AMOUNT = 43;
    private static final int TICK = -1;
    private static final ExtraData EXTRA_DATA = mock();
    private static final String TRANSACTION_HASH = "transaction-hash";

    @MockBean
    private IdentityUtil identityUtil;

    @MockBean
    private ExtraDataMapper extraDataMapper;

    @Autowired
    private QubicjTransactionMapper mapper;

    @BeforeEach
    void initMocks() {
        // simply return input as hex string
        when(identityUtil.getIdentityFromPublicKey(any())).then(args -> Hex.encodeHexString(args.getArgument(0, byte[].class)));
        when(extraDataMapper.map(anyInt(), any(byte[].class))).thenReturn(EXTRA_DATA);
    }

    @Test
    void mapTransaction() {
        SignedTransaction signedTransaction = signedTestTransaction(Qx.Procedure.QX_ADD_BID_ORDER.getCode(),
                new byte[INPUT_SIZE]);

        Transaction transaction = mapper.map(signedTransaction);
        assertThat(transaction.sourcePublicId()).isEqualTo("010203");
        assertThat(transaction.destinationPublicId()).isEqualTo("040506");
        assertThat(transaction.inputType()).isEqualTo(6);
        assertThat(transaction.inputSize()).isEqualTo(42);
        assertThat(transaction.amount()).isEqualTo(43);
        assertThat(transaction.tick()).isEqualTo(4294967295L);
        assertThat(transaction.extraData()).isEqualTo(EXTRA_DATA);
    }

    private SignedTransaction signedTestTransaction(int inputType, byte[] extraData) {
        return SignedTransaction.builder()
                .transactionHash(TRANSACTION_HASH)
                .transaction(at.qubic.api.domain.std.Transaction.builder()
                        .sourcePublicKey(SOURCE_PUBLIC_KEY)
                        .destinationPublicKey(DESTINATION_PUBLIC_KEY)
                        .inputType((short) inputType)
                        .inputSize(INPUT_SIZE)
                        .amount(AMOUNT)
                        .tick(TICK) // -> 4294967295L max unsigned int value
                        .extraData(extraData)
                        .build())
                .build();
    }

}