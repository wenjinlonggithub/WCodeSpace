package com.okx.finance.account.service;

import com.okx.finance.account.dto.TransferRequest;
import com.okx.finance.account.mapper.AccountMapper;
import com.okx.finance.common.dto.Result;
import com.okx.finance.common.entity.Account;
import com.okx.finance.common.util.JwtUtil;
import com.okx.finance.common.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;

    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(2);

    public Result<?> getBalance(String token, String currency) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        Account account = accountMapper.findByUserIdAndCurrency(userId, currency);

        if (account == null) {
            account = createAccount(userId, currency);
        }

        return Result.success(account);
    }

    public Result<?> getAllBalances(String token) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        List<Account> accounts = accountMapper.findByUserId(userId);

        return Result.success(accounts);
    }

    @Transactional
    public Result<?> transfer(String token, TransferRequest request) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        Account fromAccount = accountMapper.findByUserIdAndCurrencyForUpdate(userId, request.getFromCurrency());

        if (fromAccount == null) {
            return Result.error("Account not found");
        }

        BigDecimal amount = new BigDecimal(request.getAmount());
        if (fromAccount.getAvailableBalance().compareTo(amount) < 0) {
            return Result.error("Insufficient balance");
        }

        Account toAccount = accountMapper.findByUserIdAndCurrencyForUpdate(userId, request.getToCurrency());
        if (toAccount == null) {
            toAccount = createAccount(userId, request.getToCurrency());
        }

        fromAccount.setAvailableBalance(fromAccount.getAvailableBalance().subtract(amount));
        fromAccount.setTotalBalance(fromAccount.getTotalBalance().subtract(amount));
        accountMapper.update(fromAccount);

        BigDecimal convertedAmount = amount.multiply(new BigDecimal(request.getRate()));
        toAccount.setAvailableBalance(toAccount.getAvailableBalance().add(convertedAmount));
        toAccount.setTotalBalance(toAccount.getTotalBalance().add(convertedAmount));
        accountMapper.update(toAccount);

        return Result.success("Transfer successful");
    }

    @Transactional
    public Result<?> freeze(String token, String currency, String amount) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        Account account = accountMapper.findByUserIdAndCurrencyForUpdate(userId, currency);

        if (account == null) {
            return Result.error("Account not found");
        }

        BigDecimal freezeAmount = new BigDecimal(amount);
        if (account.getAvailableBalance().compareTo(freezeAmount) < 0) {
            return Result.error("Insufficient balance");
        }

        account.setAvailableBalance(account.getAvailableBalance().subtract(freezeAmount));
        account.setFrozenBalance(account.getFrozenBalance().add(freezeAmount));
        accountMapper.update(account);

        return Result.success("Freeze successful");
    }

    @Transactional
    public Result<?> unfreeze(String token, String currency, String amount) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        Account account = accountMapper.findByUserIdAndCurrencyForUpdate(userId, currency);

        if (account == null) {
            return Result.error("Account not found");
        }

        BigDecimal unfreezeAmount = new BigDecimal(amount);
        if (account.getFrozenBalance().compareTo(unfreezeAmount) < 0) {
            return Result.error("Insufficient frozen balance");
        }

        account.setFrozenBalance(account.getFrozenBalance().subtract(unfreezeAmount));
        account.setAvailableBalance(account.getAvailableBalance().add(unfreezeAmount));
        accountMapper.update(account);

        return Result.success("Unfreeze successful");
    }

    private Account createAccount(Long userId, String currency) {
        Account account = new Account();
        account.setId(idGenerator.nextId());
        account.setUserId(userId);
        account.setCurrency(currency);
        account.setAvailableBalance(BigDecimal.ZERO);
        account.setFrozenBalance(BigDecimal.ZERO);
        account.setTotalBalance(BigDecimal.ZERO);
        account.setAccountType(1);

        accountMapper.insert(account);
        return account;
    }
}
