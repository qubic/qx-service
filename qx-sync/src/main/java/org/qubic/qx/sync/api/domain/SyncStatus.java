package org.qubic.qx.sync.api.domain;

import lombok.Builder;

@Builder
public record SyncStatus(
    Long latestLiveTick,
    Long latestEventTick,
    Long latestProcessedTick)
{ }
