package org.qubic.qx.adapter.qubicj;

import at.qubic.api.domain.qx.response.QxFees;
import org.junit.jupiter.api.Test;
import org.qubic.qx.adapter.qubicj.mapping.QubicjOxMapper;
import org.qubic.qx.api.domain.AssetOrder;
import org.qubic.qx.api.domain.EntityOrder;
import org.qubic.qx.api.domain.Fees;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = """
    backend=qubicj
""")
class QubicjOxMapperSpringIT {

    @Autowired
    private QubicjOxMapper mapper;

    @Test
    void mapFees() {
        QxFees source = QxFees.builder().assetIssuanceFee(1).transferFee(2).tradeFee(3).build();
        Fees target = mapper.mapFees(source);
        assertThat(target).isNotNull();
        assertThat(target.assetIssuanceFee()).isEqualTo(1);
        assertThat(target.transferFee()).isEqualTo(2);
        assertThat(target.tradeFee()).isEqualTo(3);
    }

    @Test
    void mapAssetOrder() {
        at.qubic.api.domain.qx.response.AssetOrder source = at.qubic.api.domain.qx.response.AssetOrder.builder()
                .entity(new byte[32])
                .price(42)
                .numberOfShares(10)
                .build();
        AssetOrder target = mapper.mapAssetOrder(source);
        assertThat(target).isNotNull();
        assertThat(target.entityId()).isEqualTo("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB");
        assertThat(target.price()).isEqualTo(42);
        assertThat(target.numberOfShares()).isEqualTo(10);
    }

    @Test
    void mapEntityOrder() {
        at.qubic.api.domain.qx.response.EntityOrder source = at.qubic.api.domain.qx.response.EntityOrder.builder()
                .issuer(new byte[32])
                .assetName(new byte[] {'F', 'O', 'O', 0, 0, 0, 0, 0})
                .price(1)
                .numberOfShares(2)
                .build();
        EntityOrder target = mapper.mapEntityOrder(source);
        assertThat(target).isNotNull();
        assertThat(target.issuerId()).isEqualTo("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB");
        assertThat(target.assetName()).isEqualTo("FOO");
        assertThat(target.price()).isEqualTo(1);
        assertThat(target.numberOfShares()).isEqualTo(2);
    }


}