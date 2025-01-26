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

        long tickNumber = 18699147;
        List<TransactionEvents> transactionEvents = apiClient.getTickEvents(tickNumber).block();
        log.trace("Transaction events: {}", transactionEvents);
        assertThat(transactionEvents).isNotNull();
        transactionEvents.forEach(transactionEvent -> log.info("Transaction: {}", transactionEvent.txId()));
        assertThat(transactionEvents).isNotEmpty();
        List<TransactionEvent> events = transactionEvents.stream().flatMap(e -> e.events().stream()).toList();

        for (TransactionEvent transactionEvent : events) {
            log.trace(transactionEvent.toString());
            String eventData = transactionEvent.eventData();
            log.debug("base64 event data: {}", eventData);
            byte[] byteEventData = Base64.decode(eventData);

            if (transactionEvent.eventType() == EventType.ASSET_ISSUANCE.getCode()) {
                AssetIssuanceEvent event = AssetIssuanceEvent.fromBytes(byteEventData);
                log.info(event.toString());
            } else if (transactionEvent.eventType() == EventType.ASSET_OWNERSHIP_CHANGE.getCode()) {
                AssetChangeEvent event = AssetChangeEvent.fromBytes(byteEventData);
                log.info("Asset ownership change: [{}]/[{}] from [{}] to [{}]. Number of shares: [{}]",
                        StringUtils.abbreviate(identityUtil.getIdentityFromPublicKey(event.getIssuerPublicKey()), "",10),
                        event.getName(),
                        identityUtil.getIdentityFromPublicKey(event.getSourcePublicKey()),
                        identityUtil.getIdentityFromPublicKey(event.getDestinationPublicKey()),
                        event.getNumberOfShares()
                );
            } else if (transactionEvent.eventType() == EventType.ASSET_POSSESSION_CHANGE.getCode()) {
                AssetChangeEvent event = AssetChangeEvent.fromBytes(byteEventData);
                log.info("Asset possession change: [{}]/[{}] from [{}] to [{}]. Number of shares: [{}]",
                        StringUtils.abbreviate(identityUtil.getIdentityFromPublicKey(event.getIssuerPublicKey()), "",10),
                        event.getName(),
                        identityUtil.getIdentityFromPublicKey(event.getSourcePublicKey()),
                        identityUtil.getIdentityFromPublicKey(event.getDestinationPublicKey()),
                        event.getNumberOfShares()
                        );
            } else if (transactionEvent.eventType() == EventType.QU_TRANSFER.getCode()) {
                QuTransferEvent event = QuTransferEvent.fromBytes(byteEventData);
                log.info("Qu transfer: from [{}] to [{}]: [{}]",
                        identityUtil.getIdentityFromPublicKey(event.getSourcePublicKey()),
                        identityUtil.getIdentityFromPublicKey(event.getDestinationPublicKey()),
                        event.getAmount()
                );
            } else if (transactionEvent.eventType() == EventType.CONTRACT_INFORMATION_MESSAGE.getCode()) {
                ContractInformationEvent cim = ContractInformationEvent.fromBytes(byteEventData);
                if (cim.getContractIndex() == Qx.CONTRACT_INDEX) {
                    QxTradeMessageEvent event = QxTradeMessageEvent.fromBytes(byteEventData);
                    log.info("Qx trade: Asset [{}]/[{}], price: [{}], shares [{}].",
                            StringUtils.abbreviate(identityUtil.getIdentityFromPublicKey(event.getIssuer()), "", 10),
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