package com.architecture.designpattern.singleton.demo;

public class Sun {
    private volatile static Sun sun; //自有永有的单例

    private Sun() { //构造方法私有化

    }

    public static Sun getInstance() { //阳光普照，方法公开化
        if (sun == null) { //无日才造日
            /**
             * ● synchronized (Sun.class) 这行代码的作用是：
             *
             *   1. 类级别锁定 - 获取 Sun 类的 Class
             *   对象作为同步锁，确保整个类只有一个线程能进入该代码块
             *   2. 防止重复创建实例 -
             *   在双重检查锁定模式中，第二层检查确保即使多个线程同时通过第一个 if
             *   检查，也只有一个线程能创建实例
             *   3. 线程安全保障 - 当多个线程同时调用 getInstance()
             *   时，synchronized 块确保只有一个线程能执行实例创建逻辑
             *   4. 性能优化 - 相比在整个方法上加
             *   synchronized，这种方式只在需要创建实例时才加锁，减少了锁竞争
             *
             *   这是单例模式中经典的"双重检查锁定"实现，既保证了线程安全又提高了性
             *   能。
             */
            synchronized (Sun.class) { //类级别锁定，防止重复创建实例，确保线程安全的双重检查锁定
                if (sun == null) {
                    sun = new Sun();
                }
            }
        }
        return sun;
    }

    public static void main(String[] args) {
        // 多线程测试案例
        int threadCount = 10000;
        Thread[] threads = new Thread[threadCount];
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                long threadStartTime = System.nanoTime();
                Sun instance = Sun.getInstance();
                long threadEndTime = System.nanoTime();
                long executionTime = threadEndTime - threadStartTime;
                System.out.println("Thread " + threadId + " got instance: " + instance.hashCode() + 
                    ", execution time: " + (executionTime / 1000) + " us");
            });
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        
        // 验证单例
        Sun demo1 = Sun.getInstance();
        Sun demo2 = Sun.getInstance();
        System.out.println("\nSingleton Verification:");
        System.out.println("Instance 1: " + demo1.hashCode());
        System.out.println("Instance 2: " + demo2.hashCode());
        System.out.println("Are they the same? " + (demo1 == demo2));
        System.out.println("\nPerformance Statistics:");
        System.out.println("Total execution time: " + (totalTime / 1_000_000) + " ms");
        System.out.println("Average thread execution time: " + (totalTime / threadCount / 1000) + " us");
    }
}
