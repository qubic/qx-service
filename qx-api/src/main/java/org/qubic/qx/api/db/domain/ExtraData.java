package org.qubic.qx.api.db.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, property="@class")
public interface ExtraData extends Serializable {

}
