package org.qubic.qx.config;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.crypto.NoCrypto;
import org.qubic.qx.adapter.CoreApiService;
import org.qubic.qx.adapter.ExtraDataMapper;
import org.qubic.qx.adapter.QxApiService;
import org.qubic.qx.adapter.qubicj.mapping.DataTypeTranslator;
import org.qubic.qx.api.service.QxService;
import org.qubic.qx.assets.Asset;
import org.qubic.qx.assets.AssetService;
import org.qubic.qx.assets.Assets;
import org.qubic.qx.repository.OrderBookRepository;
import org.qubic.qx.repository.TickRepository;
import org.qubic.qx.repository.TransactionRepository;
import org.qubic.qx.sync.TickSyncJob;
import org.qubic.qx.sync.TickSyncJobRunner;
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
    TickSyncJob tickSyncJob(TickRepository tickRepository, TransactionRepository transactionRepository,
                            CoreApiService coreService, AssetService assetService) {
        return new TickSyncJob(tickRepository, transactionRepository, coreService, assetService);
    }

    @Bean
    TickSyncJobRunner tickSyncJobRunner(TickSyncJob tickSyncJob, @Value("${sync.interval}") Duration syncInterval) {
        return new TickSyncJobRunner(tickSyncJob, syncInterval);
    }

}
