package org.qubic.qx.sync.adapter;

import at.qubic.api.crypto.IdentityUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.domain.ExtraData;
import org.qubic.qx.sync.domain.QxAssetOrderData;
import org.qubic.qx.sync.domain.QxIssueAssetData;
import org.qubic.qx.sync.domain.QxTransferAssetData;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Slf4j
class ExtraDataMapperTest {

    private final IdentityUtil identityUtil = mock();
    private final ExtraDataMapper mapper = new ExtraDataMapper(identityUtil);

    @BeforeEach
    void initMocks() {
        // simply return input as hex string
        when(identityUtil.getIdentityFromPublicKey(any())).then(args -> Hex.encodeHexString(args.getArgument(0, byte[].class)));
    }

    @Test
    void mapQxOrder() throws Exception {
        byte[] input = Hex.decodeHex("0000000000000000000000000000000000000000000000000000000000000000" +
                "464f4f00000000007b000000000000002d00000000000000");
        ExtraData mapped = mapper.map(5, input);
        assertThat(mapped).isInstanceOf(QxAssetOrderData.class);
        QxAssetOrderData extraData = (QxAssetOrderData) mapped;
        assertThat(extraData).isNotNull();
        assertThat(extraData.issuer()).isEqualTo("0000000000000000000000000000000000000000000000000000000000000000"); // uses mock
        assertThat(extraData.name()).isEqualTo("FOO");
        assertThat(extraData.price()).isEqualTo(123);
        assertThat(extraData.numberOfShares()).isEqualTo(45);

        assertThat(mapper.map(6, input)).isEqualTo(mapped); // add bid
        assertThat(mapper.map(7, input)).isEqualTo(mapped); // remove ask
        assertThat(mapper.map(8, input)).isEqualTo(mapped); // remove bid
    }

    @Test
    void mapQxTransferAsset() {
        byte[] input = ByteBuffer.allocate(80)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(new byte[32])
                .put(new byte[] {1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0})
                .put(new byte[] {'F','O','O',0,0,0,0,0})
                .putLong(666)
                .array();

        ExtraData mapped = mapper.map(2, input);

        assertThat(mapped).isInstanceOf(QxTransferAssetData.class);
        QxTransferAssetData extraData = (QxTransferAssetData) mapped;
        assertThat(extraData).isNotNull();
        assertThat(extraData.issuer()).isEqualTo("0000000000000000000000000000000000000000000000000000000000000000");  // uses mock
        assertThat(extraData.newOwner()).isEqualTo("0101010000000000000000000000000000000000000000000000000000000000");  // uses mock
        assertThat(extraData.name()).isEqualTo("FOO");
        assertThat(extraData.numberOfShares()).isEqualTo(666);
    }

    @Test
    void mapQxIssueAsset() {
        byte[] input = ByteBuffer.allocate(25)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(new byte[] {'F','O','O',0,0,0,0,0})
                .putLong(666)
                .put(new byte[] {0,0,0,0,0,0,0})
                .put((byte) 16)
                .array();

        ExtraData mapped = mapper.map(1, input);

        assertThat(mapped).isInstanceOf(QxIssueAssetData.class);
        QxIssueAssetData extraData = (QxIssueAssetData) mapped;
        assertThat(extraData.name()).isEqualTo("FOO");
        assertThat(extraData.numberOfShares()).isEqualTo(666);
        assertThat(extraData.unitOfMeasurement()).isEqualTo("AAAAAAAAAA==");
        assertThat(extraData.numberOfDecimalPlaces()).isEqualTo((byte) 16);
    }

    @Test
    void mapQxIssueAsset_withNullCharacters() {
        byte[] input = ByteBuffer.allocate(25)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(new byte[] {'F','O','O',0,0,0,0,0})
                .putLong(666)
                .put(new byte[] {0,-48,0,-48,-48,-48,-48})
                .put((byte) 16)
                .array();

        ExtraData mapped = mapper.map(1, input);

        assertThat(mapped).isInstanceOf(QxIssueAssetData.class);
        QxIssueAssetData extraData = (QxIssueAssetData) mapped;
        assertThat(extraData.name()).isEqualTo("FOO");
        assertThat(extraData.numberOfShares()).isEqualTo(666);
        assertThat(extraData.unitOfMeasurement()).isEqualTo("ANAA0NDQ0A==");
        assertThat(extraData.numberOfDecimalPlaces()).isEqualTo((byte) 16);
    }

    @Test
    void mapQxOrder_withMissingName_thenProcess() {
        byte[] input = Base64.decodeBase64("uXOQ1aRJ7kCH5cV762dGBongHEYBh0EXzUemX49l+IQBAAAAAAAAAAEAAAAAAAAAAQAAAAAAAAA=");
        ExtraData mapped = mapper.map(5, input);
        assertThat(mapped).isInstanceOf(QxAssetOrderData.class);

        QxAssetOrderData extraData = (QxAssetOrderData) mapped;
        assertThat(extraData).isNotNull();
        assertThat(extraData.name()).isEmpty();
    }

    // mapper will throw if incompatible data is mapped
    @Test
    void mapQxOrder_withInvalidInputData_thenThrow() {
        assertThatThrownBy(() -> mapper.map(5, new byte[0])).isInstanceOf(BufferUnderflowException.class);
    }

}