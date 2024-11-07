package org.qubic.qx.api.adapter.il;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.AbstractSpringIntegrationTest;
import org.qubic.qx.api.adapter.CoreArchiveApiService;
import org.qubic.qx.api.adapter.domain.TickData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class IntegrationArchiveApiServiceIT extends AbstractSpringIntegrationTest {

    @Autowired
    private CoreArchiveApiService apiService;

    @Test
    void getTickData() {
        String responseJson = """
                {"tickData":
                    {
                    "computorIndex":598,
                    "epoch":129,
                    "tickNumber":16394274,
                    "timestamp":"1728301113000",
                    "varStruct":"",
                    "timeLock":"OX8cA6pGRid4/cQlOvW4bgwm9zwSnPwgxYDwwEbTQHk=",
                    "transactionIds":["mmuyrzbufplgvgffxmvelkngezggcliszqlfhulqvclvzlwvfxrpnipdmeee"],
                    "contractFees":[],
                    "signatureHex":"bd158d0aeaa5d42543bb4ddeb8563e239fd4e2fdca93951d817a960f5c9def096cc51750023f545fd6d1ea5384f410d2dcf47aaf5b6e5e6b6b1c5021b11d1e00"}
                }""";

        prepareResponse(response -> response.setResponseCode(HttpStatus.OK.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(responseJson));

        TickData result = apiService.getTickData(16394274);
        assertThat(result).isEqualTo(new TickData(129, 16394274, Instant.parse("2024-10-07T11:38:33Z")));
        assertRequest("/v1/ticks/16394274/tick-data");

    }
}