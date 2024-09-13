package org.qubic.qx.sync;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

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

    public void loopUntilTargetTick(long syncToTick) {

        Flux<Long> syncTicks = Mono.just(syncToTick)
                .doOnNext(t -> this.targetTick = t)
                .flatMapMany(syncJob::sync);

        Mono<Boolean> updateSyncedTick = Mono.defer(() -> syncJob.updateLatestSyncedTick(syncToTick));

        Flux.concat(syncTicks, updateSyncedTick)
                .retryWhen(Retry.indefinitely())
                .doOnComplete(() -> log.info("Tick sync to [{}] completed.", this.targetTick))
                .doOnError(t -> log.error("Error syncing to tick [{}].", this.targetTick, t))
                .subscribe();

    }

    public void loopForever() {

        Flux<Long> syncTicks = Flux.defer(() -> syncJob.getCurrentTick()
                        .doOnNext(tick -> targetTick = tick)
                        .flatMapMany(syncJob::sync));

        Mono<Boolean> updateSyncedTick = Mono.defer(() -> syncJob.updateLatestSyncedTick(targetTick));

        Flux.concat(syncTicks, updateSyncedTick)
                .retryWhen(Retry.indefinitely())
                .doOnComplete(() -> log.info("Sync to [{}] completed.", targetTick))
                .doOnError(t -> log.error("Error syncing to tick [{}].", targetTick, t))
                .doOnTerminate(() -> log.info("Sync terminated. Next sync run at [{}].", Instant.now().plus(sleepDuration)))
                .repeatWhen(repeat -> repeat.delayElements(sleepDuration))
                .subscribe();

    }

}
