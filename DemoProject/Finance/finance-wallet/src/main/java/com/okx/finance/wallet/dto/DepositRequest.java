package com.okx.finance.wallet.dto;

import lombok.Data;

@Data
public class DepositRequest {
    private String currency;
    private String amount;
    private String txHash;
}
