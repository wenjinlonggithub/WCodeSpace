package com.architecture.concurrent.countdownlatch;

import java.util.concurrent.*;

/**
 * CountDownLatch 使用示例
 * 演示各种实际业务场景
 */
public class CountDownLatchDemo {

    /**
     * 示例1：主线程等待多个工作线程完成
     * 场景：批量数据处理，等待所有批次完成
     */
    public static void demo1_WaitForWorkers() throws InterruptedException {
        System.out.println("\n=== 示例1：主线程等待多个工作线程完成 ===");

        int workerCount = 5;
        CountDownLatch latch = new CountDownLatch(workerCount);

        for (int i = 1; i <= workerCount; i++) {
            final int workerId = i;
            new Thread(() -> {
                try {
                    System.out.println("工作线程 " + workerId + " 开始处理...");
                    Thread.sleep((long) (Math.random() * 2000)); // 模拟耗时操作
                    System.out.println("工作线程 " + workerId + " 完成！");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown(); // 计数器减1
                    System.out.println("剩余任务数: " + latch.getCount());
                }
            }, "Worker-" + workerId).start();
        }

        System.out.println("主线程等待所有工作线程完成...");
        latch.await(); // 阻塞直到计数器为0
        System.out.println("所有工作线程已完成，主线程继续执行！");
    }

    /**
     * 示例2：多线程同时开始（起跑线）
     * 场景：压测、秒杀模拟
     */
    public static void demo2_StartTogether() throws InterruptedException {
        System.out.println("\n=== 示例2：多线程同时开始（起跑线） ===");

        int runnerCount = 10;
        CountDownLatch startSignal = new CountDownLatch(1); // 起跑信号
        CountDownLatch doneSignal = new CountDownLatch(runnerCount); // 完成信号

        for (int i = 1; i <= runnerCount; i++) {
            final int runnerId = i;
            new Thread(() -> {
                try {
                    System.out.println("运动员 " + runnerId + " 准备就绪，等待发令枪...");
                    startSignal.await(); // 等待起跑信号

                    // 发令枪响，所有线程同时开始
                    long startTime = System.currentTimeMillis();
                    Thread.sleep((long) (Math.random() * 1000)); // 模拟跑步
                    long duration = System.currentTimeMillis() - startTime;

                    System.out.println("运动员 " + runnerId + " 完成，耗时: " + duration + "ms");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    doneSignal.countDown();
                }
            }, "Runner-" + runnerId).start();
        }

        Thread.sleep(1000); // 等待所有运动员准备就绪
        System.out.println("\n🔫 发令枪响！所有运动员开始！\n");
        startSignal.countDown(); // 发出起跑信号

        doneSignal.await(); // 等待所有运动员完成
        System.out.println("\n比赛结束！");
    }

    /**
     * 示例3：微服务聚合查询
     * 场景：并行调用多个服务，等待所有响应后聚合返回
     */
    public static void demo3_ServiceAggregation() throws InterruptedException {
        System.out.println("\n=== 示例3：微服务聚合查询 ===");

        CountDownLatch latch = new CountDownLatch(3);
        ConcurrentHashMap<String, Object> result = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(3);

        String userId = "USER_12345";

        // 查询用户服务
        executor.submit(() -> {
            try {
                Thread.sleep(500);
                result.put("userInfo", "张三, 25岁, 男");
                System.out.println("[用户服务] 查询完成");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        // 查询订单服务
        executor.submit(() -> {
            try {
                Thread.sleep(800);
                result.put("orders", "订单数: 15, 总金额: ¥5680");
                System.out.println("[订单服务] 查询完成");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        // 查询积分服务
        executor.submit(() -> {
            try {
                Thread.sleep(300);
                result.put("points", "积分余额: 3200");
                System.out.println("[积分服务] 查询完成");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        System.out.println("等待所有服务响应...");
        boolean success = latch.await(2, TimeUnit.SECONDS); // 超时等待

        if (success) {
            System.out.println("\n聚合结果:");
            result.forEach((k, v) -> System.out.println("  " + k + ": " + v));
        } else {
            System.out.println("部分服务超时！");
        }

        executor.shutdown();
    }

    /**
     * 示例4：应用启动预热
     * 场景：多个组件并行初始化，全部完成后才接受请求
     */
    public static void demo4_ApplicationStartup() throws InterruptedException {
        System.out.println("\n=== 示例4：应用启动预热 ===");

        CountDownLatch latch = new CountDownLatch(4);

        // 初始化数据库连接池
        new Thread(() -> {
            try {
                System.out.println("[数据库] 初始化连接池...");
                Thread.sleep(1000);
                System.out.println("[数据库] 初始化完成 ✓");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }).start();

        // 初始化缓存
        new Thread(() -> {
            try {
                System.out.println("[缓存] 预热数据...");
                Thread.sleep(1500);
                System.out.println("[缓存] 初始化完成 ✓");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }).start();

        // 加载配置
        new Thread(() -> {
            try {
                System.out.println("[配置] 加载配置文件...");
                Thread.sleep(500);
                System.out.println("[配置] 初始化完成 ✓");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }).start();

        // 注册MQ监听器
        new Thread(() -> {
            try {
                System.out.println("[消息队列] 注册监听器...");
                Thread.sleep(800);
                System.out.println("[消息队列] 初始化完成 ✓");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }).start();

        System.out.println("\n应用启动中，等待所有组件初始化...\n");
        latch.await();
        System.out.println("\n✅ 所有组件就绪，应用启动成功，开始接受请求！");
    }

    /**
     * 示例5：演示 countDown 的 CAS 线程安全机制
     */
    public static void demo5_ConcurrentCountDown() throws InterruptedException {
        System.out.println("\n=== 示例5：并发 countDown 的线程安全性 ===");

        int threadCount = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        CountDownLatch allThreadsReady = new CountDownLatch(threadCount);
        CountDownLatch startSignal = new CountDownLatch(1);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            new Thread(() -> {
                allThreadsReady.countDown(); // 当前线程准备就绪
                try {
                    startSignal.await(); // 等待所有线程就绪后同时执行
                    // 100个线程同时调用 countDown，测试 CAS 并发安全性
                    latch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Thread-" + threadId).start();
        }

        allThreadsReady.await(); // 等待所有线程准备就绪
        System.out.println(threadCount + " 个线程已就绪");

        long start = System.currentTimeMillis();
        startSignal.countDown(); // 发令：所有线程同时 countDown

        latch.await(); // 等待所有 countDown 完成
        long duration = System.currentTimeMillis() - start;

        System.out.println("最终计数: " + latch.getCount() + " (应该是 0)");
        System.out.println(threadCount + " 个线程并发 countDown 耗时: " + duration + "ms");
        System.out.println("✓ CAS 保证了计数器的线程安全！");
    }

    public static void main(String[] args) throws InterruptedException {
        demo1_WaitForWorkers();
        Thread.sleep(1000);

        demo2_StartTogether();
        Thread.sleep(1000);

        demo3_ServiceAggregation();
        Thread.sleep(1000);

        demo4_ApplicationStartup();
        Thread.sleep(1000);

        demo5_ConcurrentCountDown();
    }
}
