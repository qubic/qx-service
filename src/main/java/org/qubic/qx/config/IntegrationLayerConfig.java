package org.qubic.qx.config;

import org.qubic.qx.adapter.CoreApiService;
import org.qubic.qx.adapter.QxApiService;
import org.qubic.qx.adapter.il.IntegrationCoreApiService;
import org.qubic.qx.adapter.il.IntegrationQxApiService;
import org.qubic.qx.adapter.il.mapping.IlQxMapper;
import org.qubic.qx.adapter.il.mapping.IlTransactionMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;

@ConditionalOnProperty(value = "backend", havingValue = "integration", matchIfMissing = true)
@Configuration
public class IntegrationLayerConfig {

    @Bean
    WebClient integrationApiWebClient(@Value("${il.base-url}") String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(3));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseUrl)
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                })
                .build();
    }

    @Bean
    CoreApiService integrationCoreApiService(WebClient integrationApiWebClient, IlTransactionMapper transactionMapper) {
        return new IntegrationCoreApiService(integrationApiWebClient, transactionMapper);
    }

    @Bean
    QxApiService integrationQxApiService(WebClient integrationApiWebClient, IlQxMapper qxIntegrationMapper) {
        return new IntegrationQxApiService(integrationApiWebClient, qxIntegrationMapper);
    }

}
