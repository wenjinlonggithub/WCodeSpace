package com.concurrency.aqs.enterprise;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 拼多多 - 拼团系统
 *
 * 业务场景：
 * 拼多多拼团系统需要精确控制拼团人数，当人数达标时触发成团逻辑。
 * 典型场景：3人团、5人团等，需要等待指定人数参团后才能成团。
 *
 * 技术方案：
 * - 使用CountDownLatch等待拼团人数达标
 * - 每个用户参团时countDown，人数满后触发成团
 * - 支持拼团超时自动取消
 *
 * 业务价值：
 * - 精确控制拼团逻辑，提升用户体验
 * - 支持高并发拼团场景
 * - 提供超时保护机制
 */
public class PinduoduoGroupBuying {

    private final String groupId;
    private final String productName;
    private final double originalPrice;
    private final double groupPrice;
    private final int requiredCount;
    private final long timeoutMs;
    private final CountDownLatch latch;
    private final List<Participant> participants;
    private volatile GroupStatus status;
    private final long createTime;

    public PinduoduoGroupBuying(String groupId, String productName, double originalPrice,
                                 double groupPrice, int requiredCount, long timeoutMs) {
        this.groupId = groupId;
        this.productName = productName;
        this.originalPrice = originalPrice;
        this.groupPrice = groupPrice;
        this.requiredCount = requiredCount;
        this.timeoutMs = timeoutMs;
        this.latch = new CountDownLatch(requiredCount);
        this.participants = new ArrayList<>();
        this.status = GroupStatus.WAITING;
        this.createTime = System.currentTimeMillis();

        // 启动超时检查线程
        startTimeoutChecker();
    }

    /**
     * 用户参团（核心方法）
     */
    public synchronized boolean join(String userId, String userName) {
        // 检查拼团状态
        if (status != GroupStatus.WAITING) {
            System.err.printf("[参团失败] 用户%s - 拼团已%s%n", userName, status.getDesc());
            return false;
        }

        // 检查是否已参团
        for (Participant p : participants) {
            if (p.userId.equals(userId)) {
                System.err.printf("[参团失败] 用户%s已参团%n", userName);
                return false;
            }
        }

        // 检查人数是否已满
        if (participants.size() >= requiredCount) {
            System.err.printf("[参团失败] 用户%s - 拼团人数已满%n", userName);
            return false;
        }

        // 参团成功
        Participant participant = new Participant(userId, userName, System.currentTimeMillis());
        participants.add(participant);
        latch.countDown();

        int currentCount = participants.size();
        System.out.printf("[参团成功] 用户%s参团 (当前%d/%d人)%n",
                userName, currentCount, requiredCount);

        // 检查是否成团
        if (currentCount == requiredCount) {
            completeGroup();
        }

        return true;
    }

