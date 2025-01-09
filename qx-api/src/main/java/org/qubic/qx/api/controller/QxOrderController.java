package org.qubic.qx.api.controller;

import at.qubic.api.domain.qx.Qx;
import at.qubic.api.domain.qx.request.QxAssetOrder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.api.controller.domain.QxOrderRequest;
import org.qubic.qx.api.controller.domain.QxOrderResponse;
import org.qubic.qx.api.controller.service.QxOrderService;
import org.qubic.qx.api.validation.AssetName;
import org.qubic.qx.api.validation.Identity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Base64;

@Slf4j
@CrossOrigin
@Validated
@RestController
@RequestMapping("/service/v1/qx")
public class QxOrderController {

    private final QxOrderService qxOrderService;

    public QxOrderController(QxOrderService qxOrderService) {
        this.qxOrderService = qxOrderService;
    }

    @PostMapping("/issuer/{issuer}/asset/{asset}/add-bid")
    public QxOrderResponse addBidOrder(@PathVariable("issuer") @Identity String issuer,
                                             @PathVariable("asset") @AssetName String asset,
                                             @RequestBody @Valid QxOrderRequest request) {
        log.info("Create add bid order for issuer [{}] and asset [{}]: {}", issuer, asset, request);
        QxAssetOrder order = qxOrderService.createAddBidOrder(issuer, asset, request.numberOfShares(), request.pricePerShare());
        return createResponse(request.from(), order);
    }

    @PostMapping("/issuer/{issuer}/asset/{asset}/add-ask")
    public QxOrderResponse addAskOrder(@PathVariable("issuer") @Identity String issuer,
                                             @PathVariable("asset") @Size(min=1, max=7) String asset,
                                             @RequestBody @Valid QxOrderRequest request) {
        log.info("Create add ask order for issuer [{}] and asset [{}]: {}", issuer, asset, request);
        QxAssetOrder order = qxOrderService.createAddAskOrder(issuer, asset, request.numberOfShares(), request.pricePerShare());
        return createResponse(request.from(), order);
    }

    @PostMapping("/issuer/{issuer}/asset/{asset}/remove-bid")
    public QxOrderResponse removeBidOrder(@PathVariable("issuer") @Identity String issuer,
                                             @PathVariable("asset") @AssetName String asset,
                                             @RequestBody @Valid QxOrderRequest request) {
        log.info("Create remove bid order for issuer [{}] and asset [{}]: {}", issuer, asset, request);
        QxAssetOrder order = qxOrderService.createRemoveBidOrder(issuer, asset, request.numberOfShares(), request.pricePerShare());
        return createResponse(request.from(), order);
    }

    @PostMapping("/issuer/{issuer}/asset/{asset}/remove-ask")
    public QxOrderResponse removeAskOrder(@PathVariable("issuer") @Identity String issuer,
                                             @PathVariable("asset") @Size(min=1, max=7) String asset,
                                             @RequestBody @Valid QxOrderRequest request) {
        log.info("Create remove ask order for issuer [{}] and asset [{}]: {}", issuer, asset, request);
        QxAssetOrder order = qxOrderService.createRemoveAskOrder(issuer, asset, request.numberOfShares(), request.pricePerShare());
        return createResponse(request.from(), order);
    }

    private QxOrderResponse createResponse(String from, QxAssetOrder order) {
        BigInteger latestTick = qxOrderService.getLatestTick();
        return new QxOrderResponse(latestTick, from, Qx.ADDRESS, order.getInputType(), new BigInteger(Long.toUnsignedString(order.getAmount())), convertToBase64(order));
    }

    private String convertToBase64(QxAssetOrder qxAssetOrder) {
        return Base64.getEncoder().encodeToString(qxAssetOrder.toBytes());
    }



}
