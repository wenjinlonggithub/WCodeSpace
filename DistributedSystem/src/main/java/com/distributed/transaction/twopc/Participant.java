package com.distributed.transaction.twopc;

/**
 * 事务参与者接口
 * 定义了参与者需要实现的方法
 */
public interface Participant {

    /**
     * 获取参与者名称
     */
    String getName();

    /**
     * 准备阶段
     * @param transactionId 事务ID
     * @return true表示可以提交，false表示不能提交
     */
    boolean prepare(String transactionId);

    /**
     * 提交阶段
     * @param transactionId 事务ID
     */
    void commit(String transactionId);

    /**
     * 回滚阶段
     * @param transactionId 事务ID
     */
    void rollback(String transactionId);
}
