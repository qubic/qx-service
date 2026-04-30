package org.qubic.qx.api.config;

import at.qubic.api.crypto.IdentityUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.qubic.qx.api.adapter.il.DataTypeTranslator;
import org.qubic.qx.api.adapter.il.IntegrationApiService;
import org.qubic.qx.api.adapter.il.IntegrationLiveClient;
import org.qubic.qx.api.adapter.il.QxMapper;
import org.qubic.qx.api.properties.IntegrationClientProperties;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Configuration
public class IntegrationLayerConfig {

    @ConfigurationProperties(prefix = "il.core.client", ignoreUnknownFields = false)
    @Bean(name="coreClientProperties")
    IntegrationClientProperties integrationCoreClientProperties() {
        return new IntegrationClientProperties();
    }

    @Bean(name="coreClient")
    WebClient integrationCoreWebClient(@Qualifier("coreClientProperties") IntegrationClientProperties properties, WebClient.Builder builder) {
        HttpClient httpClient = createHttpClient();
        URI uri = createUri(properties);
        log.info("Integration layer core API url: {}", uri);
        return createClient(builder, httpClient, uri);
    }

    @Bean
    IntegrationLiveClient integrationCoreApiService(@Qualifier("coreClient") WebClient webClient) {
        return new IntegrationLiveClient(webClient);
    }

    @Bean
    DataTypeTranslator dataTypeTranslator(IdentityUtil identityUtil) {
        return new DataTypeTranslator(identityUtil);
    }

    @Bean
    IntegrationApiService integrationQxApiService(IdentityUtil identityUtil, IntegrationLiveClient client, QxMapper qxIntegrationMapper) {
        return new IntegrationApiService(identityUtil, client, qxIntegrationMapper);
    }

    // helper methods
    private static HttpClient createHttpClient() {
        return HttpClient.create()
                .responseTimeout(Duration.ofSeconds(3));
    }

    private static URI createUri(IntegrationClientProperties properties) {
        return UriComponentsBuilder.newInstance()
                .scheme(properties.getScheme())
                .host(properties.getHost())
                .port(StringUtils.stripToNull(properties.getPort()))
                .build()
                .toUri();
    }

    private static WebClient createClient(WebClient.Builder builder, HttpClient httpClient, URI uri) {
        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(uri.toString())
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                })
                .build();
    }

}
