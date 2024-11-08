package org.qubic.qx.sync.adapter.il;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.domain.event.EventType;
import at.qubic.api.domain.event.response.*;
import at.qubic.api.domain.qx.Qx;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.qubic.qx.sync.domain.TransactionEvent;
import org.qubic.qx.sync.domain.TransactionEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Tag("MANUAL") // do not run in CI
class IntegrationEventApiServiceManualIT {

    @Autowired
    private IdentityUtil identityUtil;

    @Autowired
    private IntegrationEventApiService apiClient;

    @Test
    void getTickEventsForQxTransaction() {

//        long tickNumber = 16585576;
        long tickNumber = 17031427;
        List<TransactionEvents> transactionEvents = apiClient.getTickEvents(tickNumber).block();
        log.info("Transaction events: {}", transactionEvents);
        assertThat(transactionEvents).isNotEmpty();

        List<TransactionEvent> events = transactionEvents.stream().flatMap(e -> e.events().stream()).toList();

        for (TransactionEvent transactionEvent : events) {
            log.info(transactionEvent.toString());
            if (transactionEvent.eventType() == EventType.ASSET_ISSUANCE.getCode()) {
                AssetIssuanceEvent event = AssetIssuanceEvent.fromBytes(Base64.decode(transactionEvent.eventData()));
                log.info(event.toString());
            } else if (transactionEvent.eventType() == EventType.ASSET_OWNERSHIP_CHANGE.getCode()) {
                AssetChangeEvent event = AssetChangeEvent.fromBytes(Base64.decode(transactionEvent.eventData()));
                log.info("Asset [{}]/[{}] ownership change from [{}] to [{}]. Number of shares: [{}]",
                        StringUtils.abbreviate(identityUtil.getIdentityFromPublicKey(event.getIssuerPublicKey()), 8),
                        event.getName(),
                        identityUtil.getIdentityFromPublicKey(event.getSourcePublicKey()),
                        identityUtil.getIdentityFromPublicKey(event.getDestinationPublicKey()),
                        event.getNumberOfShares()
                );
            } else if (transactionEvent.eventType() == EventType.ASSET_POSSESSION_CHANGE.getCode()) {
                AssetChangeEvent event = AssetChangeEvent.fromBytes(Base64.decode(transactionEvent.eventData()));
                log.info("Asset [{}]/[{}] possession change from [{}] to [{}]. Number of shares: [{}]",
                        StringUtils.abbreviate(identityUtil.getIdentityFromPublicKey(event.getIssuerPublicKey()), 8),
                        event.getName(),
                        identityUtil.getIdentityFromPublicKey(event.getSourcePublicKey()),
                        identityUtil.getIdentityFromPublicKey(event.getDestinationPublicKey()),
                        event.getNumberOfShares()
                        );
            } else if (transactionEvent.eventType() == EventType.QU_TRANSFER.getCode()) {
                QuTransferEvent event = QuTransferEvent.fromBytes(Base64.decode(transactionEvent.eventData()));
                log.info("Qu transfer from [{}] to [{}]: [{}]",
                        identityUtil.getIdentityFromPublicKey(event.getSourcePublicKey()),
                        identityUtil.getIdentityFromPublicKey(event.getDestinationPublicKey()),
                        event.getAmount()
                );
            } else if (transactionEvent.eventType() == EventType.CONTRACT_INFORMATION_MESSAGE.getCode()) {
                ContractInformationEvent cim = ContractInformationEvent.fromBytes(Base64.decode(transactionEvent.eventData()));
                if (cim.getContractIndex() == Qx.CONTRACT_INDEX) {
                    QxTradeMessageEvent event = QxTradeMessageEvent.fromBytes(Base64.decode(transactionEvent.eventData()));
                    log.info("Qx trade event. Asset [{}]/[{}], price: [{}], shares [{}].",
                            StringUtils.abbreviate(identityUtil.getIdentityFromPublicKey(event.getIssuer()), 8),
                            event.getAssetName(),
                            event.getPrice(), event.getNumberOfShares());
                } else {
                    log.warn("Unknown contract information message: {}", cim);
                }
            } else {
                log.warn("Unprocessed event of type [{}]", transactionEvent.eventType());
            }
        }


    }

}