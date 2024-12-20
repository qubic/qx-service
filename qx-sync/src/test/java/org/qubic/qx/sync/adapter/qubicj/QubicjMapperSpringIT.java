package org.qubic.qx.sync.adapter.qubicj;

import at.qubic.api.domain.qx.Qx;
import at.qubic.api.domain.std.SignedTransaction;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.ExtraDataMapper;
import org.qubic.qx.sync.adapter.qubicj.mapping.QubicjMapper;
import org.qubic.qx.sync.domain.AssetOrder;
import org.qubic.qx.sync.domain.ExtraData;
import org.qubic.qx.sync.domain.TickData;
import org.qubic.qx.sync.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = """
    backend=qubicj
""")
class QubicjMapperSpringIT {

    @MockitoBean
    private ExtraDataMapper extraDataMapper;

    @Autowired
    private QubicjMapper mapper;

    @Test
    void mapAssetOrder() {
        at.qubic.api.domain.qx.response.AssetOrder source = at.qubic.api.domain.qx.response.AssetOrder.builder()
                .entity(new byte[32])
                .price(42)
                .numberOfShares(10)
                .build();
        AssetOrder target = mapper.map(source);
        assertThat(target).isNotNull();
        assertThat(target.entityId()).isEqualTo("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB");
        assertThat(target.price()).isEqualTo(42);
        assertThat(target.numberOfShares()).isEqualTo(10);
    }

    @Test
    void mapTickData() {
        at.qubic.api.domain.std.response.TickData source = at.qubic.api.domain.std.response.TickData.builder()
                .epoch((short) 1)
                .tick(2)
                .year((byte) 24)
                .month((byte) 9)
                .day((byte) 7)
                .hour((byte) 13)
                .minute((byte) 7)
                .second((byte) 2)
                .millisecond((short) 123)
                .build();

        TickData target = mapper.map(source);
        assertThat(target).isNotNull();
        assertThat(target.epoch()).isEqualTo(1);
        assertThat(target.tick()).isEqualTo(2);
        assertThat(target.timestamp()).isEqualTo("2024-09-07T13:07:02.123Z");
    }

    private static final ExtraData EXTRA_DATA = mock();

    @Test
    void mapTransaction() {
        SignedTransaction signedTransaction = SignedTransaction.builder()
                .transactionHash("transaction-hash")
                .transaction(at.qubic.api.domain.std.Transaction.builder()
                        .sourcePublicKey(new byte[] {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0})
                        .destinationPublicKey(new byte[32])
                        .inputType(Qx.Procedure.QX_ADD_BID_ORDER.getCode())
                        .inputSize((short) 42)
                        .amount(43)
                        .tick(-1) // -> 4294967295L max unsigned int value
                        .extraData(new byte[(short) 42])
                        .build())
                .build();

        when(extraDataMapper.map(6, new byte[(short) 42])).thenReturn(EXTRA_DATA);

        Transaction transaction = mapper.map(signedTransaction);
        assertThat(transaction.sourcePublicId()).isEqualTo("BAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAARMID");
        assertThat(transaction.destinationPublicId()).isEqualTo("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB");
        assertThat(transaction.inputType()).isEqualTo(6);
        assertThat(transaction.inputSize()).isEqualTo(42);
        assertThat(transaction.amount()).isEqualTo(43);
        assertThat(transaction.tick()).isEqualTo(4294967295L);
        assertThat(transaction.extraData()).isEqualTo(EXTRA_DATA);
    }

}