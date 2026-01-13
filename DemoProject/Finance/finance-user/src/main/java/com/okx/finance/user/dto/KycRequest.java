package com.okx.finance.user.dto;

import lombok.Data;

@Data
public class KycRequest {
    private String realName;
    private String idCard;
    private Integer kycLevel;
}
