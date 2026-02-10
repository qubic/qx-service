package org.qubic.qx.api.controller;

import org.qubic.qx.api.controller.service.TransactionsService;
import org.qubic.qx.api.db.dto.TransactionDto;
import org.qubic.qx.api.validation.AssetName;
import org.qubic.qx.api.validation.Identity;
import org.qubic.qx.api.validation.Pagination;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
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

    // issued assets

    @Cacheable(CACHE_NAME_ISSUED_ASSETS)
    @GetMapping("/issued-assets")
    public List<TransactionDto> getIssuedAssets(@Pagination Pageable pageable) {
        return transactionsService.getIssuedAssets(pageable);
    }

    // transfers

    @Cacheable(CACHE_NAME_TRANSFERS)
    @GetMapping("/transfers")
    public List<TransactionDto> getTransferTransactions(@Pagination Pageable pageable) {
        return transactionsService.getTransferTransactions(pageable);
    }

    @Cacheable(cacheNames = CACHE_NAME_TRANSFERS_ASSET)
    @GetMapping("/issuer/{issuer}/asset/{asset}/transfers")
    public List<TransactionDto> getTransferTransactionsForAsset(@PathVariable @Identity String issuer,
                                                                @PathVariable @AssetName String asset,
                                                                @Pagination Pageable pageable) {
        return transactionsService.getTransferTransactionsForAsset(issuer, asset, pageable);
    }

    @Cacheable(CACHE_NAME_TRANSFERS_ENTITY)
    @GetMapping("/entity/{identity}/transfers")
    public List<TransactionDto> getTransferTransactionsForEntity(@PathVariable @Identity String identity,
                                                                 @Pagination Pageable pageable) {
        return transactionsService.getTransferTransactionsForEntity(identity, pageable);
    }


}
