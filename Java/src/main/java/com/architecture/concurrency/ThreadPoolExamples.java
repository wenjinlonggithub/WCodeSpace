package com.architecture.concurrency;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * Java线程池详解和最佳实践
 * 
 * 涵盖内容：
 * 1. 线程池类型和特点
 * 2. 自定义线程池配置
 * 3. 任务提交和结果处理
 * 4. 线程池监控和调优
 * 5. 异常处理和优雅关闭
 */

public class ThreadPoolExamples {
    
    // ============= 线程池类型演示 =============
    
    public static void demonstrateThreadPoolTypes() {
        System.out.println("=== Thread Pool Types Demo ===");
        
        // 1. FixedThreadPool - 固定大小线程池
        System.out.println("\n--- Fixed Thread Pool ---");
        ExecutorService fixedPool = Executors.newFixedThreadPool(3);
        
        for (int i = 0; i < 6; i++) {
            final int taskId = i;
            fixedPool.submit(() -> {
                System.out.println("FixedPool Task " + taskId + " executed by " + 
                    Thread.currentThread().getName());
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
            });
        }
        
        shutdownAndWait(fixedPool, "FixedThreadPool");
        
        // 2. CachedThreadPool - 缓存线程池
        System.out.println("\n--- Cached Thread Pool ---");
        ExecutorService cachedPool = Executors.newCachedThreadPool();
        
        for (int i = 0; i < 6; i++) {
            final int taskId = i;
            cachedPool.submit(() -> {
                System.out.println("CachedPool Task " + taskId + " executed by " + 
                    Thread.currentThread().getName());
                try { Thread.sleep(500); } catch (InterruptedException e) {}
            });
        }
        
        shutdownAndWait(cachedPool, "CachedThreadPool");
        
        // 3. SingleThreadExecutor - 单线程执行器
        System.out.println("\n--- Single Thread Executor ---");
        ExecutorService singlePool = Executors.newSingleThreadExecutor();
        
        for (int i = 0; i < 4; i++) {
            final int taskId = i;
            singlePool.submit(() -> {
                System.out.println("SinglePool Task " + taskId + " executed by " + 
                    Thread.currentThread().getName());
                try { Thread.sleep(300); } catch (InterruptedException e) {}
            });
        }
        
        shutdownAndWait(singlePool, "SingleThreadExecutor");
        
        // 4. ScheduledThreadPool - 定时任务线程池
        System.out.println("\n--- Scheduled Thread Pool ---");
        ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(2);
        
        // 延迟执行
        scheduledPool.schedule(() -> {
            System.out.println("Delayed task executed after 1 second");
        }, 1, TimeUnit.SECONDS);
        
        // 固定频率执行
        ScheduledFuture<?> periodicTask = scheduledPool.scheduleAtFixedRate(() -> {
            System.out.println("Periodic task executed at " + System.currentTimeMillis());
        }, 0, 500, TimeUnit.MILLISECONDS);
        
        // 运行3秒后取消定时任务
        try {
            Thread.sleep(3000);
            periodicTask.cancel(false);
        } catch (InterruptedException e) {}
        
        shutdownAndWait(scheduledPool, "ScheduledThreadPool");
    }
    
    // ============= 自定义线程池配置 =============
    
