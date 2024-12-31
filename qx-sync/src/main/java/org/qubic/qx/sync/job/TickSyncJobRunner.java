package org.qubic.qx.sync.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.io.Serializable;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

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
                .doOnError(logError())
                .retryWhen(getRetrySpec())
                .doOnTerminate(() -> log.debug("Sync run finished. Next run in [{}].", sleepDuration))
                .repeatWhen(repeat -> repeat.delayElements(sleepDuration));

        syncLoop.subscribe(
                        x -> {},
                        err -> log.error("Finished with error.", err),
                        () -> log.debug("Completed sync loop.")
                );

    }

    private static Consumer<Throwable> logError() {
        return t -> {
            if (reactor.core.Exceptions.isRetryExhausted(t) && t.getCause() instanceof WebClientResponseException wce) {
                log.error("Error running sync job: {}. Cause: {}", t.getMessage(), wce.getMessage());
            } else {
                log.error("Error running sync job.", t);
            }
        };
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
