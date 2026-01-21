package com.hospital.gateway.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lvzeqiang
 * @date 2022/7/14 10:03
 * @description
 **/
@Getter
@Setter
public class CheckTokenResp {

    private int code;
    private String msg;
    private TokenResp data;

    public Long getAppId() {
        return data.getClientSecret().getAppId();
    }

    @Getter
    @Setter
    private static class TokenResp {
        private String clientId;
        private String username;
        @JSONField(name = "clientSecurect")
        private ClientSecret clientSecret;
    }

    @Getter
    @Setter
    private static class ClientSecret {
        private Long appId;
    }
}
