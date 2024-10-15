package org.qubic.qx.api.controller.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.qubic.qx.api.controller.domain.Asset;

@Mapper(componentModel = "spring")
public interface AssetsMapper {

    @Mapping(target = "issuer", source = "issuer")
    @Mapping(target = "name", source = "name")
    Asset map(org.qubic.qx.api.db.domain.Asset source);

}
