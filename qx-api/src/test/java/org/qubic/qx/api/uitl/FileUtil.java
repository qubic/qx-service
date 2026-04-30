package org.qubic.qx.api.uitl;

import org.apache.commons.io.IOUtils;
import org.qubic.qx.api.util.JsonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class FileUtil {

    public static <T> T readJsonFile(String path, Class<T> clazz) throws IOException {
        return JsonUtil.fromJson(readFile(path), clazz);
    }

    public static String readFile(String path) throws IOException {
        try (InputStream inputStream = FileUtil.class.getResourceAsStream(path)) {
            return IOUtils.toString(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
        }
    }



}
