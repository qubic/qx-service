package org.qubic.qx.api.db.dto;

import java.io.Serializable;
import java.time.Instant;

public record OhlcPriceData(Instant time,
                            long open,
                            long high,
                            long low,
                            long close)
        implements Serializable
{ }
