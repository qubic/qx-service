package org.qubic.qx.api.db.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.sql.Date;
import java.time.Instant;
import java.time.ZoneOffset;

@ReadingConverter
public class SqlDateToInstantConverter implements Converter<java.sql.Date, Instant> {

    @Override
    public Instant convert(Date source) {
        return source.toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC).toInstant();
    }

}
