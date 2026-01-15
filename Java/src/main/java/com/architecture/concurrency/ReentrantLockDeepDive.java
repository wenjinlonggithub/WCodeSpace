package com.architecture.concurrency;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

/**
 * 可重入锁（ReentrantLock）深度解析 - 银行账户转账系统
 *
 * 核心知识点：
 * 1. 可重入性：同一线程可以多次获取同一把锁
 * 2. 锁计数机制：每次lock()增加计数，unlock()减少计数
 * 3. 公平锁 vs 非公平锁
 * 4. tryLock() 尝试获取锁
 * 5. lockInterruptibly() 可中断获取锁
 * 6. Condition 条件变量
 *
 * 业务场景：银行账户转账系统
 * - 账户余额查询
 * - 存款/取款（需要可重入性：先查余额，再操作）
 * - 转账（需要同时锁定两个账户，避免死锁）
 * - 透支保护（使用 Condition 等待余额充足）
 */
public class ReentrantLockDeepDive {

    /**
     * 银行账户类 - 演示可重入锁的核心机制
     */
    static class BankAccount {
        private String accountId;
        private double balance;
        private final ReentrantLock lock;
        private final Condition sufficientBalance; // 余额充足条件

        /**
         * @param accountId 账户ID
         * @param initialBalance 初始余额
         * @param fair 是否使用公平锁
         */
        public BankAccount(String accountId, double initialBalance, boolean fair) {
            this.accountId = accountId;
            this.balance = initialBalance;
            this.lock = new ReentrantLock(fair); // 可选择公平或非公平锁
            this.sufficientBalance = lock.newCondition();
        }

