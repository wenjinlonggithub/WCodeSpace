package com.distributed.transaction.tcc;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**

 * TCC (Try-Confirm-Cancel) 事务协调器
 * 
 * TCC是一种分布式事务解决方案，它通过三个阶段来确保分布式环境下的数据一致性：
 * 
 * 1. Try阶段：
 *    - 完成所有业务检查（如库存、资金等）
 *    - 预留必要的业务资源
 *    - 不进行实际的业务操作
 * 
 * 2. Confirm阶段：
 *    - 真正执行业务操作
 *    - 使用Try阶段预留的业务资源
 *    - 操作具有幂等性，允许失败后重试
 * 
 * 3. Cancel阶段：
 *    - 释放Try阶段预留的业务资源
 *    - 回滚Try阶段所做的准备操作
 *    - 操作具有幂等性，允许失败后重试
 * 
 * 本协调器负责管理整个TCC事务流程，包括参与者注册、事务执行控制以及状态管理。
 * 当Try阶段全部成功时，进入Confirm阶段；当任一环节失败时，进入Cancel阶段来回滚。
 * 
 * 负责协调所有参与者完成 TCC 分布式事务
 */
public class TCCTransactionCoordinator {

    // 事务参与者列表
    private final List<TCCParticipant> participants = new ArrayList<>();

    // 事务状态记录
    private final Map<String, TransactionStatus> transactionStatusMap = new ConcurrentHashMap<>();

    /**
     * 注册参与者
     */
    public void registerParticipant(TCCParticipant participant) {
        participants.add(participant);
        System.out.println("参与者注册成功: " + participant.getName());
    }

    /**
     * 执行 TCC 分布式事务
     * @param transactionId 事务ID
     * @return 事务是否成功
     */
    public boolean executeTransaction(String transactionId) {
        System.out.println("\n========== 开始执行 TCC 事务: " + transactionId + " ==========");

        // 记录事务开始
        transactionStatusMap.put(transactionId, TransactionStatus.STARTED);

        // Try 阶段
        boolean tryResult = tryPhase(transactionId);

        if (tryResult) {
            // Try 阶段成功，执行 Confirm 阶段
            System.out.println("\nTry 阶段全部成功，开始 Confirm 阶段...");
            boolean confirmResult = confirmPhase(transactionId);

            if (confirmResult) {
                transactionStatusMap.put(transactionId, TransactionStatus.CONFIRMED);
                System.out.println("\n========== TCC 事务提交成功: " + transactionId + " ==========\n");
                return true;
            } else {
                // Confirm 失败，需要重试或人工介入
                System.err.println("\nConfirm 阶段失败，需要重试或人工介入");
                transactionStatusMap.put(transactionId, TransactionStatus.CONFIRM_FAILED);
                return false;
            }
        } else {
            // Try 阶段失败，执行 Cancel 阶段
            System.out.println("\nTry 阶段失败，开始 Cancel 阶段...");
            cancelPhase(transactionId);
            transactionStatusMap.put(transactionId, TransactionStatus.CANCELLED);
            System.out.println("\n========== TCC 事务已取消: " + transactionId + " ==========\n");
            return false;
        }
    }

    /**
     * Try 阶段
     * 尝试执行业务，完成所有业务检查，预留必须的业务资源
     */
    private boolean tryPhase(String transactionId) {
        System.out.println("\n---------- Try 阶段 ----------");

        TCCTransactionContext context = new TCCTransactionContext(transactionId, "TRY");

        for (TCCParticipant participant : participants) {
            try {
                System.out.println("调用参与者 " + participant.getName() + " 的 Try 方法...");
                boolean success = participant.tryExecute(context);

                if (success) {
                    System.out.println("参与者 " + participant.getName() + " Try 成功");
                } else {
                    System.out.println("参与者 " + participant.getName() + " Try 失败");
                    return false;
                }
            } catch (Exception e) {
                System.err.println("参与者 " + participant.getName() + " Try 异常: " + e.getMessage());
                return false;
            }
        }

        return true;
    }

    /**
     * Confirm 阶段
     * 确认执行业务，只使用 Try 阶段预留的业务资源
     */
    private boolean confirmPhase(String transactionId) {
        System.out.println("\n---------- Confirm 阶段 ----------");

        TCCTransactionContext context = new TCCTransactionContext(transactionId, "CONFIRM");

        boolean allSuccess = true;

        for (TCCParticipant participant : participants) {
            try {
                System.out.println("调用参与者 " + participant.getName() + " 的 Confirm 方法...");
                boolean success = participant.confirmExecute(context);

                if (success) {
                    System.out.println("参与者 " + participant.getName() + " Confirm 成功");
                } else {
                    System.err.println("参与者 " + participant.getName() + " Confirm 失败");
                    allSuccess = false;
                    // 注意：Confirm 失败需要重试，不能执行 Cancel
                }
            } catch (Exception e) {
                System.err.println("参与者 " + participant.getName() + " Confirm 异常: " + e.getMessage());
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    /**
     * Cancel 阶段
     * 取消执行业务，释放 Try 阶段预留的业务资源
     */
    private void cancelPhase(String transactionId) {
        System.out.println("\n---------- Cancel 阶段 ----------");

        TCCTransactionContext context = new TCCTransactionContext(transactionId, "CANCEL");

        // 反向执行 Cancel（与 Try 的顺序相反）
        for (int i = participants.size() - 1; i >= 0; i--) {
            TCCParticipant participant = participants.get(i);
            try {
                System.out.println("调用参与者 " + participant.getName() + " 的 Cancel 方法...");
                boolean success = participant.cancelExecute(context);

                if (success) {
                    System.out.println("参与者 " + participant.getName() + " Cancel 成功");
                } else {
                    System.err.println("参与者 " + participant.getName() + " Cancel 失败");
                    // 注意：Cancel 失败需要重试或人工介入
                }
            } catch (Exception e) {
                System.err.println("参与者 " + participant.getName() + " Cancel 异常: " + e.getMessage());
            }
        }
    }

    /**
     * 获取事务状态
     */
    public TransactionStatus getTransactionStatus(String transactionId) {
        return transactionStatusMap.get(transactionId);
    }

    /**
     * 事务状态枚举
     */
    public enum TransactionStatus {
        STARTED,        // 已开始
        CONFIRMED,      // 已确认
        CANCELLED,      // 已取消
        CONFIRM_FAILED  // 确认失败
    }
}
