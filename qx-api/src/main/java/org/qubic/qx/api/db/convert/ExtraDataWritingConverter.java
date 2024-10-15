package org.qubic.qx.api.db.convert;

import lombok.SneakyThrows;
import org.postgresql.util.PGobject;
import org.qubic.qx.api.db.domain.ExtraData;
import org.qubic.qx.api.util.JsonUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class ExtraDataWritingConverter implements Converter<ExtraData, PGobject> {

    @SneakyThrows
    @Override
    public PGobject convert(ExtraData source) {
        String json = JsonUtil.toJson(source);
        PGobject pgObject = new PGobject();
        pgObject.setType("json");
        pgObject.setValue(json);
        return pgObject;
    }

}
