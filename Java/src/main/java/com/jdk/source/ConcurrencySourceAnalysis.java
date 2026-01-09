package com.jdk.source;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.lang.ref.*;
import java.lang.reflect.*;

/**
 * JDK并发源码深度分析
 * 
 * 深入解析：
 * 1. ThreadLocal实现原理和内存泄漏
 * 2. ConcurrentHashMap分段锁机制
 * 3. AtomicInteger的CAS实现
 * 4. ReentrantLock的AQS原理
 * 5. FutureTask的状态机制
 * 6. 引用类型和垃圾回收
 */

public class ConcurrencySourceAnalysis {
    
    /**
     * ThreadLocal实现原理分析
     * 
     * 核心原理：
     * 1. 每个Thread都有一个ThreadLocalMap
     * 2. ThreadLocalMap的key是ThreadLocal对象（弱引用）
     * 3. value是实际存储的值
     * 4. 通过ThreadLocal对象作为key来存取值
     */
    
    static class ThreadLocalAnalysis {
        
        // 演示ThreadLocal的基本使用
        private static final ThreadLocal<String> threadLocalString = new ThreadLocal<String>() {
            @Override
            protected String initialValue() {
                return "Initial-" + Thread.currentThread().getName();
            }
        };
        
        private static final ThreadLocal<Integer> threadLocalInteger = 
            ThreadLocal.withInitial(() -> (int)(Math.random() * 100));
        
        /**
         * ThreadLocal源码分析：
         * 
         * public void set(T value) {
         *     Thread t = Thread.currentThread();
         *     ThreadLocalMap map = getMap(t);  // 获取当前线程的ThreadLocalMap
         *     if (map != null)
         *         map.set(this, value);        // this作为key，value作为值
         *     else
         *         createMap(t, value);         // 创建新的ThreadLocalMap
         * }
         * 
         * public T get() {
         *     Thread t = Thread.currentThread();
         *     ThreadLocalMap map = getMap(t);
         *     if (map != null) {
         *         ThreadLocalMap.Entry e = map.getEntry(this);
         *         if (e != null) {
         *             return (T)e.value;
         *         }
         *     }
         *     return setInitialValue();
         * }
         */
        
        public static void demonstrateThreadLocalBehavior() {
            System.out.println("=== ThreadLocal Behavior Analysis ===");
            
            // 创建多个线程测试ThreadLocal的隔离性
            ExecutorService executor = Executors.newFixedThreadPool(3);
            
            for (int i = 0; i < 3; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    // 每个线程设置不同的值
                    threadLocalString.set("Thread-" + threadId + "-Value");
                    threadLocalInteger.set(threadId * 10);
                    
                    // 验证线程隔离性
                    System.out.println(Thread.currentThread().getName() + 
                        " - String: " + threadLocalString.get() + 
                        ", Integer: " + threadLocalInteger.get());
                    
                    // 模拟一些工作
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    // 再次读取，验证值没有被其他线程影响
                    System.out.println(Thread.currentThread().getName() + 
                        " - After sleep - String: " + threadLocalString.get() + 
                        ", Integer: " + threadLocalInteger.get());
                });
            }
            
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        /**
         * ThreadLocal内存泄漏分析
         * 
         * 内存泄漏原因：
         * 1. ThreadLocalMap的Entry继承自WeakReference<ThreadLocal<?>>
         * 2. key（ThreadLocal对象）是弱引用，会被GC回收
         * 3. value是强引用，如果不手动remove()，会造成内存泄漏
         * 4. 线程池中的线程长期存活，加剧了内存泄漏问题
         */
        
        static class MemoryLeakDemo {
            private static final int THREAD_COUNT = 5;
            private static final int ITERATIONS = 1000;
            
