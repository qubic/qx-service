package org.qubic.qx.adapter.il.qx;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.qubic.qx.adapter.il.qx.mapping.QxIntegrationMapper;
import org.qubic.qx.api.domain.Fees;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Tag("SIT") // TODO replace with mock server
class QxIntegrationApiClientIT {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://95.216.243.140/")
            .defaultHeaders(httpHeaders -> httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON)))
            .build();
    private final QxIntegrationMapper qxMapper = Mappers.getMapper(QxIntegrationMapper.class);
    private final QxIntegrationApiClient apiClient = new QxIntegrationApiClient(webClient, qxMapper);

    @Test
    void getFees() {
        StepVerifier.create(apiClient.getFees())
                .expectNext(new Fees(1_000_000_000, 1_000_000, 5_000_000))
                .verifyComplete();
    }

    @Test
    void getAskOrders() {
        StepVerifier.create(apiClient.getAskOrders("CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL", "CFB")
                .doOnNext(l -> log.info("{}", l)))
                .assertNext(l -> assertThat(l).isNotEmpty())
                .verifyComplete();
    }

    @Test
    void getBidOrders() {
        StepVerifier.create(apiClient.getBidOrders("CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL", "CFB")
                        .doOnNext(l -> log.info("{}", l)))
                .assertNext(l -> assertThat(l).isNotEmpty())
                .verifyComplete();
    }

}