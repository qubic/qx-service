package org.qubic.qx.config;

import at.qubic.api.crypto.NoCrypto;
import lombok.extern.slf4j.Slf4j;
import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.network.*;
import at.qubic.api.properties.ComputorProperties;
import at.qubic.api.properties.NetworkProperties;
import at.qubic.api.service.ComputorService;
import org.qubic.qx.adapter.qubicj.NodeService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Slf4j
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

    @Bean
    IdentityUtil identityUtil() {
        return new IdentityUtil(true, new NoCrypto());
    }

    @Bean
    ComputorService computorService(Nodes nodes, IdentityUtil identityUtil) {
        return new ComputorService(nodes, identityUtil);
    }

    // TODO extract interface for core integration layer
    @Bean
    NodeService nodeService(ComputorService computorService) {
        return new NodeService(computorService);
    }


}
