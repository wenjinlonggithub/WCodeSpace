package com.architecture.designpattern.singleton.demo;

public class Sun {
    /**
     * ==================== volatile关键字深度解析 ====================
     * 
     * 【背景故事】
     * 在多核CPU时代，每个CPU核心都有自己的缓存(L1/L2/L3)，为了性能优化，
     * 变量可能被缓存在CPU缓存中而不是主内存中。这就产生了缓存一致性问题。
     * 
     * 【核心原理】
     * 1. 可见性问题的本质
     *    - 线程A在CPU1上修改变量x=1，写入CPU1的缓存
     *    - 线程B在CPU2上读取变量x，从CPU2的缓存读取，仍然是旧值0
     *    - volatile强制所有读写操作直接作用于主内存
     * 
     * 2. 指令重排序的危险
     *    CPU和编译器为了优化性能，可能会重新排列指令执行顺序：
     *    原始代码：a = 1; b = 2; c = a + b;
     *    重排后：  b = 2; a = 1; c = a + b; (不影响单线程结果)
     *    但在多线程环境下可能导致其他线程看到不一致的状态
     * 
     * 3. 对象创建的三个步骤（关键！）
     *    sun = new Sun(); 实际分解为：
     *    ① memory = allocate();   // 分配内存空间
     *    ② ctorInstance(memory);  // 初始化对象
     *    ③ sun = memory;          // 设置引用指向内存地址
     *    
     *    如果发生指令重排序变成 ①③②，其他线程可能看到sun != null但对象未初始化！
     * 
     * 4. 内存屏障机制
     *    volatile在JVM层面会插入4种内存屏障：
     *    - LoadLoad屏障：  确保volatile读之前的所有读操作先完成
     *    - LoadStore屏障： 确保volatile读之前的所有读操作在任何写操作前完成
     *    - StoreStore屏障：确保volatile写之前的所有写操作先完成
     *    - StoreLoad屏障： 确保volatile写在任何后续读写操作前完成
     * 
     * 【为什么单例模式需要volatile】
     * 如果没有volatile，可能出现：
     * 1. 线程A执行到sun = new Sun()，但只完成了内存分配和引用赋值，对象未初始化
     * 2. 线程B看到sun != null，直接返回这个半初始化的对象
     * 3. 线程B使用这个对象时可能出现空指针或不一致状态
     * 
     * volatile确保对象完全构造完成后，引用才对其他线程可见
     */
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
