package com.okx.finance.trading.dto;

import lombok.Data;

@Data
public class PlaceOrderRequest {
    private String symbol;
    private String orderType;
    private String side;
    private String price;
    private String quantity;
    private String timeInForce = "GTC";
}
