package org.qubic.qx.api.config;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.crypto.NoCrypto;
import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.adapter.CoreApiService;
import org.qubic.qx.api.adapter.QxApiService;
import org.qubic.qx.api.controller.service.*;
import org.qubic.qx.api.db.AssetsRepository;
import org.qubic.qx.api.db.TradesRepository;
import org.qubic.qx.api.db.TransactionsRepository;
import org.qubic.qx.api.validation.IdentityValidator;
import org.qubic.qx.api.validation.ValidationUtility;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class QxServiceConfig {

    @Bean
    IdentityUtil identityUtil() {
        return new IdentityUtil(true, new NoCrypto()); // use no crypto to avoid shared lib dependency
    }

    @Bean
    ValidationUtility validationUtility(IdentityUtil identityUtil) {
        return new ValidationUtility(identityUtil);
    }

    @Bean
    IdentityValidator identityValidator() {
        return new IdentityValidator(identityUtil());
    }

    @Bean
    AssetsService assetsService(AssetsRepository assetsRepository) {
        return new AssetsService(assetsRepository);
    }

    @Bean
    QxService qxService(QxApiService qxApiService) {
        return new QxService(qxApiService);
    }

    @Bean
    TradesService tradesService(TradesRepository tradesRepository) {
        return new TradesService(tradesRepository);
    }

    @Bean
    ChartService chartService(TradesRepository tradesRepository) {
        return new ChartService(tradesRepository);
    }

    @Bean
    TransactionsService transactionsService(TransactionsRepository transactionsRepository) {
        return new TransactionsService(transactionsRepository);
    }

    @Bean
    QxOrderService qxOrderService(CoreApiService coreApiService, IdentityUtil identityUtil) {
        return new QxOrderService(coreApiService, identityUtil);
    }

}
