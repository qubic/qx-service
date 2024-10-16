package org.qubic.qx.sync.config;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.crypto.NoCrypto;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.ExtraDataMapper;
import org.qubic.qx.sync.adapter.QxApiService;
import org.qubic.qx.sync.adapter.qubicj.mapping.DataTypeTranslator;
import org.qubic.qx.sync.api.service.QxService;
import org.qubic.qx.sync.api.service.TradesService;
import org.qubic.qx.sync.assets.Asset;
import org.qubic.qx.sync.assets.AssetService;
import org.qubic.qx.sync.assets.Assets;
import org.qubic.qx.sync.repository.OrderBookRepository;
import org.qubic.qx.sync.repository.TickRepository;
import org.qubic.qx.sync.repository.TradeRepository;
import org.qubic.qx.sync.repository.TransactionRepository;
import org.qubic.qx.sync.job.OrderBookCalculator;
import org.qubic.qx.sync.job.TickSyncJob;
import org.qubic.qx.sync.job.TickSyncJobRunner;
import org.qubic.qx.sync.job.TransactionProcessor;
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
    QxService qxService(QxApiService qxApiService) {
        return new QxService(qxApiService);
    }

    @Bean
    TradesService tradesService(TradeRepository tradeRepository) {
        return new TradesService(tradeRepository);
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
    TransactionProcessor transactionProcessor(CoreApiService coreApiService, AssetService assetService, OrderBookCalculator orderBookCalculator,
                                              TransactionRepository transactionRepository, TradeRepository tradeRepository) {
        return new TransactionProcessor(coreApiService, assetService, orderBookCalculator, transactionRepository, tradeRepository);
    }

    @Bean
    TickSyncJob tickSyncJob(TickRepository tickRepository, CoreApiService coreService, TransactionProcessor transactionProcessor) {
        return new TickSyncJob(tickRepository, coreService, transactionProcessor);
    }

    @Bean
    TickSyncJobRunner tickSyncJobRunner(TickSyncJob tickSyncJob, @Value("${sync.interval}") Duration syncInterval) {
        return new TickSyncJobRunner(tickSyncJob, syncInterval);
    }

}
