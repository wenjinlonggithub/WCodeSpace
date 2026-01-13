package com.okx.finance.wallet.dto;

import lombok.Data;

@Data
public class WithdrawRequest {
    private String currency;
    private String amount;
    private String address;
    private String tag;
}
