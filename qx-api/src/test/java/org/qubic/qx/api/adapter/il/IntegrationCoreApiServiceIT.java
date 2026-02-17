package org.qubic.qx.api.adapter.il;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.adapter.CoreApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationCoreApiServiceIT extends AbstractSpringIntegrationTest {

    @Autowired
    private CoreApiService coreApiService;

    @Test
    void getLatestTick() {
        String responseJson = """
        {
          "tickInfo": {
            "tick": 44224811,
            "duration": 1,
            "epoch": 200,
            "initialTick": 43910000
          }
        }""";

        prepareResponse(response -> response
                .setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseJson));

        BigInteger latestTick = coreApiService.getLatestTick();
        assertThat(latestTick).isEqualTo(44224811);

        assertRequest("/live/v1/tick-info");
    }

}