package org.qubic.qx.sync.job;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.domain.event.EventType;
import at.qubic.api.domain.event.response.AssetChangeEvent;
import at.qubic.api.domain.event.response.ContractInformationEvent;
import at.qubic.api.domain.event.response.QxTradeMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.qubic.qx.sync.adapter.Qx;
import org.qubic.qx.sync.domain.*;

import java.util.*;
import java.util.function.Predicate;

@Slf4j
public class EventsProcessor {

    private final IdentityUtil identityUtil;

    public EventsProcessor(IdentityUtil identityUtil) {
        this.identityUtil = identityUtil;
    }

    public List<Trade> calculateTrades(TransactionWithMeta tx, QxAssetOrderData orderData) {

        List<Trade> trades = new ArrayList<>();
        List<AssetChangeEvent> assetTransfers = getAssetTransfers(tx.getEvents());
        List<QxTradeMessageEvent> qxTrades = getTrades(tx.getEvents());

        log.info("Events for transaction [{}]: [{}] asset transfers, [{}] trades",
                tx.transactionHash(),assetTransfers.size(), qxTrades.size());

        log.debug("Asset transfers: {}", assetTransfers);
        log.debug("Trades: {}", qxTrades);

        for (int i = 0; i < qxTrades.size(); i++) {

            QxTradeMessageEvent qxTrade = qxTrades.get(i);
            log.info("Processing trade event: {}", qxTrade);

            String maker = tryToInferMakerFromEvents(qxTrades, assetTransfers, isAskOrder(getOrderType(tx.inputType())), i);

            Trade trade = new Trade(tx.tick(),
                    Objects.requireNonNull(tx.getTime()).getEpochSecond(),
                    tx.transactionHash(),
                    !isAskOrder(getOrderType(tx.inputType())),
                    tx.sourcePublicId(),
                    maker,
                    orderData.issuer(),
                    orderData.name(),
                    qxTrade.getPrice(),
                    qxTrade.getNumberOfShares());
            log.info("Detected trade: {}", trade);
            trades.add(trade);

        }
        return trades;
    }

    public boolean isAssetTransferred(List<TransactionEvent> events) {
        return events != null && events.stream().anyMatch(byTransactionEvent(EventType.ASSET_OWNERSHIP_CHANGE));
    }

    public boolean isAssetIssued(List<TransactionEvent> events) {
        return events != null && events.stream().anyMatch(byTransactionEvent(EventType.ASSET_ISSUANCE));
    }

    private String tryToInferMakerFromEvents(List<QxTradeMessageEvent> qxTrades, List<AssetChangeEvent> assetTransfers, boolean isAskOrder, int i) {
        Optional<AssetChangeEvent> assetChangeEvent = getAssetChangeEventForTrade(qxTrades, assetTransfers, i);
        return assetChangeEvent.map(e -> isAskOrder
                        ? getIdentityFromPublicKey(e.getDestinationPublicKey())
                        : getIdentityFromPublicKey(e.getSourcePublicKey()))
                .orElse(null);
    }

    private String getIdentityFromPublicKey(byte[] publicKey) {
        return identityUtil.getIdentityFromPublicKey(publicKey);
    }

    private static Optional<AssetChangeEvent> getAssetChangeEventForTrade(List<QxTradeMessageEvent> qxTrades, List<AssetChangeEvent> assetTransfers, int i) {

        QxTradeMessageEvent qxTrade = qxTrades.get(i);
        List<AssetChangeEvent> relevantAssetChanges = assetTransfers.stream()
                .filter(tr -> tr.getNumberOfShares() == qxTrade.getNumberOfShares())
                .toList();

        if (relevantAssetChanges.size() == 1) { // easy

            return Optional.of(relevantAssetChanges.getFirst());

        } else if (qxTrades.size() == assetTransfers.size() // one asset change per trade
                && qxTrade.getNumberOfShares() == assetTransfers.get(i).getNumberOfShares()) {

            log.info("Taking trade #{} to find maker.", i);
            return Optional.of(assetTransfers.get(i)); // hope that order is deterministic

        } else {
            log.warn("Could not infer maker from trade events: {} {}. Index: [{}].", qxTrades, assetTransfers, i);
            return Optional.empty();

        }

    }



    private static List<QxTradeMessageEvent> getTrades(List<TransactionEvent> relevantEvents) {
        return relevantEvents.stream()
                .filter(byTransactionEvent(EventType.CONTRACT_INFORMATION_MESSAGE))
                .filter(e -> { // filter trade messages
                    ContractInformationEvent cie = ContractInformationEvent.fromBytes(Base64.getDecoder().decode(e.eventData()));
                    return cie.getType() == 0 && cie.getContractIndex() == Qx.CONTRACT_INDEX;
                })
                .map(e -> QxTradeMessageEvent.fromBytes(Base64.getDecoder().decode(e.eventData())))
                .toList();
    }

    private static List<AssetChangeEvent> getAssetTransfers(List<TransactionEvent> relevantEvents) {
        return Objects.requireNonNull(relevantEvents).stream()
                .filter(byTransactionEvent(EventType.ASSET_OWNERSHIP_CHANGE))
                .map(e -> AssetChangeEvent.fromBytes(Base64.getDecoder().decode(e.eventData())))
                .toList();
    }

    private static Predicate<TransactionEvent> byTransactionEvent(EventType quTransfer) {
        return e -> e.eventType() == quTransfer.getCode();
    }

    private static Qx.OrderType getOrderType(int inputType) {
        Qx.OrderType orderType = Qx.OrderType.fromCode(inputType);
        assert orderType == Qx.OrderType.ADD_BID || orderType == Qx.OrderType.ADD_ASK || orderType == Qx.OrderType.REMOVE_BID || orderType == Qx.OrderType.REMOVE_ASK;
        return orderType;
    }

    private static boolean isAskOrder(Qx.OrderType orderType) {
        return orderType == Qx.OrderType.REMOVE_ASK || orderType == Qx.OrderType.ADD_ASK;
    }

}
