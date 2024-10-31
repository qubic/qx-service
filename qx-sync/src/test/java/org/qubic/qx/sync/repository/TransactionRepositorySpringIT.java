package org.qubic.qx.sync.repository;

import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.AbstractRedisTest;
import org.qubic.qx.sync.domain.QxAssetOrderData;
import org.qubic.qx.sync.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.test.StepVerifier;

public class TransactionRepositorySpringIT extends AbstractRedisTest {

    private static final String SERIALIZED_TX = """
            {"transactionHash":"transaction-hash","sourcePublicId":"source-id","destinationPublicId":"target-id",\
            "amount":1,"tick":2,"inputType":3,"inputSize":4,"extraData":{"@class":".%s","issuer":"issuer","name":"name","price":42,\
            "numberOfShares":123},"moneyFlew":null}""".formatted(QxAssetOrderData.class.getSimpleName());

    private static final QxAssetOrderData EXTRA_DATA = new QxAssetOrderData(
            "issuer",
            "name",
            42L,
            123L);

    private static final Transaction TX = new Transaction(
            "transaction-hash",
            "source-id",
            "target-id",
            1,
            2,
            3,
            4,
             EXTRA_DATA,
            null);

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private ReactiveRedisTemplate<String, Transaction> redisTransactionOperations;
    @Autowired
    private ReactiveStringRedisTemplate redisStringTemplate;

    @Test
    void putTransaction_thenPushIntoTransactionProcessingQueue() {

        StepVerifier.create(transactionRepository.putTransaction(TX))
                .expectNext(TX)
                .verifyComplete();

        StepVerifier.create(redisTransactionOperations
                        .opsForList()
                        .range("queue:transactions", 0, 1))
                .expectNext(TX)
                .verifyComplete();

        StepVerifier.create(redisStringTemplate
                        .opsForList()
                        .rightPop("queue:transactions"))
                .expectNext(SERIALIZED_TX)
                .verifyComplete();

    }

}
