package org.qubic.qx.api.config;

import org.qubic.qx.api.db.*;
import org.qubic.qx.api.db.convert.ExtraDataReadingConverter;
import org.qubic.qx.api.db.convert.ExtraDataWritingConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

import java.util.List;

@Configuration
@EnableJdbcRepositories(basePackageClasses = DatabaseRepositories.class)
public class DatabaseConfiguration extends AbstractJdbcConfiguration {

    @Bean
    ExtraDataReadingConverter extraDataReadingConverter() {
        return new ExtraDataReadingConverter();
    }

    @Bean
    ExtraDataWritingConverter extraDataWritingConverter() {
        return new ExtraDataWritingConverter();
    }

    @Override
    protected List<?> userConverters() {
        return List.of(extraDataReadingConverter(), extraDataWritingConverter());
    }

    @Bean
    AssetsDbService assetsDbService(AssetsRepository assetsRepository) {
        return new AssetsDbService(assetsRepository);
    }

    @Bean
    EntitiesDbService entitiesDbService(EntitiesRepository entitiesRepository) {
        return new EntitiesDbService(entitiesRepository);
    }

}
