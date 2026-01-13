package com.okx.finance.account.controller;

import com.okx.finance.account.dto.TransferRequest;
import com.okx.finance.account.service.AccountService;
import com.okx.finance.common.dto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/balance")
    public Result<?> getBalance(@RequestHeader("Authorization") String token,
                                @RequestParam String currency) {
        return accountService.getBalance(token, currency);
    }

    @GetMapping("/balances")
    public Result<?> getAllBalances(@RequestHeader("Authorization") String token) {
        return accountService.getAllBalances(token);
    }

    @PostMapping("/transfer")
    public Result<?> transfer(@RequestHeader("Authorization") String token,
                              @RequestBody TransferRequest request) {
        return accountService.transfer(token, request);
    }

    @PostMapping("/freeze")
    public Result<?> freeze(@RequestHeader("Authorization") String token,
                            @RequestParam String currency,
                            @RequestParam String amount) {
        return accountService.freeze(token, currency, amount);
    }

    @PostMapping("/unfreeze")
    public Result<?> unfreeze(@RequestHeader("Authorization") String token,
                              @RequestParam String currency,
                              @RequestParam String amount) {
        return accountService.unfreeze(token, currency, amount);
    }
}