    public static void demonstrateCustomThreadPool() {
        System.out.println("\n=== Custom Thread Pool Demo ===");
        
        // 自定义线程工厂
        ThreadFactory customThreadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "CustomPool-Thread-" + threadNumber.getAndIncrement());
                thread.setDaemon(false); // 非守护线程
                thread.setPriority(Thread.NORM_PRIORITY);
                return thread;
            }
        };
        
        // 自定义拒绝策略
        RejectedExecutionHandler customRejectedHandler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                System.out.println("Task rejected: " + r.toString() + 
                    " - Pool: " + executor.toString());
            }
        };
        
        // 创建自定义线程池
        ThreadPoolExecutor customPool = new ThreadPoolExecutor(
            2,                          // corePoolSize: 核心线程数
            4,                          // maximumPoolSize: 最大线程数
            60L,                        // keepAliveTime: 空闲线程存活时间
            TimeUnit.SECONDS,           // 时间单位
            new ArrayBlockingQueue<>(3), // workQueue: 工作队列
            customThreadFactory,        // threadFactory: 线程工厂
            customRejectedHandler       // handler: 拒绝策略
        );
        
        // 提交任务测试
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            try {
                customPool.submit(() -> {
                    System.out.println("Custom Task " + taskId + " executed by " + 
                        Thread.currentThread().getName());
                    try { Thread.sleep(2000); } catch (InterruptedException e) {}
                });
            } catch (RejectedExecutionException e) {
                System.out.println("Task " + taskId + " was rejected");
            }
        }
        
        // 监控线程池状态
        monitorThreadPool(customPool);
        
        shutdownAndWait(customPool, "CustomThreadPool");
    }
    
    // ============= 任务提交和结果处理 =============
    
    public static void demonstrateTaskSubmission() throws InterruptedException, ExecutionException {
        System.out.println("\n=== Task Submission Demo ===");
        
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // 1. submit(Runnable) - 无返回值
        Future<?> future1 = executor.submit(() -> {
            System.out.println("Runnable task executed");
        });
        
        System.out.println("Runnable result: " + future1.get()); // null
        
        // 2. submit(Callable) - 有返回值
        Future<String> future2 = executor.submit(() -> {
            Thread.sleep(1000);
            return "Callable task result";
        });
        
        System.out.println("Callable result: " + future2.get());
        
        // 3. invokeAll - 批量提交任务
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            final int taskId = i;
            tasks.add(() -> {
                Thread.sleep(500);
                return taskId * taskId;
            });
        }
        
        List<Future<Integer>> results = executor.invokeAll(tasks);
        System.out.println("InvokeAll results:");
        for (int i = 0; i < results.size(); i++) {
            System.out.println("Task " + i + " result: " + results.get(i).get());
        }
        
        // 4. invokeAny - 返回最先完成的任务结果
        List<Callable<String>> anyTasks = new ArrayList<>();
        anyTasks.add(() -> { Thread.sleep(1000); return "Task 1"; });
        anyTasks.add(() -> { Thread.sleep(500); return "Task 2"; });
        anyTasks.add(() -> { Thread.sleep(1500); return "Task 3"; });
        
        String anyResult = executor.invokeAny(anyTasks);
        System.out.println("InvokeAny result: " + anyResult);
        
        shutdownAndWait(executor, "TaskSubmissionPool");
    }
    
    // ============= CompletableFuture异步编程 =============
    
    public static void demonstrateCompletableFuture() {
        System.out.println("\n=== CompletableFuture Demo ===");
        
        // 1. 异步执行
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
            return "Hello";
        });
        
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(800); } catch (InterruptedException e) {}
            return "World";
        });
        
        // 2. 组合多个Future
        CompletableFuture<String> combinedFuture = future1.thenCombine(future2, (s1, s2) -> {
            return s1 + " " + s2 + "!";
        });
        
        try {
            System.out.println("Combined result: " + combinedFuture.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 3. 链式调用
        CompletableFuture<Integer> chainFuture = CompletableFuture
            .supplyAsync(() -> 10)
            .thenApply(x -> x * 2)
            .thenApply(x -> x + 5)
            .thenCompose(x -> CompletableFuture.supplyAsync(() -> x * 3));
        
        try {
            System.out.println("Chain result: " + chainFuture.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 4. 异常处理
        CompletableFuture<String> exceptionFuture = CompletableFuture
            .supplyAsync(() -> {
                if (Math.random() > 0.5) {
                    throw new RuntimeException("Random exception");
                }
                return "Success";
            })
            .exceptionally(throwable -> {
                System.out.println("Exception handled: " + throwable.getMessage());
                return "Default value";
            });
        
        try {
            System.out.println("Exception handling result: " + exceptionFuture.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 5. 等待所有任务完成
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
            CompletableFuture.runAsync(() -> { 
                try { Thread.sleep(500); } catch (InterruptedException e) {}
                System.out.println("Task A completed"); 
            }),
            CompletableFuture.runAsync(() -> { 
                try { Thread.sleep(700); } catch (InterruptedException e) {}
                System.out.println("Task B completed"); 
            }),
            CompletableFuture.runAsync(() -> { 
                try { Thread.sleep(300); } catch (InterruptedException e) {}
                System.out.println("Task C completed"); 
            })
        );
        
        try {
            allOf.get();
            System.out.println("All tasks completed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // ============= 工具方法 =============
    
    private static void shutdownAndWait(ExecutorService executor, String poolName) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println(poolName + " did not terminate gracefully, forcing shutdown");
                executor.shutdownNow();
            } else {
                System.out.println(poolName + " terminated gracefully");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    private static void monitorThreadPool(ThreadPoolExecutor executor) {
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                System.out.printf("Pool Status - Active: %d, Pool Size: %d, Queue Size: %d, Completed: %d%n",
                    executor.getActiveCount(),
                    executor.getPoolSize(),
                    executor.getQueue().size(),
                    executor.getCompletedTaskCount());
                
                try { Thread.sleep(500); } catch (InterruptedException e) { break; }
            }
        }).start();
    }
    
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        demonstrateThreadPoolTypes();
        demonstrateCustomThreadPool();
        demonstrateTaskSubmission();
        demonstrateCompletableFuture();
    }
}