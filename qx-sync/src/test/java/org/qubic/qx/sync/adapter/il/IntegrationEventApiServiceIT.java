package org.qubic.qx.sync.adapter.il;

import io.micrometer.core.instrument.util.IOUtils;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.domain.EpochAndTick;
import org.qubic.qx.sync.domain.EventHeader;
import org.qubic.qx.sync.domain.TransactionEvent;
import org.qubic.qx.sync.domain.TransactionEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationEventApiServiceIT extends AbstractIntegrationApiTest {

    private static final int TICK = 16317860;

    @Autowired
    private IntegrationEventApiService apiClient;

    @Test
    void getTickEvents() {
        String responseJson = IOUtils.toString(Objects.requireNonNull(getClass().getResourceAsStream(
                "/testdata/il/get-tick-events-0-response.json"
        )), StandardCharsets.UTF_8);

        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseJson));

        List<TransactionEvents> transactionEvents = apiClient.getTickEvents(TICK).block();
        assertThat(transactionEvents).hasSize(2);

        TransactionEvents transactionEvents1 = transactionEvents.getFirst();
        assertThat(transactionEvents1.txId()).isEqualTo("izohgnpbyxczccpjvxrsseadaolfuwskpomivtwlhgiuarildsyoxxradhib");
        assertThat(transactionEvents1.events()).hasSize(2);
        assertTransactionEvent(transactionEvents1.events().getFirst(),
                "mYYyiOE+5Q5akQnPqfVWH3yzhna3SqfR741fJr3dNxsAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEBCDwAAAAAA",
                "78991",
                "11486275204490993574");
        assertTransactionEvent(transactionEvents1.events().getLast(),
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACZhjKI4T7lDlqRCc+p9VYffLOGdrdKp9HvjV8mvd03G0BCDwAAAAAA",
                "78992",
                "8574750727124548928");

        TransactionEvents transactionEvents2 = transactionEvents.getLast();
        assertThat(transactionEvents2.txId()).isEqualTo("nnqvizkgfdlscbepwfabkatcybihvcgxlctqmlmwfciumjkersuvheiajpib");
        assertThat(transactionEvents2.events()).hasSize(2);
        assertTransactionEvent(transactionEvents2.events().getFirst(),
                "vgOheQqQ4aQZ270SGUyQI7ahOElchrLgMK0yhSqTi0MAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEBCDwAAAAAA",
                "78993",
                "2366547716797884857");
        assertTransactionEvent(transactionEvents2.events().getLast(),
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC+A6F5CpDhpBnbvRIZTJAjtqE4SVyGsuAwrTKFKpOLQ0BCDwAAAAAA",
                "78994",
                "14965835435466498310");

        assertRequest("/v1/events/getTickEvents");
    }

    @Test
    void getEventProcessingStatus() {
        String responseJson = IOUtils.toString(Objects.requireNonNull(getClass().getResourceAsStream(
                "/testdata/il/get-tick-status-response.json"
        )), StandardCharsets.UTF_8);

        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseJson));

        EpochAndTick tickInfo = apiClient.getLastProcessedTick().block();
        assertThat(tickInfo).isNotNull();
        assertThat(tickInfo.tickNumber()).isEqualTo(17127356);
        assertThat(tickInfo.epoch()).isEqualTo(134);
    }

    private static void assertTransactionEvent(TransactionEvent transactionEvent, String data, String eventId, String eventDigest) {
        assertThat(transactionEvent.eventType()).isEqualTo(0);
        assertThat(transactionEvent.eventSize()).isEqualTo(72);
        assertThat(transactionEvent.eventData()).isEqualTo(data);
        EventHeader header = transactionEvent.header();
        assertThat(header.epoch()).isEqualTo(129);
        assertThat(header.tick()).isEqualTo(TICK);
        assertThat(header.eventId()).isEqualTo(eventId);
        assertThat(header.eventDigest()).isEqualTo(eventDigest);
    }

}