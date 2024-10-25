package org.qubic.qx.api.controller;

import jakarta.validation.constraints.Size;
import org.qubic.qx.api.controller.domain.TransactionDto;
import org.qubic.qx.api.controller.service.TransactionsService;
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

    // TODO make own 'filters' endpoint? These could be stored, cached and lead to fewer variations of get methods.
    // .../transactions/filter/1, .../transactions/filter/2

    // transfers

    @GetMapping("/transfers")
    public List<TransactionDto> getTransferTransactions() {
        return transactionsService.getTransferTransactions();
    }

    @GetMapping("/issuer/{issuer}/asset/{asset}/transfers")
    public List<TransactionDto> getTransferTransactionsForAsset(@PathVariable("issuer") @Size(min = 60, max = 60) String issuer,
                                                                @PathVariable("asset") @Size(min = 1, max = 7) String asset) {
        return transactionsService.getTransferTransactionsForAsset(issuer, asset);
    }


    @GetMapping("/entity/{identity}/transfers")
    public List<TransactionDto> getTransferTransactionsForSender(@PathVariable("identity") @Size(min = 60, max = 60) String identity) {
        return transactionsService.getTransferTransactionsForSourrce(identity);
    }

    // orders

    @GetMapping("/orders")
    public List<TransactionDto> getOrderTransactions() {
        return transactionsService.getOrderTransactions();
    }

    @GetMapping("/issuer/{issuer}/asset/{asset}/orders")
    public List<TransactionDto> getOrderTransactionsForAsset(@PathVariable("issuer") @Size(min = 60, max = 60) String issuer,
                                                             @PathVariable("asset") @Size(min = 1, max = 7) String asset) {
        return transactionsService.getOrderTransactionsForAsset(issuer, asset);
    }

    @GetMapping("/entity/{identity}/orders")
    public List<TransactionDto> getOrderTransactionsForSender(@PathVariable("identity") @Size(min = 60, max = 60) String identity) {
        return transactionsService.getOrderTransactionsForSourrce(identity);
    }



}
