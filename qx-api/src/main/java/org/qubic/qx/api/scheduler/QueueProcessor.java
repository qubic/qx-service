package org.qubic.qx.api.scheduler;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.redis.repository.QueueProcessingRepository;
import org.qubic.qx.api.scheduler.mapping.RedisToDomainMapper;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class QueueProcessor<T, S> {

    private final QueueProcessingRepository<S> redisRepository;
    private final CrudRepository<T, Long> repository;
    private final RedisToDomainMapper<T, S> mapper;

    public QueueProcessor(QueueProcessingRepository<S> redisRepository, CrudRepository<T, Long> repository, RedisToDomainMapper<T, S> mapper) {
        this.redisRepository = redisRepository;
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<T> process() {
        ArrayList<T> processed = new ArrayList<>();

        boolean itemsAvailable = true;
        do  {
            S sourceDto = redisRepository.readFromQueue();
            if (sourceDto == null) {
                itemsAvailable = false;
                log.debug("Queue is empty");
            } else {
                log.info("Processing from queue: {}", sourceDto);
                process(sourceDto).ifPresent(processed::add);
            }

        } while (itemsAvailable);
        return processed;
    }

    protected Optional<T> process(@NonNull S sourceDto) {
        try {
            T targetDto = mapper.map(sourceDto);
            targetDto = repository.save(targetDto);
            log.info("Saved to database: {}", targetDto);
            postProcess(targetDto, sourceDto);
            removeFromProcessingQueue(sourceDto);
            return Optional.of(targetDto);
        } catch (RuntimeException e) {
            log.error("Error processing redis message {}.", sourceDto, e);
            Long length = redisRepository.pushIntoErrorsQueue(sourceDto);
            log.warn("Moved message into error queue. Error queue length: [{}].", length);
            removeFromProcessingQueue(sourceDto);
            return Optional.empty();
        }
    }

    protected void postProcess(@NonNull final T targetDto, @NonNull final S sourceDto) {
    }

    protected void removeFromProcessingQueue(S sourceDto) {
        Long removed = redisRepository.removeFromProcessingQueue(sourceDto);
        log.info("Removed [{}] messages from processing queue.", removed);
    }

}
