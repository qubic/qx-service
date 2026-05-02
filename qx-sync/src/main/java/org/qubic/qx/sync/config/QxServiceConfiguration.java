package org.qubic.qx.sync.config;

import at.qubic.api.crypto.IdentityUtil;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.ExtraDataMapper;
import org.qubic.qx.sync.job.EventsProcessor;
import org.qubic.qx.sync.job.TickSyncJob;
import org.qubic.qx.sync.job.TickSyncJobRunner;
import org.qubic.qx.sync.job.TransactionProcessor;
import org.qubic.qx.sync.repository.TickRepository;
import org.qubic.qx.sync.repository.TradeRepository;
import org.qubic.qx.sync.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class QxServiceConfiguration {

    @Bean
    IdentityUtil identityUtil() {
        return new IdentityUtil();
    }

    @Bean
    ExtraDataMapper extraDataMapper(IdentityUtil identityUtil) {
        return new ExtraDataMapper(identityUtil);
    }

    @Bean
    EventsProcessor eventsProcessor() {
        return new EventsProcessor();
    }

    @Bean
    TransactionProcessor transactionProcessor(TransactionRepository transactionRepository,
                                              TradeRepository tradeRepository,
                                              EventsProcessor eventsProcessor) {
        return new TransactionProcessor(transactionRepository, tradeRepository, eventsProcessor);
    }

    @Bean
    TickSyncJob tickSyncJob(TickRepository tickRepository, CoreApiService coreService, TransactionProcessor transactionProcessor) {
        return new TickSyncJob(tickRepository, coreService, transactionProcessor);
    }

    @Bean
    TickSyncJobRunner tickSyncJobRunner(TickSyncJob tickSyncJob,
                                        @Value("${sync.interval}") Duration syncInterval,
                                        @Value("${sync.retry-interval}") Duration retryDuration) {
        return new TickSyncJobRunner(tickSyncJob, syncInterval, retryDuration);
    }

}
