package com.concurrency.aqs.enterprise;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 蚂蚁金服 - 支付系统
 *
 * 业务场景：
 * 支付宝支付系统需要保证同一账户的支付操作串行化处理，防止余额并发扣减导致的数据不一致。
 * 典型场景：用户同时发起多笔支付、转账等操作。
 *
 * 技术方案：
 * - 使用ReentrantLock保证同一账户的支付操作串行
 * - 按账户ID加锁，支持可重入
 * - 分段锁设计，不同账户的操作可以并发
 *
 * 业务价值：
 * - 防止余额并发扣减导致的数据不一致
 * - 保证支付操作的原子性
 * - 支持高并发支付场景
 */
public class AlipayPaymentSystem {

    // 账户锁池（分段锁）
    private final Map<String, ReentrantLock> accountLocks;
    // 账户余额
    private final Map<String, Account> accounts;

    public AlipayPaymentSystem() {
        this.accountLocks = new ConcurrentHashMap<>();
        this.accounts = new ConcurrentHashMap<>();
    }

    /**
     * 创建账户
     */
    public void createAccount(String accountId, BigDecimal initialBalance) {
        Account account = new Account(accountId, initialBalance);
        accounts.put(accountId, account);
        accountLocks.put(accountId, new ReentrantLock());
        System.out.printf("[账户创建] 账户%s创建成功, 初始余额: %.2f元%n",
                accountId, initialBalance.doubleValue());
    }

    /**
     * 支付（核心方法）
     *
     * @param accountId 账户ID
     * @param amount    支付金额
     * @param orderId   订单ID
     * @return 支付是否成功
     */
    public boolean pay(String accountId, BigDecimal amount, String orderId) {
        // 获取账户锁
        ReentrantLock lock = accountLocks.get(accountId);
        if (lock == null) {
            System.err.printf("[支付失败] 账户%s不存在%n", accountId);
            return false;
        }

        lock.lock();
        try {
            Account account = accounts.get(accountId);
            if (account == null) {
                System.err.printf("[支付失败] 账户%s不存在%n", accountId);
                return false;
            }

            // 检查余额
            if (account.balance.compareTo(amount) < 0) {
                System.err.printf("[支付失败] 账户%s余额不足 (余额: %.2f, 需要: %.2f)%n",
                        accountId, account.balance.doubleValue(), amount.doubleValue());
                return false;
            }

            // 扣减余额
            account.balance = account.balance.subtract(amount);
            account.transactionCount++;

            System.out.printf("[支付成功] 账户%s支付%.2f元 (订单: %s, 剩余: %.2f元)%n",
                    accountId, amount.doubleValue(), orderId, account.balance.doubleValue());

            return true;

        } finally {
            lock.unlock();
        }
    }

    /**
     * 转账（涉及两个账户，需要按顺序加锁避免死锁）
     */
    public boolean transfer(String fromAccountId, String toAccountId, BigDecimal amount, String transferId) {
        // 按账户ID排序，避免死锁
        String firstLockId = fromAccountId.compareTo(toAccountId) < 0 ? fromAccountId : toAccountId;
        String secondLockId = fromAccountId.compareTo(toAccountId) < 0 ? toAccountId : fromAccountId;

        ReentrantLock firstLock = accountLocks.get(firstLockId);
        ReentrantLock secondLock = accountLocks.get(secondLockId);

        if (firstLock == null || secondLock == null) {
            System.err.println("[转账失败] 账户不存在");
            return false;
        }

        // 按顺序加锁
        firstLock.lock();
        try {
            secondLock.lock();
            try {
                Account fromAccount = accounts.get(fromAccountId);
                Account toAccount = accounts.get(toAccountId);

                if (fromAccount == null || toAccount == null) {
                    System.err.println("[转账失败] 账户不存在");
                    return false;
                }

                // 检查余额
                if (fromAccount.balance.compareTo(amount) < 0) {
                    System.err.printf("[转账失败] 账户%s余额不足%n", fromAccountId);
                    return false;
                }

                // 转账
                fromAccount.balance = fromAccount.balance.subtract(amount);
                toAccount.balance = toAccount.balance.add(amount);
                fromAccount.transactionCount++;
                toAccount.transactionCount++;

                System.out.printf("[转账成功] %s -> %s: %.2f元 (转账ID: %s)%n",
                        fromAccountId, toAccountId, amount.doubleValue(), transferId);

                return true;

            } finally {
                secondLock.unlock();
            }
        } finally {
            firstLock.unlock();
        }
    }

    /**
     * 查询余额
     */
    public BigDecimal getBalance(String accountId) {
        ReentrantLock lock = accountLocks.get(accountId);
        if (lock == null) {
            return null;
        }

        lock.lock();
        try {
            Account account = accounts.get(accountId);
            return account != null ? account.balance : null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取账户信息
     */
    public Account getAccount(String accountId) {
        ReentrantLock lock = accountLocks.get(accountId);
        if (lock == null) {
            return null;
        }

        lock.lock();
        try {
            return accounts.get(accountId);
        } finally {
            lock.unlock();
        }
    }

    // 账户实体
    public static class Account {
        private final String accountId;
        private BigDecimal balance;
        private int transactionCount;

        public Account(String accountId, BigDecimal balance) {
            this.accountId = accountId;
            this.balance = balance;
            this.transactionCount = 0;
        }

        @Override
        public String toString() {
            return String.format("Account{id='%s', balance=%.2f, transactions=%d}",
                    accountId, balance.doubleValue(), transactionCount);
        }
    }

    // 测试示例
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 支付宝支付系统演示 ===\n");

        AlipayPaymentSystem system = new AlipayPaymentSystem();

        // 创建账户
        system.createAccount("ACC-001", BigDecimal.valueOf(10000));
        system.createAccount("ACC-002", BigDecimal.valueOf(5000));
        system.createAccount("ACC-003", BigDecimal.valueOf(8000));

        System.out.println();

        // 模拟并发支付
        Thread[] threads = new Thread[20];
        for (int i = 0; i < 20; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                String accountId = "ACC-00" + (index % 3 + 1);
                BigDecimal amount = BigDecimal.valueOf(100 + Math.random() * 200);
                String orderId = "ORDER-" + index;

                system.pay(accountId, amount, orderId);
            }, "PayThread-" + i);
            threads[i].start();
        }

        // 等待所有支付完成
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("\n=== 支付完成后账户状态 ===");
        for (int i = 1; i <= 3; i++) {
            String accountId = "ACC-00" + i;
            Account account = system.getAccount(accountId);
            if (account != null) {
                System.out.println(account);
            }
        }

        // 测试转账
        System.out.println("\n=== 测试转账 ===");
        system.transfer("ACC-001", "ACC-002", BigDecimal.valueOf(500), "TRANS-001");
        system.transfer("ACC-002", "ACC-003", BigDecimal.valueOf(300), "TRANS-002");

        System.out.println("\n=== 转账后账户状态 ===");
        for (int i = 1; i <= 3; i++) {
            String accountId = "ACC-00" + i;
            Account account = system.getAccount(accountId);
            if (account != null) {
                System.out.println(account);
            }
        }
    }
}
