package com.architecture.jvm.tuning;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * JVM性能调优实战示例
 * 
 * 演示各种调优技术和最佳实践
 */
public class PerformanceTuningExample {
    
    private static final AtomicLong allocatedObjects = new AtomicLong(0);
    
    public static void main(String[] args) {
        PerformanceTuningExample example = new PerformanceTuningExample();
        
        System.out.println("=== JVM性能调优实战 ===\n");
        
        // 1. 内存分配优化
        example.memoryAllocationOptimization();
        
        // 2. GC调优实践
        example.gcTuningPractice();
        
        // 3. 对象生命周期优化
        example.objectLifecycleOptimization();
        
        // 4. 内存泄漏检测和预防
        example.memoryLeakDetection();
        
        // 5. JIT编译优化
        example.jitCompilationOptimization();
        
        // 6. 调优策略总结
        example.tuningStrategySummary();
    }
    
    /**
     * 内存分配优化
     */
    private void memoryAllocationOptimization() {
        System.out.println("=== 内存分配优化 ===");
        
        // 对比不同的内存分配策略
        System.out.println("1. 对象池 vs 频繁创建对比:");
        
        // 方式1: 频繁创建对象
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            ExpensiveObject obj = new ExpensiveObject(i);
            obj.doWork();
        }
        long frequentCreationTime = System.currentTimeMillis() - startTime;
        System.out.println("   频繁创建耗时: " + frequentCreationTime + " ms");
        
