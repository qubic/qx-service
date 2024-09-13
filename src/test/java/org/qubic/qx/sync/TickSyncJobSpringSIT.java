package org.qubic.qx.sync;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.qubic.qx.AbstractRedisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Tag("SIT")
@SpringBootTest(properties = {"spring.data.redis.port=16379"})
class TickSyncJobSpringSIT extends AbstractRedisTest {

    @Autowired
    private TickSyncJob tickSyncJob;

    @Test
    void sync() {

        tickSyncJob.getCurrentTick()
                .flatMapMany(currentTick -> tickSyncJob.sync(currentTick)
                        .take(10))
                .blockLast();


    }


}