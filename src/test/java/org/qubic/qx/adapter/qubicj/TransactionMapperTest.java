package org.qubic.qx.adapter.qubicj;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.domain.qx.Qx;
import at.qubic.api.domain.std.SignedTransaction;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.domain.QxAssetOrderData;
import org.qubic.qx.domain.QxIssueAssetData;
import org.qubic.qx.domain.QxTransferAssetData;
import org.qubic.qx.domain.Transaction;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TransactionMapperTest {

    private static final byte[] SOURCE_PUBLIC_KEY = {1, 2, 3};
    private static final byte[] DESTINATION_PUBLIC_KEY = {4, 5, 6};
    private static final byte[] ISSUER_PUBLIC_KEY = new byte[32];
    private static final byte[] NEW_OWNER_PUBLIC_KEY = new byte[] {1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    private static final short INPUT_SIZE = (short) 42;
    private static final int AMOUNT = 43;
    private static final int TICK = -1;
    private static final String TRANSACTION_HASH = "transaction-hash";
    private final IdentityUtil identityUtil = mock();
    private final TransactionMapper mapper = new TransactionMapper(identityUtil);

    @BeforeEach
    void initMocks() {
        // simply return input as hex string
        when(identityUtil.getIdentityFromPublicKey(any())).then(args -> Hex.encodeHexString(args.getArgument(0, byte[].class)));
    }

    @Test
    void mapTransaction() throws DecoderException {
        SignedTransaction signedTransaction = signedTestTransaction(Qx.Procedure.QX_ADD_BID_ORDER.getCode(),
                Hex.decodeHex("0000000000000000000000000000000000000000000000000000000000000000" +
                        "510000000000000002000000000000000100000000000000"));

        Transaction transaction = mapper.map(signedTransaction);
        assertThat(transaction.sourcePublicId()).isEqualTo("010203");
        assertThat(transaction.destinationPublicId()).isEqualTo("040506");
        assertThat(transaction.inputType()).isEqualTo(6);
        assertThat(transaction.inputSize()).isEqualTo(42);
        assertThat(transaction.amount()).isEqualTo(43);
        assertThat(transaction.tick()).isEqualTo(4294967295L);
    }

    @Test
    void mapQxOrderDataTransaction() throws DecoderException {
        SignedTransaction signedTransaction = signedTestTransaction(Qx.Procedure.QX_ADD_ASK_ORDER.getCode(),
                Hex.decodeHex("0000000000000000000000000000000000000000000000000000000000000000" +
                        "464f4f00000000007b000000000000002d00000000000000"));

        Transaction transaction = mapper.map(signedTransaction);
        assertThat(transaction.inputType()).isEqualTo(5);

        assertThat(transaction.extraData()).isInstanceOf(QxAssetOrderData.class);
        QxAssetOrderData extraData = (QxAssetOrderData) transaction.extraData();
        assertThat(extraData).isNotNull();
        assertThat(extraData.issuer()).isEqualTo("0000000000000000000000000000000000000000000000000000000000000000");
        assertThat(extraData.name()).isEqualTo("FOO");
        assertThat(extraData.price()).isEqualTo(123);
        assertThat(extraData.numberOfShares()).isEqualTo(45);
    }

    @Test
    void mapQxTransferAssetTransaction() {
        byte[] extraData = ByteBuffer.allocate(80)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(ISSUER_PUBLIC_KEY)
                .put(NEW_OWNER_PUBLIC_KEY)
                .put(new byte[] {'F','O','O',0,0,0,0,0})
                .putLong(666)
                .array();
        SignedTransaction signedTransaction = signedTestTransaction(Qx.Procedure.QX_TRANSFER_SHARE.getCode(),
                extraData);

        Transaction transaction = mapper.map(signedTransaction);
        assertThat(transaction.inputType()).isEqualTo(2);

        assertThat(transaction.extraData()).isInstanceOf(QxTransferAssetData.class);
        QxTransferAssetData transferData = (QxTransferAssetData) transaction.extraData();
        assertThat(extraData).isNotNull();
        assertThat(transferData.issuer()).isEqualTo("0000000000000000000000000000000000000000000000000000000000000000");
        assertThat(transferData.newOwner()).isEqualTo("0101010000000000000000000000000000000000000000000000000000000000");
        assertThat(transferData.assetName()).isEqualTo("FOO");
        assertThat(transferData.numberOfUnits()).isEqualTo(666);
    }

    @Test
    void mapQxIssueAssetTransaction() {
        byte[] extraData = ByteBuffer.allocate(25)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(new byte[] {'F','O','O',0,0,0,0,0})
                .putLong(666)
                .put(new byte[] {0,0,0,0,0,2,0,0})
                .put((byte) 16)
                .array();
        SignedTransaction signedTransaction = signedTestTransaction(Qx.Procedure.QX_ISSUE_ASSET.getCode(),
                extraData);

        Transaction transaction = mapper.map(signedTransaction);
        assertThat(transaction.inputType()).isEqualTo(1);

        assertThat(transaction.extraData()).isInstanceOf(QxIssueAssetData.class);
        QxIssueAssetData issueData = (QxIssueAssetData) transaction.extraData();
        assertThat(extraData).isNotNull();
        assertThat(issueData.name()).isEqualTo("FOO");
        assertThat(issueData.numberOfUnits()).isEqualTo(666);
        assertThat(issueData.unitOfMeasurement()).isEqualTo("0000020");
        assertThat(issueData.numberOfDecimalPlaces()).isEqualTo((byte) 16);
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