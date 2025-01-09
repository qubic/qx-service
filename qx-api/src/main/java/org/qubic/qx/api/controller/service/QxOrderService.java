package org.qubic.qx.api.controller.service;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.domain.qx.request.*;
import org.qubic.qx.api.adapter.CoreApiService;

import java.math.BigInteger;

public class QxOrderService {

    private final CoreApiService coreApiService;
    private final IdentityUtil identityUtil;

    public QxOrderService(CoreApiService coreApiService, IdentityUtil identityUtil) {
        this.coreApiService = coreApiService;
        this.identityUtil = identityUtil;
    }

    public QxAssetOrder createAddBidOrder(String issuer, String assetName, BigInteger numberOfShares, BigInteger pricePerShare) {
        return QxAddBidOrder.builder()
                .orderData(assetOrderData(issuer, assetName, numberOfShares, pricePerShare))
                .build();
    }

    public QxAssetOrder createAddAskOrder(String issuer, String assetName, BigInteger numberOfShares, BigInteger pricePerShare) {
        return QxAddAskOrder.builder()
                .orderData(assetOrderData(issuer, assetName, numberOfShares, pricePerShare))
                .build();
    }

    public QxAssetOrder createRemoveBidOrder(String issuer, String assetName, BigInteger numberOfShares, BigInteger pricePerShare) {
        return QxRemoveBidOrder.builder()
                .orderData(assetOrderData(issuer, assetName, numberOfShares, pricePerShare))
                .build();
    }

    public QxAssetOrder createRemoveAskOrder(String issuer, String assetName, BigInteger numberOfShares, BigInteger pricePerShare) {
        return QxRemoveAskOrder.builder()
                .orderData(assetOrderData(issuer, assetName, numberOfShares, pricePerShare))
                .build();
    }

    private QxAssetOrderData assetOrderData(String issuer, String assetName, BigInteger numberOfShares, BigInteger pricePerShare) {
        return QxAssetOrderData.builder()
                .assetIssuer(identityUtil.getPublicKeyFromIdentity(issuer))
                .assetName(assetName)
                .numberOfShares(numberOfShares.longValue())
                .price(pricePerShare.longValue())
                .build();
    }

    public BigInteger getLatestTick() {
        return coreApiService.getLatestTick();
    }

}
