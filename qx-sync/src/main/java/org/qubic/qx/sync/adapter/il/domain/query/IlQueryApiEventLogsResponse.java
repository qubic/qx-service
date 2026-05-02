package org.qubic.qx.sync.adapter.il.domain.query;

import java.util.List;

public record IlQueryApiEventLogsResponse(IlQueryApiHits hits, List<IlQueryApiEventLog> eventLogs) {
}
