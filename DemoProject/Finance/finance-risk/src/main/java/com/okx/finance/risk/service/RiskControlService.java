package com.okx.finance.risk.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class RiskControlService {

    public boolean checkTradeRisk(Long userId, String symbol, BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("1000000")) > 0) {
            return false;
        }
        return true;
    }

    public boolean checkWithdrawalRisk(Long userId, String currency, BigDecimal amount) {
        if (amount.compareTo(new BigDecimal("100000")) > 0) {
            return false;
        }
        return true;
    }

    public boolean checkAML(Long userId, String address) {
        return true;
    }
}
