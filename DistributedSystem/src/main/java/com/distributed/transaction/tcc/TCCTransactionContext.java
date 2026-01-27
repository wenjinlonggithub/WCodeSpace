package com.distributed.transaction.tcc;

/**
 * TCC 事务上下文
 * 用于在 Try、Confirm、Cancel 阶段传递事务信息
 */
public class TCCTransactionContext {

    private String transactionId;
    private String action; // TRY, CONFIRM, CANCEL
    private Object businessData;

    public TCCTransactionContext(String transactionId, String action) {
        this.transactionId = transactionId;
        this.action = action;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Object getBusinessData() {
        return businessData;
    }

    public void setBusinessData(Object businessData) {
        this.businessData = businessData;
    }

    @Override
    public String toString() {
        return "TCCTransactionContext{" +
                "transactionId='" + transactionId + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}
