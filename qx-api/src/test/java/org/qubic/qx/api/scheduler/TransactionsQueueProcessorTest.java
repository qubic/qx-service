package org.qubic.qx.api.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.TransactionsRepository;
import org.qubic.qx.api.db.domain.Transaction;
import org.qubic.qx.api.redis.QxCacheManager;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;
import org.qubic.qx.api.redis.repository.TransactionsRedisRepository;
import org.qubic.qx.api.scheduler.mapping.TransactionMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Slf4j
class TransactionsQueueProcessorTest extends QueueProcessorTest<Transaction, TransactionRedisDto> {

    public TransactionsQueueProcessorTest() {
        this.redisRepository = mock(TransactionsRedisRepository.class);
        this.repository = mock(TransactionsRepository.class);
        this.mapper = mock(TransactionMapper.class);
        QxCacheManager qxCacheManager = mock();
        processor = new TransactionsProcessor(redisRepository, repository, mapper,  qxCacheManager);
    }

    @Test
    void process_givenNoRelevantEvents_thenDoNotStore() {
        TransactionRedisDto source = createSourceMock();
        when(source.relevantEvents()).thenReturn(false);
        Optional<Transaction> transaction = processor.process(source);
        assertThat(transaction).isEmpty();
        verifyNoInteractions(repository);
        verifyNoInteractions(mapper);
    }

    @Override
    protected Transaction createTargetMock() {
        return mock();
    }

    @Override
    protected TransactionRedisDto createSourceMock() {
        TransactionRedisDto dto = mock();
        when(dto.transactionHash()).thenReturn("foo");
        when(dto.relevantEvents()).thenReturn(true);
        return dto;
    }

}