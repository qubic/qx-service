package org.qubic.qx.api.controller.domain;

import java.io.Serializable;
import java.time.Instant;

public record OhlcPriceData(Instant time,
                            long open,
                            long high,
                            long low,
                            long close)
        implements Serializable
{ }
