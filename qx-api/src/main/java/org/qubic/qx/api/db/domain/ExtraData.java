package org.qubic.qx.api.db.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, property="@class")
public interface ExtraData {

}
