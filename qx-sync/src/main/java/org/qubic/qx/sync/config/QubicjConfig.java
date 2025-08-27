package org.qubic.qx.sync.config;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.network.*;
import at.qubic.api.properties.ComputorProperties;
import at.qubic.api.properties.NetworkProperties;
import at.qubic.api.service.ComputorService;
import at.qubic.api.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.EventApiService;
import org.qubic.qx.sync.adapter.qubicj.QubicjCoreApiService;
import org.qubic.qx.sync.adapter.qubicj.QubicjEventApiService;
import org.qubic.qx.sync.adapter.qubicj.mapping.QubicjMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Slf4j
@ConditionalOnProperty(value = "backend", havingValue = "qubicj")
@Configuration
public class QubicjConfig {

    @Bean
    @ConfigurationProperties("computor")
    ComputorProperties computorProperties() {
        return new ComputorProperties();
    }

    @Bean
    @ConfigurationProperties(value = "network", ignoreUnknownFields = false)
    NetworkProperties networkProperties() {
        return new NetworkProperties();
    }

    @Bean
    NetworkStatus networkStatus() {
        return new HealthCheckNetworkStatus();
    }

    @Bean
    NodesManagementStrategy nodesManagementStrategy(NetworkProperties properties, NetworkStatus networkStatus, Environment environment) {
        boolean usePublicPeers = environment.getRequiredProperty("use-public-peers", Boolean.class);
        log.info("Using port [{}].", properties.getPort());
        return usePublicPeers
                ? new PublicNodesStrategy(networkStatus, properties)
                : new FixNodesStrategy(networkStatus, properties);
    }

    @Bean
    Nodes nodes(NodesManagementStrategy nodesManagementStrategy, ComputorProperties properties) {
        Nodes nodes = new Nodes(nodesManagementStrategy);
        log.info("Using computors {}.", Arrays.toString(properties.getHosts()));
        for (String host : properties.getHosts()) {
            nodes.addNode(host);
        }
        return nodes;
    }

    // create bean without shared lib crypto dependency
    @Bean
    TransactionService transactionService(IdentityUtil identityUtil) {
        return new TransactionService(identityUtil);
    }

    @Bean
    ComputorService computorService(Nodes nodes, IdentityUtil identityUtil, TransactionService transactionService) {
        return new ComputorService(nodes, identityUtil, transactionService);
    }

    @Bean
    CoreApiService qubicjCoreApiService(ComputorService computorService, QubicjMapper mapper) {
        return new QubicjCoreApiService(computorService, mapper);
    }

    @Bean
    EventApiService eventApiService() {
        return new QubicjEventApiService();
    }

}
