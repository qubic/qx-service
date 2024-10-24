package org.qubic.qx.api.controller;

import jakarta.validation.constraints.Size;
import org.apache.commons.collections4.CollectionUtils;
import org.qubic.qx.api.controller.domain.TransactionDto;
import org.qubic.qx.api.controller.service.TransactionsService;
import org.qubic.qx.api.validation.AllOf;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@Validated
@RestController
@RequestMapping("/service/v1/qx")
public class TransactionsController {

    private final TransactionsService transactionsService;

    public TransactionsController(TransactionsService transactionsService) {
        this.transactionsService = transactionsService;
    }

    @GetMapping("/transactions")
    public List<TransactionDto> getTransactions(@RequestParam(value = "inputTypes", required = false) @AllOf({1,2,5,6,7,8}) List<Integer> inputTypes) {
        return CollectionUtils.isEmpty(inputTypes)
                ? transactionsService.getTransactions()
                : transactionsService.getTransactionsForTypes(inputTypes);
    }

    @GetMapping("/issuer/{issuer}/asset/{asset}/transactions")
    public List<TransactionDto> getTransactionsForAsset(@PathVariable("issuer") @Size(min = 60, max = 60) String issuer,
                                                        @PathVariable("asset") @Size(min = 1, max = 7) String asset) {
        return transactionsService.getTransactionsForAsset(issuer, asset);
    }

    @GetMapping("/entity/{identity}/transactions")
    public List<TransactionDto> getTransactionsForEntity(@PathVariable("identity") @Size(min = 60, max = 60) String identity) {
        return transactionsService.getTransactionsForEntity(identity);
    }

}
