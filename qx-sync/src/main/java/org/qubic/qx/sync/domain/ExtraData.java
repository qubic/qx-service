package org.qubic.qx.sync.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use=JsonTypeInfo.Id.MINIMAL_CLASS, property="@class")
public interface ExtraData {

}
