package com.architecture.batchprocess.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * API3响应类
 */
public class ApiResponse3 {

    private BigDecimal coefficient;
    private Map<String, Object> metadata;

    public ApiResponse3() {
        this.metadata = new HashMap<>();
    }

    public ApiResponse3(BigDecimal coefficient, Map<String, Object> metadata) {
        this.coefficient = coefficient;
        this.metadata = metadata;
    }

    /**
     * 默认值（降级使用）
     */
    public static ApiResponse3 defaultValue() {
        return new ApiResponse3(BigDecimal.ONE, new HashMap<>());
    }

    public BigDecimal getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(BigDecimal coefficient) {
        this.coefficient = coefficient;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "ApiResponse3{" +
                "coefficient=" + coefficient +
                ", metadata=" + metadata +
                '}';
    }
}
