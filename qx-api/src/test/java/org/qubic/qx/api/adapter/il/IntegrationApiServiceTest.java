package org.qubic.qx.api.adapter.il;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.domain.qx.Qx;
import at.qubic.api.domain.qx.Qx.Function;
import at.qubic.api.domain.qx.request.QxGetAssetAskOrders;
import at.qubic.api.domain.qx.request.QxGetAssetBidOrders;
import at.qubic.api.domain.qx.request.QxGetEntityAskOrders;
import at.qubic.api.domain.qx.request.QxGetEntityBidOrders;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.qubic.qx.api.adapter.il.domain.QuerySmartContractRequest;
import org.qubic.qx.api.adapter.il.domain.QuerySmartContractResponse;
import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;
import org.qubic.qx.api.uitl.FileUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IntegrationApiServiceTest {

    private final IdentityUtil identityUtil = new IdentityUtil();
    private final IntegrationLiveClient integrationLiveClient = mock();
    private final QxMapper qxMapper = Mappers.getMapper(QxMapper.class);

    private final IntegrationApiService service = new IntegrationApiService(identityUtil, integrationLiveClient, qxMapper);

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // quick and dirty solution for injecting the data type translator
        Field f = QxMapperImpl.class.getDeclaredField("dataTypeTranslator");
        f.setAccessible(true);
        f.set(qxMapper, new DataTypeTranslator(identityUtil));
    }

    @Test
    void getFees() {
        QuerySmartContractRequest expectedRequest = new QuerySmartContractRequest(Qx.CONTRACT_INDEX, Function.QX_GET_FEE.getCode(), 0, "");
        QuerySmartContractResponse response = new QuerySmartContractResponse("AMqaO2QAAADAxi0A");
        when(integrationLiveClient.querySmartContract(expectedRequest)).thenReturn(response);

        Fees fees = service.getFees();

        assertThat(fees.assetIssuanceFee()).isEqualTo(1_000_000_000);
        assertThat(fees.transferFee()).isEqualTo(100);
        assertThat(fees.tradeFee()).isEqualTo(3_000_000);

        ArgumentCaptor<QuerySmartContractRequest> requestCaptor = ArgumentCaptor.forClass(QuerySmartContractRequest.class);
        verify(integrationLiveClient).querySmartContract(requestCaptor.capture());
    }

    @Test
    void getAssetAskOrders() throws Exception {
        String issuer = "CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL";
        String asset = "CFB";

        byte[] issuerPublicKey = identityUtil.getPublicKeyFromIdentity(issuer);
        QxGetAssetAskOrders getOrders = new QxGetAssetAskOrders(issuerPublicKey, asset, 0);
        byte[] inputBytes = getOrders.toBytes();
        String base64Input = Base64.encodeBase64String(inputBytes);
        QuerySmartContractRequest expectedRequest = new QuerySmartContractRequest(getOrders.getContractIndex(), getOrders.getInputType(), inputBytes.length, base64Input);

        QuerySmartContractResponse response =  FileUtil.readJsonFile("/testdata/db/responses/get-asset-ask-orders-response.json", QuerySmartContractResponse.class);
        when(integrationLiveClient.querySmartContract(expectedRequest)).thenReturn(response);

        List<AssetOrder> assetAskOrders = service.getAssetAskOrders(issuer, asset);

        assertThat(assetAskOrders).hasSize(144);
        AssetOrder firstOrder = assetAskOrders.getFirst();
        assertThat(firstOrder.entityId()).isEqualTo("BDSEQVHDWENCDACLLMEPWHTDJPGDSDGTODHPYNMRPDHKTOAXFKSPRYGEXEOF");
        assertThat(firstOrder.numberOfShares()).isEqualTo(329295222);
        assertThat(firstOrder.price()).isEqualTo(2);
    }

    @Test
    void getAssetBidOrders() throws Exception {
        String issuer = "CFBMEMZOIDEXQAUXYYSZIURADQLAPWPMNJXQSNVQZAHYVOPYUKKJBJUCTVJL";
        String asset = "CFB";

        byte[] issuerPublicKey = identityUtil.getPublicKeyFromIdentity(issuer);
        QxGetAssetBidOrders getOrders = new QxGetAssetBidOrders(issuerPublicKey, asset, 0);

        byte[] inputBytes = getOrders.toBytes();
        String base64Input = Base64.encodeBase64String(inputBytes);
        QuerySmartContractRequest expectedRequest = new QuerySmartContractRequest(getOrders.getContractIndex(), getOrders.getInputType(), inputBytes.length, base64Input);

        QuerySmartContractResponse response =  FileUtil.readJsonFile("/testdata/db/responses/get-asset-bid-orders-response.json", QuerySmartContractResponse.class);
        when(integrationLiveClient.querySmartContract(expectedRequest)).thenReturn(response);

        List<AssetOrder> assetAskOrders = service.getAssetBidOrders(issuer, asset);

        assertThat(assetAskOrders).hasSize(1);
        AssetOrder firstOrder = assetAskOrders.getFirst();
        assertThat(firstOrder.entityId()).isEqualTo("JHCGKVNUKTBVJGGZTCDBPVSYIOIDFUPCQOVCQFNMEGYHVWMINTEJITLGTXGO");
        assertThat(firstOrder.numberOfShares()).isEqualTo(23116901);
        assertThat(firstOrder.price()).isEqualTo(1);
    }

    @Test
    void getEntityAskOrders() throws IOException {
        String entity = "BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC";
        byte[] entityPublicKey = identityUtil.getPublicKeyFromIdentity(entity);
        QxGetEntityAskOrders getOrders = new QxGetEntityAskOrders(entityPublicKey, 0);
        byte[] inputBytes = getOrders.toBytes();
        String base64Input = Base64.encodeBase64String(inputBytes);
        QuerySmartContractRequest expectedRequest = new QuerySmartContractRequest(getOrders.getContractIndex(), getOrders.getInputType(), inputBytes.length, base64Input);
        QuerySmartContractResponse response =  FileUtil.readJsonFile("/testdata/db/responses/get-entity-ask-orders-response.json", QuerySmartContractResponse.class);
        when(integrationLiveClient.querySmartContract(expectedRequest)).thenReturn(response);

        List<EntityOrder> entityOrders = service.getEntityAskOrders(entity);

        assertThat(entityOrders).hasSize(5);
        EntityOrder firstOrder = entityOrders.getFirst();
        assertThat(firstOrder.price()).isEqualTo(1);
        assertThat(firstOrder.numberOfShares()).isEqualTo(676);
        assertThat(firstOrder.assetName()).isEqualTo("CODED");
        assertThat(firstOrder.issuerId()).isEqualTo("CODEDBUUDDYHECBVSUONSSWTOJRCLZSWHFHZIUWVFGNWVCKIWJCSDSWGQAAI");
    }

    @Test
    void getEntityBidOrders() throws IOException {
        String entity = "BOJOBRHAZILUCDADNGBXYUIYHNIDCKBSQEWGUFCAJASIOPFNBMWWXDJHCCTC";
        byte[] entityPublicKey = identityUtil.getPublicKeyFromIdentity(entity);
        QxGetEntityBidOrders getOrders = new QxGetEntityBidOrders(entityPublicKey, 0);
        byte[] inputBytes = getOrders.toBytes();
        String base64Input = Base64.encodeBase64String(inputBytes);
        QuerySmartContractRequest expectedRequest = new QuerySmartContractRequest(getOrders.getContractIndex(), getOrders.getInputType(), inputBytes.length, base64Input);
        QuerySmartContractResponse response =  FileUtil.readJsonFile("/testdata/db/responses/get-entity-bid-orders-response.json", QuerySmartContractResponse.class);
        when(integrationLiveClient.querySmartContract(expectedRequest)).thenReturn(response);

        List<EntityOrder> entityOrders = service.getEntityBidOrders(entity);

        assertThat(entityOrders).hasSize(2);
        EntityOrder firstOrder = entityOrders.getFirst();
        assertThat(firstOrder.price()).isEqualTo(9_500_000_000L);
        assertThat(firstOrder.numberOfShares()).isEqualTo(2);
        assertThat(firstOrder.assetName()).isEqualTo("QX");
        assertThat(firstOrder.issuerId()).isEqualTo("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFXIB");
    }

}
