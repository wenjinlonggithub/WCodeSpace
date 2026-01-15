package com.architecture.core.loadbalance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dubbo 负载均衡策略示例
 *
 * Dubbo 提供了多种负载均衡策略：
 * 1. RandomLoadBalance：随机负载均衡（默认）
 * 2. RoundRobinLoadBalance：轮询负载均衡
 * 3. LeastActiveLoadBalance：最少活跃调用数负载均衡
 * 4. ConsistentHashLoadBalance：一致性哈希负载均衡
 * 5. ShortestResponseLoadBalance：最短响应时间负载均衡
 *
 * 核心原理：
 * - 基于权重的算法
 * - 预热机制：服务刚启动时逐步增加权重
 * - 活跃调用数统计
 */
public class LoadBalanceDemo {

    /**
     * 服务提供者模型
     */
    static class Invoker {
        private String host;
        private int port;
        private int weight;
        private long startTime;
        private AtomicInteger activeCount;
        private long totalResponseTime;
        private int requestCount;

        public Invoker(String host, int port, int weight) {
            this.host = host;
            this.port = port;
            this.weight = weight;
            this.startTime = System.currentTimeMillis();
            this.activeCount = new AtomicInteger(0);
            this.totalResponseTime = 0;
            this.requestCount = 0;
        }

        public String getUrl() {
            return host + ":" + port;
        }

        public int getWeight() {
            return weight;
        }

        public int getActiveCount() {
            return activeCount.get();
        }

        public void incrementActive() {
            activeCount.incrementAndGet();
        }

        public void decrementActive() {
            activeCount.decrementAndGet();
        }

        public void recordResponse(long responseTime) {
            totalResponseTime += responseTime;
            requestCount++;
        }

        public double getAverageResponseTime() {
            return requestCount == 0 ? 0 : (double) totalResponseTime / requestCount;
        }

        public int getWarmupWeight() {
            long uptime = System.currentTimeMillis() - startTime;
            int warmup = 600000; // 预热时间 10 分钟
            if (uptime > 0 && uptime < warmup) {
                return (int) ((float) uptime / warmup * weight);
            }
            return weight;
        }
    }

    /**
     * 1. 随机负载均衡（加权随机）
     */
    static class RandomLoadBalance {
        public Invoker select(List<Invoker> invokers) {
            int length = invokers.size();
            boolean sameWeight = true;
            int[] weights = new int[length];
            int totalWeight = 0;

            for (int i = 0; i < length; i++) {
                int weight = invokers.get(i).getWarmupWeight();
                totalWeight += weight;
                weights[i] = weight;
                if (sameWeight && i > 0 && weight != weights[i - 1]) {
                    sameWeight = false;
                }
            }

            if (totalWeight > 0 && !sameWeight) {
                // 基于权重的随机选择
                int offset = ThreadLocalRandom.current().nextInt(totalWeight);
                for (int i = 0; i < length; i++) {
                    offset -= weights[i];
                    if (offset < 0) {
                        return invokers.get(i);
                    }
                }
            }

            // 权重相同，随机选择
            return invokers.get(ThreadLocalRandom.current().nextInt(length));
        }
    }

    /**
     * 2. 轮询负载均衡（加权轮询）
     */
    static class RoundRobinLoadBalance {
        private static class WeightedRoundRobin {
            private int weight;
            private AtomicInteger current = new AtomicInteger(0);

            public WeightedRoundRobin(int weight) {
                this.weight = weight;
            }

            public int increaseCurrent() {
                return current.addAndGet(weight);
            }

            public void decreaseCurrent(int total) {
                current.addAndGet(-total);
            }
        }

        private ConcurrentHashMap<String, WeightedRoundRobin> map = new ConcurrentHashMap<>();

        public Invoker select(List<Invoker> invokers) {
            int totalWeight = 0;
            int maxCurrent = Integer.MIN_VALUE;
            Invoker selectedInvoker = null;
            WeightedRoundRobin selectedWRR = null;

            for (Invoker invoker : invokers) {
                String key = invoker.getUrl();
                int weight = invoker.getWarmupWeight();

                WeightedRoundRobin wrr = map.computeIfAbsent(key, k -> new WeightedRoundRobin(weight));

                int current = wrr.increaseCurrent();
                if (current > maxCurrent) {
                    maxCurrent = current;
                    selectedInvoker = invoker;
                    selectedWRR = wrr;
                }

                totalWeight += weight;
            }

            if (selectedInvoker != null && selectedWRR != null) {
                selectedWRR.decreaseCurrent(totalWeight);
                return selectedInvoker;
            }

            return invokers.get(0);
        }
    }

