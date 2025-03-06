package org.qubic.qx.sync.api;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.api.domain.SyncStatus;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class StatusControllerIT {

    private WebTestClient client;

    @BeforeEach
    public void setUpTestNode(ApplicationContext context) {
        client = WebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .baseUrl("/service/v1/status")
                .build();
    }

    @Test
    void getSyncStatus() {
        client.get().uri("/sync")
                .exchange()
                .expectStatus().isOk()
                .expectBody(SyncStatus.class)
                .value(status -> assertThat(status).isEqualTo(SyncStatus.builder()
                                .latestLiveTick(0L)
                                .latestProcessedTick(0L)
                                .latestEventTick(0L)
                        .build()));
    }

}
