package com.architecture.core.cluster;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Dubbo 集群容错策略示例
 *
 * Dubbo 提供了多种集群容错策略：
 * 1. Failover：失败自动切换，重试其他服务器（默认）
 * 2. Failfast：快速失败，只发起一次调用，失败立即报错
 * 3. Failsafe：失败安全，出现异常时忽略
 * 4. Failback：失败自动恢复，后台记录失败请求，定时重发
 * 5. Forking：并行调用多个服务器，只要一个成功即返回
 * 6. Broadcast：广播调用所有提供者，任意一个报错则报错
 * 7. Available：调用目前可用的实例（可能不是最优）
 *
 * 核心原理：
 * - 在集群调用失败时，根据不同策略进行容错处理
 * - 配合负载均衡策略选择服务提供者
 * - 支持重试次数配置
 */
public class ClusterDemo {

    /**
     * 服务调用接口
     */
    interface Invoker<T> {
        String getUrl();
        T invoke(Invocation invocation) throws Exception;
        boolean isAvailable();
    }

    /**
     * 调用信息
     */
    static class Invocation {
        private String methodName;
        private Object[] arguments;

        public Invocation(String methodName, Object... arguments) {
            this.methodName = methodName;
            this.arguments = arguments;
        }

        public String getMethodName() {
            return methodName;
        }

        public Object[] getArguments() {
            return arguments;
        }
    }

    /**
     * 模拟服务提供者
     */
    static class MockInvoker implements Invoker<String> {
        private String url;
        private double failRate;
        private long responseTime;

        public MockInvoker(String url, double failRate, long responseTime) {
            this.url = url;
            this.failRate = failRate;
            this.responseTime = responseTime;
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String invoke(Invocation invocation) throws Exception {
            Thread.sleep(responseTime);

            if (ThreadLocalRandom.current().nextDouble() < failRate) {
                throw new Exception("调用失败: " + url);
            }

            return "调用成功: " + url + " -> " + invocation.getMethodName();
        }

        @Override
        public boolean isAvailable() {
            return ThreadLocalRandom.current().nextDouble() >= failRate;
        }
    }

    /**
     * 1. Failover 失败自动切换
     * 当调用失败时，自动切换到其他可用的服务提供者，并重试
     */
    static class FailoverClusterInvoker implements Invoker<String> {
        private List<Invoker<String>> invokers;
        private int retries;

        public FailoverClusterInvoker(List<Invoker<String>> invokers, int retries) {
            this.invokers = invokers;
            this.retries = retries;
        }

        @Override
        public String getUrl() {
            return "failover";
        }

        @Override
        public String invoke(Invocation invocation) throws Exception {
            List<Invoker<String>> copyInvokers = new ArrayList<>(invokers);
            Exception lastException = null;

            for (int i = 0; i <= retries; i++) {
                if (copyInvokers.isEmpty()) {
                    throw new Exception("没有可用的服务提供者");
                }

                // 随机选择一个服务提供者
                Invoker<String> invoker = copyInvokers.get(
                    ThreadLocalRandom.current().nextInt(copyInvokers.size())
                );

                try {
                    System.out.println("尝试调用 [" + (i + 1) + "]: " + invoker.getUrl());
                    return invoker.invoke(invocation);
                } catch (Exception e) {
                    lastException = e;
                    copyInvokers.remove(invoker);
                    System.out.println("调用失败，准备重试: " + e.getMessage());
                }
            }

            throw new Exception("重试" + retries + "次后仍然失败", lastException);
        }

        @Override
        public boolean isAvailable() {
            return invokers.stream().anyMatch(Invoker::isAvailable);
        }
    }

    /**
     * 2. Failfast 快速失败
     * 只发起一次调用，失败立即报错，适用于非幂等性操作
     */
    static class FailfastClusterInvoker implements Invoker<String> {
        private List<Invoker<String>> invokers;

        public FailfastClusterInvoker(List<Invoker<String>> invokers) {
            this.invokers = invokers;
        }

        @Override
        public String getUrl() {
            return "failfast";
        }

        @Override
        public String invoke(Invocation invocation) throws Exception {
            Invoker<String> invoker = invokers.get(
                ThreadLocalRandom.current().nextInt(invokers.size())
            );

            System.out.println("Failfast 调用: " + invoker.getUrl());
            return invoker.invoke(invocation);
        }

        @Override
        public boolean isAvailable() {
            return invokers.stream().anyMatch(Invoker::isAvailable);
        }
    }

    /**
     * 3. Failsafe 失败安全
     * 出现异常时，忽略异常，适用于写入审计日志等操作
     */
    static class FailsafeClusterInvoker implements Invoker<String> {
        private List<Invoker<String>> invokers;

        public FailsafeClusterInvoker(List<Invoker<String>> invokers) {
            this.invokers = invokers;
        }

        @Override
        public String getUrl() {
            return "failsafe";
        }

        @Override
        public String invoke(Invocation invocation) {
            try {
                Invoker<String> invoker = invokers.get(
                    ThreadLocalRandom.current().nextInt(invokers.size())
                );

                System.out.println("Failsafe 调用: " + invoker.getUrl());
                return invoker.invoke(invocation);
            } catch (Exception e) {
                System.out.println("Failsafe 忽略异常: " + e.getMessage());
                return "failsafe return null";
            }
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }

    /**
     * 4. Failback 失败自动恢复
     * 后台记录失败请求，定时重发
     */
    static class FailbackClusterInvoker implements Invoker<String> {
        private List<Invoker<String>> invokers;
        private Queue<Invocation> failedInvocations = new LinkedList<>();

