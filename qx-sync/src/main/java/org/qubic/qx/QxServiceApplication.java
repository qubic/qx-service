package org.qubic.qx;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.sync.TickSyncJobRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import redis.embedded.RedisServer;

import java.io.IOException;

@Slf4j
@SpringBootApplication
public class QxServiceApplication implements ApplicationRunner {


    private final Environment environment;
    private final TickSyncJobRunner tickSyncJobRunner;

    public QxServiceApplication(Environment environment, TickSyncJobRunner tickSyncJobRunner) {
        this.environment = environment;
        this.tickSyncJobRunner = tickSyncJobRunner;
    }

    @Override
    public void run(ApplicationArguments args) {
        Boolean syncEnabled = environment.getProperty("sync.enabled", Boolean.class, false);
        Boolean embeddedRedisEnabled = environment.getProperty("redis.embedded", Boolean.class, false);

        if (embeddedRedisEnabled) {
            try {
                int port = environment.getProperty("spring.data.redis.port", Integer.class, 6378);
                RedisServer.newRedisServer().port(port).build().start();
                log.info("Redis started on port {}", port);
            } catch (Exception e) {
                String message = e.getCause() != null && e.getCause() instanceof IOException
                            ? e.getCause().getMessage() : e.getMessage();
                log.warn("Embedded redis requested but could not start server: {}", message);
            }
        }

        if (syncEnabled) {
            log.info("Starting. Running sync job...");
            tickSyncJobRunner.loopForever();
        } else {
            log.info("Starting without sync job...");
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(QxServiceApplication.class, args);
    }

}
