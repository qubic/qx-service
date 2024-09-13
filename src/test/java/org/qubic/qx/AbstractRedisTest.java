package org.qubic.qx;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import redis.embedded.RedisServer;

import java.io.IOException;

public class AbstractRedisTest {

    protected static final RedisServer REDIS_SERVER = createRedis();

    @BeforeAll
    public static void startRedis() throws IOException {
        REDIS_SERVER.start();
    }

    @AfterAll
    public static void stopRedis() throws IOException {
        REDIS_SERVER.stop();
    }

    @SneakyThrows
    private static RedisServer createRedis() {
        return RedisServer.newRedisServer()
                .port(16379)
                .build();
    }

}
