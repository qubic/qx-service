package org.qubic.qx.api.controller.converter;

import org.qubic.qx.api.controller.domain.ChartInterval;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

public class StringToChartIntervalConverter implements Converter<String, ChartInterval> {

    @Nullable
    @Override
    public ChartInterval convert(String source) {
        return ChartInterval.fromKey( source);
    }
}
