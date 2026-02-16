package org.qubic.qx.sync.adapter.il;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.CoreApiService;
import org.qubic.qx.sync.adapter.il.domain.query.IlQueryApiTransaction;
import org.qubic.qx.sync.domain.TickData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Tag("MANUAL")
@Tag("SIT")
class IntegrationQueryApiServiceManualIT {

    private final IntegrationQueryApiService apiService;

    @Autowired
    IntegrationQueryApiServiceManualIT(CoreApiService apiService) {
        this.apiService = (IntegrationQueryApiService) apiService;
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
        List<IlQueryApiTransaction> transactions = apiService.getTickTransactions(tick).collectList().block();
        assertThat(transactions).isNotNull();
        log.info("Transactions: {}", transactions);
    }

}