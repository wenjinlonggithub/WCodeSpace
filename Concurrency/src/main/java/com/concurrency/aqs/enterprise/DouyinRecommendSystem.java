package com.concurrency.aqs.enterprise;

import java.util.*;
import java.util.concurrent.*;

/**
 * 字节跳动 - 抖音推荐系统多路召回
 *
 * 业务场景：
 * 抖音推荐系统需要并行调用多个推荐算法（协同过滤、内容召回、热门召回等），
 * 等待所有召回服务返回后进行结果聚合，为用户生成推荐列表。
 *
 * 技术方案：
 * - 使用CountDownLatch等待多个召回服务返回
 * - 并行调用多个推荐算法，设置超时时间
 * - 结果聚合和排序
 *
 * 业务价值：
 * - 降低推荐延迟，提升用户体验
 * - 支持多路召回策略
 * - 提供超时保护机制
 */
public class DouyinRecommendSystem {

    private final ExecutorService executorService;
    private final int recallTimeout;

    public DouyinRecommendSystem(int threadPoolSize, int recallTimeout) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.recallTimeout = recallTimeout;
    }

    /**
     * 为用户生成推荐列表（核心方法）
     *
     * @param userId 用户ID
     * @param count  推荐数量
     * @return 推荐视频列表
     */
    public List<Video> recommend(String userId, int count) {
        long startTime = System.currentTimeMillis();

        // 定义多路召回策略
        List<RecallStrategy> strategies = Arrays.asList(
                new CollaborativeFilteringRecall(),
                new ContentBasedRecall(),
                new HotRecall(),
                new FollowingRecall()
        );

        // 使用CountDownLatch等待所有召回完成
        CountDownLatch latch = new CountDownLatch(strategies.size());
        List<Future<List<Video>>> futures = new ArrayList<>();

        System.out.printf("[推荐系统] 用户%s开始多路召回 (策略数: %d)%n", userId, strategies.size());

        // 并行执行所有召回策略
        for (RecallStrategy strategy : strategies) {
            Future<List<Video>> future = executorService.submit(() -> {
                try {
                    long recallStart = System.currentTimeMillis();
                    List<Video> videos = strategy.recall(userId, count);
                    long recallTime = System.currentTimeMillis() - recallStart;

                    System.out.printf("[召回完成] %s - 召回%d个视频, 耗时%dms%n",
                            strategy.getName(), videos.size(), recallTime);

                    return videos;
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        // 等待所有召回完成（带超时）
        try {
            boolean completed = latch.await(recallTimeout, TimeUnit.MILLISECONDS);
            if (!completed) {
                System.err.printf("[推荐系统] 召回超时 (超过%dms)%n", recallTimeout);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[推荐系统] 召回被中断");
        }

        // 收集所有召回结果
        Map<String, Video> videoMap = new ConcurrentHashMap<>();
        for (Future<List<Video>> future : futures) {
            try {
                List<Video> videos = future.get(100, TimeUnit.MILLISECONDS);
                for (Video video : videos) {
                    videoMap.putIfAbsent(video.videoId, video);
                }
            } catch (TimeoutException e) {
                System.err.println("[推荐系统] 获取召回结果超时");
            } catch (Exception e) {
                System.err.println("[推荐系统] 获取召回结果失败: " + e.getMessage());
            }
        }

        // 结果排序和截断
        List<Video> result = new ArrayList<>(videoMap.values());
        result.sort((v1, v2) -> Double.compare(v2.score, v1.score));
        if (result.size() > count) {
            result = result.subList(0, count);
        }

        long totalTime = System.currentTimeMillis() - startTime;
        System.out.printf("[推荐完成] 用户%s推荐%d个视频, 总耗时%dms%n", userId, result.size(), totalTime);

        return result;
    }

    public void shutdown() {
        executorService.shutdown();
    }

    // 召回策略接口
    interface RecallStrategy {
        String getName();

        List<Video> recall(String userId, int count);
    }

    // 协同过滤召回
    static class CollaborativeFilteringRecall implements RecallStrategy {
        @Override
        public String getName() {
            return "协同过滤召回";
        }

        @Override
        public List<Video> recall(String userId, int count) {
            simulateNetworkDelay(50, 150);
            List<Video> videos = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                videos.add(new Video("CF-" + i, "协同过滤视频" + i, 0.8 + Math.random() * 0.2));
            }
            return videos;
        }
    }

    // 内容召回
    static class ContentBasedRecall implements RecallStrategy {
        @Override
        public String getName() {
            return "内容召回";
        }

        @Override
        public List<Video> recall(String userId, int count) {
            simulateNetworkDelay(30, 100);
            List<Video> videos = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                videos.add(new Video("CB-" + i, "内容相关视频" + i, 0.7 + Math.random() * 0.2));
            }
            return videos;
        }
    }

    // 热门召回
    static class HotRecall implements RecallStrategy {
        @Override
        public String getName() {
            return "热门召回";
        }

        @Override
        public List<Video> recall(String userId, int count) {
            simulateNetworkDelay(20, 80);
            List<Video> videos = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                videos.add(new Video("HOT-" + i, "热门视频" + i, 0.9 + Math.random() * 0.1));
            }
            return videos;
        }
    }

    // 关注召回
    static class FollowingRecall implements RecallStrategy {
        @Override
        public String getName() {
            return "关注召回";
        }

        @Override
        public List<Video> recall(String userId, int count) {
            simulateNetworkDelay(40, 120);
            List<Video> videos = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                videos.add(new Video("FOL-" + i, "关注作者视频" + i, 0.85 + Math.random() * 0.15));
            }
            return videos;
        }
    }

    // 模拟网络延迟
    private static void simulateNetworkDelay(int minMs, int maxMs) {
        try {
            Thread.sleep((long) (minMs + Math.random() * (maxMs - minMs)));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // 视频实体
    public static class Video {
        private final String videoId;
        private final String title;
        private final double score;

        public Video(String videoId, String title, double score) {
            this.videoId = videoId;
            this.title = title;
            this.score = score;
        }

        @Override
        public String toString() {
            return String.format("Video{id='%s', title='%s', score=%.2f}", videoId, title, score);
        }
    }

    // 测试示例
    public static void main(String[] args) {
        System.out.println("=== 抖音推荐系统演示 ===\n");

        DouyinRecommendSystem system = new DouyinRecommendSystem(8, 500);

        // 为3个用户生成推荐
        String[] users = {"User-001", "User-002", "User-003"};
        for (String userId : users) {
            System.out.println("\n" + "=".repeat(50));
            List<Video> recommendations = system.recommend(userId, 10);

            System.out.println("\n推荐结果:");
            for (int i = 0; i < recommendations.size(); i++) {
                System.out.printf("%d. %s%n", i + 1, recommendations.get(i));
            }
        }

        system.shutdown();
    }
}
