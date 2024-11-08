package org.qubic.qx.sync.job;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.domain.event.EventType;
import at.qubic.api.domain.event.response.AssetChangeEvent;
import at.qubic.api.domain.event.response.ContractInformationEvent;
import at.qubic.api.domain.event.response.QxTradeMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.qubic.qx.sync.adapter.Qx;
import org.qubic.qx.sync.domain.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
public class EventsProcessor {

    private final IdentityUtil identityUtil;

    public EventsProcessor(IdentityUtil identityUtil) {
        this.identityUtil = identityUtil;
    }

    public List<Trade> calculateTrades(long tickNumber, Instant tickTime, List<TransactionEvents> events, List<Transaction> txs) {
        List<Trade> trades = new ArrayList<>();
        for (Transaction tx : txs) {

            if (tx.extraData() instanceof QxAssetOrderData orderData) {

                List<TransactionEvent> relevantEvents = events.stream()
                        .filter(event -> StringUtils.equals(event.txId(), tx.transactionHash()))
                        .flatMap(e -> e.events().stream())
                        .toList();


                List<TransactionEvent> quTransfers = relevantEvents.stream()
                        .filter(byTransactionEvent(EventType.QU_TRANSFER))
                        .toList();

                List<TransactionEvent> assetTransfers = relevantEvents.stream()
                        .filter(byTransactionEvent(EventType.ASSET_OWNERSHIP_CHANGE))
                        .toList();

                List<TransactionEvent> qxTrades = relevantEvents.stream()
                        .filter(byTransactionEvent(EventType.CONTRACT_INFORMATION_MESSAGE))
                        .filter(e -> { // filter trade messages
                            ContractInformationEvent cie = ContractInformationEvent.fromBytes(Base64.getDecoder().decode(e.eventData()));
                            return cie.getType() == 0 && cie.getContractIndex() == Qx.CONTRACT_INDEX;
                        })
                        .toList();

                log.info("Events for transaction [{}]: [{}] qu transfers, [{}] asset transfers, [{}] trades",
                        tx.transactionHash(), quTransfers.size(), assetTransfers.size(), qxTrades.size());

                for (int i = 0; i < qxTrades.size(); i++) {

                    boolean isAskOrder = isAskOrder(getOrderType(tx));
                    TransactionEvent event = qxTrades.get(i);
                    QxTradeMessageEvent qxTrade = QxTradeMessageEvent.fromBytes(Base64.getDecoder().decode(event.eventData()));
                    String maker = tryToInferMakerFromEvents(qxTrades, assetTransfers, quTransfers, isAskOrder, i);
                    Trade trade = new Trade(tickNumber, tickTime.getEpochSecond(), tx.transactionHash(), !isAskOrder, tx.sourcePublicId(), maker, orderData.issuer(), orderData.name(), qxTrade.getPrice(), qxTrade.getNumberOfShares());
                    log.info("Detected trade: {}", trade);
                    trades.add(trade);

                }
            }
        }
        return trades;
    }

    private String tryToInferMakerFromEvents(List<TransactionEvent> qxTrades, List<TransactionEvent> assetTransfers, List<TransactionEvent> quTransfers, boolean isAskOrder, int i) {
        String maker = qxTrades.size() == assetTransfers.size() && qxTrades.size() == quTransfers.size()
                ? isAskOrder ? getSourcePublicKey(getAssetChangeEvent(assetTransfers.get(i))) : getDestinationPublicKey(getAssetChangeEvent(assetTransfers.get(i)))
                : null;
        if (StringUtils.isBlank(maker)) {
            log.warn("Could not infer maker from trade events: {} {} {}", qxTrades, assetTransfers, quTransfers);
        }
        return maker;
    }

    private String getSourcePublicKey(AssetChangeEvent assetChangeEvent) {
        return identityUtil.getIdentityFromPublicKey(assetChangeEvent.getSourcePublicKey());
    }

    private String getDestinationPublicKey(AssetChangeEvent assetChangeEvent) {
        return identityUtil.getIdentityFromPublicKey(assetChangeEvent.getDestinationPublicKey());
    }

    private static AssetChangeEvent getAssetChangeEvent(TransactionEvent transactionEvent) {
        return AssetChangeEvent.fromBytes(Base64.getDecoder().decode(transactionEvent.eventData()));
    }

    private static Predicate<TransactionEvent> byTransactionEvent(EventType quTransfer) {
        return e -> e.eventType() == quTransfer.getCode();
    }

    private static Qx.OrderType getOrderType(Transaction tx) {
        Qx.OrderType orderType = Qx.OrderType.fromCode(tx.inputType());
        assert orderType == Qx.OrderType.ADD_BID || orderType == Qx.OrderType.ADD_ASK || orderType == Qx.OrderType.REMOVE_BID || orderType == Qx.OrderType.REMOVE_ASK;
        return orderType;
    }

    private static boolean isAskOrder(Qx.OrderType orderType) {
        return orderType == Qx.OrderType.REMOVE_ASK || orderType == Qx.OrderType.ADD_ASK;
    }

}
