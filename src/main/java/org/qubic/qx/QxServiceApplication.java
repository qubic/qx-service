package org.qubic.qx;

import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.sync.TickSyncJobRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@SpringBootApplication
public class QxServiceApplication implements ApplicationRunner {

    private final TickSyncJobRunner tickSyncJobRunner;

    public QxServiceApplication(TickSyncJobRunner tickSyncJobRunner) {
        this.tickSyncJobRunner = tickSyncJobRunner;
    }

    @Override
    public void run(ApplicationArguments args) {
        final long syncToTick = getSyncToTickValue(args.getOptionValues("sync"));
        if (syncToTick > 0) {
            log.info("Starting. Syncing to target tick [{}].", syncToTick);
            tickSyncJobRunner.loopUntilTargetTick(syncToTick);
        } else if (args.containsOption("sync")) {
            log.info("Starting. Running sync job...");
            tickSyncJobRunner.loopForever();
        } else {
            log.info("Starting without sync job...");
        }
    }

    private static long getSyncToTickValue(List<String> ticks) {
        return CollectionUtils.isEmpty(ticks) ? 0 : Long.parseLong(ticks.getFirst());
    }

    public static void main(String[] args) {
        SpringApplication.run(QxServiceApplication.class, args);
    }
}
