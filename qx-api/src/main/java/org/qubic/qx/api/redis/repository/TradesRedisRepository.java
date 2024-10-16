package org.qubic.qx.api.redis.repository;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.redis.dto.TradeRedisDto;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
public class TradesRedisRepository implements QueueProcessingRepository<TradeRedisDto> {

    static final String KEY_QUEUE_RECEIVE = "queue:trades";
    static final String KEY_QUEUE_PROCESS = "queue:trades:processing";
    static final String KEY_QUEUE_ERRORS = "queue:trades:errors";

    private final RedisTemplate<String, TradeRedisDto> redisTemplate;

    public TradesRedisRepository(RedisTemplate<String, TradeRedisDto> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public TradeRedisDto readFromQueue() {
        return redisTemplate.opsForList().rightPopAndLeftPush(KEY_QUEUE_RECEIVE, KEY_QUEUE_PROCESS);
    }

    public Long removeFromProcessingQueue(TradeRedisDto trade) {
        return redisTemplate.opsForList().remove(KEY_QUEUE_PROCESS, 1, trade);
    }

    public Long pushIntoErrorsQueue(TradeRedisDto trade) {
        log.warn("Push into errors queue: {}", trade);
        return redisTemplate.opsForList().leftPush(KEY_QUEUE_ERRORS, trade);
    }

}
