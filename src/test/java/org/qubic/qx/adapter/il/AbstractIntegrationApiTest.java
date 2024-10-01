package org.qubic.qx.adapter.il;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
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

    @SuppressWarnings("SameParameterValue")
    protected @NotNull WebClient createWebClient(String baseUrl) {

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(1));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                })
                .build();
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