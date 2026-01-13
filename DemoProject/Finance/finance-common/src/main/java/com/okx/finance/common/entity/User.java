package com.okx.finance.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String realName;
    private String idCard;
    private Integer kycLevel;
    private Integer status;
    private String apiKey;
    private String apiSecret;
}
