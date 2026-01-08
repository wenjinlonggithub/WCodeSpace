package com.architecture.jvm.memory;

import java.lang.management.*;
import java.util.List;

/**
 * JVM内存区域详细分析
 * 
 * 深入解析JVM内存结构的各个区域
 */
public class MemoryAreaAnalysis {
    
    public static void main(String[] args) {
        MemoryAreaAnalysis analysis = new MemoryAreaAnalysis();
        
        System.out.println("=== JVM内存区域分析 ===\n");
        
        // 1. 分析堆内存区域
        analysis.analyzeHeapMemory();
        
        // 2. 分析非堆内存区域
        analysis.analyzeNonHeapMemory();
        
        // 3. 分析各个内存池
        analysis.analyzeMemoryPools();
        
        // 4. 分析垃圾收集器信息
        analysis.analyzeGarbageCollectors();
        
        // 5. 演示内存分配
        analysis.demonstrateMemoryAllocation();
    }
    
    /**
     * 分析堆内存区域
     */
    private void analyzeHeapMemory() {
        System.out.println("=== 堆内存区域分析 ===");
        
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        
        System.out.println("堆内存区域包含:");
        System.out.println("  • 年轻代 (Young Generation)");
        System.out.println("    - Eden区: 新对象分配区域");
        System.out.println("    - Survivor0区: GC存活对象");
        System.out.println("    - Survivor1区: GC存活对象");
        System.out.println("  • 老年代 (Old Generation)");
        System.out.println("    - 长期存活对象存储区域");
        
        System.out.println("\n当前堆内存使用情况:");
        printMemoryUsage("堆内存", heapUsage);
        
        // 堆内存特点
        System.out.println("堆内存特点:");
        System.out.println("  ✓ 所有线程共享");
        System.out.println("  ✓ 存储对象实例和数组");
        System.out.println("  ✓ 是垃圾回收的主要目标");
        System.out.println("  ✓ 可以通过-Xms和-Xmx参数调整\n");
    }
    
    /**
     * 分析非堆内存区域
     */
    private void analyzeNonHeapMemory() {
        System.out.println("=== 非堆内存区域分析 ===");
        
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        
        System.out.println("非堆内存区域包含:");
        System.out.println("  • 方法区 (Method Area)");
        System.out.println("    - 存储类信息、常量池、方法字节码");
        System.out.println("  • 直接内存 (Direct Memory)");
        System.out.println("    - NIO操作使用的堆外内存");
        System.out.println("  • 代码缓存 (Code Cache)");
        System.out.println("    - JIT编译后的本地代码缓存");
        
        System.out.println("\n当前非堆内存使用情况:");
        printMemoryUsage("非堆内存", nonHeapUsage);
        
        // 非堆内存特点
        System.out.println("非堆内存特点:");
        System.out.println("  ✓ 方法区被所有线程共享");
        System.out.println("  ✓ 存储类的元数据信息");
        System.out.println("  ✓ 包含运行时常量池");
        System.out.println("  ✓ JDK8后方法区实现改为元空间(Metaspace)\n");
    }
    
    /**
     * 分析各个内存池
     */
    private void analyzeMemoryPools() {
        System.out.println("=== 内存池详细分析 ===");
        
        List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
        
        for (MemoryPoolMXBean pool : memoryPools) {
            System.out.println("内存池: " + pool.getName());
            System.out.println("  类型: " + pool.getType());
            
            MemoryUsage usage = pool.getUsage();
            if (usage != null) {
                printMemoryUsage("  当前使用", usage);
            }
            
            MemoryUsage peakUsage = pool.getPeakUsage();
            if (peakUsage != null) {
                printMemoryUsage("  峰值使用", peakUsage);
            }
            
            // 获取内存管理器信息
            String[] managers = pool.getMemoryManagerNames();
            System.out.println("  管理器: " + String.join(", ", managers));
            
            System.out.println();
        }
    }
    
