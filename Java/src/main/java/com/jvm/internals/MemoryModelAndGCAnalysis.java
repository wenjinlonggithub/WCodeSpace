package com.jvm.internals;

import java.lang.management.*;
import java.util.*;
import java.util.concurrent.*;
import java.lang.ref.*;

/**
 * Java内存模型和垃圾回收深度分析
 * 
 * 详细内容：
 * 1. Java内存模型 (JMM) 详解
 * 2. 垃圾回收算法原理
 * 3. 不同垃圾回收器对比
 * 4. 内存泄漏检测和分析
 * 5. 引用类型详解
 * 6. 内存调优实践
 * 7. GC日志分析
 */

public class MemoryModelAndGCAnalysis {
    
    /**
     * Java内存模型 (JMM) 分析
     * 
     * 核心概念：
     * 1. 主内存和工作内存
     * 2. 内存间交互操作
     * 3. volatile关键字
     * 4. synchronized同步
     * 5. happens-before规则
     */
    
    static class JavaMemoryModelAnalysis {
        
        private static volatile boolean volatileFlag = false;
        private static boolean normalFlag = false;
        private static int sharedVariable = 0;
        
        /**
         * volatile关键字演示
         */
        public static void demonstrateVolatile() {
            System.out.println("=== Java Memory Model - Volatile Analysis ===");
            
            // 1. volatile可见性演示
            System.out.println("\n--- Volatile Visibility Demo ---");
            
            Thread writerThread = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    System.out.println("Writer: Setting volatile flag to true");
                    volatileFlag = true;
                    
                    Thread.sleep(500);
                    System.out.println("Writer: Setting normal flag to true");
                    normalFlag = true;
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            Thread readerThread = new Thread(() -> {
                while (!volatileFlag) {
                    // 等待volatile标志
                }
                System.out.println("Reader: Volatile flag is now true");
                
                // 检查普通变量的可见性
                if (normalFlag) {
                    System.out.println("Reader: Normal flag is also true (happens-before)");
                } else {
                    System.out.println("Reader: Normal flag is still false");
                }
            });
            
            writerThread.start();
            readerThread.start();
            
            try {
                writerThread.join();
                readerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 2. volatile禁止重排序演示
            System.out.println("\n--- Volatile Ordering Demo ---");
            demonstrateVolatileOrdering();
        }
        
        private static void demonstrateVolatileOrdering() {
            final int ITERATIONS = 1000000;
            
            for (int test = 0; test < 5; test++) {
                VolatileOrderingTest orderingTest = new VolatileOrderingTest();
                
                Thread thread1 = new Thread(() -> {
                    for (int i = 0; i < ITERATIONS; i++) {
                        orderingTest.writer();
                    }
                });
                
                Thread thread2 = new Thread(() -> {
                    for (int i = 0; i < ITERATIONS; i++) {
                        orderingTest.reader();
                    }
                });
                
                thread1.start();
                thread2.start();
                
                try {
                    thread1.join();
                    thread2.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                System.out.println("Test " + (test + 1) + " - Reordering detected: " + 
                    orderingTest.getReorderingCount() + " times");
            }
        }
        
        /**
         * synchronized内存语义演示
         */
        public static void demonstrateSynchronized() {
            System.out.println("\n--- Synchronized Memory Semantics ---");
            
            SynchronizedDemo demo = new SynchronizedDemo();
            CountDownLatch latch = new CountDownLatch(2);
            
            // 写线程
            Thread writer = new Thread(() -> {
                try {
                    demo.synchronizedWrite(42);
                    System.out.println("Writer: Data written");
                } finally {
                    latch.countDown();
                }
            });
            
            // 读线程
            Thread reader = new Thread(() -> {
                try {
                    Thread.sleep(100); // 确保写操作先执行
                    int value = demo.synchronizedRead();
                    System.out.println("Reader: Data read = " + value);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
            
            writer.start();
            reader.start();
            
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    // volatile重排序测试类
    static class VolatileOrderingTest {
        private int a = 0;
        private volatile boolean flag = false;
        private int reorderingCount = 0;
        
        public void writer() {
            a = 1;          // 1
            flag = true;    // 2 (volatile写)
        }
        
        public void reader() {
            if (flag) {     // 3 (volatile读)
                if (a == 0) { // 4
                    reorderingCount++; // 如果发生重排序，a可能还是0
                }
            }
        }
        
        public int getReorderingCount() {
            return reorderingCount;
        }
    }
    
    // synchronized演示类
    static class SynchronizedDemo {
        private int data = 0;
        private boolean ready = false;
        private final Object lock = new Object();
        
        public void synchronizedWrite(int value) {
            synchronized (lock) {
                data = value;
                ready = true;
            }
        }
        
        public int synchronizedRead() {
            synchronized (lock) {
                if (ready) {
                    return data;
                }
                return -1;
            }
        }
    }
    
    /**
     * 垃圾回收分析
     */
    
    static class GarbageCollectionAnalysis {
        
        /**
         * GC信息监控
         */
        public static void analyzeGCBehavior() {
            System.out.println("\n=== Garbage Collection Analysis ===");
            
            // 获取GC MXBeans
            List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            
            System.out.println("\n--- Available Garbage Collectors ---");
            for (GarbageCollectorMXBean gcBean : gcBeans) {
                System.out.println("GC Name: " + gcBean.getName());
                System.out.println("  Memory Pool Names: " + Arrays.toString(gcBean.getMemoryPoolNames()));
                System.out.println("  Collection Count: " + gcBean.getCollectionCount());
                System.out.println("  Collection Time: " + gcBean.getCollectionTime() + " ms");
                System.out.println();
            }
            
            // 记录GC前的状态
            long initialCollectionCount = getTotalCollectionCount(gcBeans);
            long initialCollectionTime = getTotalCollectionTime(gcBeans);
            MemoryUsage initialHeapUsage = memoryBean.getHeapMemoryUsage();
            
            System.out.println("--- Triggering Garbage Collection ---");
            System.out.println("Initial heap usage: " + formatBytes(initialHeapUsage.getUsed()));
            
            // 创建大量对象触发GC
            List<byte[]> objects = new ArrayList<>();
            try {
                for (int i = 0; i < 10000; i++) {
                    objects.add(new byte[1024]); // 1KB per object
                    
                    if (i % 2000 == 1999) {
                        MemoryUsage currentUsage = memoryBean.getHeapMemoryUsage();
                        System.out.println("After " + (i + 1) + " objects: " + 
                            formatBytes(currentUsage.getUsed()));
                    }
                }
                
                // 手动触发GC
                System.gc();
                Thread.sleep(100); // 等待GC完成
                
                // 清理一半对象
                for (int i = 0; i < objects.size() / 2; i++) {
                    objects.set(i, null);
                }
                
                System.gc();
                Thread.sleep(100);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 记录GC后的状态
            long finalCollectionCount = getTotalCollectionCount(gcBeans);
            long finalCollectionTime = getTotalCollectionTime(gcBeans);
            MemoryUsage finalHeapUsage = memoryBean.getHeapMemoryUsage();
            
            System.out.println("\n--- GC Statistics ---");
            System.out.println("Total collections: " + (finalCollectionCount - initialCollectionCount));
            System.out.println("Total collection time: " + (finalCollectionTime - initialCollectionTime) + " ms");
            System.out.println("Final heap usage: " + formatBytes(finalHeapUsage.getUsed()));
            System.out.println("Memory freed: " + 
                formatBytes(initialHeapUsage.getUsed() - finalHeapUsage.getUsed()));
        }
        
        /**
         * 不同代的GC行为分析
         */
        public static void analyzeGenerationalGC() {
            System.out.println("\n--- Generational GC Analysis ---");
            
            List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
            
            // 记录初始状态
            Map<String, Long> initialCounts = new HashMap<>();
            Map<String, Long> initialTimes = new HashMap<>();
            
            for (GarbageCollectorMXBean gcBean : gcBeans) {
                initialCounts.put(gcBean.getName(), gcBean.getCollectionCount());
                initialTimes.put(gcBean.getName(), gcBean.getCollectionTime());
            }
            
            // 创建短生命周期对象（应该在年轻代回收）
            System.out.println("Creating short-lived objects...");
            for (int i = 0; i < 100000; i++) {
                String temp = "Short lived object " + i;
                temp.hashCode(); // 使用对象
            }
            
            // 创建长生命周期对象（可能进入老年代）
            System.out.println("Creating long-lived objects...");
            List<String> longLived = new ArrayList<>();
            for (int i = 0; i < 50000; i++) {
                longLived.add("Long lived object " + i);
            }
            
            // 触发GC
            System.gc();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 分析GC结果
            System.out.println("\nGC Activity by Collector:");
            for (GarbageCollectorMXBean gcBean : gcBeans) {
                String name = gcBean.getName();
                long countDiff = gcBean.getCollectionCount() - initialCounts.get(name);
                long timeDiff = gcBean.getCollectionTime() - initialTimes.get(name);
                
                if (countDiff > 0) {
                    System.out.println(name + ":");
                    System.out.println("  Collections: " + countDiff);
                    System.out.println("  Time: " + timeDiff + " ms");
                    System.out.println("  Avg time per collection: " + 
                        (timeDiff / (double) countDiff) + " ms");
                }
            }
        }
        
        private static long getTotalCollectionCount(List<GarbageCollectorMXBean> gcBeans) {
            return gcBeans.stream().mapToLong(GarbageCollectorMXBean::getCollectionCount).sum();
        }
        
        private static long getTotalCollectionTime(List<GarbageCollectorMXBean> gcBeans) {
            return gcBeans.stream().mapToLong(GarbageCollectorMXBean::getCollectionTime).sum();
        }
        
        private static String formatBytes(long bytes) {
            if (bytes < 0) return "N/A";
            
            String[] units = {"B", "KB", "MB", "GB"};
            int unitIndex = 0;
            double size = bytes;
            
            while (size >= 1024 && unitIndex < units.length - 1) {
                size /= 1024;
                unitIndex++;
            }
            
            return String.format("%.2f %s", size, units[unitIndex]);
        }
    }
    
    /**
     * 引用类型分析
     */
    
    static class ReferenceTypesAnalysis {
        
        /**
         * 四种引用类型演示
         */
        public static void demonstrateReferenceTypes() {
            System.out.println("\n=== Reference Types Analysis ===");
            
            // 1. 强引用 (Strong Reference)
            System.out.println("\n--- Strong Reference ---");
            Object strongRef = new Object();
            System.out.println("Strong reference created: " + strongRef);
            
            // 2. 软引用 (Soft Reference)
            System.out.println("\n--- Soft Reference ---");
            SoftReference<byte[]> softRef = new SoftReference<>(new byte[1024 * 1024]); // 1MB
            System.out.println("Soft reference created, object exists: " + (softRef.get() != null));
            
            // 3. 弱引用 (Weak Reference)
            System.out.println("\n--- Weak Reference ---");
            WeakReference<Object> weakRef = new WeakReference<>(new Object());
            System.out.println("Weak reference created, object exists: " + (weakRef.get() != null));
            
            // 4. 虚引用 (Phantom Reference)
            System.out.println("\n--- Phantom Reference ---");
            ReferenceQueue<Object> queue = new ReferenceQueue<>();
            Object phantomTarget = new Object();
            PhantomReference<Object> phantomRef = new PhantomReference<>(phantomTarget, queue);
            System.out.println("Phantom reference created");
            
            // 触发GC观察引用行为
            System.out.println("\nTriggering GC to observe reference behavior...");
            
            // 移除强引用
            phantomTarget = null;
            
            // 多次GC
            for (int i = 0; i < 3; i++) {
                System.gc();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                System.out.println("After GC " + (i + 1) + ":");
                System.out.println("  Soft reference object exists: " + (softRef.get() != null));
                System.out.println("  Weak reference object exists: " + (weakRef.get() != null));
                System.out.println("  Phantom reference in queue: " + (queue.poll() != null));
            }
        }
        
        /**
         * WeakHashMap演示
         */
        public static void demonstrateWeakHashMap() {
            System.out.println("\n--- WeakHashMap Demo ---");
            
            WeakHashMap<Object, String> weakMap = new WeakHashMap<>();
            HashMap<Object, String> strongMap = new HashMap<>();
            
            // 创建键对象
            Object key1 = new Object();
            Object key2 = new Object();
            Object key3 = new Object();
            
            // 添加到两个Map中
            weakMap.put(key1, "Value1");
            weakMap.put(key2, "Value2");
            weakMap.put(key3, "Value3");
            
            strongMap.put(key1, "Value1");
            strongMap.put(key2, "Value2");
            strongMap.put(key3, "Value3");
            
            System.out.println("Initial sizes - WeakHashMap: " + weakMap.size() + 
                ", HashMap: " + strongMap.size());
            
            // 移除部分键的强引用
            key1 = null;
            key2 = null;
            // key3保持强引用
            
            // 触发GC
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            System.out.println("After GC - WeakHashMap: " + weakMap.size() + 
                ", HashMap: " + strongMap.size());
            
            // 再次GC确保清理完成
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            System.out.println("Final sizes - WeakHashMap: " + weakMap.size() + 
                ", HashMap: " + strongMap.size());
        }
        
        /**
         * 内存泄漏检测演示
         */
        public static void demonstrateMemoryLeak() {
            System.out.println("\n--- Memory Leak Detection ---");
            
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            
            // 模拟内存泄漏 - 静态集合持有对象引用
            System.out.println("Simulating memory leak...");
            
            long initialMemory = memoryBean.getHeapMemoryUsage().getUsed();
            System.out.println("Initial memory: " + formatBytes(initialMemory));
            
            // 创建"泄漏"对象
            for (int i = 0; i < 10000; i++) {
                LeakyClass.addObject(new byte[1024]); // 1KB per object
            }
            
            long afterLeakMemory = memoryBean.getHeapMemoryUsage().getUsed();
            System.out.println("Memory after leak: " + formatBytes(afterLeakMemory));
            System.out.println("Memory increase: " + formatBytes(afterLeakMemory - initialMemory));
            
            // 尝试GC，但泄漏的对象不会被回收
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            long afterGCMemory = memoryBean.getHeapMemoryUsage().getUsed();
            System.out.println("Memory after GC: " + formatBytes(afterGCMemory));
            System.out.println("Memory still leaked: " + formatBytes(afterGCMemory - initialMemory));
            
            // 清理泄漏
            LeakyClass.clearObjects();
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            long finalMemory = memoryBean.getHeapMemoryUsage().getUsed();
            System.out.println("Memory after cleanup: " + formatBytes(finalMemory));
        }
        
        private static String formatBytes(long bytes) {
            if (bytes < 0) return "N/A";
            
            String[] units = {"B", "KB", "MB", "GB"};
            int unitIndex = 0;
            double size = bytes;
            
            while (size >= 1024 && unitIndex < units.length - 1) {
                size /= 1024;
                unitIndex++;
            }
            
            return String.format("%.2f %s", size, units[unitIndex]);
        }
    }
    
    // 内存泄漏演示类
    static class LeakyClass {
        private static final List<Object> leakedObjects = new ArrayList<>();
        
        public static void addObject(Object obj) {
            leakedObjects.add(obj);
        }
        
        public static void clearObjects() {
            leakedObjects.clear();
        }
        
        public static int getLeakedObjectCount() {
            return leakedObjects.size();
        }
    }
    
    public static void main(String[] args) {
        JavaMemoryModelAnalysis.demonstrateVolatile();
        JavaMemoryModelAnalysis.demonstrateSynchronized();
        
        GarbageCollectionAnalysis.analyzeGCBehavior();
        GarbageCollectionAnalysis.analyzeGenerationalGC();
        
        ReferenceTypesAnalysis.demonstrateReferenceTypes();
        ReferenceTypesAnalysis.demonstrateWeakHashMap();
        ReferenceTypesAnalysis.demonstrateMemoryLeak();
    }
}