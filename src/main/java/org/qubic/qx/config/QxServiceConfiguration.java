package org.qubic.qx.config;

import org.qubic.qx.adapter.il.qx.QxIntegrationApiClient;
import org.qubic.qx.adapter.qubicj.NodeService;
import org.qubic.qx.adapter.il.qx.mapping.QxIntegrationMapper;
import org.qubic.qx.api.service.QxService;
import org.qubic.qx.repository.TickRepository;
import org.qubic.qx.repository.TransactionRepository;
import org.qubic.qx.sync.TickSyncJob;
import org.qubic.qx.sync.TickSyncJobRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class QxServiceConfiguration {

    @Bean
    TickSyncJob tickSyncJob(TickRepository tickRepository, TransactionRepository transactionRepository, NodeService nodeService) {
        return new TickSyncJob(tickRepository, transactionRepository, nodeService);
    }

    @Bean
    TickSyncJobRunner tickSyncJobRunner(TickSyncJob tickSyncJob, @Value("${sync.interval}") Duration syncInterval) {
        return new TickSyncJobRunner(tickSyncJob, syncInterval);
    }

    @Bean
    QxIntegrationApiClient qxApiClient(@Value("${il.base-url}") String baseUrl, QxIntegrationMapper qxIntegrationMapper) {
        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
        return new QxIntegrationApiClient(webClient, qxIntegrationMapper);
    }

    @Bean
    QxService qxService(QxIntegrationApiClient qxApiClient) {
        return new QxService(qxApiClient);
    }

}