    /**
     * 等待拼团完成
     */
    public boolean awaitCompletion() {
        try {
            boolean completed = latch.await(timeoutMs, TimeUnit.MILLISECONDS);
            return completed && status == GroupStatus.SUCCESS;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 拼团成功
     */
    private void completeGroup() {
        status = GroupStatus.SUCCESS;
        long duration = System.currentTimeMillis() - createTime;

        System.out.println("\n" + "=".repeat(50));
        System.out.printf("[拼团成功] 拼团%s成团！耗时%d秒%n", groupId, duration / 1000);
        System.out.printf("商品: %s%n", productName);
        System.out.printf("原价: %.2f元, 拼团价: %.2f元, 省%.2f元%n",
                originalPrice, groupPrice, originalPrice - groupPrice);
        System.out.println("参团用户:");
        for (int i = 0; i < participants.size(); i++) {
            Participant p = participants.get(i);
            System.out.printf("  %d. %s (用户ID: %s)%n", i + 1, p.userName, p.userId);
        }
        System.out.println("=".repeat(50) + "\n");
    }

    /**
     * 拼团失败（超时）
     */
    private synchronized void failGroup() {
        if (status != GroupStatus.WAITING) {
            return;
        }

        status = GroupStatus.FAILED;
        long duration = System.currentTimeMillis() - createTime;

        System.out.println("\n" + "=".repeat(50));
        System.out.printf("[拼团失败] 拼团%s超时失败 (耗时%d秒)%n", groupId, duration / 1000);
        System.out.printf("需要%d人, 实际%d人%n", requiredCount, participants.size());
        System.out.println("=".repeat(50) + "\n");

        // 释放所有等待
        while (latch.getCount() > 0) {
            latch.countDown();
        }
    }

    /**
     * 启动超时检查
     */
    private void startTimeoutChecker() {
        new Thread(() -> {
            try {
                Thread.sleep(timeoutMs);
                if (status == GroupStatus.WAITING) {
                    failGroup();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "TimeoutChecker-" + groupId).start();
    }

    /**
     * 获取拼团详情
     */
    public GroupDetail getDetail() {
        return new GroupDetail(
                groupId,
                productName,
                originalPrice,
                groupPrice,
                requiredCount,
                participants.size(),
                status,
                new ArrayList<>(participants)
        );
    }

    // 参团者
    public static class Participant {
        private final String userId;
        private final String userName;
        private final long joinTime;

        public Participant(String userId, String userName, long joinTime) {
            this.userId = userId;
            this.userName = userName;
            this.joinTime = joinTime;
        }
    }

    // 拼团状态
    public enum GroupStatus {
        WAITING("进行中"),
        SUCCESS("成功"),
        FAILED("失败");

        private final String desc;

        GroupStatus(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    // 拼团详情
    public static class GroupDetail {
        private final String groupId;
        private final String productName;
        private final double originalPrice;
        private final double groupPrice;
        private final int requiredCount;
        private final int currentCount;
        private final GroupStatus status;
        private final List<Participant> participants;

        public GroupDetail(String groupId, String productName, double originalPrice,
                           double groupPrice, int requiredCount, int currentCount,
                           GroupStatus status, List<Participant> participants) {
            this.groupId = groupId;
            this.productName = productName;
            this.originalPrice = originalPrice;
            this.groupPrice = groupPrice;
            this.requiredCount = requiredCount;
            this.currentCount = currentCount;
            this.status = status;
            this.participants = participants;
        }
    }

    // 测试示例
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 拼多多拼团系统演示 ===\n");

        // 场景1：成功拼团（3人团）
        System.out.println("【场景1：成功拼团】");
        PinduoduoGroupBuying group1 = new PinduoduoGroupBuying(
                "GROUP-001",
                "iPhone 16 Pro 256GB",
                7999.00,
                7499.00,
                3,
                30000  // 30秒超时
        );

        // 3个用户参团
        new Thread(() -> group1.join("U001", "张三")).start();
        Thread.sleep(500);
        new Thread(() -> group1.join("U002", "李四")).start();
        Thread.sleep(500);
        new Thread(() -> group1.join("U003", "王五")).start();

        // 等待拼团完成
        group1.awaitCompletion();

        Thread.sleep(2000);

        // 场景2：拼团失败（超时）
        System.out.println("\n【场景2：拼团超时失败】");
        PinduoduoGroupBuying group2 = new PinduoduoGroupBuying(
                "GROUP-002",
                "小米14 Ultra 16GB+512GB",
                6499.00,
                5999.00,
                5,
                5000  // 5秒超时
        );

        // 只有2个用户参团
        new Thread(() -> group2.join("U004", "赵六")).start();
        Thread.sleep(500);
        new Thread(() -> group2.join("U005", "孙七")).start();

        // 等待超时
        group2.awaitCompletion();

        Thread.sleep(2000);

        // 场景3：高并发拼团
        System.out.println("\n【场景3：高并发拼团】");
        PinduoduoGroupBuying group3 = new PinduoduoGroupBuying(
                "GROUP-003",
                "茅台飞天53度 500ml",
                2999.00,
                2699.00,
                10,
                30000
        );

        // 20个用户同时参团（只有前10个能成功）
        Thread[] threads = new Thread[20];
        for (int i = 0; i < 20; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                group3.join("U" + (100 + index), "用户" + (100 + index));
            });
            threads[i].start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        group3.awaitCompletion();
    }
}
