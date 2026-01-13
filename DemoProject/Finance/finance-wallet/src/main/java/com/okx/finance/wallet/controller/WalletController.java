package com.okx.finance.wallet.controller;

import com.okx.finance.common.dto.Result;
import com.okx.finance.wallet.dto.DepositRequest;
import com.okx.finance.wallet.dto.WithdrawRequest;
import com.okx.finance.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping("/deposit/address")
    public Result<?> getDepositAddress(@RequestHeader("Authorization") String token,
                                       @RequestParam String currency) {
        return walletService.getDepositAddress(token, currency);
    }

    @PostMapping("/deposit")
    public Result<?> deposit(@RequestHeader("Authorization") String token,
                            @RequestBody DepositRequest request) {
        return walletService.deposit(token, request);
    }

    @PostMapping("/withdraw")
    public Result<?> withdraw(@RequestHeader("Authorization") String token,
                             @RequestBody WithdrawRequest request) {
        return walletService.withdraw(token, request);
    }

    @GetMapping("/deposits")
    public Result<?> getDepositHistory(@RequestHeader("Authorization") String token) {
        return walletService.getDepositHistory(token);
    }

    @GetMapping("/withdrawals")
    public Result<?> getWithdrawalHistory(@RequestHeader("Authorization") String token) {
        return walletService.getWithdrawalHistory(token);
    }
}
