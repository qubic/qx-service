package org.qubic.qx.api.config;

import org.qubic.qx.api.db.DatabaseRepositories;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@EnableJdbcRepositories(basePackageClasses = DatabaseRepositories.class)
public class DatabaseConfiguration {
}
