package org.qubic.qx.api.controller.mapping;

import org.mapstruct.Mapper;
import org.qubic.qx.api.controller.domain.Asset;

@Mapper(componentModel = "spring")
public interface AssetsMapper {

    Asset map(org.qubic.qx.api.db.domain.Asset source);

}