        public FailbackClusterInvoker(List<Invoker<String>> invokers) {
            this.invokers = invokers;
            startRetryTask();
        }

        @Override
        public String getUrl() {
            return "failback";
        }

        @Override
        public String invoke(Invocation invocation) {
            try {
                Invoker<String> invoker = invokers.get(
                    ThreadLocalRandom.current().nextInt(invokers.size())
                );

                System.out.println("Failback 调用: " + invoker.getUrl());
                return invoker.invoke(invocation);
            } catch (Exception e) {
                System.out.println("Failback 记录失败请求: " + e.getMessage());
                failedInvocations.offer(invocation);
                return "failback scheduled";
            }
        }

        private void startRetryTask() {
            new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(5000);
                        retryFailedInvocations();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }).start();
        }

        private void retryFailedInvocations() {
            if (failedInvocations.isEmpty()) {
                return;
            }

            System.out.println("\n=== 开始重试失败的请求 ===");
            Queue<Invocation> retryQueue = new LinkedList<>(failedInvocations);
            failedInvocations.clear();

            while (!retryQueue.isEmpty()) {
                Invocation invocation = retryQueue.poll();
                try {
                    Invoker<String> invoker = invokers.get(
                        ThreadLocalRandom.current().nextInt(invokers.size())
                    );
                    invoker.invoke(invocation);
                    System.out.println("重试成功: " + invocation.getMethodName());
                } catch (Exception e) {
                    System.out.println("重试失败: " + invocation.getMethodName());
                    failedInvocations.offer(invocation);
                }
            }
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }

    /**
     * 5. Forking 并行调用
     * 并行调用多个服务器，只要一个成功即返回
     */
    static class ForkingClusterInvoker implements Invoker<String> {
        private List<Invoker<String>> invokers;
        private int forks;

        public ForkingClusterInvoker(List<Invoker<String>> invokers, int forks) {
            this.invokers = invokers;
            this.forks = Math.min(forks, invokers.size());
        }

        @Override
        public String getUrl() {
            return "forking";
        }

        @Override
        public String invoke(Invocation invocation) throws Exception {
            List<Invoker<String>> selected = new ArrayList<>();
            for (int i = 0; i < forks; i++) {
                selected.add(invokers.get(i));
            }

            System.out.println("Forking 并行调用 " + forks + " 个服务提供者");

            // 简化实现：顺序调用，实际应该使用线程池并行调用
            for (Invoker<String> invoker : selected) {
                try {
                    return invoker.invoke(invocation);
                } catch (Exception e) {
                    System.out.println("Forking 调用失败: " + invoker.getUrl());
                }
            }

            throw new Exception("所有并行调用都失败了");
        }

        @Override
        public boolean isAvailable() {
            return invokers.stream().anyMatch(Invoker::isAvailable);
        }
    }

    /**
     * 6. Broadcast 广播调用
     * 广播调用所有提供者，逐个调用，任意一个报错则报错
     */
    static class BroadcastClusterInvoker implements Invoker<String> {
        private List<Invoker<String>> invokers;

        public BroadcastClusterInvoker(List<Invoker<String>> invokers) {
            this.invokers = invokers;
        }

        @Override
        public String getUrl() {
            return "broadcast";
        }

        @Override
        public String invoke(Invocation invocation) throws Exception {
            System.out.println("Broadcast 广播调用所有服务提供者");

            Exception exception = null;
            String result = null;

            for (Invoker<String> invoker : invokers) {
                try {
                    result = invoker.invoke(invocation);
                    System.out.println("Broadcast 成功: " + invoker.getUrl());
                } catch (Exception e) {
                    exception = e;
                    System.out.println("Broadcast 失败: " + invoker.getUrl());
                }
            }

            if (exception != null) {
                throw exception;
            }

            return result;
        }

        @Override
        public boolean isAvailable() {
            return invokers.stream().allMatch(Invoker::isAvailable);
        }
    }

    public static void main(String[] args) throws Exception {
        // 创建服务提供者列表
        List<Invoker<String>> invokers = Arrays.asList(
            new MockInvoker("192.168.1.1:20880", 0.7, 100),  // 高失败率
            new MockInvoker("192.168.1.2:20880", 0.3, 100),  // 中失败率
            new MockInvoker("192.168.1.3:20880", 0.1, 100)   // 低失败率
        );

        Invocation invocation = new Invocation("sayHello", "World");

        // 测试 Failover
        System.out.println("=== 测试 Failover 策略 ===");
        try {
            FailoverClusterInvoker failover = new FailoverClusterInvoker(invokers, 2);
            String result = failover.invoke(invocation);
            System.out.println("结果: " + result);
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
        }

        System.out.println("\n=== 测试 Failfast 策略 ===");
        try {
            FailfastClusterInvoker failfast = new FailfastClusterInvoker(invokers);
            String result = failfast.invoke(invocation);
            System.out.println("结果: " + result);
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
        }

        System.out.println("\n=== 测试 Failsafe 策略 ===");
        FailsafeClusterInvoker failsafe = new FailsafeClusterInvoker(invokers);
        String result = failsafe.invoke(invocation);
        System.out.println("结果: " + result);

        System.out.println("\n=== 测试 Forking 策略 ===");
        try {
            ForkingClusterInvoker forking = new ForkingClusterInvoker(invokers, 2);
            result = forking.invoke(invocation);
            System.out.println("结果: " + result);
        } catch (Exception e) {
            System.out.println("失败: " + e.getMessage());
        }
    }
}
