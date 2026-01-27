package com.distributed.transaction.tcc;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 账户服务 TCC 参与者
 * 实现账户扣款的 TCC 模式
 */
public class AccountServiceTCCParticipant implements TCCParticipant {

    private final String name = "AccountService";

    // 模拟账户余额数据库
    private final Map<Long, BigDecimal> accountBalances = new ConcurrentHashMap<>();

    // 模拟冻结金额记录
    private final Map<String, FrozenRecord> frozenRecords = new ConcurrentHashMap<>();

    // 记录已处理的事务（用于幂等性）
    private final Set<String> processedTransactions = ConcurrentHashMap.newKeySet();

    public AccountServiceTCCParticipant() {
        // 初始化测试数据
        accountBalances.put(1L, new BigDecimal("1000.00"));
        accountBalances.put(2L, new BigDecimal("500.00"));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean tryExecute(TCCTransactionContext context) {
        try {
            String transactionId = context.getTransactionId();

            // 幂等性检查
            if (frozenRecords.containsKey(transactionId)) {
                System.out.println("  [" + name + "] Try 阶段已执行，跳过（幂等性）");
                return true;
            }

            // 模拟业务参数
            Long userId = 1L;
            BigDecimal amount = new BigDecimal("100.00");

            System.out.println("  [" + name + "] Try 阶段：检查余额并冻结金额");

            // 1. 检查账户是否存在
            BigDecimal balance = accountBalances.get(userId);
            if (balance == null) {
                System.out.println("  [" + name + "] 账户不存在");
                return false;
            }

            // 2. 检查余额是否充足
            if (balance.compareTo(amount) < 0) {
                System.out.println("  [" + name + "] 余额不足: 当前余额=" + balance + ", 需要扣减=" + amount);
                return false;
            }

            // 3. 冻结金额（不实际扣减）
            FrozenRecord record = new FrozenRecord(userId, amount);
            frozenRecords.put(transactionId, record);

            System.out.println("  [" + name + "] 冻结金额成功: userId=" + userId + ", amount=" + amount +
                    ", 当前余额=" + balance);

            // 模拟网络延迟
            Thread.sleep(50);

            return true;
        } catch (Exception e) {
            System.err.println("  [" + name + "] Try 阶段异常: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean confirmExecute(TCCTransactionContext context) {
        try {
            String transactionId = context.getTransactionId();

            // 幂等性检查
            if (processedTransactions.contains(transactionId + ":CONFIRM")) {
                System.out.println("  [" + name + "] Confirm 阶段已执行，跳过（幂等性）");
                return true;
            }

            System.out.println("  [" + name + "] Confirm 阶段：扣减冻结的金额");

            // 获取冻结记录
            FrozenRecord record = frozenRecords.get(transactionId);
            if (record == null) {
                System.err.println("  [" + name + "] 未找到冻结记录");
                return false;
            }

            // 扣减账户余额
            BigDecimal balance = accountBalances.get(record.userId);
            BigDecimal newBalance = balance.subtract(record.amount);
            accountBalances.put(record.userId, newBalance);

            System.out.println("  [" + name + "] 扣减余额成功: userId=" + record.userId +
                    ", 扣减金额=" + record.amount + ", 新余额=" + newBalance);

            // 清除冻结记录
            frozenRecords.remove(transactionId);

            // 记录已处理（幂等性）
            processedTransactions.add(transactionId + ":CONFIRM");

            // 模拟网络延迟
            Thread.sleep(50);

            return true;
        } catch (Exception e) {
            System.err.println("  [" + name + "] Confirm 阶段异常: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean cancelExecute(TCCTransactionContext context) {
        try {
            String transactionId = context.getTransactionId();

            // 幂等性检查
            if (processedTransactions.contains(transactionId + ":CANCEL")) {
                System.out.println("  [" + name + "] Cancel 阶段已执行，跳过（幂等性）");
                return true;
            }

            System.out.println("  [" + name + "] Cancel 阶段：释放冻结的金额");

            // 获取冻结记录
            FrozenRecord record = frozenRecords.get(transactionId);
            if (record == null) {
                System.out.println("  [" + name + "] 未找到冻结记录，无需取消");
                return true;
            }

            // 释放冻结金额
            frozenRecords.remove(transactionId);

            System.out.println("  [" + name + "] 释放冻结金额成功: userId=" + record.userId + ", amount=" + record.amount);

            // 记录已处理（幂等性）
            processedTransactions.add(transactionId + ":CANCEL");

            // 模拟网络延迟
            Thread.sleep(50);

            return true;
        } catch (Exception e) {
            System.err.println("  [" + name + "] Cancel 阶段异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取账户余额（用于测试）
     */
    public BigDecimal getBalance(Long userId) {
        return accountBalances.get(userId);
    }

    /**
     * 冻结记录
     */
    private static class FrozenRecord {
        Long userId;
        BigDecimal amount;

        FrozenRecord(Long userId, BigDecimal amount) {
            this.userId = userId;
            this.amount = amount;
        }
    }
}
