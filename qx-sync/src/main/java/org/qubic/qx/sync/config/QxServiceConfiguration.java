package org.qubic.qx.sync.config;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.crypto.NoCrypto;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.EventApiService;
import org.qubic.qx.sync.adapter.ExtraDataMapper;
import org.qubic.qx.sync.adapter.qubicj.mapping.DataTypeTranslator;
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
        return new IdentityUtil(true, new NoCrypto());
    }

    @Bean
    ExtraDataMapper extraDataMapper(IdentityUtil identityUtil) {
        return new ExtraDataMapper(identityUtil);
    }

    // only necessary for qubicj but it is automatically injected by mapstruct
    @Bean
    DataTypeTranslator dataTypeTranslator(IdentityUtil identityUtil, ExtraDataMapper extraDataMapper) {
        return new DataTypeTranslator(identityUtil, extraDataMapper);
    }

    @Bean
    EventsProcessor eventsProcessor(IdentityUtil identityUtil) {
        return new EventsProcessor(identityUtil);
    }

    @Bean
    TransactionProcessor transactionProcessor(TransactionRepository transactionRepository,
                                              TradeRepository tradeRepository,
                                              EventsProcessor eventsProcessor) {
        return new TransactionProcessor(transactionRepository, tradeRepository, eventsProcessor);
    }

    @Bean
    TickSyncJob tickSyncJob(TickRepository tickRepository, CoreApiService coreService,
                            EventApiService eventApiService, TransactionProcessor transactionProcessor) {
        return new TickSyncJob(tickRepository, coreService, eventApiService, transactionProcessor);
    }

    @Bean
    TickSyncJobRunner tickSyncJobRunner(TickSyncJob tickSyncJob, @Value("${sync.interval}") Duration syncInterval) {
        return new TickSyncJobRunner(tickSyncJob, syncInterval);
    }

}
