package com.concurrency.aqs.enterprise;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 腾讯 - 微信红包系统
 *
 * 业务场景：
 * 微信红包系统需要处理高并发抢红包请求，保证红包分配的原子性和公平性。
 * 春节期间峰值可达每秒数十万次抢红包请求。
 *
 * 技术方案：
 * - 使用ReentrantLock保证红包分配的原子性
 * - 公平锁模式，防止饥饿问题
 * - 二倍均值法分配红包金额
 *
 * 业务价值：
 * - 处理每秒数十万次抢红包请求
 * - 保证公平性，避免某些用户一直抢不到
 * - 保证金额分配的随机性和合理性
 */
public class WeChatRedPacket {

    private final String packetId;
    private final BigDecimal totalAmount;
    private final int totalCount;
    private final List<RedPacketRecord> records;
    private final ReentrantLock lock;

    private BigDecimal remainAmount;
    private int remainCount;
    private volatile boolean finished;

    public WeChatRedPacket(String packetId, BigDecimal totalAmount, int totalCount) {
        this.packetId = packetId;
        this.totalAmount = totalAmount;
        this.totalCount = totalCount;
        this.remainAmount = totalAmount;
        this.remainCount = totalCount;
        this.records = new ArrayList<>();
        // 使用公平锁，防止饥饿问题
        this.lock = new ReentrantLock(true);
        this.finished = false;
    }

    /**
     * 抢红包（核心方法）
     *
     * @param userId 用户ID
     * @return 抢到的金额，null表示已抢完
     */
    public BigDecimal grab(String userId) {
        // 快速检查，避免不必要的锁竞争
        if (finished) {
            return null;
        }

        lock.lock();
        try {
            // 双重检查
            if (remainCount <= 0) {
                finished = true;
                return null;
            }

            // 计算本次抢到的金额（二倍均值法）
            BigDecimal amount = calculateAmount();

            // 更新剩余金额和数量
            remainAmount = remainAmount.subtract(amount);
            remainCount--;

            // 记录抢红包记录
            RedPacketRecord record = new RedPacketRecord(
                    userId,
                    amount,
                    System.currentTimeMillis(),
                    totalCount - remainCount
            );
            records.add(record);

            System.out.printf("[红包-%s] 用户%s抢到%.2f元 (第%d个, 剩余%d个)%n",
                    packetId, userId, amount.doubleValue(), record.sequence, remainCount);

            // 最后一个红包
            if (remainCount == 0) {
                finished = true;
                System.out.printf("[红包-%s] 已抢完！%n", packetId);
            }

            return amount;

        } finally {
            lock.unlock();
        }
    }

    /**
     * 二倍均值法计算红包金额
     * 保证每个红包金额在 (0, 剩余均值的2倍) 之间
     */
    private BigDecimal calculateAmount() {
        if (remainCount == 1) {
            // 最后一个红包，返回剩余所有金额
            return remainAmount;
        }

        // 计算范围：(0, 剩余均值的2倍)
        BigDecimal avg = remainAmount.divide(
                BigDecimal.valueOf(remainCount),
                2,
                RoundingMode.HALF_UP
        );
        BigDecimal maxAmount = avg.multiply(BigDecimal.valueOf(2));

        // 随机金额，保留2位小数
        double randomAmount = Math.random() * maxAmount.doubleValue();
        BigDecimal amount = BigDecimal.valueOf(randomAmount)
                .setScale(2, RoundingMode.HALF_UP);

        // 确保至少0.01元
        if (amount.compareTo(BigDecimal.valueOf(0.01)) < 0) {
            amount = BigDecimal.valueOf(0.01);
        }

        return amount;
    }

    /**
     * 获取红包详情
     */
    public RedPacketDetail getDetail() {
        lock.lock();
        try {
            return new RedPacketDetail(
                    packetId,
                    totalAmount,
                    totalCount,
                    remainAmount,
                    remainCount,
                    finished,
                    new ArrayList<>(records)
            );
        } finally {
            lock.unlock();
        }
    }

    // 红包记录
    public static class RedPacketRecord {
        private final String userId;
        private final BigDecimal amount;
        private final long timestamp;
        private final int sequence;

        public RedPacketRecord(String userId, BigDecimal amount, long timestamp, int sequence) {
            this.userId = userId;
            this.amount = amount;
            this.timestamp = timestamp;
            this.sequence = sequence;
        }

        @Override
        public String toString() {
            return String.format("第%d个: 用户%s 抢到%.2f元", sequence, userId, amount.doubleValue());
        }
    }

    // 红包详情
    public static class RedPacketDetail {
        private final String packetId;
        private final BigDecimal totalAmount;
        private final int totalCount;
        private final BigDecimal remainAmount;
        private final int remainCount;
        private final boolean finished;
        private final List<RedPacketRecord> records;

        public RedPacketDetail(String packetId, BigDecimal totalAmount, int totalCount,
                               BigDecimal remainAmount, int remainCount, boolean finished,
                               List<RedPacketRecord> records) {
            this.packetId = packetId;
            this.totalAmount = totalAmount;
            this.totalCount = totalCount;
            this.remainAmount = remainAmount;
            this.remainCount = remainCount;
            this.finished = finished;
            this.records = records;
        }

        public void printSummary() {
            System.out.println("\n=== 红包详情 ===");
            System.out.printf("红包ID: %s%n", packetId);
            System.out.printf("总金额: %.2f元%n", totalAmount.doubleValue());
            System.out.printf("总个数: %d个%n", totalCount);
            System.out.printf("剩余金额: %.2f元%n", remainAmount.doubleValue());
            System.out.printf("剩余个数: %d个%n", remainCount);
            System.out.printf("状态: %s%n", finished ? "已抢完" : "进行中");

            if (!records.isEmpty()) {
                System.out.println("\n抢红包记录:");
                records.forEach(System.out::println);

                // 统计信息
                BigDecimal sum = records.stream()
                        .map(r -> r.amount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal max = records.stream()
                        .map(r -> r.amount)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
                BigDecimal min = records.stream()
                        .map(r -> r.amount)
                        .min(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);

                System.out.printf("\n统计: 已发%.2f元, 最大%.2f元, 最小%.2f元%n",
                        sum.doubleValue(), max.doubleValue(), min.doubleValue());
            }
        }
    }

    // 测试示例
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 腾讯微信红包系统演示 ===\n");

        // 创建一个100元10个的红包
        WeChatRedPacket redPacket = new WeChatRedPacket(
                "RP20260119001",
                BigDecimal.valueOf(100.00),
                10
        );

        // 模拟20个用户抢红包（只有10个能抢到）
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            final String userId = "User-" + (i + 1);
            Thread thread = new Thread(() -> {
                try {
                    // 随机延迟，模拟网络延迟
                    Thread.sleep((long) (Math.random() * 100));

                    BigDecimal amount = redPacket.grab(userId);
                    if (amount == null) {
                        System.out.printf("[红包-%s] 用户%s 来晚了，红包已抢完%n",
                                redPacket.packetId, userId);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, userId);
            threads.add(thread);
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 打印红包详情
        redPacket.getDetail().printSummary();

        // 验证金额
        System.out.println("\n=== 验证 ===");
        BigDecimal actualSum = redPacket.records.stream()
                .map(r -> r.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.printf("总金额: %.2f元, 实发: %.2f元, 差额: %.2f元%n",
                redPacket.totalAmount.doubleValue(),
                actualSum.doubleValue(),
                redPacket.totalAmount.subtract(actualSum).doubleValue());
    }
}
