package com.architecture.batchprocess.model;

import java.math.BigDecimal;

/**
 * API2响应类
 */
public class ApiResponse2 {

    private BigDecimal adjustment;
    private String status;

    public ApiResponse2() {
    }

    public ApiResponse2(BigDecimal adjustment, String status) {
        this.adjustment = adjustment;
        this.status = status;
    }

    /**
     * 默认值（降级使用）
     */
    public static ApiResponse2 defaultValue() {
        return new ApiResponse2(BigDecimal.ZERO, "DEFAULT");
    }

    public BigDecimal getAdjustment() {
        return adjustment;
    }

    public void setAdjustment(BigDecimal adjustment) {
        this.adjustment = adjustment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ApiResponse2{" +
                "adjustment=" + adjustment +
                ", status='" + status + '\'' +
                '}';
    }
}
