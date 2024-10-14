package org.qubic.qx.sync.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.QxApiService;
import org.qubic.qx.sync.adapter.il.IntegrationCoreApiService;
import org.qubic.qx.sync.adapter.il.IntegrationQxApiService;
import org.qubic.qx.sync.adapter.il.mapping.IlCoreMapper;
import org.qubic.qx.sync.adapter.il.mapping.IlQxMapper;
import org.qubic.qx.sync.properties.IntegrationClientProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@Slf4j
@ConditionalOnProperty(value = "backend", havingValue = "integration", matchIfMissing = true)
@Configuration
public class IntegrationLayerConfig {

    @ConfigurationProperties(prefix = "il.client", ignoreUnknownFields = false)
    @Bean
    IntegrationClientProperties integrationClientProperties() {
        return new IntegrationClientProperties();
    }

    @Bean
    WebClient integrationApiWebClient(IntegrationClientProperties properties) {

        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(3));

        URI uri = UriComponentsBuilder.newInstance()
                .scheme(properties.getScheme())
                .host(properties.getHost())
                .port(StringUtils.stripToNull(properties.getPort()))
                .build()
                .toUri();

        log.info("Integration layer API url: {}", uri);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(uri.toString())
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                })
                .build();
    }

    @Bean
    CoreApiService integrationCoreApiService(WebClient integrationApiWebClient, IlCoreMapper transactionMapper) {
        return new IntegrationCoreApiService(integrationApiWebClient, transactionMapper);
    }

    @Bean
    QxApiService integrationQxApiService(WebClient integrationApiWebClient, IlQxMapper qxIntegrationMapper) {
        return new IntegrationQxApiService(integrationApiWebClient, qxIntegrationMapper);
    }

}
