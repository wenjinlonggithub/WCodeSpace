package com.distributed.transaction.tcc;

/**
 * TCC 事务参与者接口
 * 定义了 Try、Confirm、Cancel 三个阶段的方法
 */
public interface TCCParticipant {

    /**
     * 获取参与者名称
     */
    String getName();

    /**
     * Try 阶段
     * - 完成所有业务检查（一致性）
     * - 预留必须的业务资源（准隔离性）
     *
     * @param context 事务上下文
     * @return true 表示成功，false 表示失败
     */
    boolean tryExecute(TCCTransactionContext context);

    /**
     * Confirm 阶段
     * - 确认执行业务
     * - 不做任何业务检查
     * - 只使用 Try 阶段预留的业务资源
     * - 必须满足幂等性
     *
     * @param context 事务上下文
     * @return true 表示成功，false 表示失败
     */
    boolean confirmExecute(TCCTransactionContext context);

    /**
     * Cancel 阶段
     * - 取消执行业务
     * - 释放 Try 阶段预留的业务资源
     * - 必须满足幂等性
     *
     * @param context 事务上下文
     * @return true 表示成功，false 表示失败
     */
    boolean cancelExecute(TCCTransactionContext context);
}
