package org.qubic.qx.api.adapter.il;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.domain.RequestContractFunction;
import at.qubic.api.domain.qx.Qx;
import at.qubic.api.domain.qx.Qx.Function;
import at.qubic.api.domain.qx.request.*;
import at.qubic.api.domain.qx.response.QxFees;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.qubic.qx.api.adapter.LiveApiService;
import org.qubic.qx.api.adapter.QxApiService;
import org.qubic.qx.api.adapter.il.domain.QuerySmartContractRequest;
import org.qubic.qx.api.adapter.il.domain.QuerySmartContractResponse;
import org.qubic.qx.api.controller.domain.AssetOrder;
import org.qubic.qx.api.controller.domain.EntityOrder;
import org.qubic.qx.api.controller.domain.Fees;

import java.math.BigInteger;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.EMPTY;


@Slf4j
public class IntegrationApiService implements LiveApiService, QxApiService {

    private final IdentityUtil identityUtil;
    private final IntegrationLiveClient integrationLiveClient;
    private final QxMapper qxMapper;

    public IntegrationApiService(IdentityUtil identityUtil, IntegrationLiveClient integrationLiveClient, QxMapper qxMapper) {
        this.identityUtil = identityUtil;
        this.integrationLiveClient = integrationLiveClient;
        this.qxMapper = qxMapper;
    }

    @Override
    public BigInteger getLatestTick() {
        return integrationLiveClient.getLatestTick();
    }


    @Override
    public Fees getFees() {
        QuerySmartContractRequest request = new QuerySmartContractRequest(Qx.CONTRACT_INDEX, Function.QX_GET_FEE.getCode(),
                0, EMPTY);
        QuerySmartContractResponse response = integrationLiveClient.querySmartContract(request);
        QxFees fees = QxFees.fromBytes(Base64.decodeBase64(response.responseData()));
        return qxMapper.mapFees(fees);
    }

    @Override
    public List<AssetOrder> getAssetAskOrders(String issuer, String asset) {
        QxGetAssetAskOrders input = new QxGetAssetAskOrders(identityUtil.getPublicKeyFromIdentity(issuer), asset, 0);
        return queryAssetOrders(input);
    }

    @Override
    public List<AssetOrder> getAssetBidOrders(String issuer, String asset) {
        QxGetAssetBidOrders input = new QxGetAssetBidOrders(identityUtil.getPublicKeyFromIdentity(issuer), asset, 0);
        return queryAssetOrders(input);
    }

    @Override
    public List<EntityOrder> getEntityAskOrders(String identity) {
        QxGetEntityAskOrders input = new QxGetEntityAskOrders(identityUtil.getPublicKeyFromIdentity(identity), 0);
        return queryEntityOrders(input);
    }

    @Override
    public List<EntityOrder> getEntityBidOrders(String identity) {
        QxGetEntityBidOrders input = new QxGetEntityBidOrders(identityUtil.getPublicKeyFromIdentity(identity), 0);
        return queryEntityOrders(input);
    }

    private List<AssetOrder> queryAssetOrders(QxGetAssetOrders input) {
        QuerySmartContractResponse response = doSmartContractCall(input);
        List<at.qubic.api.domain.qx.response.AssetOrder> assetOrders = at.qubic.api.domain.qx.response.AssetOrder.getAssetOrders(Base64.decodeBase64(response.responseData()));
        return qxMapper.mapQxAssetOrderList(assetOrders);
    }

    private List<EntityOrder> queryEntityOrders(QxGetEntityOrders input) {
        QuerySmartContractResponse response = doSmartContractCall(input);
        List<at.qubic.api.domain.qx.response.EntityOrder> entityOrders = at.qubic.api.domain.qx.response.EntityOrder.getEntityOrders(Base64.decodeBase64(response.responseData()));
        return qxMapper.mapQxEntityOrderList(entityOrders);

    }

    private QuerySmartContractResponse doSmartContractCall(RequestContractFunction input) {
        byte[] inputData = input.toBytes();
        QuerySmartContractRequest request = new QuerySmartContractRequest(input.getContractIndex(),
                input.getInputType(),
                inputData.length,
                Base64.encodeBase64String(inputData));
        return integrationLiveClient.querySmartContract(request);
    }
}
