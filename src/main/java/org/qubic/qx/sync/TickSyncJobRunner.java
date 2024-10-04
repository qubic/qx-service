package org.qubic.qx.sync;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

@Slf4j
public class TickSyncJobRunner {

    private final TickSyncJob syncJob;
    private final Duration sleepDuration;
    private long targetTick;

    public TickSyncJobRunner(TickSyncJob syncJob, Duration sleepDuration) {
        this.syncJob = syncJob;
        this.sleepDuration = sleepDuration;
    }

    public void loopForever() {

        Mono<?> updateAllOrderBooks = syncJob.updateAllOrderBooks();

        Flux<Long> syncTicks = Flux.defer(() -> syncJob.getCurrentTick()
                        .doOnNext(tick -> targetTick = tick)
                        .flatMapMany(syncJob::sync));

        Mono<Boolean> updateSyncedTick = Mono.defer(() -> syncJob.updateLatestSyncedTick(targetTick));

        Flux<? extends Serializable> syncLoop = Flux.concat(syncTicks, updateSyncedTick)
                .doOnComplete(() -> log.debug("Sync to [{}] completed.", targetTick))
                .doOnError(t -> log.error("Error syncing to tick [{}].", targetTick, t))
                .retryWhen(Retry.indefinitely())
                .doOnTerminate(() -> log.info("Sync run finished. Next run at [{}].", Instant.now().plus(sleepDuration)))
                .repeatWhen(repeat -> repeat.delayElements(sleepDuration));

        updateAllOrderBooks
                .thenMany(syncLoop)
                .subscribe(
                        x -> log.debug("Sync loop emitted value: {}", x),
                        err -> log.error("Finished with error.", err),
                        () -> log.debug("Completed sync loop.")
                );

    }

}
