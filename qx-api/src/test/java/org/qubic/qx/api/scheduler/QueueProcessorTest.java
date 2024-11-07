package org.qubic.qx.api.scheduler;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.redis.repository.QueueProcessingRepository;
import org.qubic.qx.api.scheduler.mapping.RedisToDomainMapper;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class QueueProcessorTest<T, S> {

    protected QueueProcessingRepository<S> redisRepository;
    protected CrudRepository<T, Long> repository;
    protected RedisToDomainMapper<T, S> mapper;

    protected QueueProcessor<T, S> processor;

    public QueueProcessorTest() {
        this.redisRepository = mock();
        this.repository = mock();
        this.mapper = mock();
        processor = new QueueProcessor<>(redisRepository, repository, mapper) { };
    }

    @Test
    void process_thenSaveAndReturnDto() {
        S redisDto = createSourceMock();
        T targetDto = createTargetMock();

        when(redisRepository.readFromQueue()).thenReturn(redisDto, (S) null);
        when(mapper.map(redisDto)).thenReturn(targetDto);
        when(repository.save(targetDto)).thenReturn(targetDto);

        List<T> targetDtos = processor.process();
        assertThat(targetDtos).contains(targetDto);

        verify(redisRepository).removeFromProcessingQueue(redisDto);
        verify(redisRepository, never()).pushIntoErrorsQueue(any());
    }

    @Test
    void process_givenMappingError_thenMoveIntoErrorQueue() {
        S redisDto = createSourceMock();

        when(redisRepository.readFromQueue()).thenReturn(redisDto, (S) null);
        when(mapper.map(redisDto)).thenThrow(new RuntimeException("exception for test"));

        List<T> targetDtos = processor.process();
        assertThat(targetDtos).isEmpty();

        verifyNoInteractions(repository);
        verify(redisRepository).pushIntoErrorsQueue(redisDto);
        verify(redisRepository).removeFromProcessingQueue(redisDto);
    }

    @Test
    void process_givenDatabaseError_thenMoveIntoErrorQueue() {
        S redisDto = createSourceMock();
        T targetDto = createTargetMock();

        when(redisRepository.readFromQueue()).thenReturn(redisDto, (S) null);
        when(mapper.map(redisDto)).thenReturn(targetDto);
        when(repository.save(targetDto)).thenThrow(new RuntimeException("exception for test"));

        List<T> targetDtos = processor.process();
        assertThat(targetDtos).isEmpty();

        verify(redisRepository).pushIntoErrorsQueue(redisDto);
        verify(redisRepository).removeFromProcessingQueue(redisDto);
    }

    // needed to create source and targets with correct type
    protected T createTargetMock() { return mock(); }
    protected S createSourceMock() { return mock(); }

}