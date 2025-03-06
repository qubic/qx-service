package org.qubic.qx.sync.api;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.api.domain.SyncStatus;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicLong;

import static org.qubic.qx.sync.job.TickSyncJob.*;

class StatusControllerTest {

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final StatusController controller = new StatusController(meterRegistry);

    @Test
    void getSyncStatus() {
        meterRegistry.gauge(METRIC_LATEST_TICK, Tags.of(TAG_KEY_SOURCE, TAG_VALUE_SYNCED), new AtomicLong(1));
        meterRegistry.gauge(METRIC_LATEST_TICK, Tags.of(TAG_KEY_SOURCE, TAG_VALUE_EVENTS), new AtomicLong(2));
        meterRegistry.gauge(METRIC_LATEST_TICK, Tags.of(TAG_KEY_SOURCE, TAG_VALUE_LIVE), new AtomicLong(3));

        StepVerifier.create(controller.getSyncStatus())
                .expectNext(SyncStatus.builder()
                        .latestProcessedTick(1L)
                        .latestEventTick(2L)
                        .latestLiveTick(3L)
                        .build())
                .verifyComplete();
    }

}