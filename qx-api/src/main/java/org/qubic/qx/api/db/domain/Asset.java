package org.qubic.qx.api.db.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Jacksonized // attention: class reused in api interface
@Builder
@Data
@Table("assets")
public class Asset implements Serializable {

    public static final String SMART_CONTRACT_ISSUER = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB";

    @JsonIgnore
    @Id
    private Long id;

    private String issuer;

    private String name;

    @JsonIgnore
    private boolean verified;

}
