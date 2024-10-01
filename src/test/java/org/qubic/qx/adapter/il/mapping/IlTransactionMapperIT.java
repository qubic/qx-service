package org.qubic.qx.adapter.il.mapping;

import org.junit.jupiter.api.Test;
import org.qubic.qx.adapter.il.domain.IlTransaction;
import org.qubic.qx.domain.QxAssetOrderData;
import org.qubic.qx.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IlTransactionMapperIT {

    @Autowired
    private IlTransactionMapper transactionMapper;

    @Test
    void mapTransaction() {

        IlTransaction ilTransaction = new IlTransaction(
                "source-identity",
                "destination-identity",
                "42",
                12345L,
                5,
                56,
                "CDC7Y799XhZKyMvThoBjD/dnCh6/OfchC0C83KJT0F9DRkIAAAAAAAMAAAAAAAAACQAAAAAAAAA=",
                "transaction-id"
        );
        Transaction transaction = transactionMapper.mapTransaction(ilTransaction);

        assertThat(transaction).isNotNull();
        assertThat(transaction.transactionHash()).isEqualTo("transaction-id");
        assertThat(transaction.sourcePublicId()).isEqualTo("source-identity");
        assertThat(transaction.destinationPublicId()).isEqualTo("destination-identity");
        assertThat(transaction.amount()).isEqualTo(42);
        assertThat(transaction.tick()).isEqualTo(12345);
        assertThat(transaction.inputType()).isEqualTo(5);
        assertThat(transaction.inputSize()).isEqualTo(56);
        // QxAssetOrderData[issuer=CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL, name=CFB, price=3, numberOfShares=9]
        assertThat(transaction.extraData()).isInstanceOf(QxAssetOrderData.class);
        QxAssetOrderData extraData = (QxAssetOrderData) transaction.extraData();
        assertThat(extraData.name()).isEqualTo("CFB");
        assertThat(extraData.issuer()).isEqualTo("CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL");
        assertThat(extraData.price()).isEqualTo(3);
        assertThat(extraData.numberOfShares()).isEqualTo(9);
    }

}