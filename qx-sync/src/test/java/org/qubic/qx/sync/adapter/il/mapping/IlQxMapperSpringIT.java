package org.qubic.qx.sync.adapter.il.mapping;

import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.adapter.il.domain.IlAssetOrder;
import org.qubic.qx.sync.api.domain.AssetOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class IlQxMapperSpringIT {

    @Autowired
    private IlQxMapper qxMapper;

    @Test
    void mapAssetOrder() {
        IlAssetOrder source = new IlAssetOrder("identity", "1", "2");
        AssetOrder target = qxMapper.mapAssetOrder(source);
        assertThat(target).isNotNull();
        assertThat(target.entityId()).isEqualTo("identity");
        assertThat(target.price()).isEqualTo(1);
        assertThat(target.numberOfShares()).isEqualTo(2);
    }

    @Test
    void mapAssetOrderList() {
        IlAssetOrder order1 = new IlAssetOrder("foo", "1", "2");
        IlAssetOrder order2 = new IlAssetOrder("bar", "3", "4");
        List<IlAssetOrder> source = List.of(order1, order2);
        List<AssetOrder> target = qxMapper.mapAssetOrderList(source);
        assertThat(target).isNotNull();
        assertThat(target.size()).isEqualTo(2);
        assertThat(target.get(0).entityId()).isEqualTo("foo");
        assertThat(target.get(1).entityId()).isEqualTo("bar");
    }

}