            public static void demonstrateMemoryLeak() {
                System.out.println("\n--- ThreadLocal Memory Leak Demo ---");
                
                ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
                
                for (int i = 0; i < THREAD_COUNT; i++) {
                    executor.submit(() -> {
                        for (int j = 0; j < ITERATIONS; j++) {
                            // 创建ThreadLocal但不remove()，可能导致内存泄漏
                            ThreadLocal<byte[]> leakyThreadLocal = new ThreadLocal<>();
                            leakyThreadLocal.set(new byte[1024]); // 1KB数据
                            
                            // 正确的做法是在finally块中remove()
                            // leakyThreadLocal.remove();
                        }
                        
                        System.out.println(Thread.currentThread().getName() + 
                            " completed " + ITERATIONS + " iterations");
                    });
                }
                
                executor.shutdown();
                try {
                    executor.awaitTermination(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // 建议进行垃圾回收观察内存使用
                System.gc();
                System.out.println("Memory leak demo completed. Check memory usage.");
            }
        }
        
        /**
         * ThreadLocal最佳实践
         */
        public static void demonstrateBestPractices() {
            System.out.println("\n--- ThreadLocal Best Practices ---");
            
            // 1. 使用static final修饰ThreadLocal
            final ThreadLocal<StringBuilder> stringBuilder = new ThreadLocal<StringBuilder>() {
                @Override
                protected StringBuilder initialValue() {
                    return new StringBuilder();
                }
            };
            
            // 2. 及时清理资源
            try {
                StringBuilder sb = stringBuilder.get();
                sb.append("Hello ThreadLocal");
                System.out.println("StringBuilder content: " + sb.toString());
            } finally {
                // 重要：及时清理，防止内存泄漏
                stringBuilder.remove();
            }
            
            // 3. 使用InheritableThreadLocal传递给子线程
            InheritableThreadLocal<String> inheritableThreadLocal = new InheritableThreadLocal<>();
            inheritableThreadLocal.set("Parent thread value");
            
            Thread childThread = new Thread(() -> {
                System.out.println("Child thread inherited value: " + 
                    inheritableThreadLocal.get());
            });
            
            childThread.start();
            try {
                childThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            inheritableThreadLocal.remove();
        }
    }
    
    /**
     * ConcurrentHashMap源码分析
     * 
     * JDK 7: 分段锁（Segment）
     * JDK 8: CAS + synchronized
     */
    
    static class ConcurrentHashMapAnalysis {
        
        /**
         * JDK 8 ConcurrentHashMap核心原理：
         * 
         * 1. 数组 + 链表 + 红黑树结构
         * 2. CAS操作保证线程安全
         * 3. synchronized锁定链表/红黑树的头节点
         * 4. 分段计数器统计size
         */
        
        public static void analyzeConcurrentHashMapStructure() {
            System.out.println("\n=== ConcurrentHashMap Structure Analysis ===");
            
            ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
            
            // 1. 基本操作的线程安全性
            System.out.println("\n--- Thread Safety Test ---");
            
            ExecutorService executor = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(10);
            
            // 10个线程并发写入
            for (int i = 0; i < 10; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < 1000; j++) {
                            String key = "key-" + (threadId * 1000 + j);
                            map.put(key, threadId * 1000 + j);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            try {
                latch.await();
                System.out.println("Final map size: " + map.size());
                System.out.println("Expected size: 10000");
                System.out.println("Data integrity: " + (map.size() == 10000 ? "PASS" : "FAIL"));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            executor.shutdown();
            
            // 2. ConcurrentHashMap特有方法
            System.out.println("\n--- ConcurrentHashMap Specific Methods ---");
            
            // putIfAbsent - 原子性操作
            Integer oldValue = map.putIfAbsent("atomic-key", 100);
            System.out.println("putIfAbsent result: " + oldValue); // null
            
            oldValue = map.putIfAbsent("atomic-key", 200);
            System.out.println("putIfAbsent result: " + oldValue); // 100
            
            // replace - 原子性替换
            boolean replaced = map.replace("atomic-key", 100, 300);
            System.out.println("replace result: " + replaced); // true
            
            // compute - 原子性计算
            map.compute("compute-key", (key, val) -> {
                return (val == null) ? 1 : val + 1;
            });
            System.out.println("compute result: " + map.get("compute-key")); // 1
            
            map.compute("compute-key", (key, val) -> val + 1);
            System.out.println("compute result: " + map.get("compute-key")); // 2
            
            // merge - 原子性合并
            map.merge("merge-key", 10, Integer::sum);
            System.out.println("merge result: " + map.get("merge-key")); // 10
            
            map.merge("merge-key", 20, Integer::sum);
            System.out.println("merge result: " + map.get("merge-key")); // 30
        }
        
        /**
         * 性能对比：ConcurrentHashMap vs Hashtable vs Collections.synchronizedMap
         */
        public static void performanceComparison() {
            System.out.println("\n--- Performance Comparison ---");
            
            int threadCount = 8;
            int operationsPerThread = 10000;
            
            // 测试ConcurrentHashMap
            ConcurrentHashMap<Integer, String> concurrentMap = new ConcurrentHashMap<>();
            long concurrentTime = testMapPerformance(concurrentMap, threadCount, operationsPerThread, "ConcurrentHashMap");
            
            // 测试Hashtable
            Hashtable<Integer, String> hashtable = new Hashtable<>();
            long hashtableTime = testMapPerformance(hashtable, threadCount, operationsPerThread, "Hashtable");
            
            // 测试Collections.synchronizedMap
            Map<Integer, String> syncMap = Collections.synchronizedMap(new java.util.HashMap<>());
            long syncMapTime = testMapPerformance(syncMap, threadCount, operationsPerThread, "SynchronizedMap");
            
            System.out.println("\nPerformance Summary:");
            System.out.println("ConcurrentHashMap: " + concurrentTime + " ms");
            System.out.println("Hashtable: " + hashtableTime + " ms");
            System.out.println("SynchronizedMap: " + syncMapTime + " ms");
            
            System.out.println("\nConcurrentHashMap is " + 
                (hashtableTime / (double) concurrentTime) + "x faster than Hashtable");
            System.out.println("ConcurrentHashMap is " + 
                (syncMapTime / (double) concurrentTime) + "x faster than SynchronizedMap");
        }
        
        private static long testMapPerformance(Map<Integer, String> map, int threadCount, 
                int operationsPerThread, String mapType) {
            
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < operationsPerThread; j++) {
                            int key = threadId * operationsPerThread + j;
                            map.put(key, "value-" + key);
                            map.get(key);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            long endTime = System.currentTimeMillis();
            executor.shutdown();
            
            System.out.println(mapType + " final size: " + map.size());
            return endTime - startTime;
        }
    }
    
    /**
     * AtomicInteger的CAS实现分析
     */
    
    static class AtomicAnalysis {
        
        /**
         * AtomicInteger核心方法源码分析：
         * 
         * public final int incrementAndGet() {
         *     return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
         * }
         * 
         * public final int getAndAddInt(Object var1, long var2, int var4) {
         *     int var5;
         *     do {
         *         var5 = this.getIntVolatile(var1, var2);  // 读取当前值
         *     } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));  // CAS操作
         *     return var5;
         * }
         */
        
        public static void demonstrateAtomicOperations() {
            System.out.println("\n=== Atomic Operations Analysis ===");
            
            AtomicInteger atomicCounter = new AtomicInteger(0);
            int threadCount = 10;
            int incrementsPerThread = 1000;
            
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            
            long startTime = System.currentTimeMillis();
            
            // 多线程并发增加计数器
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < incrementsPerThread; j++) {
                            atomicCounter.incrementAndGet(); // 原子性递增
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            try {
                latch.await();
                long endTime = System.currentTimeMillis();
                
                System.out.println("AtomicInteger final value: " + atomicCounter.get());
                System.out.println("Expected value: " + (threadCount * incrementsPerThread));
                System.out.println("Time taken: " + (endTime - startTime) + " ms");
                System.out.println("Correctness: " + 
                    (atomicCounter.get() == threadCount * incrementsPerThread ? "PASS" : "FAIL"));
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            executor.shutdown();
            
            // 演示其他原子操作
            System.out.println("\n--- Other Atomic Operations ---");
            
            AtomicInteger atomic = new AtomicInteger(10);
            
            // compareAndSet - CAS操作
            boolean success = atomic.compareAndSet(10, 20);
            System.out.println("CAS (10 -> 20): " + success + ", value: " + atomic.get());
            
            success = atomic.compareAndSet(10, 30); // 期望值不匹配
            System.out.println("CAS (10 -> 30): " + success + ", value: " + atomic.get());
            
            // getAndUpdate - 原子性更新
            int oldValue = atomic.getAndUpdate(x -> x * 2);
            System.out.println("getAndUpdate (*2): old=" + oldValue + ", new=" + atomic.get());
            
            // updateAndGet - 原子性更新
            int newValue = atomic.updateAndGet(x -> x + 5);
            System.out.println("updateAndGet (+5): new=" + newValue);
            
            // accumulateAndGet - 原子性累积
            int result = atomic.accumulateAndGet(10, Integer::sum);
            System.out.println("accumulateAndGet (+10): result=" + result);
        }
        
        /**
         * 性能对比：AtomicInteger vs synchronized
         */
        public static void performanceComparison() {
            System.out.println("\n--- Atomic vs Synchronized Performance ---");
            
            int threadCount = 8;
            int operationsPerThread = 100000;
            
            // 测试AtomicInteger
            AtomicInteger atomicCounter = new AtomicInteger(0);
            long atomicTime = testAtomicPerformance(atomicCounter, threadCount, operationsPerThread);
            
            // 测试synchronized
            SynchronizedCounter syncCounter = new SynchronizedCounter();
            long syncTime = testSynchronizedPerformance(syncCounter, threadCount, operationsPerThread);
            
            System.out.println("AtomicInteger time: " + atomicTime + " ms, final value: " + atomicCounter.get());
            System.out.println("Synchronized time: " + syncTime + " ms, final value: " + syncCounter.getValue());
            System.out.println("AtomicInteger is " + (syncTime / (double) atomicTime) + "x faster");
        }
        
        private static long testAtomicPerformance(AtomicInteger counter, int threadCount, int operationsPerThread) {
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < operationsPerThread; j++) {
                            counter.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            long endTime = System.currentTimeMillis();
            executor.shutdown();
            
            return endTime - startTime;
        }
        
        private static long testSynchronizedPerformance(SynchronizedCounter counter, int threadCount, int operationsPerThread) {
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < operationsPerThread; j++) {
                            counter.increment();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            long endTime = System.currentTimeMillis();
            executor.shutdown();
            
            return endTime - startTime;
        }
        
        static class SynchronizedCounter {
            private int value = 0;
            
            public synchronized void increment() {
                value++;
            }
            
            public synchronized int getValue() {
                return value;
            }
        }
    }
    
    public static void main(String[] args) {
        // 1. ThreadLocal分析
        ThreadLocalAnalysis.demonstrateThreadLocalBehavior();
        ThreadLocalAnalysis.MemoryLeakDemo.demonstrateMemoryLeak();
        ThreadLocalAnalysis.demonstrateBestPractices();
        
        // 2. ConcurrentHashMap分析
        ConcurrentHashMapAnalysis.analyzeConcurrentHashMapStructure();
        ConcurrentHashMapAnalysis.performanceComparison();
        
        // 3. Atomic类分析
        AtomicAnalysis.demonstrateAtomicOperations();
        AtomicAnalysis.performanceComparison();
    }
}