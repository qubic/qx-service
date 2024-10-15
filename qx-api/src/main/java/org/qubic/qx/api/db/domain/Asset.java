package org.qubic.qx.api.db.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Builder
@Data
@Table("assets")
public class Asset {

    @Id
    private Long id;
    private String issuer;
    private String name;
    private boolean verified;

}
