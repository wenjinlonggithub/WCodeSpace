package com.architecture.designpattern.singleton.demo;

public class Sun {
    private volatile static Sun sun; //自有永有的单例

    private Sun() { //构造方法私有化

    }

    public static Sun getInstance() { //阳光普照，方法公开化
        if (sun == null) { //无日才造日
            synchronized (Sun.class) {
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
