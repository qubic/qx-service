package org.qubic.qx.api.redis.repository;

public interface QueueProcessingRepository<T> {

    T readFromQueue();
    Long removeFromProcessingQueue(T trade);
    Long pushIntoErrorsQueue(T trade);

}
