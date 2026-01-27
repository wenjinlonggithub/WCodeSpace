package com.distributed.transaction.twopc;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 账户服务参与者
 * 模拟账户扣款操作
 */
public class AccountServiceParticipant implements Participant {

    private final String name = "AccountService";

    // 模拟账户余额数据库
    private final Map<Long, BigDecimal> accountBalances = new ConcurrentHashMap<>();

    // 模拟冻结金额（用于准备阶段）
    private final Map<String, FrozenAmount> frozenAmounts = new ConcurrentHashMap<>();

    public AccountServiceParticipant() {
        // 初始化一些测试数据
        accountBalances.put(1L, new BigDecimal("1000.00"));
        accountBalances.put(2L, new BigDecimal("500.00"));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean prepare(String transactionId) {
        try {
            // 模拟业务逻辑：检查账户余额并冻结金额
            Long userId = 1L;
            BigDecimal amount = new BigDecimal("100.00");

            System.out.println("  [" + name + "] 检查账户余额...");
            BigDecimal balance = accountBalances.get(userId);

            if (balance == null) {
                System.out.println("  [" + name + "] 账户不存在");
                return false;
            }

            if (balance.compareTo(amount) < 0) {
                System.out.println("  [" + name + "] 余额不足: 当前余额=" + balance + ", 需要扣减=" + amount);
                return false;
            }

            // 冻结金额
            frozenAmounts.put(transactionId, new FrozenAmount(userId, amount));
            System.out.println("  [" + name + "] 冻结金额成功: userId=" + userId + ", amount=" + amount);

            // 模拟网络延迟
            Thread.sleep(100);

            return true;
        } catch (Exception e) {
            System.err.println("  [" + name + "] 准备阶段异常: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void commit(String transactionId) {
        try {
            FrozenAmount frozen = frozenAmounts.get(transactionId);
            if (frozen == null) {
                System.err.println("  [" + name + "] 未找到冻结金额记录");
                return;
            }

            // 扣减账户余额
            BigDecimal balance = accountBalances.get(frozen.userId);
            BigDecimal newBalance = balance.subtract(frozen.amount);
            accountBalances.put(frozen.userId, newBalance);

            System.out.println("  [" + name + "] 扣减余额成功: userId=" + frozen.userId +
                    ", 扣减金额=" + frozen.amount + ", 新余额=" + newBalance);

            // 清除冻结记录
            frozenAmounts.remove(transactionId);

            // 模拟网络延迟
            Thread.sleep(100);
        } catch (Exception e) {
            System.err.println("  [" + name + "] 提交阶段异常: " + e.getMessage());
        }
    }

    @Override
    public void rollback(String transactionId) {
        try {
            FrozenAmount frozen = frozenAmounts.get(transactionId);
            if (frozen == null) {
                System.out.println("  [" + name + "] 未找到冻结金额记录，无需回滚");
                return;
            }

            // 解冻金额
            frozenAmounts.remove(transactionId);
            System.out.println("  [" + name + "] 解冻金额成功: userId=" + frozen.userId + ", amount=" + frozen.amount);

            // 模拟网络延迟
            Thread.sleep(100);
        } catch (Exception e) {
            System.err.println("  [" + name + "] 回滚阶段异常: " + e.getMessage());
        }
    }

    /**
     * 获取账户余额（用于测试）
     */
    public BigDecimal getBalance(Long userId) {
        return accountBalances.get(userId);
    }

    /**
     * 冻结金额记录
     */
    private static class FrozenAmount {
        Long userId;
        BigDecimal amount;

        FrozenAmount(Long userId, BigDecimal amount) {
            this.userId = userId;
            this.amount = amount;
        }
    }
}