        /**
         * 查询余额 - 需要加锁保证一致性
         */
        public double getBalance() {
            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() +
                    " 查询账户 " + accountId + " 余额: " + balance);
                return balance;
            } finally {
                lock.unlock();
            }
        }

        /**
         * 【核心演示1】存款 - 展示可重入性
         * deposit() 内部调用 getBalance()，同一线程再次获取锁
         */
        public void deposit(double amount) {
            lock.lock(); // 第一次获取锁，holdCount = 1
            try {
                System.out.println("\n" + Thread.currentThread().getName() +
                    " [存款] 开始，金额: " + amount);
                System.out.println("  → 当前锁持有计数: " + lock.getHoldCount());

                // 调用 getBalance()，同一线程再次获取锁（可重入）
                double oldBalance = getBalance(); // holdCount = 2

                System.out.println("  → getBalance()后锁持有计数: " + lock.getHoldCount());

                Thread.sleep(100); // 模拟处理时间
                balance += amount;

                System.out.println("  → 存款完成: " + oldBalance + " → " + balance);

                // 通知等待余额的线程
                sufficientBalance.signalAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock(); // holdCount减1
                System.out.println("  → unlock后锁持有计数: " + lock.getHoldCount());
            }
        }

        /**
         * 【核心演示2】取款 - 展示可重入性和条件变量
         */
        public boolean withdraw(double amount) {
            lock.lock();
            try {
                System.out.println("\n" + Thread.currentThread().getName() +
                    " [取款] 开始，金额: " + amount);

                double currentBalance = getBalance(); // 可重入：再次获取锁

                if (currentBalance < amount) {
                    System.out.println("  → 余额不足! 需要: " + amount +
                        ", 当前: " + currentBalance);
                    return false;
                }

                Thread.sleep(100); // 模拟处理时间
                balance -= amount;

                System.out.println("  → 取款成功: " + (balance + amount) + " → " + balance);
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            } finally {
                lock.unlock();
            }
        }

        /**
         * 【核心演示3】等待充足余额后取款 - 使用 Condition
         */
        public boolean withdrawWithWait(double amount, long timeoutMs) throws InterruptedException {
            lock.lock();
            try {
                System.out.println("\n" + Thread.currentThread().getName() +
                    " [等待取款] 需要金额: " + amount);

                // 循环等待，直到余额充足
                while (balance < amount) {
                    System.out.println("  → 余额不足，等待存款... 当前余额: " + balance);

                    // 等待条件满足，释放锁让其他线程可以存款
                    boolean signaled = sufficientBalance.await(timeoutMs, TimeUnit.MILLISECONDS);

                    if (!signaled) {
                        System.out.println("  → 等待超时，取款失败");
                        return false;
                    }
                }

                balance -= amount;
                System.out.println("  → 取款成功: " + (balance + amount) + " → " + balance);
                return true;
            } finally {
                lock.unlock();
            }
        }

        /**
         * 【核心演示4】尝试取款 - 使用 tryLock() 避免阻塞
         */
        public boolean tryWithdraw(double amount, long timeoutMs) {
            try {
                // 尝试在指定时间内获取锁，获取不到直接返回
                if (lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS)) {
                    try {
                        System.out.println("\n" + Thread.currentThread().getName() +
                            " [尝试取款] 成功获取锁");

                        if (balance >= amount) {
                            balance -= amount;
                            System.out.println("  → 取款成功，余额: " + balance);
                            return true;
                        } else {
                            System.out.println("  → 余额不足");
                            return false;
                        }
                    } finally {
                        lock.unlock();
                    }
                } else {
                    System.out.println("\n" + Thread.currentThread().getName() +
                        " [尝试取款] 获取锁超时，放弃操作");
                    return false;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        public Lock getLock() {
            return lock;
        }

        public String getAccountId() {
            return accountId;
        }
    }

    /**
     * 【核心演示5】转账服务 - 避免死锁的关键设计
     */
    static class TransferService {

        /**
         * 安全的转账方法 - 通过账户ID排序避免死锁
         *
         * 死锁场景：
         * 线程A: 从账户1转到账户2 → lock(账户1) → lock(账户2)
         * 线程B: 从账户2转到账户1 → lock(账户2) → lock(账户1)
         *
         * 解决方案：
         * 始终按照账户ID的字典序获取锁，保证全局顺序一致
         */
        public static boolean transfer(BankAccount from, BankAccount to, double amount) {
            System.out.println("\n=== 转账开始 ===");
            System.out.println("从 " + from.getAccountId() + " 转账到 " +
                to.getAccountId() + "，金额: " + amount);

            // 按照账户ID排序，确保全局锁顺序一致
            BankAccount first, second;
            if (from.getAccountId().compareTo(to.getAccountId()) < 0) {
                first = from;
                second = to;
            } else {
                first = to;
                second = from;
            }

            // 按顺序获取锁
            first.getLock().lock();
            System.out.println("  → 获取第一个锁: " + first.getAccountId());

            try {
                second.getLock().lock();
                System.out.println("  → 获取第二个锁: " + second.getAccountId());

                try {
                    // 检查余额
                    if (from.balance < amount) {
                        System.out.println("  → 转账失败：余额不足");
                        return false;
                    }

                    // 执行转账
                    Thread.sleep(100); // 模拟处理时间
                    from.balance -= amount;
                    to.balance += amount;

                    System.out.println("  → 转账成功!");
                    System.out.println("  → " + from.getAccountId() + " 余额: " + from.balance);
                    System.out.println("  → " + to.getAccountId() + " 余额: " + to.balance);
                    return true;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                } finally {
                    second.getLock().unlock();
                    System.out.println("  → 释放第二个锁: " + second.getAccountId());
                }
            } finally {
                first.getLock().unlock();
                System.out.println("  → 释放第一个锁: " + first.getAccountId());
            }
        }

        /**
         * 尝试转账 - 使用 tryLock 避免死锁
         */
        public static boolean tryTransfer(BankAccount from, BankAccount to,
                                         double amount, long timeoutMs) {
            try {
                if (from.getLock().tryLock(timeoutMs, TimeUnit.MILLISECONDS)) {
                    try {
                        if (to.getLock().tryLock(timeoutMs, TimeUnit.MILLISECONDS)) {
                            try {
                                if (from.balance >= amount) {
                                    from.balance -= amount;
                                    to.balance += amount;
                                    System.out.println("  → 尝试转账成功");
                                    return true;
                                }
                            } finally {
                                to.getLock().unlock();
                            }
                        }
                    } finally {
                        from.getLock().unlock();
                    }
                }
                System.out.println("  → 尝试转账失败：无法获取锁");
                return false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    /**
     * 【核心演示6】公平锁 vs 非公平锁的性能对比
     */
    static class FairnessDemo {
        public static void demonstrateFairness() throws InterruptedException {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("【性能测试】公平锁 vs 非公平锁");
            System.out.println("=".repeat(60));

            // 测试非公平锁
            BankAccount unfairAccount = new BankAccount("非公平锁账户", 1000, false);
            long unfairTime = testConcurrentOperations(unfairAccount, "非公平锁");

            Thread.sleep(1000);

            // 测试公平锁
            BankAccount fairAccount = new BankAccount("公平锁账户", 1000, true);
            long fairTime = testConcurrentOperations(fairAccount, "公平锁");

            System.out.println("\n【性能对比结果】");
            System.out.println("非公平锁耗时: " + unfairTime + "ms");
            System.out.println("公平锁耗时: " + fairTime + "ms");
            System.out.println("性能差异: " + String.format("%.2f%%",
                (fairTime - unfairTime) * 100.0 / unfairTime));
        }

        private static long testConcurrentOperations(BankAccount account, String type)
                throws InterruptedException {
            System.out.println("\n--- 测试 " + type + " ---");

            int threadCount = 10;
            int operationsPerThread = 100;
            Thread[] threads = new Thread[threadCount];

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < operationsPerThread; j++) {
                        if (j % 2 == 0) {
                            account.deposit(10);
                        } else {
                            account.withdraw(10);
                        }
                    }
                }, type + "-Thread-" + threadId);
                threads[i].start();
            }

            for (Thread thread : threads) {
                thread.join();
            }

            long endTime = System.currentTimeMillis();
            System.out.println(type + " 最终余额: " + account.getBalance());

            return endTime - startTime;
        }
    }

    /**
     * 主测试方法
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=".repeat(60));
        System.out.println("可重入锁（ReentrantLock）核心原理演示");
        System.out.println("=".repeat(60));

        // ========== 测试1: 可重入性演示 ==========
        System.out.println("\n【测试1】可重入性演示");
        System.out.println("-".repeat(60));
        BankAccount account1 = new BankAccount("A001", 1000, false);

        // deposit() 方法内部调用 getBalance()，展示同一线程多次获取锁
        account1.deposit(500);

        // ========== 测试2: 多线程并发操作 ==========
        System.out.println("\n\n【测试2】多线程并发存取款");
        System.out.println("-".repeat(60));
        BankAccount account2 = new BankAccount("A002", 2000, false);

        Thread t1 = new Thread(() -> account2.deposit(300), "线程1");
        Thread t2 = new Thread(() -> account2.withdraw(500), "线程2");
        Thread t3 = new Thread(() -> account2.deposit(200), "线程3");

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println("\n最终余额: " + account2.getBalance());

        // ========== 测试3: Condition 条件变量 ==========
        System.out.println("\n\n【测试3】Condition 条件变量 - 等待余额充足");
        System.out.println("-".repeat(60));
        BankAccount account3 = new BankAccount("A003", 100, false);

        // 启动取款线程，会因余额不足而等待
        Thread withdrawThread = new Thread(() -> {
            try {
                account3.withdrawWithWait(500, 5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "取款线程");

        withdrawThread.start();
        Thread.sleep(1000); // 等待取款线程进入等待状态

        // 存款，触发条件
        Thread depositThread = new Thread(() -> {
            account3.deposit(500);
        }, "存款线程");
        depositThread.start();

        withdrawThread.join();
        depositThread.join();

        // ========== 测试4: tryLock 避免阻塞 ==========
        System.out.println("\n\n【测试4】tryLock() 避免阻塞");
        System.out.println("-".repeat(60));
        BankAccount account4 = new BankAccount("A004", 1000, false);

        // 线程1持有锁
        Thread lockHolder = new Thread(() -> {
            account4.getLock().lock();
            try {
                System.out.println("线程1持有锁，睡眠2秒...");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                account4.getLock().unlock();
            }
        }, "锁持有线程");

        lockHolder.start();
        Thread.sleep(500); // 确保线程1先获取锁

        // 线程2尝试获取锁（只等待500ms）
        Thread tryLockThread = new Thread(() -> {
            account4.tryWithdraw(100, 500);
        }, "尝试获取锁的线程");

        tryLockThread.start();
        lockHolder.join();
        tryLockThread.join();

        // ========== 测试5: 转账避免死锁 ==========
        System.out.println("\n\n【测试5】转账操作 - 避免死锁");
        System.out.println("-".repeat(60));
        BankAccount accountA = new BankAccount("A-Alice", 1000, false);
        BankAccount accountB = new BankAccount("B-Bob", 1000, false);

        // 并发转账：A→B 和 B→A 同时进行
        Thread transfer1 = new Thread(() -> {
            TransferService.transfer(accountA, accountB, 300);
        }, "转账线程1");

        Thread transfer2 = new Thread(() -> {
            TransferService.transfer(accountB, accountA, 200);
        }, "转账线程2");

        transfer1.start();
        transfer2.start();

        transfer1.join();
        transfer2.join();

        // ========== 测试6: 公平锁 vs 非公平锁 ==========
        FairnessDemo.demonstrateFairness();

        // ========== 总结 ==========
        System.out.println("\n\n" + "=".repeat(60));
        System.out.println("【核心知识点总结】");
        System.out.println("=".repeat(60));
        System.out.println("1. 可重入性：同一线程可多次获取同一锁，通过 holdCount 计数");
        System.out.println("2. 锁计数：lock()增加计数，unlock()减少计数，计数为0时释放锁");
        System.out.println("3. 公平锁：按照请求顺序获取锁，性能较低但避免饥饿");
        System.out.println("4. 非公平锁：允许插队，性能更高但可能导致某些线程长期无法获取锁");
        System.out.println("5. tryLock()：尝试获取锁，避免无限期阻塞");
        System.out.println("6. Condition：精细化的线程等待/通知机制");
        System.out.println("7. 避免死锁：通过全局锁顺序或 tryLock() 机制");
        System.out.println("=".repeat(60));
    }
}
