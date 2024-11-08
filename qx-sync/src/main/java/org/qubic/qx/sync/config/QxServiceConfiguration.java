package org.qubic.qx.sync.config;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.crypto.NoCrypto;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.EventApiService;
import org.qubic.qx.sync.adapter.ExtraDataMapper;
import org.qubic.qx.sync.adapter.QxApiService;
import org.qubic.qx.sync.adapter.qubicj.mapping.DataTypeTranslator;
import org.qubic.qx.sync.assets.Asset;
import org.qubic.qx.sync.assets.AssetService;
import org.qubic.qx.sync.assets.Assets;
import org.qubic.qx.sync.job.*;
import org.qubic.qx.sync.mapper.TransactionMapper;
import org.qubic.qx.sync.repository.OrderBookRepository;
import org.qubic.qx.sync.repository.TickRepository;
import org.qubic.qx.sync.repository.TradeRepository;
import org.qubic.qx.sync.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

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

    @Bean
    AssetService assetService(Assets assets, QxApiService qxApiService, OrderBookRepository orderBookRepository) {
        return new AssetService(assets, qxApiService, orderBookRepository);
    }

    @Bean
    OrderBookCalculator orderBookCalculator() {
        return new OrderBookCalculator();
    }

    @Bean
    OrderBookProcessor orderBookProcessor(CoreApiService coreService, AssetService assetService, OrderBookCalculator orderBookCalculator) {
        return new OrderBookProcessor(coreService, assetService, orderBookCalculator);
    }

    @Bean
    EventsProcessor eventsProcessor(IdentityUtil identityUtil) {
        return new EventsProcessor(identityUtil);
    }

    @Bean
    TransactionProcessor transactionProcessor(TransactionRepository transactionRepository,
                                              TradeRepository tradeRepository, TransactionMapper transactionMapper,
                                              EventsProcessor eventsProcessor, OrderBookProcessor orderBookProcessor) {
        return new TransactionProcessor(transactionRepository, tradeRepository, transactionMapper, eventsProcessor, orderBookProcessor);
    }

    @Bean
    TickSyncJob tickSyncJob(AssetService assetService, TickRepository tickRepository, CoreApiService coreService,
                            EventApiService eventApiService, TransactionProcessor transactionProcessor) {
        return new TickSyncJob(assetService, tickRepository, coreService, eventApiService, transactionProcessor);
    }

    @Bean
    TickSyncJobRunner tickSyncJobRunner(TickSyncJob tickSyncJob, @Value("${sync.interval}") Duration syncInterval) {
        return new TickSyncJobRunner(tickSyncJob, syncInterval);
    }

}
