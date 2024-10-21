package org.qubic.qx.api.scheduler;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.TransactionsRepository;
import org.qubic.qx.api.db.domain.Transaction;
import org.qubic.qx.api.redis.dto.TransactionRedisDto;
import org.qubic.qx.api.redis.repository.TransactionsRedisRepository;
import org.qubic.qx.api.scheduler.mapping.TransactionMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class QueueProcessorTest {

    private final TransactionsRedisRepository redisRepository = mock();
    private final TransactionsRepository repository = mock();
    private final TransactionMapper mapper = mock();

    private final QueueProcessor<Transaction, TransactionRedisDto> processor = new QueueProcessor<>(redisRepository, repository, mapper);

    @Test
    void process_thenSaveAndReturnDto() {
        TransactionRedisDto redisDto = mock();
        Transaction targetDto = mock();

        when(redisRepository.readFromQueue()).thenReturn(redisDto, (TransactionRedisDto) null);
        when(mapper.map(redisDto)).thenReturn(targetDto);
        when(repository.save(targetDto)).thenReturn(targetDto);

        List<Transaction> targetDtos = processor.process();
        assertThat(targetDtos).contains(targetDto);

        verify(redisRepository).removeFromProcessingQueue(redisDto);
        verify(redisRepository, never()).pushIntoErrorsQueue(any());
    }

    @Test
    void process_givenMappingError_thenMoveIntoErrorQueue() {
        TransactionRedisDto redisDto = mock();

        when(redisRepository.readFromQueue()).thenReturn(redisDto, (TransactionRedisDto) null);
        when(mapper.map(redisDto)).thenThrow(new RuntimeException("exception for test"));

        List<Transaction> targetDtos = processor.process();
        assertThat(targetDtos).isEmpty();

        verifyNoInteractions(repository);
        verify(redisRepository).pushIntoErrorsQueue(redisDto);
        verify(redisRepository).removeFromProcessingQueue(redisDto);
    }

    @Test
    void process_givenDatabaseError_thenMoveIntoErrorQueue() {
        TransactionRedisDto redisDto = mock();
        Transaction targetDto = mock();

        when(redisRepository.readFromQueue()).thenReturn(redisDto, (TransactionRedisDto) null);
        when(mapper.map(redisDto)).thenReturn(targetDto);
        when(repository.save(targetDto)).thenThrow(new RuntimeException("exception for test"));

        List<Transaction> targetDtos = processor.process();
        assertThat(targetDtos).isEmpty();

        verify(redisRepository).pushIntoErrorsQueue(redisDto);
        verify(redisRepository).removeFromProcessingQueue(redisDto);
    }

}