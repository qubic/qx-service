package org.qubic.qx.sync.api;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.sync.api.domain.SyncStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static org.qubic.qx.sync.job.TickSyncJob.*;

@Slf4j
@RestController
@RequestMapping("/v1/status")
public class StatusController {

    private final MeterRegistry meterRegistry;

    public StatusController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @GetMapping("/sync")
    public Mono<SyncStatus> getSyncStatus() {
        Gauge syncedTick = find(TAG_VALUE_SYNCED);
        Gauge eventTick = find(TAG_VALUE_EVENTS);
        Gauge liveTick = find(TAG_VALUE_LIVE);

        return Mono.just(SyncStatus.builder()
                .latestProcessedTick(getGaugeValue(syncedTick))
                .latestEventTick(getGaugeValue(eventTick))
                .latestLiveTick(getGaugeValue(liveTick))
                .build());
    }

    private Gauge find(String tagValue) {
        return meterRegistry.find(METRIC_LATEST_TICK).tag(TAG_KEY_SOURCE, tagValue).gauge();
    }

    private static Long getGaugeValue(Gauge gauge) {
        return gauge == null ? null : (long) gauge.value();
    }

}