    /**
     * 3. 最少活跃调用数负载均衡
     */
    static class LeastActiveLoadBalance {
        public Invoker select(List<Invoker> invokers) {
            int length = invokers.size();
            int leastActive = -1;
            int leastCount = 0;
            int[] leastIndexes = new int[length];
            int[] weights = new int[length];
            int totalWeight = 0;
            int firstWeight = 0;
            boolean sameWeight = true;

            for (int i = 0; i < length; i++) {
                Invoker invoker = invokers.get(i);
                int active = invoker.getActiveCount();
                int weight = invoker.getWarmupWeight();

                if (leastActive == -1 || active < leastActive) {
                    leastActive = active;
                    leastCount = 1;
                    leastIndexes[0] = i;
                    totalWeight = weight;
                    firstWeight = weight;
                    sameWeight = true;
                } else if (active == leastActive) {
                    leastIndexes[leastCount++] = i;
                    totalWeight += weight;
                    if (sameWeight && weight != firstWeight) {
                        sameWeight = false;
                    }
                }
                weights[i] = weight;
            }

            if (leastCount == 1) {
                return invokers.get(leastIndexes[0]);
            }

            if (!sameWeight && totalWeight > 0) {
                int offsetWeight = ThreadLocalRandom.current().nextInt(totalWeight);
                for (int i = 0; i < leastCount; i++) {
                    int leastIndex = leastIndexes[i];
                    offsetWeight -= weights[leastIndex];
                    if (offsetWeight < 0) {
                        return invokers.get(leastIndex);
                    }
                }
            }

            return invokers.get(leastIndexes[ThreadLocalRandom.current().nextInt(leastCount)]);
        }
    }

    /**
     * 4. 一致性哈希负载均衡
     */
    static class ConsistentHashLoadBalance {
        private TreeMap<Long, Invoker> virtualNodes = new TreeMap<>();
        private int replicaNumber = 160; // 虚拟节点数

        public ConsistentHashLoadBalance(List<Invoker> invokers) {
            for (Invoker invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    byte[] digest = md5(invoker.getUrl() + i);
                    for (int j = 0; j < 4; j++) {
                        long hash = hash(digest, j);
                        virtualNodes.put(hash, invoker);
                    }
                }
            }
        }

        public Invoker select(String key) {
            byte[] digest = md5(key);
            long hash = hash(digest, 0);

            Map.Entry<Long, Invoker> entry = virtualNodes.ceilingEntry(hash);
            if (entry == null) {
                entry = virtualNodes.firstEntry();
            }
            return entry.getValue();
        }

        private byte[] md5(String key) {
            // 简化实现
            return key.getBytes();
        }

        private long hash(byte[] digest, int number) {
            return ((long) (digest[3 + number * 4] & 0xFF) << 24)
                    | ((long) (digest[2 + number * 4] & 0xFF) << 16)
                    | ((long) (digest[1 + number * 4] & 0xFF) << 8)
                    | (digest[number * 4] & 0xFF);
        }
    }

    /**
     * 5. 最短响应时间负载均衡
     */
    static class ShortestResponseLoadBalance {
        public Invoker select(List<Invoker> invokers) {
            int length = invokers.size();
            double minResponseTime = Double.MAX_VALUE;
            int shortestCount = 0;
            int[] shortestIndexes = new int[length];

            for (int i = 0; i < length; i++) {
                Invoker invoker = invokers.get(i);
                double responseTime = invoker.getAverageResponseTime();

                if (responseTime < minResponseTime) {
                    minResponseTime = responseTime;
                    shortestCount = 1;
                    shortestIndexes[0] = i;
                } else if (responseTime == minResponseTime) {
                    shortestIndexes[shortestCount++] = i;
                }
            }

            if (shortestCount == 1) {
                return invokers.get(shortestIndexes[0]);
            }

            return invokers.get(shortestIndexes[ThreadLocalRandom.current().nextInt(shortestCount)]);
        }
    }

    public static void main(String[] args) {
        // 创建服务提供者列表
        List<Invoker> invokers = Arrays.asList(
            new Invoker("192.168.1.1", 20880, 100),
            new Invoker("192.168.1.2", 20880, 200),
            new Invoker("192.168.1.3", 20880, 300)
        );

        // 测试随机负载均衡
        System.out.println("=== 随机负载均衡 ===");
        RandomLoadBalance random = new RandomLoadBalance();
        testLoadBalance(invokers, random);

        // 测试轮询负载均衡
        System.out.println("\n=== 轮询负载均衡 ===");
        RoundRobinLoadBalance roundRobin = new RoundRobinLoadBalance();
        testLoadBalance(invokers, roundRobin);

        // 测试最少活跃调用数负载均衡
        System.out.println("\n=== 最少活跃调用数负载均衡 ===");
        LeastActiveLoadBalance leastActive = new LeastActiveLoadBalance();
        testLoadBalance(invokers, leastActive);

        // 测试一致性哈希负载均衡
        System.out.println("\n=== 一致性哈希负载均衡 ===");
        ConsistentHashLoadBalance consistentHash = new ConsistentHashLoadBalance(invokers);
        for (int i = 0; i < 10; i++) {
            String key = "user" + i;
            Invoker selected = consistentHash.select(key);
            System.out.println(key + " -> " + selected.getUrl());
        }
    }

    private static void testLoadBalance(List<Invoker> invokers, Object loadBalance) {
        Map<String, Integer> stats = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            Invoker selected;
            if (loadBalance instanceof RandomLoadBalance) {
                selected = ((RandomLoadBalance) loadBalance).select(invokers);
            } else if (loadBalance instanceof RoundRobinLoadBalance) {
                selected = ((RoundRobinLoadBalance) loadBalance).select(invokers);
            } else {
                selected = ((LeastActiveLoadBalance) loadBalance).select(invokers);
            }
            stats.merge(selected.getUrl(), 1, Integer::sum);
        }
        stats.forEach((url, count) ->
            System.out.println(url + " 被选中: " + count + " 次"));
    }
}
