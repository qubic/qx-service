package org.qubic.qx.api.db.convert;

import org.postgresql.util.PGobject;
import org.qubic.qx.api.db.domain.ExtraData;
import org.qubic.qx.api.util.JsonUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ExtraDataReadingConverter implements Converter<PGobject, ExtraData> {

    @Override
    public ExtraData convert(PGobject source) {
        String json = source.getValue();
        return JsonUtil.fromJson(json, ExtraData.class);
    }

}
