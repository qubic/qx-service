package org.qubic.qx.sync.adapter.il.mapping;

import org.mapstruct.Mapper;
import org.qubic.qx.sync.adapter.il.domain.IlAssetOrder;
import org.qubic.qx.sync.api.domain.AssetOrder;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IlQxMapper {

    AssetOrder mapAssetOrder(IlAssetOrder assetOrder);

    List<AssetOrder> mapAssetOrderList(List<IlAssetOrder> assetOrders);

}
