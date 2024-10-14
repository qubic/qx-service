package org.qubic.qx.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtil {

    private static final ObjectMapper JSON = new ObjectMapper();

    public static String toJson(final Object toBeConverted) {
        try {
            return JSON.writeValueAsString(toBeConverted);
        } catch (JsonProcessingException e) {
            log.error("Could not convert: {}", toBeConverted);
            throw new RuntimeException("Could not convert to json.", e);
        }
    }

}
