package org.qubic.qx.api.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.qubic.qx.api.adapter.CoreApiService;
import org.qubic.qx.api.adapter.CoreArchiveApiService;
import org.qubic.qx.api.adapter.QxApiService;
import org.qubic.qx.api.adapter.il.*;
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

    @ConfigurationProperties(prefix = "il.qx.client", ignoreUnknownFields = false)
    @Bean(name="qxClientProperties")
    IntegrationClientProperties integrationQxClientProperties() {
        return new IntegrationClientProperties();
    }

    @ConfigurationProperties(prefix = "il.archive.client", ignoreUnknownFields = false)
    @Bean(name="archiveClientProperties")
    IntegrationClientProperties integrationArchiveClientProperties() {
        return new IntegrationClientProperties();
    }

    @ConfigurationProperties(prefix = "il.core.client", ignoreUnknownFields = false)
    @Bean(name="coreClientProperties")
    IntegrationClientProperties integrationCoreClientProperties() {
        return new IntegrationClientProperties();
    }

    @Bean(name="qxClient")
    WebClient integrationQxWebClient(@Qualifier("qxClientProperties") IntegrationClientProperties properties, WebClient.Builder builder) {
        HttpClient httpClient = createHttpClient();
        URI uri = createUri(properties);
        log.info("Integration layer qx API url: {}", uri);
        return createClient(builder, httpClient, uri);
    }

    @Bean(name="archiveClient")
    WebClient integrationArchiveWebClient(@Qualifier("archiveClientProperties") IntegrationClientProperties properties, WebClient.Builder builder) {
        HttpClient httpClient = createHttpClient();
        URI uri = createUri(properties);
        log.info("Integration layer archive API url: {}", uri);
        return createClient(builder, httpClient, uri);
    }

    @Bean(name="coreClient")
    WebClient integrationCoreWebClient(@Qualifier("coreClientProperties") IntegrationClientProperties properties, WebClient.Builder builder) {
        HttpClient httpClient = createHttpClient();
        URI uri = createUri(properties);
        log.info("Integration layer core API url: {}", uri);
        return createClient(builder, httpClient, uri);
    }

    @Bean
    CoreArchiveApiService integrationArchiveService(@Qualifier("archiveClient") WebClient webClient, ArchiveMapper archiveMapper) {
        return new IntegrationArchiveApiService(webClient, archiveMapper);
    }

    @Bean
    QxApiService integrationQxApiService(@Qualifier("qxClient") WebClient integrationApiWebClient, QxMapper qxIntegrationMapper) {
        return new IntegrationQxApiService(integrationApiWebClient, qxIntegrationMapper);
    }

    @Bean
    CoreApiService integrationCoreApiService(@Qualifier("coreClient") WebClient webClient) {
        return new IntegrationCoreApiService(webClient);
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
