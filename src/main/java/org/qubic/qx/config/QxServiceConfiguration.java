package org.qubic.qx.config;

import at.qubic.api.crypto.IdentityUtil;
import org.qubic.qx.adapter.qubicj.NodeService;
import org.qubic.qx.adapter.il.qx.QxApiClient;
import org.qubic.qx.api.mapping.QxMapper;
import org.qubic.qx.api.service.QxService;
import org.qubic.qx.repository.TickRepository;
import org.qubic.qx.sync.TickSyncJob;
import org.qubic.qx.sync.TickSyncJobRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class QxServiceConfiguration {

    @Bean
    TickRepository tickRepository(ReactiveStringRedisTemplate redisStringTemplate) {
        return new TickRepository(redisStringTemplate);
    }

    @Bean
    TickSyncJob tickSyncJob(TickRepository tickRepository, NodeService nodeService, IdentityUtil identityUtil) {
        return new TickSyncJob(tickRepository, nodeService, identityUtil);
    }

    @Bean
    TickSyncJobRunner tickSyncJobRunner(TickSyncJob tickSyncJob, @Value("${sync.interval}") Duration syncInterval) {
        return new TickSyncJobRunner(tickSyncJob, syncInterval);
    }

    @Bean
    QxApiClient qxApiClient(@Value("${il.base-url}") String baseUrl) {
        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
        return new QxApiClient(webClient);
    }

    @Bean
    QxService qxService(QxApiClient qxApiClient, QxMapper qxMapper) {
        return new QxService(qxApiClient, qxMapper);
    }

}
