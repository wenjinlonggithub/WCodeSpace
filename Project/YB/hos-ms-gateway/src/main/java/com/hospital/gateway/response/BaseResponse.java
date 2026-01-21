package com.hospital.gateway.response;


import com.hospital.gateway.enums.ResultCodeEnum;
import org.slf4j.MDC;

public class BaseResponse {

    private int code;
    private String msg;

    public static boolean isOk(BaseResponse response) {
        return response != null && response.getCode() == ResultCodeEnum.SUCCESS.getCode();
    }

    public String getTraceId() {
        return MDC.get("traceId");
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setCode(final int code) {
        this.code = code;
    }

    public void setMsg(final String msg) {
        this.msg = msg;
    }

}
