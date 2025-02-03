package org.qubic.qx.sync.properties;

import lombok.Data;

@Data
public class IntegrationClientProperties {

    private String scheme;
    private String host;
    private String port;
    private int retries;

}
