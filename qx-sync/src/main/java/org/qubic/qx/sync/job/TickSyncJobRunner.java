package org.qubic.qx.sync.job;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.Serializable;
import java.time.Duration;
import java.util.NoSuchElementException;

@Slf4j
public class TickSyncJobRunner {

    private final TickSyncJob syncJob;
    private final Duration sleepDuration;

    public TickSyncJobRunner(TickSyncJob syncJob, Duration sleepDuration) {
        this.syncJob = syncJob;
        this.sleepDuration = sleepDuration;
    }

    public void loopForever() {

        Flux<? extends Serializable> syncLoop = runSyncJobMono()
                .flatMap(syncJob::updateLatestSyncedTick)
                .doOnNext(tick -> log.debug("Sync to [{}] completed.", tick))
                .doOnError(t -> log.error("Error running sync job.", t))
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofMillis(100)))
                .doOnTerminate(() -> log.debug("Sync run finished. Next run in [{}].", sleepDuration))
                .repeatWhen(repeat -> repeat.delayElements(sleepDuration));

        syncLoop.subscribe(
                        x -> {},
                        err -> log.error("Finished with error.", err),
                        () -> log.debug("Completed sync loop.")
                );

    }

    private Mono<Long> runSyncJobMono() {
        return syncJob.sync()
                .last()
                .onErrorResume(NoSuchElementException.class, e -> Mono.empty());
    }

}
