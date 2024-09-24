package org.qubic.qx.repository;

import org.junit.jupiter.api.Test;
import org.qubic.qx.AbstractRedisTest;
import org.qubic.qx.domain.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(properties = {"spring.data.redis.port=16379"})
public class TransactionRepositorySpringIT extends AbstractRedisTest {

    private static final String SERIALIZED_TX = """
            {"transactionHash":"transaction-hash","sourcePublicId":"source-id","destinationPublicId":"target-id","amount":1,"tick":2,"inputType":3,"inputSize":4,"extraData":"01020304"}""";
    private static final Transaction TX = new Transaction("transaction-hash", "source-id", "target-id", 1, 2, 3, 4, "01020304");


    private final TransactionRepository transactionRepository;
    private final ReactiveRedisTemplate<String, Transaction> redisTransactionOperations;
    private final ReactiveStringRedisTemplate redisStringTemplate;

    @Autowired
    public TransactionRepositorySpringIT(TransactionRepository transactionRepository, ReactiveRedisTemplate<String, Transaction> redisTransactionOperations, ReactiveStringRedisTemplate redisStringTemplate) {
        this.transactionRepository = transactionRepository;
        this.redisTransactionOperations = redisTransactionOperations;
        this.redisStringTemplate = redisStringTemplate;
    }

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
