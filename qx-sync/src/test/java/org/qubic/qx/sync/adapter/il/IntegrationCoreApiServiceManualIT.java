package org.qubic.qx.sync.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.il.domain.IlTransactions;
import org.qubic.qx.sync.domain.TickData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Tag("MANUAL")
@Tag("SIT")
class IntegrationCoreApiServiceManualIT {

    private final IntegrationCoreApiService apiService;

    @Autowired
    IntegrationCoreApiServiceManualIT(CoreApiService apiService) {
        this.apiService = (IntegrationCoreApiService) apiService;
    }

    @Test
    void getCurrentTick() {
        Long tick = apiService.getCurrentTick().block();
        assertThat(tick).isPositive();
        log.info("Current tick: {}", tick);
    }

    @Test
    void getTickData() {
        Long tick = apiService.getCurrentTick().block();
        assertThat(tick).isNotNull();
        assertThat(tick).isPositive();
        TickData tickData = apiService.getTickData(tick).block();
        assertThat(tickData).isNotNull();
        log.info("Tick data: {}", tickData);
    }

    @Test
    void getTickTransactions() {
        Long tick = apiService.getCurrentTick().block();
        assertThat(tick).isNotNull();
        assertThat(tick).isPositive();
        IlTransactions transactions = apiService.getTickTransactions(tick).block();
        assertThat(transactions).isNotNull();
        log.info("Transactions: {}", transactions.transactions());
    }

}