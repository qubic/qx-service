package org.qubic.qx.api.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.redis.repository.QueueProcessingRepository;
import org.qubic.qx.api.scheduler.mapping.RedisToDomainMapper;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class QueueProcessor<T, S> {

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
            S dto = redisRepository.readFromQueue();
            if (dto == null) {
                itemsAvailable = false;
                log.debug("Transaction queue is empty");
            } else {
                log.info("Processing from queue: {}", dto);
                process(dto).ifPresent(processed::add);
            }

        } while (itemsAvailable);
        return processed;
    }

    protected Optional<T> process(S dto) {
        try {
            T trade = mapper.map(dto);
            trade = repository.save(trade);
            log.info("Saved to database: {}", trade);
            return Optional.of(trade);
        } catch (RuntimeException e) {
            log.error("Error processing redis message {}.", dto, e);
            Long length = redisRepository.pushIntoErrorsQueue(dto);
            log.warn("Moved message into error queue. Error queue length: [{}].", length);
            Long removed = redisRepository.removeFromProcessingQueue(dto);
            log.warn("Removed [{}] messages from processing queue.", removed);
            return Optional.empty();
        }
    }
}
