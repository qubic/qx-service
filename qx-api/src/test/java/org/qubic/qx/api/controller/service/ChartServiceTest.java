package org.qubic.qx.api.controller.service;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.controller.domain.AvgPriceData;
import org.qubic.qx.api.db.TradesRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChartServiceTest {

    private final TradesRepository tradesRepository = mock();
    private final ChartService chartService = new ChartService(tradesRepository);

    @Test
    void getAveragePriceForAsset() {
        List<AvgPriceData> expected = List.of(new AvgPriceData(LocalDate.EPOCH, 1, 3, 5, 7, 13.13, 17));
        when(tradesRepository.findAveragePriceByAssetGroupedByDay(eq("foo"), eq("bar"), any(Instant.class))).thenReturn(expected);
        List<AvgPriceData> result = chartService.getAveragePriceForAsset("foo", "bar");
        assertThat(result).isEqualTo(expected);
    }
}