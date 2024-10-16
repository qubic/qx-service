package org.qubic.qx.api;

import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;


@ImportTestcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(properties = """
    il.client.scheme=http
    il.client.host=localhost
    il.client.port=1234
    spring.data.redis.port=26379
""")
public abstract class AbstractSpringIntegrationTest {

    // test database

    @ServiceConnection
    static PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:14.13");

    // test redis

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
                .port(26379)
                .build();
    }

    // mock integration api for test

    protected final MockWebServer integrationLayer = new MockWebServer();

    protected void prepareResponse(Consumer<MockResponse> consumer) {
        MockResponse response = new MockResponse();
        consumer.accept(response);
        integrationLayer.enqueue(response);
    }

    @SneakyThrows
    protected void assertRequest(String expectedPath) {
        RecordedRequest request = integrationLayer.takeRequest(1, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getPath()).isEqualTo(expectedPath);
    }

    @BeforeEach
    void setUp() throws Exception {
        integrationLayer.start(1234);
    }

    @AfterEach
    void tearDown() throws Exception {
        integrationLayer.shutdown();
    }



}
