package org.qubic.qx.api.scheduler.mapping;

public interface RedisToDomainMapper<T, S> {

    T map(S source);

}
