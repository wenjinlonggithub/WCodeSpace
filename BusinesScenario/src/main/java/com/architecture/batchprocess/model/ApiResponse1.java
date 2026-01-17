package com.architecture.batchprocess.model;

import java.math.BigDecimal;

/**
 * API1响应类
 */
public class ApiResponse1 {

    private BigDecimal factor;
    private String code;
    private String message;

    public ApiResponse1() {
    }

    public ApiResponse1(BigDecimal factor, String code, String message) {
        this.factor = factor;
        this.code = code;
        this.message = message;
    }

    /**
     * 默认值（降级使用）
     */
    public static ApiResponse1 defaultValue() {
        return new ApiResponse1(BigDecimal.ONE, "DEFAULT", "使用默认值");
    }

    public BigDecimal getFactor() {
        return factor;
    }

    public void setFactor(BigDecimal factor) {
        this.factor = factor;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ApiResponse1{" +
                "factor=" + factor +
                ", code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
