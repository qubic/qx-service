package org.qubic.qx.api;

import com.redis.testcontainers.RedisContainer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.CacheManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;


@ImportTestcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(properties = """
    il.qx.client.scheme=http
    il.qx.client.host=localhost
    il.qx.client.port=1234
    il.archive.client.scheme=http
    il.archive.client.host=localhost
    il.archive.client.port=1234
    il.core.client.scheme=http
    il.core.client.host=localhost
    il.core.client.port=1234
    spring.data.redis.port=26379
    spring.cache.type=NONE
""")
@Slf4j
public abstract class AbstractSpringIntegrationTest {

    // test database
    @ServiceConnection
    static PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:14.13");

    // test redis db
    @ServiceConnection
    static RedisContainer redisContainer = new RedisContainer("redis:7.0.15");

    // caching
    @Autowired
    protected CacheManager cacheManager;

    protected void evictAllCaches() {
        for(String name : cacheManager.getCacheNames()){
            log.info("Evicting cache [{}].", name);
            Objects.requireNonNull(cacheManager.getCache(name)).clear();
        }
    }

    // test web server (integration api)
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
        assertThat(request.getPath()).startsWith(expectedPath);
    }

    @BeforeEach
    protected void setUp() throws Exception {
        integrationLayer.start(1234);
    }

    @AfterEach
    protected void tearDown() throws Exception {
        log.info("Shutting down integration layer.");
        integrationLayer.shutdown();
        log.info("Integration layer shut down.");
    }

}
