package org.qubic.qx.config;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.crypto.NoCrypto;
import org.qubic.qx.adapter.ExtraDataMapper;
import org.qubic.qx.adapter.il.IntegrationQxApiService;
import org.qubic.qx.adapter.il.mapping.QxIntegrationMapper;
import org.qubic.qx.api.service.QxService;
import org.qubic.qx.assets.Asset;
import org.qubic.qx.assets.Assets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;

@Configuration
public class QxServiceConfiguration {

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

    // create bean without shared lib crypto dependency
    @Bean
    IdentityUtil identityUtil() {
        return new IdentityUtil(true, new NoCrypto());
    }

    @Bean
    ExtraDataMapper extraDataMapper(IdentityUtil identityUtil) {
        return new ExtraDataMapper(identityUtil);
    }

    @Bean
    IntegrationQxApiService integrationQxApiService(WebClient integrationApiWebClient, QxIntegrationMapper qxIntegrationMapper) {
        return new IntegrationQxApiService(integrationApiWebClient, qxIntegrationMapper);
    }

    @Bean
    QxService qxService(IntegrationQxApiService integrationApiService) {
        return new QxService(integrationApiService);
    }

    @Bean
    Assets assets(Environment environment) {
        String assetsKey = "assets";
        String[] knownAssets = environment.getRequiredProperty(assetsKey, String[].class);
        Assets assets = new Assets();
        for (String name : knownAssets) {
            String assetIssuer = environment.getRequiredProperty(String.format("%s.%s.issuer", assetsKey, name));
            String assetName = environment.getRequiredProperty(String.format("%s.%s.name", assetsKey, name));
            assets.add(new Asset(assetIssuer, assetName));
        }
        return assets;
    }

}
