package com.okx.finance.account.dto;

import lombok.Data;

@Data
public class TransferRequest {
    private String fromCurrency;
    private String toCurrency;
    private String amount;
    private String rate;
}
