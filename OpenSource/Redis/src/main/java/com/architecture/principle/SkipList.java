package com.architecture.principle;

import java.util.Random;

/**
 * 跳表实现 - Redis ZSet底层数据结构之一
 * 跳表是一种有序数据结构，通过在每个节点中维持多个指向其他节点的指针，从而达到快速访问节点的目的
 * 时间复杂度：查找、插入、删除都是O(logN)
 */
public class SkipList<T extends Comparable<T>> {

    private static final int MAX_LEVEL = 32;
    private static final double P_FACTOR = 0.25;

    private Node<T> head;
    private int level;
    private Random random;

    static class Node<T> {
        T value;
        double score;
        Node<T>[] forward;

        @SuppressWarnings("unchecked")
        public Node(T value, double score, int level) {
            this.value = value;
            this.score = score;
            this.forward = new Node[level];
        }
    }

    public SkipList() {
        this.head = new Node<>(null, Double.MIN_VALUE, MAX_LEVEL);
        this.level = 1;
        this.random = new Random();
    }

    /**
     * 随机生成层数
     */
    private int randomLevel() {
        int level = 1;
        while (random.nextDouble() < P_FACTOR && level < MAX_LEVEL) {
            level++;
        }
        return level;
    }

    /**
     * 插入元素
     */
    public void insert(T value, double score) {
        Node<T>[] update = new Node[MAX_LEVEL];
        Node<T> current = head;

        // 从最高层开始查找插入位置
        for (int i = level - 1; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].score < score) {
                current = current.forward[i];
            }
            update[i] = current;
        }

        // 随机生成新节点的层数
        int newLevel = randomLevel();
        if (newLevel > level) {
            for (int i = level; i < newLevel; i++) {
                update[i] = head;
            }
            level = newLevel;
        }

        // 创建新节点并插入
        Node<T> newNode = new Node<>(value, score, newLevel);
        for (int i = 0; i < newLevel; i++) {
            newNode.forward[i] = update[i].forward[i];
            update[i].forward[i] = newNode;
        }
    }

    /**
     * 删除元素
     */
    public boolean delete(T value, double score) {
        Node<T>[] update = new Node[MAX_LEVEL];
        Node<T> current = head;

        for (int i = level - 1; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].score < score) {
                current = current.forward[i];
            }
            update[i] = current;
        }

        current = current.forward[0];
        if (current != null && current.score == score && current.value.equals(value)) {
            for (int i = 0; i < level; i++) {
                if (update[i].forward[i] != current) {
                    break;
                }
                update[i].forward[i] = current.forward[i];
            }

            while (level > 1 && head.forward[level - 1] == null) {
                level--;
            }
            return true;
        }
        return false;
    }

    /**
     * 查找元素
     */
    public Node<T> search(double score) {
        Node<T> current = head;
        for (int i = level - 1; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].score < score) {
                current = current.forward[i];
            }
        }
        current = current.forward[0];
        if (current != null && current.score == score) {
            return current;
        }
        return null;
    }

    /**
     * 范围查询 - 获取score在[minScore, maxScore]之间的所有元素
     * 这是跳表的核心优势之一
     */
    public java.util.List<Node<T>> rangeByScore(double minScore, double maxScore) {
        java.util.List<Node<T>> result = new java.util.ArrayList<>();
        Node<T> current = head;

        // 快速定位到第一个>=minScore的节点
        for (int i = level - 1; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].score < minScore) {
                current = current.forward[i];
            }
        }

        // 从第0层开始遍历
        current = current.forward[0];
        while (current != null && current.score <= maxScore) {
            result.add(current);
            current = current.forward[0];
        }

        return result;
    }

    /**
     * 获取排名（从0开始，0表示第一名）
     */
    public long getRank(double score) {
        Node<T> current = head;
        long rank = 0;

        for (int i = level - 1; i >= 0; i--) {
            while (current.forward[i] != null && current.forward[i].score < score) {
                // 统计跨越的节点数
                Node<T> temp = current;
                while (temp.forward[0] != current.forward[i]) {
                    rank++;
                    temp = temp.forward[0];
                }
                rank++; // current.forward[i]本身
                current = current.forward[i];
            }
        }

        return rank;
    }

    /**
     * 获取第k个元素（从0开始）
     */
    public Node<T> getByRank(long rank) {
        Node<T> current = head.forward[0];
        long count = 0;

        while (current != null) {
            if (count == rank) {
                return current;
            }
            count++;
            current = current.forward[0];
        }

        return null;
    }

    /**
     * 获取跳表中元素个数
     */
    public long size() {
        Node<T> current = head.forward[0];
        long count = 0;
        while (current != null) {
            count++;
            current = current.forward[0];
        }
        return count;
    }

    /**
     * 打印跳表结构
     */
    public void print() {
        for (int i = level - 1; i >= 0; i--) {
            Node<T> current = head.forward[i];
            System.out.print("Level " + i + ": ");
            while (current != null) {
                System.out.print(current.value + "(" + current.score + ") ");
                current = current.forward[i];
            }
            System.out.println();
        }
    }

    // ==================== 业务场景案例 ====================

    /**
     * 业务场景1：游戏排行榜系统
     *
     * 问题场景：
     * 在线游戏需要实时维护玩家积分排行榜，要求：
     * 1. 快速更新玩家分数（玩家完成任务、击败敌人等）
     * 2. 快速查询TOP N排行
     * 3. 快速查询某个玩家的排名
     * 4. 支持分数范围查询（如查询1000-2000分的所有玩家）
     *
     * 为什么用跳表？
     * - 插入/更新：O(logN)，远快于普通数组的O(N)
     * - 范围查询：O(logN + M)，M为结果数量
     * - 排名查询：O(logN)
     * - 有序性：天然支持排行
     * - 内存友好：比平衡树更省内存
     */
    static class GameLeaderboard {
        private SkipList<String> leaderboard;

        static class Player {
            String playerId;
            String playerName;
            double score;

            public Player(String playerId, String playerName, double score) {
                this.playerId = playerId;
                this.playerName = playerName;
                this.score = score;
            }
        }

        public GameLeaderboard() {
            this.leaderboard = new SkipList<>();
        }

        /**
         * 玩家完成任务，增加分数
         */
        public void addScore(String playerId, double score) {
            // 实际场景中需要先查询旧分数并删除，这里简化处理
            leaderboard.insert(playerId, score);
            System.out.println("玩家 " + playerId + " 获得 " + score + " 分");
        }

        /**
         * 获取TOP N排行榜
         */
        public void showTopN(int n) {
            System.out.println("\n===== TOP " + n + " 排行榜 =====");
            Node<String> current = leaderboard.head.forward[0];
            int rank = 1;
            int count = 0;

            // 跳表是升序的，需要找到最高分开始
            java.util.List<Node<String>> all = new java.util.ArrayList<>();
            while (current != null) {
                all.add(current);
                current = current.forward[0];
            }

            // 从高到低输出
            for (int i = all.size() - 1; i >= 0 && count < n; i--) {
                Node<String> node = all.get(i);
                System.out.printf("第%d名: 玩家%s - %.0f分\n", rank++, node.value, node.score);
                count++;
            }
        }

        /**
         * 查询某个玩家的排名
         */
        public void showPlayerRank(String playerId, double score) {
            long totalPlayers = leaderboard.size();
            long rank = leaderboard.getRank(score);
            // 因为跳表是升序，排名需要反转
            long actualRank = totalPlayers - rank;
            System.out.println("玩家 " + playerId + " 当前排名: 第" + actualRank + "名");
        }

        /**
         * 查询分数段的玩家
         */
        public void showPlayersByScoreRange(double minScore, double maxScore) {
            System.out.println("\n分数在 " + minScore + "-" + maxScore + " 之间的玩家：");
            java.util.List<Node<String>> players = leaderboard.rangeByScore(minScore, maxScore);
            for (Node<String> player : players) {
                System.out.printf("玩家%s: %.0f分\n", player.value, player.score);
            }
        }
    }

    /**
     * 业务场景2：延迟任务队列
     *
     * 问题场景：
     * 需要实现一个延迟任务调度系统：
     * 1. 任务可以指定在未来某个时间执行
     * 2. 需要快速找到最早要执行的任务
     * 3. 支持动态添加新任务
     * 4. 支持取消未执行的任务
     *
     * 为什么用跳表？
     * - 按时间戳排序，天然有序
     * - 查找最早任务：O(1)，直接取第一个
     * - 插入新任务：O(logN)
     * - 删除任务：O(logN)
     * - 范围查询：获取当前时间前的所有任务 O(logN + M)
     */
    static class DelayedTaskQueue {
        private SkipList<String> taskQueue;

        static class Task {
            String taskId;
            String taskName;
            long executeTime; // 执行时间戳

            public Task(String taskId, String taskName, long executeTime) {
                this.taskId = taskId;
                this.taskName = taskName;
                this.executeTime = executeTime;
            }
        }

        public DelayedTaskQueue() {
            this.taskQueue = new SkipList<>();
        }

        /**
         * 添加延迟任务
         */
        public void addTask(String taskId, String taskName, long delayMillis) {
            long executeTime = System.currentTimeMillis() + delayMillis;
            taskQueue.insert(taskId, executeTime);
            System.out.println("任务 [" + taskName + "] 已添加，将在 " + delayMillis + "ms 后执行");
        }

        /**
         * 获取应该执行的任务
         */
        public java.util.List<Node<String>> getReadyTasks() {
            long now = System.currentTimeMillis();
            // 获取所有executeTime <= now的任务
            return taskQueue.rangeByScore(0, now);
        }

        /**
         * 执行到期的任务
         */
        public void processTasks() {
            java.util.List<Node<String>> readyTasks = getReadyTasks();
            if (readyTasks.isEmpty()) {
                System.out.println("暂无到期任务");
                return;
            }

            System.out.println("\n执行到期任务：");
            for (Node<String> taskNode : readyTasks) {
                System.out.println("执行任务: " + taskNode.value);
                // 执行完成后删除
                taskQueue.delete(taskNode.value, taskNode.score);
            }
        }

        /**
         * 取消任务
         */
        public void cancelTask(String taskId, long executeTime) {
            boolean removed = taskQueue.delete(taskId, executeTime);
            if (removed) {
                System.out.println("任务 " + taskId + " 已取消");
            } else {
                System.out.println("任务 " + taskId + " 不存在或已执行");
            }
        }

        /**
         * 查看待执行任务
         */
        public void showPendingTasks() {
            System.out.println("\n待执行任务列表：");
            Node<String> current = taskQueue.head.forward[0];
            long now = System.currentTimeMillis();
            int count = 0;

            while (current != null) {
                long remainingTime = (long)current.score - now;
                System.out.printf("任务%s: 剩余%dms\n", current.value, remainingTime);
                current = current.forward[0];
                count++;
            }

            if (count == 0) {
                System.out.println("无待执行任务");
            }
        }
    }

    /**
     * 业务场景3：实时竞价系统
     *
     * 问题场景：
     * 在线广告竞价系统，需要：
     * 1. 实时接收广告主的出价
     * 2. 快速找到最高出价
     * 3. 支持动态更新出价
     * 4. 查询价格区间的所有竞价
     *
     * 为什么用跳表？
     * - 实时插入：O(logN)
     * - 查找最高价：O(1)
     * - 范围查询：O(logN + M)
     * - 内存效率高
     */
    static class BiddingSystem {
        private SkipList<String> bids;

        static class Bid {
            String advertiserId;
            double bidPrice;
            long timestamp;

            public Bid(String advertiserId, double bidPrice) {
                this.advertiserId = advertiserId;
                this.bidPrice = bidPrice;
                this.timestamp = System.currentTimeMillis();
            }
        }

        public BiddingSystem() {
            this.bids = new SkipList<>();
        }

        /**
         * 提交竞价
         */
        public void submitBid(String advertiserId, double bidPrice) {
            bids.insert(advertiserId, bidPrice);
            System.out.println("广告主 " + advertiserId + " 出价: ￥" + bidPrice);
        }

        /**
         * 获取最高出价
         */
        public void showHighestBid() {
            Node<String> current = bids.head.forward[0];
            if (current == null) {
                System.out.println("暂无竞价");
                return;
            }

            // 找到最高价
            Node<String> highest = current;
            while (current != null) {
                if (current.score > highest.score) {
                    highest = current;
                }
                current = current.forward[0];
            }

            System.out.println("\n当前最高出价: ￥" + highest.score + " (广告主: " + highest.value + ")");
        }

        /**
         * 显示价格区间的竞价
         */
        public void showBidsByRange(double minPrice, double maxPrice) {
            System.out.println("\n价格区间 ￥" + minPrice + "-￥" + maxPrice + " 的竞价：");
            java.util.List<Node<String>> rangesBids = bids.rangeByScore(minPrice, maxPrice);
            for (Node<String> bid : rangesBids) {
                System.out.printf("广告主%s: ￥%.2f\n", bid.value, bid.score);
            }
        }

        /**
         * 显示TOP N竞价
         */
        public void showTopBids(int n) {
            System.out.println("\n===== TOP " + n + " 竞价 =====");
            java.util.List<Node<String>> all = new java.util.ArrayList<>();
            Node<String> current = bids.head.forward[0];

            while (current != null) {
                all.add(current);
                current = current.forward[0];
            }

            // 从高到低排序
            all.sort((a, b) -> Double.compare(b.score, a.score));

            for (int i = 0; i < Math.min(n, all.size()); i++) {
                Node<String> bid = all.get(i);
                System.out.printf("第%d名: 广告主%s - ￥%.2f\n", i + 1, bid.value, bid.score);
            }
        }
    }

    // ==================== 主函数：运行所有场景示例 ====================

    public static void main(String[] args) throws InterruptedException {
        System.out.println("============================================");
        System.out.println("       跳表数据结构 - 业务场景演示");
        System.out.println("============================================\n");

        // 场景1：游戏排行榜
        System.out.println("\n【场景1：游戏排行榜系统】");
        System.out.println("问题：需要实时维护百万级玩家的积分排行榜");
        System.out.println("解决方案：使用跳表，O(logN)插入，O(logN)查询排名\n");

        GameLeaderboard leaderboard = new GameLeaderboard();
        leaderboard.addScore("P001", 1500);
        leaderboard.addScore("P002", 2300);
        leaderboard.addScore("P003", 1800);
        leaderboard.addScore("P004", 2100);
        leaderboard.addScore("P005", 1900);
        leaderboard.addScore("P006", 2500);
        leaderboard.addScore("P007", 1700);
        leaderboard.addScore("P008", 2200);

        leaderboard.showTopN(5);
        leaderboard.showPlayerRank("P003", 1800);
        leaderboard.showPlayersByScoreRange(1800, 2200);

        // 场景2：延迟任务队列
        System.out.println("\n\n【场景2：延迟任务调度系统】");
        System.out.println("问题：需要调度大量延迟任务，按时间顺序执行");
        System.out.println("解决方案：使用跳表按时间戳排序，快速获取到期任务\n");

        DelayedTaskQueue taskQueue = new DelayedTaskQueue();
        taskQueue.addTask("T001", "发送邮件", 1000);
        taskQueue.addTask("T002", "清理缓存", 2000);
        taskQueue.addTask("T003", "备份数据", 3000);
        taskQueue.addTask("T004", "生成报表", 1500);

        taskQueue.showPendingTasks();

        System.out.println("\n等待1.5秒...");
        Thread.sleep(1500);

        taskQueue.processTasks();
        taskQueue.showPendingTasks();

        System.out.println("\n再等待1秒...");
        Thread.sleep(1000);

        taskQueue.processTasks();
        taskQueue.showPendingTasks();

        // 场景3：实时竞价系统
        System.out.println("\n\n【场景3：在线广告竞价系统】");
        System.out.println("问题：需要实时处理广告竞价，快速找到最高出价");
        System.out.println("解决方案：使用跳表维护有序竞价，快速范围查询\n");

        BiddingSystem biddingSystem = new BiddingSystem();
        biddingSystem.submitBid("ADV001", 5.50);
        biddingSystem.submitBid("ADV002", 7.20);
        biddingSystem.submitBid("ADV003", 6.80);
        biddingSystem.submitBid("ADV004", 8.50);
        biddingSystem.submitBid("ADV005", 7.00);
        biddingSystem.submitBid("ADV006", 9.20);

        biddingSystem.showHighestBid();
        biddingSystem.showTopBids(3);
        biddingSystem.showBidsByRange(7.0, 8.5);

        // 总结
        System.out.println("\n\n============================================");
        System.out.println("                   总结");
        System.out.println("============================================");
        System.out.println("跳表适用场景：");
        System.out.println("1. 需要有序存储的场景");
        System.out.println("2. 频繁插入/删除操作");
        System.out.println("3. 需要范围查询");
        System.out.println("4. 需要快速查找排名");
        System.out.println("\n时间复杂度：");
        System.out.println("- 插入：O(logN)");
        System.out.println("- 删除：O(logN)");
        System.out.println("- 查找：O(logN)");
        System.out.println("- 范围查询：O(logN + M)，M为结果数");
        System.out.println("\n相比其他数据结构的优势：");
        System.out.println("- 比平衡树实现简单");
        System.out.println("- 比普通链表查询快");
        System.out.println("- 比数组插入/删除快");
        System.out.println("- 内存占用相对较小");
        System.out.println("============================================");
    }
}
