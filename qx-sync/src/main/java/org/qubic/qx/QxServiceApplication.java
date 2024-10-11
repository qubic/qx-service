package org.qubic.qx;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.sync.TickSyncJobRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

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
