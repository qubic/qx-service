package org.qubic.qx.adapter.il;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.qubic.qx.adapter.il.qx.QxApiClient;
import org.qubic.qx.adapter.il.qx.domain.QxFees;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.util.List;

@Tag("SIT") // TODO replace with mock server
class QxApiClientIT {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://95.216.243.140/")
            .defaultHeaders(httpHeaders -> httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON)))
            .build();
    private final QxApiClient apiClient = new QxApiClient(webClient);

    @Test
    void getFees() {
        StepVerifier.create(apiClient.getFees())
                .expectNext(new QxFees(1_000_000_000, 1_000_000, 5_000_000))
                .verifyComplete();
    }

    @Test
    void getAskOrders() {
        // CFB
        // CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL
        StepVerifier.create(apiClient.getAskOrders("CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL", "CFB"))
                .expectNextCount(1)
                .verifyComplete();
    }

}