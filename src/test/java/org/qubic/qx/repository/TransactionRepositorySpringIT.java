package org.qubic.qx.repository;

import org.junit.jupiter.api.Test;
import org.qubic.qx.AbstractRedisTest;
import org.qubic.qx.domain.QxAssetOrderData;
import org.qubic.qx.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class TransactionRepositorySpringIT extends AbstractRedisTest {

    private static final String SERIALIZED_TX = """
            {"transactionHash":"transaction-hash","sourcePublicId":"source-id","destinationPublicId":"target-id",\
            "amount":1,"tick":2,"inputType":3,"inputSize":4,"extraData":{"@class":".%s","issuer":"issuer","name":"name","price":42,\
            "numberOfShares":123}}""".formatted(QxAssetOrderData.class.getSimpleName());

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
             EXTRA_DATA);

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private ReactiveRedisTemplate<String, Transaction> redisTransactionOperations;
    @Autowired
    private ReactiveStringRedisTemplate redisStringTemplate;

    @Test
    void putTransaction() {

        StepVerifier.create(transactionRepository.putTransaction(TX))
                .expectNext(TX)
                .verifyComplete();

        StepVerifier.create(redisTransactionOperations.<String, Transaction>opsForHash()
                .get("tx:transaction-hash", "tx"))
                .expectNext(TX)
                .verifyComplete();

        StepVerifier.create(redisStringTemplate.opsForHash().get("tx:transaction-hash", "tx"))
                .expectNext(SERIALIZED_TX)
                .verifyComplete();

    }

    @Test
    void getTransaction() {

        Mono<Transaction> storeAndGetTransaction = redisStringTemplate.opsForHash()
                .put("tx:foo", "tx", SERIALIZED_TX)
                .then(transactionRepository.getTransaction("foo"));

        StepVerifier.create(storeAndGetTransaction)
                .expectNext(TX)
                .verifyComplete();

    }

}
