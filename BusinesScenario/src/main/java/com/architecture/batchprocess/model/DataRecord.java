package com.architecture.batchprocess.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 数据记录实体类
 * 用于批处理的数据模型
 */
public class DataRecord {

    private Long id;
    private String businessKey;
    private BigDecimal baseValue;
    private BigDecimal result;
    private String status; // PENDING, PROCESSING, SUCCESS, FAILED
    private String errorMsg;
    private Date processedAt;
    private Date createdAt;
    private Date updatedAt;

    public DataRecord() {
    }

    public DataRecord(Long id, String businessKey, BigDecimal baseValue) {
        this.id = id;
        this.businessKey = businessKey;
        this.baseValue = baseValue;
        this.status = "PENDING";
        this.createdAt = new Date();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public BigDecimal getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(BigDecimal baseValue) {
        this.baseValue = baseValue;
    }

    public BigDecimal getResult() {
        return result;
    }

    public void setResult(BigDecimal result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Date getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Date processedAt) {
        this.processedAt = processedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "DataRecord{" +
                "id=" + id +
                ", businessKey='" + businessKey + '\'' +
                ", baseValue=" + baseValue +
                ", result=" + result +
                ", status='" + status + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", processedAt=" + processedAt +
                '}';
    }
}
