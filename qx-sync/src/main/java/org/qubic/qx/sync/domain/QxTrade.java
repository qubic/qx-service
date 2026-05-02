package org.qubic.qx.sync.domain;

import at.qubic.api.util.AssetUtil;
import at.qubic.api.util.BufferUtil;
import lombok.Builder;
import lombok.Value;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Builder
@Value
public class QxTrade {

    byte[] issuer;
    String assetName;
    long price;
    long numberOfShares;

    public static QxTrade fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        QxTrade event = QxTrade.builder()
                .issuer(BufferUtil.getByteArray(buffer, 32))
                .assetName(AssetUtil.nameToString(BufferUtil.getByteArray(buffer, 8)))
                .price(buffer.getLong())
                .numberOfShares(buffer.getLong())
                .build();
        assert !buffer.hasRemaining();
        return event;
    }

}
