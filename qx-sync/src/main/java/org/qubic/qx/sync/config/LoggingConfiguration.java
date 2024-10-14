package org.qubic.qx.sync.config;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.sync.logging.RequestLoggingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Slf4j
@Configuration
public class LoggingConfiguration {

    @Bean
    public RequestLoggingFilter requestLoggingFilter(Environment env) {
        boolean logUrl = env.getProperty("log.url", Boolean.class, false);
        log.info("log.url: [{}]", logUrl);
        boolean logHeaders = env.getProperty("log.headers", Boolean.class, false);
        log.info("log.headers: [{}]", logHeaders);
        boolean logBody = env.getProperty("log.body", Boolean.class, false);
        log.info("log.body: [{}]", logBody);
        return new RequestLoggingFilter(logUrl, logHeaders, logBody);
    }

}
