package com.distributed.transaction.twopc;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 两阶段提交协调器
 * 负责协调所有参与者完成分布式事务
 */
public class TwoPhaseCommitCoordinator {

    // 事务参与者列表
    private final List<Participant> participants = new ArrayList<>();

    // 事务状态记录
    private final Map<String, TransactionStatus> transactionStatusMap = new ConcurrentHashMap<>();


    /*
     * 分布式事务协调器锁 - 核心原理解析：
     * 
     * 1. 线程安全性保障：
     *    - 在多线程环境下，确保同一时间只有一个线程能够执行事务操作
     *    - 防止多个并发事务同时修改共享资源（如participants列表、transactionStatusMap）
     * 
     * 2. 事务隔离性：
     *    - 保证事务的ACID特性中的I（Isolation）
     *    - 防止不同事务之间相互干扰，确保每个事务的状态变更按序进行
     * 
     * 3. 数据一致性：
     *    - 避免脏读、不可重复读和幻读问题
     *    - 确保事务状态map中的数据与实际执行状态保持一致
     * 
     * 4. 两阶段提交协议保障：
     *    - 在prepare阶段和commit/rollback阶段之间保持原子性
     *    - 防止在执行过程中其他线程干扰当前事务流程
     * 
     * 5. 典型应用场景：
     *    - 多个服务同时访问同一资源的分布式系统
     *    - 高并发环境下的事务管理
     *    - 需要跨数据库、跨服务的数据一致性保证
     */
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 注册参与者
     */
    public void registerParticipant(Participant participant) {
        participants.add(participant);
        System.out.println("参与者注册成功: " + participant.getName());
    }

    /**
     * 执行分布式事务
     * @param transactionId 事务ID
     * @return 事务是否成功
     */
    public boolean executeTransaction(String transactionId) {
        lock.lock();
        try {
            System.out.println("\n========== 开始执行分布式事务: " + transactionId + " ==========");

            // 记录事务开始
            transactionStatusMap.put(transactionId, TransactionStatus.STARTED);

            // 第一阶段：准备阶段
            boolean prepareResult = preparePhase(transactionId);

            if (prepareResult) {
                // 所有参与者都准备成功，执行提交阶段
                System.out.println("\n所有参与者准备成功，开始提交阶段...");
                commitPhase(transactionId);
                transactionStatusMap.put(transactionId, TransactionStatus.COMMITTED);
                System.out.println("\n========== 事务提交成功: " + transactionId + " ==========\n");
                return true;
            } else {
                // 有参与者准备失败，执行回滚阶段
                System.out.println("\n有参与者准备失败，开始回滚阶段...");
                rollbackPhase(transactionId);
                transactionStatusMap.put(transactionId, TransactionStatus.ABORTED);
                System.out.println("\n========== 事务已回滚: " + transactionId + " ==========\n");
                return false;
            }
        } catch (Exception e) {
            System.err.println("事务执行异常: " + e.getMessage());
            rollbackPhase(transactionId);
            transactionStatusMap.put(transactionId, TransactionStatus.ABORTED);
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 第一阶段：准备阶段
     * 询问所有参与者是否可以提交事务
     */
    private boolean preparePhase(String transactionId) {
        System.out.println("\n---------- 第一阶段：准备阶段 ----------");

        for (Participant participant : participants) {
            try {
                System.out.println("向参与者 " + participant.getName() + " 发送准备请求...");
                boolean canCommit = participant.prepare(transactionId);

                if (canCommit) {
                    System.out.println("参与者 " + participant.getName() + " 准备成功 (返回 Yes)");
                } else {
                    System.out.println("参与者 " + participant.getName() + " 准备失败 (返回 No)");
                    return false;
                }
            } catch (Exception e) {
                System.err.println("参与者 " + participant.getName() + " 准备异常: " + e.getMessage());
                return false;
            }
        }

        return true;
    }

    /**
     * 第二阶段：提交阶段
     * 通知所有参与者提交事务
     */
    private void commitPhase(String transactionId) {
        System.out.println("\n---------- 第二阶段：提交阶段 ----------");

        for (Participant participant : participants) {
            try {
                System.out.println("向参与者 " + participant.getName() + " 发送提交请求...");
                participant.commit(transactionId);
                System.out.println("参与者 " + participant.getName() + " 提交成功");
            } catch (Exception e) {
                System.err.println("参与者 " + participant.getName() + " 提交异常: " + e.getMessage());
                // 注意：在实际系统中，这里需要重试机制或人工介入
            }
        }
    }

    /**
     * 第二阶段：回滚阶段
     * 通知所有参与者回滚事务
     */
    private void rollbackPhase(String transactionId) {
        System.out.println("\n---------- 第二阶段：回滚阶段 ----------");

        for (Participant participant : participants) {
            try {
                System.out.println("向参与者 " + participant.getName() + " 发送回滚请求...");
                participant.rollback(transactionId);
                System.out.println("参与者 " + participant.getName() + " 回滚成功");
            } catch (Exception e) {
                System.err.println("参与者 " + participant.getName() + " 回滚异常: " + e.getMessage());
                // 注意：在实际系统中，这里需要重试机制或人工介入
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
        STARTED,    // 已开始
        COMMITTED,  // 已提交
        ABORTED     // 已中止
    }
}
