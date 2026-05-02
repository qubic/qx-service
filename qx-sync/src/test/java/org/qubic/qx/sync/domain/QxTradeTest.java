package org.qubic.qx.sync.domain;

import at.qubic.api.crypto.IdentityUtil;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QxTradeTest {

    private final IdentityUtil identityUtil = new IdentityUtil();

    @Test
    void fromBytes() {
        String base64 = "6gIJSEHk2S4xHug8yKSrd+N198FnlWKkHU0tuhFz6QRDT0RFRAAAABgAAAAAAAAALXoHAAAAAAA=";
        QxTrade qxTrade = QxTrade.fromBytes(Base64.decodeBase64(base64));
        assertThat(qxTrade).isNotNull();
        assertThat(qxTrade.getIssuer()).hasSize(32);
        assertThat(identityUtil.getIdentityFromPublicKey(qxTrade.getIssuer())).isEqualTo("CODEDCGWIUUJJBHTXYQWAOKVKJMDBXHOKMNDVAHDUEDNSQKVYYJGLSDAYKHG");
        assertThat(qxTrade.getAssetName()).isEqualTo("CODED");
        assertThat(qxTrade.getPrice()).isEqualTo(24);
        assertThat(qxTrade.getNumberOfShares()).isEqualTo(490_029);
    }

}