    /**
     * 分析垃圾收集器信息
     */
    private void analyzeGarbageCollectors() {
        System.out.println("=== 垃圾收集器分析 ===");
        
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            System.out.println("垃圾收集器: " + gcBean.getName());
            System.out.println("  收集次数: " + gcBean.getCollectionCount());
            System.out.println("  收集时间: " + gcBean.getCollectionTime() + " ms");
            
            String[] memoryPoolNames = gcBean.getMemoryPoolNames();
            System.out.println("  管理的内存池: " + String.join(", ", memoryPoolNames));
            System.out.println("  是否有效: " + gcBean.isValid());
            System.out.println();
        }
    }
    
    /**
     * 演示内存分配
     */
    private void demonstrateMemoryAllocation() {
        System.out.println("=== 内存分配演示 ===");
        
        // 演示Eden区分配
        System.out.println("1. Eden区分配演示");
        demonstrateEdenAllocation();
        
        // 演示大对象分配
        System.out.println("2. 大对象分配演示");
        demonstrateLargeObjectAllocation();
        
        // 演示方法区分配
        System.out.println("3. 方法区分配演示");
        demonstrateMethodAreaAllocation();
    }
    
    /**
     * 演示Eden区分配
     */
    private void demonstrateEdenAllocation() {
        long before = getCurrentHeapUsed();
        
        // 分配大量小对象到Eden区
        Object[] smallObjects = new Object[10000];
        for (int i = 0; i < smallObjects.length; i++) {
            smallObjects[i] = new SmallObject(i);
        }
        
        long after = getCurrentHeapUsed();
        System.out.println("  分配10000个小对象");
        System.out.println("  内存增长: " + (after - before) / 1024 + " KB");
    }
    
    /**
     * 演示大对象分配
     */
    private void demonstrateLargeObjectAllocation() {
        long before = getCurrentHeapUsed();
        
        // 分配大对象，可能直接进入老年代
        byte[][] largeObjects = new byte[5][];
        for (int i = 0; i < largeObjects.length; i++) {
            largeObjects[i] = new byte[2 * 1024 * 1024]; // 2MB
        }
        
        long after = getCurrentHeapUsed();
        System.out.println("  分配5个大对象(2MB each)");
        System.out.println("  内存增长: " + (after - before) / 1024 / 1024 + " MB");
    }
    
    /**
     * 演示方法区分配
     */
    private void demonstrateMethodAreaAllocation() {
        long before = getCurrentNonHeapUsed();
        
        // 加载一些类增加方法区使用
        try {
            Class.forName("java.util.concurrent.ConcurrentHashMap");
            Class.forName("java.util.concurrent.ThreadPoolExecutor");
            Class.forName("java.util.concurrent.CompletableFuture");
        } catch (ClassNotFoundException e) {
            System.out.println("类加载失败: " + e.getMessage());
        }
        
        long after = getCurrentNonHeapUsed();
        System.out.println("  加载几个复杂类");
        System.out.println("  方法区增长: " + (after - before) / 1024 + " KB");
    }
    
    /**
     * 获取当前堆内存使用量
     */
    private long getCurrentHeapUsed() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }
    
    /**
     * 获取当前非堆内存使用量
     */
    private long getCurrentNonHeapUsed() {
        return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
    }
    
    /**
     * 打印内存使用信息
     */
    private void printMemoryUsage(String label, MemoryUsage usage) {
        System.out.printf("%s:\n", label);
        System.out.printf("    初始: %d MB\n", usage.getInit() / 1024 / 1024);
        System.out.printf("    已用: %d MB\n", usage.getUsed() / 1024 / 1024);
        System.out.printf("    已提交: %d MB\n", usage.getCommitted() / 1024 / 1024);
        if (usage.getMax() > 0) {
            System.out.printf("    最大: %d MB\n", usage.getMax() / 1024 / 1024);
            System.out.printf("    使用率: %.2f%%\n", 
                    (double) usage.getUsed() / usage.getMax() * 100);
        }
    }
    
    /**
     * 小对象类
     */
    static class SmallObject {
        private final int id;
        private final String name;
        
        public SmallObject(int id) {
            this.id = id;
            this.name = "Object_" + id;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
    }
}