package com.hospital.gateway.enums;

import com.hospital.gateway.response.ResponseCode;
import lombok.Getter;

@Getter
public enum ResultCodeEnum implements ResponseCode {

    FAILED(1, "FAILED"),
    SUCCESS(0, "成功");

    private final int code;
    private final String msg;

    ResultCodeEnum(final int code, final String msg) {
        this.code = code;
        this.msg = msg;
    }
}