        // 方式2: 使用对象池
        ObjectPool<ExpensiveObject> pool = new ObjectPool<>(
                () -> new ExpensiveObject(0), 10);
        
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            ExpensiveObject obj = pool.borrowObject();
            try {
                obj.setId(i);
                obj.doWork();
            } finally {
                pool.returnObject(obj);
            }
        }
        long poolingTime = System.currentTimeMillis() - startTime;
        System.out.println("   对象池耗时: " + poolingTime + " ms");
        
        double improvement = ((double)(frequentCreationTime - poolingTime) / frequentCreationTime) * 100;
        System.out.printf("   性能提升: %.2f%%\n", improvement);
        
        // StringBuilder vs String拼接
        System.out.println("\n2. StringBuilder vs String拼接对比:");
        
        // String拼接
        startTime = System.currentTimeMillis();
        String result = "";
        for (int i = 0; i < 10000; i++) {
            result += "item" + i + ",";
        }
        long stringConcatTime = System.currentTimeMillis() - startTime;
        System.out.println("   String拼接耗时: " + stringConcatTime + " ms");
        
        // StringBuilder
        startTime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("item").append(i).append(",");
        }
        String result2 = sb.toString();
        long stringBuilderTime = System.currentTimeMillis() - startTime;
        System.out.println("   StringBuilder耗时: " + stringBuilderTime + " ms");
        
        improvement = ((double)(stringConcatTime - stringBuilderTime) / stringConcatTime) * 100;
        System.out.printf("   性能提升: %.2f%%\n", improvement);
        
        System.out.println();
    }
    
    /**
     * GC调优实践
     */
    private void gcTuningPractice() {
        System.out.println("=== GC调优实践 ===");
        
        // 记录GC前状态
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        long initialGCCount = 0;
        long initialGCTime = 0;
        
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            initialGCCount += gcBean.getCollectionCount();
            initialGCTime += gcBean.getCollectionTime();
        }
        
        System.out.println("1. 分代垃圾回收优化演示:");
        System.out.println("   初始GC状态 - 次数: " + initialGCCount + ", 时间: " + initialGCTime + "ms");
        
        // 创建不同生命周期的对象
        List<Object> longLivedObjects = new ArrayList<>();
        
        // 短生命周期对象（应该很快被回收）
        for (int i = 0; i < 50000; i++) {
            ShortLivedObject shortObj = new ShortLivedObject(i);
            shortObj.doSomething();
            
            // 偶尔创建长生命周期对象
            if (i % 1000 == 0) {
                longLivedObjects.add(new LongLivedObject(i));
            }
        }
        
        // 记录GC后状态
        long finalGCCount = 0;
        long finalGCTime = 0;
        
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            finalGCCount += gcBean.getCollectionCount();
            finalGCTime += gcBean.getCollectionTime();
        }
        
        System.out.println("   最终GC状态 - 次数: " + finalGCCount + ", 时间: " + finalGCTime + "ms");
        System.out.println("   触发GC次数: " + (finalGCCount - initialGCCount));
        System.out.println("   GC总耗时: " + (finalGCTime - initialGCTime) + "ms");
        
        // 分析内存使用
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        
        System.out.printf("   堆内存使用: %d MB / %d MB (%.2f%%)\n",
                heapUsage.getUsed() / 1024 / 1024,
                heapUsage.getMax() / 1024 / 1024,
                (double) heapUsage.getUsed() / heapUsage.getMax() * 100);
        
        System.out.println("   长生命周期对象数量: " + longLivedObjects.size());
        
        System.out.println("\n2. GC调优建议:");
        System.out.println("   ✓ 减少短期对象的创建");
        System.out.println("   ✓ 合理设置年轻代大小");
        System.out.println("   ✓ 选择合适的垃圾收集器");
        System.out.println("   ✓ 调整GC参数匹配应用特性");
        
        System.out.println();
    }
    
    /**
     * 对象生命周期优化
     */
    private void objectLifecycleOptimization() {
        System.out.println("=== 对象生命周期优化 ===");
        
        System.out.println("1. 对象创建优化:");
        
        // 延迟初始化示例
        LazyInitializedClass lazyObj = new LazyInitializedClass();
        System.out.println("   延迟初始化对象创建完成");
        
        // 只有在需要时才初始化昂贵的资源
        String expensiveResource = lazyObj.getExpensiveResource();
        System.out.println("   昂贵资源已初始化: " + expensiveResource.length() + " 字符");
        
        System.out.println("\n2. 对象复用优化:");
        
        // 使用缓存避免重复创建
        CachedResourceManager cacheManager = new CachedResourceManager();
        
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            String key = "resource" + (i % 100); // 模拟100种不同资源
            String resource = cacheManager.getResource(key);
        }
        long cachedTime = System.currentTimeMillis() - startTime;
        System.out.println("   缓存方式耗时: " + cachedTime + " ms");
        
        // 不使用缓存的对比
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            String key = "resource" + (i % 100);
            // 模拟每次都创建新资源
            String resource = createExpensiveResource(key);
        }
        long noCacheTime = System.currentTimeMillis() - startTime;
        System.out.println("   非缓存方式耗时: " + noCacheTime + " ms");
        
        double cacheImprovement = ((double)(noCacheTime - cachedTime) / noCacheTime) * 100;
        System.out.printf("   缓存性能提升: %.2f%%\n", cacheImprovement);
        
        System.out.println("\n3. 资源管理优化:");
        System.out.println("   ✓ 及时关闭资源（try-with-resources）");
        System.out.println("   ✓ 使用软引用和弱引用管理缓存");
        System.out.println("   ✓ 避免在循环中创建大对象");
        System.out.println("   ✓ 合理使用static修饰符");
        
        System.out.println();
    }
    
    /**
     * 内存泄漏检测和预防
     */
    private void memoryLeakDetection() {
        System.out.println("=== 内存泄漏检测和预防 ===");
        
        System.out.println("1. 常见内存泄漏场景演示:");
        
        // 场景1: 集合类内存泄漏（已修复版本）
        MemoryLeakDemo leakDemo = new MemoryLeakDemo();
        System.out.println("   集合泄漏预防: 使用WeakHashMap或及时清理");
        
        // 场景2: 监听器泄漏预防
        EventPublisher publisher = new EventPublisher();
        EventListener listener = new EventListener();
        
        System.out.println("   监听器泄漏预防: 及时移除监听器");
        publisher.addListener(listener);
        // 重要：使用完后移除监听器
        publisher.removeListener(listener);
        
        System.out.println("\n2. 内存泄漏检测技巧:");
        System.out.println("   ✓ 定期检查堆内存增长趋势");
        System.out.println("   ✓ 使用内存分析工具（MAT、JProfiler）");
        System.out.println("   ✓ 监控长期存活的对象");
        System.out.println("   ✓ 检查集合类的大小变化");
        
        // 演示内存使用监控
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapBefore = memoryBean.getHeapMemoryUsage();
        
        // 创建一些对象
        List<byte[]> memoryConsumer = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            memoryConsumer.add(new byte[1024]); // 1KB each
        }
        
        MemoryUsage heapAfter = memoryBean.getHeapMemoryUsage();
        long memoryIncrease = heapAfter.getUsed() - heapBefore.getUsed();
        System.out.printf("   内存增长: %d KB\n", memoryIncrease / 1024);
        
        System.out.println("\n3. 预防措施:");
        System.out.println("   ✓ 使用try-with-resources自动关闭资源");
        System.out.println("   ✓ 避免在长期存活对象中持有短期对象引用");
        System.out.println("   ✓ 及时清理集合和缓存");
        System.out.println("   ✓ 使用弱引用管理监听器和回调");
        
        System.out.println();
    }
    
    /**
     * JIT编译优化
     */
    private void jitCompilationOptimization() {
        System.out.println("=== JIT编译优化 ===");
        
        System.out.println("1. 热点方法优化演示:");
        
        // 方法预热
        HotSpotMethodExample hotSpot = new HotSpotMethodExample();
        
        // 第一次执行（解释执行）
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            hotSpot.computeIntensive(i);
        }
        long interpretedTime = System.currentTimeMillis() - startTime;
        System.out.println("   第一次执行（解释执行）: " + interpretedTime + " ms");
        
        // 触发JIT编译的更多执行
        for (int i = 0; i < 50000; i++) {
            hotSpot.computeIntensive(i);
        }
        
        // 第二次执行（编译后）
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            hotSpot.computeIntensive(i);
        }
        long compiledTime = System.currentTimeMillis() - startTime;
        System.out.println("   第二次执行（编译后）: " + compiledTime + " ms");
        
        if (interpretedTime > 0) {
            double jitImprovement = ((double)(interpretedTime - compiledTime) / interpretedTime) * 100;
            System.out.printf("   JIT编译性能提升: %.2f%%\n", jitImprovement);
        }
        
        System.out.println("\n2. 方法内联优化:");
        
        // 演示方法内联的效果
        MethodInliningExample inlining = new MethodInliningExample();
        
        startTime = System.currentTimeMillis();
        long result = 0;
        for (int i = 0; i < 100000; i++) {
            result += inlining.inlinableMethod(i);
        }
        long inlineTime = System.currentTimeMillis() - startTime;
        System.out.println("   内联方法调用耗时: " + inlineTime + " ms");
        
        startTime = System.currentTimeMillis();
        result = 0;
        for (int i = 0; i < 100000; i++) {
            result += inlining.nonInlinableMethod(i);
        }
        long nonInlineTime = System.currentTimeMillis() - startTime;
        System.out.println("   非内联方法调用耗时: " + nonInlineTime + " ms");
        
        System.out.println("\n3. JIT优化技巧:");
        System.out.println("   ✓ 让热点代码充分预热");
        System.out.println("   ✓ 避免在热点方法中使用反射");
        System.out.println("   ✓ 保持方法简单以便内联");
        System.out.println("   ✓ 使用final修饰符帮助优化");
        
        System.out.println();
    }
    
    /**
     * 调优策略总结
     */
    private void tuningStrategySummary() {
        System.out.println("=== 调优策略总结 ===");
        
        System.out.println("1. 内存调优策略:");
        System.out.println("   ✓ 合理设置堆大小（-Xms, -Xmx）");
        System.out.println("   ✓ 调整年轻代和老年代比例（-XX:NewRatio）");
        System.out.println("   ✓ 优化Survivor区大小（-XX:SurvivorRatio）");
        System.out.println("   ✓ 设置大对象阈值（-XX:PretenureSizeThreshold）");
        
        System.out.println("\n2. GC调优策略:");
        System.out.println("   ✓ 选择合适的垃圾收集器");
        System.out.println("     - Serial GC: 单核CPU，小应用");
        System.out.println("     - Parallel GC: 多核CPU，高吞吐量");
        System.out.println("     - G1 GC: 大堆内存，低延迟");
        System.out.println("     - ZGC/Shenandoah: 超大堆，极低延迟");
        
        System.out.println("\n3. 应用层优化:");
        System.out.println("   ✓ 对象池化技术");
        System.out.println("   ✓ 字符串优化（StringBuilder）");
        System.out.println("   ✓ 集合类选择优化");
        System.out.println("   ✓ 缓存策略优化");
        System.out.println("   ✓ 避免内存泄漏");
        
        System.out.println("\n4. 监控和诊断:");
        System.out.println("   ✓ 建立监控体系");
        System.out.println("   ✓ 定期分析GC日志");
        System.out.println("   ✓ 使用性能分析工具");
        System.out.println("   ✓ 进行压力测试验证");
        
        System.out.println("\n5. 调优原则:");
        System.out.println("   ✓ 一次只调整一个参数");
        System.out.println("   ✓ 充分测试后再应用到生产");
        System.out.println("   ✓ 记录调优过程和效果");
        System.out.println("   ✓ 持续监控和优化");
        
        System.out.println();
    }
    
    // 模拟昂贵的资源创建
    private String createExpensiveResource(String key) {
        // 模拟耗时操作
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append(key).append(i);
        }
        return sb.toString();
    }
    
    // 辅助类定义
    
    static class ExpensiveObject {
        private int id;
        private byte[] data = new byte[1024]; // 1KB
        
        public ExpensiveObject(int id) {
            this.id = id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        public void doWork() {
            // 模拟一些工作
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte)(id % 256);
            }
        }
    }
    
    static class ObjectPool<T> {
        private final List<T> pool = new ArrayList<>();
        private final java.util.function.Supplier<T> factory;
        
        public ObjectPool(java.util.function.Supplier<T> factory, int initialSize) {
            this.factory = factory;
            for (int i = 0; i < initialSize; i++) {
                pool.add(factory.get());
            }
        }
        
        public synchronized T borrowObject() {
            if (pool.isEmpty()) {
                return factory.get();
            }
            return pool.remove(pool.size() - 1);
        }
        
        public synchronized void returnObject(T obj) {
            pool.add(obj);
        }
    }
    
    static class ShortLivedObject {
        private int value;
        
        public ShortLivedObject(int value) {
            this.value = value;
        }
        
        public void doSomething() {
            value = value * 2 + 1;
        }
    }
    
    static class LongLivedObject {
        private final int id;
        private final long timestamp;
        
        public LongLivedObject(int id) {
            this.id = id;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    static class LazyInitializedClass {
        private String expensiveResource;
        
        public String getExpensiveResource() {
            if (expensiveResource == null) {
                // 延迟初始化
                expensiveResource = createExpensiveResource();
            }
            return expensiveResource;
        }
        
        private String createExpensiveResource() {
            // 模拟昂贵的资源创建
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                sb.append("expensive_resource_").append(i);
            }
            return sb.toString();
        }
    }
    
    static class CachedResourceManager {
        private final java.util.Map<String, String> cache = new java.util.HashMap<>();
        
        public String getResource(String key) {
            return cache.computeIfAbsent(key, this::createResource);
        }
        
        private String createResource(String key) {
            // 模拟资源创建
            return "Resource_" + key + "_" + System.currentTimeMillis();
        }
    }
    
    static class MemoryLeakDemo {
        // 使用WeakHashMap防止内存泄漏
        private final java.util.WeakHashMap<String, Object> cache = new java.util.WeakHashMap<>();
        
        public void addToCache(String key, Object value) {
            cache.put(key, value);
        }
    }
    
    static class EventPublisher {
        private final List<EventListener> listeners = new ArrayList<>();
        
        public void addListener(EventListener listener) {
            listeners.add(listener);
        }
        
        public void removeListener(EventListener listener) {
            listeners.remove(listener);
        }
    }
    
    static class EventListener {
        // 监听器实现
    }
    
    static class HotSpotMethodExample {
        public long computeIntensive(int n) {
            long result = 0;
            for (int i = 0; i < n; i++) {
                result += Math.sqrt(i) * Math.sin(i);
            }
            return (long)result;
        }
    }
    
    static class MethodInliningExample {
        
        // 小方法，容易被内联
        public final int inlinableMethod(int x) {
            return x * x + x + 1;
        }
        
        // 大方法，不容易被内联
        public int nonInlinableMethod(int x) {
            int result = 0;
            for (int i = 0; i < 10; i++) {
                result += Math.sqrt(x + i) * Math.cos(x - i);
            }
            return (int)result;
        }
    }
}