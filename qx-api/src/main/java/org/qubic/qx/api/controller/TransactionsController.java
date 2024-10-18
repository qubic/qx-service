package org.qubic.qx.api.controller;

import jakarta.validation.constraints.Size;
import org.qubic.qx.api.controller.domain.TradeDto;
import org.qubic.qx.api.validation.OneOf;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@Validated
@RestController
@RequestMapping("/service/v1/qx")
public class TransactionsController {

    @GetMapping("/transactions")
    public List<TradeDto> getTrades() {
        return List.of(); // FIXME
    }

    @GetMapping("/issuer/{issuer}/asset/{asset}/transactions")
    public List<TradeDto> getAssetTransactions(@PathVariable("issuer") @Size(min = 60, max = 60) String issuer,
                                               @PathVariable("asset") @Size(min = 1, max = 7) String asset) {
        return List.of(); // FIXME
    }

    @GetMapping("/entity/{identity}/transactions")
    public List<TradeDto>  getEntityTransactions(@PathVariable("identity") @Size(min = 60, max = 60) String identity) {
        return List.of(); // FIXME
    }

    @GetMapping("/transactions/type/{inputType}")
    public List<TradeDto>  getTransactionsPerType(@PathVariable("inputType") @OneOf({1,2,5,6,7,8}) int type) {
        return List.of(); // FIXME
    }

}
