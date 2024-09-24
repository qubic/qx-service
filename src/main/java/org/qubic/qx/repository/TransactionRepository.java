package org.qubic.qx.repository;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.domain.Transaction;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

@Slf4j
public class TransactionRepository {

    public static final String KEY_TRANSACTION = "tx:%s";

    private final ReactiveRedisTemplate<String, Transaction> redisTransactionOperations;

    public TransactionRepository(ReactiveRedisTemplate<String, Transaction> redisTransactionOperations) {
        this.redisTransactionOperations = redisTransactionOperations;
    }

    public Mono<Transaction> putTransaction(Transaction transaction) {
        return redisTransactionOperations.opsForHash()
                .put(String.format(KEY_TRANSACTION, transaction.transactionHash()), "tx", transaction)
                .doOnNext(added -> log.info("Put transaction [{}]: [{}].", transaction.transactionHash(), added))
                .map(added -> transaction);
    }

    public Mono<Transaction> getTransaction(String transactionHash) {
        return redisTransactionOperations.<String, Transaction>opsForHash()
                .get(String.format(KEY_TRANSACTION, transactionHash), "tx")
                .doOnNext(tx -> log.info("Got transaction [{}]: [{}].", transactionHash, tx));
    }

}
