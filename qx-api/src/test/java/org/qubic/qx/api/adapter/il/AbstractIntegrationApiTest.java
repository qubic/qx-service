package org.qubic.qx.api.adapter.il;

import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = """
    il.client.scheme=http
    il.client.host=localhost
    il.client.port=1234
    backend=integration
""")
abstract class AbstractIntegrationApiTest {

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