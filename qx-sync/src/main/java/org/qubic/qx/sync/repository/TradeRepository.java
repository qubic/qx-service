package org.qubic.qx.sync.repository;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.sync.domain.Trade;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

@Slf4j
public class TradeRepository {

    static final String KEY_QUEUE_TRADES = "queue:trades";

    private final ReactiveRedisTemplate<String, Trade> redisTradeTemplate;

    public TradeRepository(ReactiveRedisTemplate<String, Trade> redisTradeTemplate) {
        this.redisTradeTemplate = redisTradeTemplate;
    }

    public Mono<Trade> putTradeIntoQueue(Trade trade) {
        return redisTradeTemplate.opsForList().leftPush(KEY_QUEUE_TRADES, trade)
                .doOnNext(count -> log.info("Pushed trade for transaction [{}] into trades queue. Queue length: [{}].", trade.transactionHash(), count))
                .map(b -> trade);
    }

}
