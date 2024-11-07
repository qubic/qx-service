package org.qubic.qx.api.scheduler;

import org.qubic.qx.api.db.TradesRepository;
import org.qubic.qx.api.db.domain.Trade;
import org.qubic.qx.api.redis.QxCacheManager;
import org.qubic.qx.api.redis.dto.TradeRedisDto;
import org.qubic.qx.api.redis.repository.TradesRedisRepository;
import org.qubic.qx.api.scheduler.mapping.TradeMapper;

import static org.mockito.Mockito.mock;

class TradesQueueProcessorTest extends QueueProcessorTest<Trade, TradeRedisDto> {

    public TradesQueueProcessorTest() {
        this.redisRepository = mock(TradesRedisRepository.class);
        this.repository = mock(TradesRepository.class);
        this.mapper = mock(TradeMapper.class);
        QxCacheManager qxCacheManager = mock();
        processor = new TradesProcessor(redisRepository, repository, mapper, qxCacheManager);
    }

    @Override
    protected Trade createTargetMock() {
        return mock();
    }

    @Override
    protected TradeRedisDto createSourceMock() {
        return mock();
    }
}