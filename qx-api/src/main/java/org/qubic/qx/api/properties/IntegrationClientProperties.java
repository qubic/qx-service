package org.qubic.qx.api.properties;

import lombok.Data;

@Data
public class IntegrationClientProperties {

    private String scheme;
    private String host;
    private String port;

}
