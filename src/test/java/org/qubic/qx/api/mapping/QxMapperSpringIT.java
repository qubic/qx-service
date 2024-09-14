package org.qubic.qx.api.mapping;

import org.junit.jupiter.api.Test;
import org.qubic.qx.adapter.il.qx.domain.QxAssetOrder;
import org.qubic.qx.adapter.il.qx.domain.QxEntityOrder;
import org.qubic.qx.adapter.il.qx.domain.QxFees;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.api.domain.EntityOrder;
import org.qubic.qx.api.domain.Fees;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class QxMapperSpringIT {

    @Autowired
    private QxMapper qxMapper;

    @Test
    void mapFees() {
        QxFees source = new QxFees(1, 2, 3);
        Fees target = qxMapper.mapFees(source);
        assertThat(target).isNotNull();
        assertThat(target.assetIssuanceFee()).isEqualTo(1);
        assertThat(target.transferFee()).isEqualTo(2);
        assertThat(target.tradeFee()).isEqualTo(3);
    }

    @Test
    void mapAssetOrder() {
        QxAssetOrder source = new QxAssetOrder("identity", "1", "2");
        AssetOrder target = qxMapper.mapAssetOrder(source);
        assertThat(target).isNotNull();
        assertThat(target.entityId()).isEqualTo("identity");
        assertThat(target.price()).isEqualTo(1);
        assertThat(target.numberOfShares()).isEqualTo(2);
    }

    @Test
    void mapEntityOrder() {
        QxEntityOrder source = new QxEntityOrder("issuer", "asset", "1", "2");
        EntityOrder target = qxMapper.mapEntityOrder(source);
        assertThat(target).isNotNull();
        assertThat(target.issuerId()).isEqualTo("issuer");
        assertThat(target.assetName()).isEqualTo("asset");
        assertThat(target.price()).isEqualTo(1);
        assertThat(target.numberOfShares()).isEqualTo(2);
    }

    @Test
    void mapAssetOrderList() {
        QxAssetOrder order1 = new QxAssetOrder("foo", "1", "2");
        QxAssetOrder order2 = new QxAssetOrder("bar", "3", "4");
        List<QxAssetOrder> source = List.of(order1, order2);
        List<AssetOrder> target = qxMapper.mapAssetOrderList(source);
        assertThat(target).isNotNull();
        assertThat(target.size()).isEqualTo(2);
        assertThat(target.get(0).entityId()).isEqualTo("foo");
        assertThat(target.get(1).entityId()).isEqualTo("bar");
    }

    @Test
    void mapEntityOrderList() {
        QxEntityOrder order1 = new QxEntityOrder("a", "b", "1", "2");
        QxEntityOrder order2 = new QxEntityOrder("c", "d", "3", "4");
        List<QxEntityOrder> source = List.of(order1, order2);
        List<EntityOrder> target = qxMapper.mapEntityOrderList(source);
        assertThat(target).isNotNull();
        assertThat(target.size()).isEqualTo(2);
        assertThat(target.get(0).issuerId()).isEqualTo("a");
        assertThat(target.get(1).issuerId()).isEqualTo("c");
    }

}