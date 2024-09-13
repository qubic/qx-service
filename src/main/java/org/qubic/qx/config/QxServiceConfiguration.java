package org.qubic.qx.config;

import org.mio.qubic.computor.crypto.IdentityUtil;
import org.qubic.qx.adapter.computor.NodeService;
import org.qubic.qx.repository.TickRepository;
import org.qubic.qx.sync.TickSyncJob;
import org.qubic.qx.sync.TickSyncJobRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

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

}
