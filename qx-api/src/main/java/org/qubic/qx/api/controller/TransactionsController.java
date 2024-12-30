package org.qubic.qx.api.controller;

import jakarta.validation.constraints.Size;
import org.qubic.qx.api.controller.domain.TransactionDto;
import org.qubic.qx.api.controller.service.TransactionsService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.qubic.qx.api.redis.QxCacheManager.*;

@CrossOrigin
@Validated
@RestController
@RequestMapping("/service/v1/qx")
public class TransactionsController {

    private final TransactionsService transactionsService;

    public TransactionsController(TransactionsService transactionsService) {
        this.transactionsService = transactionsService;
    }

    // make own 'filters' endpoint? These could be stored, cached and lead to fewer variations of get methods.
    // .../transactions/filter/1, .../transactions/filter/2

    // issued assets

    @Cacheable(CACHE_NAME_ISSUED_ASSETS)
    @GetMapping("/issued-assets")
    public List<TransactionDto> getIssuedAssets() {
        return transactionsService.getIssuedAssets();
    }

    // transfers

    @Cacheable(CACHE_NAME_TRANSFERS)
    @GetMapping("/transfers")
    public List<TransactionDto> getTransferTransactions() {
        return transactionsService.getTransferTransactions();
    }

    @Cacheable(cacheNames = CACHE_NAME_TRANSFERS_ASSET, key = CACHE_KEY_ASSET)
    @GetMapping("/issuer/{issuer}/asset/{asset}/transfers")
    public List<TransactionDto> getTransferTransactionsForAsset(@PathVariable("issuer") @Size(min = 60, max = 60) String issuer,
                                                                @PathVariable("asset") @Size(min = 1, max = 7) String asset) {
        return transactionsService.getTransferTransactionsForAsset(issuer, asset);
    }

    @Cacheable(CACHE_NAME_TRANSFERS_ENTITY)
    @GetMapping("/entity/{identity}/transfers")
    public List<TransactionDto> getTransferTransactionsForEntity(@PathVariable("identity") @Size(min = 60, max = 60) String identity) {
        return transactionsService.getTransferTransactionsForEntity(identity);
    }


}
