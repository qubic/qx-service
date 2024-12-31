package org.qubic.qx.sync.job;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.io.Serializable;
import java.time.Duration;
import java.util.NoSuchElementException;

@Slf4j
public class TickSyncJobRunner {

    private final TickSyncJob syncJob;
    private final Duration sleepDuration;
    private final Duration retryDuration;

    public TickSyncJobRunner(TickSyncJob syncJob, Duration sleepDuration, Duration retryDuration) {
        this.syncJob = syncJob;
        this.sleepDuration = sleepDuration;
        log.info("Sync interval: {}", sleepDuration);
        this.retryDuration = retryDuration;
        log.info("Sync retry interval: {}", retryDuration);

    }

    public void loopForever() {

        Flux<? extends Serializable> syncLoop = runSyncJobMono()
                .flatMap(syncJob::updateLatestSyncedTick)
                .doOnNext(tick -> log.debug("Sync to [{}] completed.", tick))
                .doOnError(t -> log.error("Error running sync job.", t))
                .retryWhen(getRetrySpec())
                .doOnTerminate(() -> log.debug("Sync run finished. Next run in [{}].", sleepDuration))
                .repeatWhen(repeat -> repeat.delayElements(sleepDuration));

        syncLoop.subscribe(
                        x -> {},
                        err -> log.error("Finished with error.", err),
                        () -> log.debug("Completed sync loop.")
                );

    }

    private RetryBackoffSpec getRetrySpec() {
        // important: do not backoff on retry. this will increase back off interval too much in case there are
        // frequent failures like rate limiting.
        return Retry.fixedDelay(Long.MAX_VALUE, retryDuration)
                .doBeforeRetry(retrySignal -> log.info("Retrying sync job. Subsequent retries [{}], total retries [{}].",
                        retrySignal.totalRetriesInARow(), retrySignal.totalRetries()));
    }

    private Mono<Long> runSyncJobMono() {
        return syncJob.sync()
                .last()
                .onErrorResume(NoSuchElementException.class, e -> Mono.empty());
    }

}
