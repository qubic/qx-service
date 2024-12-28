package org.qubic.qx.sync.repository;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.sync.repository.domain.TransactionMessage;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

@Slf4j
public class TransactionRepository {

    public static final String KEY_QUEUE_TRANSACTIONS = "queue:transactions";

    private final ReactiveRedisTemplate<String, TransactionMessage> redisTransactionOperations;

    public TransactionRepository(ReactiveRedisTemplate<String, TransactionMessage> redisTransactionOperations) {
        this.redisTransactionOperations = redisTransactionOperations;
    }

    public Mono<TransactionMessage> putTransactionIntoQueue(TransactionMessage transaction) {
        return redisTransactionOperations.opsForList().leftPush(KEY_QUEUE_TRANSACTIONS, transaction)
                .doOnNext(count -> log.info("Pushed transaction [{}] into transaction queue. Queue length: [{}].", transaction.transactionHash(), count))
                .map(added -> transaction);
    }

}
