package org.qubic.qx.api.controller.service;

import org.junit.jupiter.api.Test;
import org.qubic.qx.api.db.TradesRepository;
import org.qubic.qx.api.db.domain.Asset;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.mockito.Mockito.*;

class TradesServiceTest {

    private static final Pageable TEST_PAGEABLE = PageRequest.of(0, 50);

    private final TradesRepository repository = mock();
    private final TradesService service = new TradesService(repository);

    @Test
    void getTrades() {
        service.getTrades(TEST_PAGEABLE);
        verify(repository).findAll(0, 50);
    }

    @Test
    void getSmartContractTrades() {
        service.getSmartContractTrades(TEST_PAGEABLE);
        verify(repository).findByIssuer(Asset.SMART_CONTRACT_ISSUER, 0, 50);
    }

    @Test
    void getTokenTrades() {
        service.getTokenTrades(TEST_PAGEABLE);
        verify(repository).findByIssuerIsNot(Asset.SMART_CONTRACT_ISSUER, 0, 50);
    }

    @Test
    void getAssetTrades() {
        service.getAssetTrades("issuer", "asset", TEST_PAGEABLE);
        verify(repository).findByIssuerAndAsset("issuer", "asset", 0, 50);
    }

    @Test
    void getEntityTrades() {
        service.getEntityTrades("entity", TEST_PAGEABLE);
        verify(repository).findByEntity("entity", 0, 50);
    }

}