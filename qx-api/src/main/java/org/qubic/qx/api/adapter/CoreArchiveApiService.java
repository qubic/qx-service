package org.qubic.qx.api.adapter;

import org.qubic.qx.api.adapter.domain.TickData;

public interface CoreArchiveApiService {

    TickData getTickData(long tick);

}
