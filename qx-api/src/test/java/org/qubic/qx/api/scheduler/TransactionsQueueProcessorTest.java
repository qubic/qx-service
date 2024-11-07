package org.qubic.qx.api.scheduler;

import at.qubic.api.crypto.IdentityUtil;
import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.db.AssetsRepository;
import org.qubic.qx.api.db.TransactionsRepository;
import org.qubic.qx.api.db.domain.Transaction;
import org.qubic.qx.api.redis.QxCacheManager;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;
import org.qubic.qx.api.redis.repository.TransactionsRedisRepository;
import org.qubic.qx.api.scheduler.mapping.TransactionMapper;

import static org.mockito.Mockito.mock;

@Slf4j
class TransactionsQueueProcessorTest extends QueueProcessorTest<Transaction, TransactionRedisDto> {

    public TransactionsQueueProcessorTest() {
        this.redisRepository = mock(TransactionsRedisRepository.class);
        this.repository = mock(TransactionsRepository.class);
        this.mapper = mock(TransactionMapper.class);
        IdentityUtil identityUtil = mock();
        AssetsRepository assetsRepository = mock();
        QxCacheManager qxCacheManager = mock();
        processor = new TransactionsProcessor(redisRepository, repository, mapper, identityUtil, assetsRepository, qxCacheManager);
    }

    @Override
    protected Transaction createTargetMock() {
        return mock();
    }

    @Override
    protected TransactionRedisDto createSourceMock() {
        return mock();
    }

}