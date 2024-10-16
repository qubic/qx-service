package org.qubic.qx.api.redis.repository;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
public class TransactionsRedisRepository implements QueueProcessingRepository<TransactionRedisDto> {

    static final String KEY_QUEUE_RECEIVE = "queue:transactions";
    static final String KEY_QUEUE_PROCESS = "queue:transactions:processing";
    static final String KEY_QUEUE_ERRORS = "queue:transactions:errors";

    private final RedisTemplate<String, TransactionRedisDto> redisTemplate;

    public TransactionsRedisRepository(RedisTemplate<String, TransactionRedisDto> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public TransactionRedisDto readFromQueue() {
        return redisTemplate.opsForList().rightPopAndLeftPush(KEY_QUEUE_RECEIVE, KEY_QUEUE_PROCESS);
    }

    public Long removeFromProcessingQueue(TransactionRedisDto transaction) {
        return redisTemplate.opsForList().remove(KEY_QUEUE_PROCESS, 1, transaction);
    }

    public Long pushIntoErrorsQueue(TransactionRedisDto transaction) {
        log.warn("Push into errors queue: {}", transaction);
        return redisTemplate.opsForList().leftPush(KEY_QUEUE_ERRORS, transaction);
    }

}
