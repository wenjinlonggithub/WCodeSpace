package com.architecture.jvm.gc;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JVM垃圾回收机制演示
 * 
 * 展示不同GC场景和垃圾回收过程
 */
public class GarbageCollectionExample {
    
    private static final List<Object> objectContainer = new ArrayList<>();
    
    public static void main(String[] args) {
        GarbageCollectionExample example = new GarbageCollectionExample();
        
        System.out.println("=== JVM垃圾回收演示 ===\n");
        
        // 1. 显示初始状态
        example.displayGCInfo("初始状态");
        
        // 2. 演示Minor GC
        example.demonstrateMinorGC();
        
        // 3. 演示Major GC
        example.demonstrateMajorGC();
        
        // 4. 演示不同年龄的对象
        example.demonstrateObjectAging();
        
        // 5. 演示内存分配压力
        example.demonstrateAllocationPressure();
        
        // 6. 显示最终状态
        example.displayGCInfo("最终状态");
    }
    
    /**
     * 显示GC信息
     */
    private void displayGCInfo(String phase) {
        System.out.println("=== " + phase + " ===");
        
        // 获取内存信息
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        
        System.out.println("堆内存使用情况:");
        System.out.printf("  已使用: %d MB / %d MB (%.2f%%)\n",
                heapUsage.getUsed() / 1024 / 1024,
                heapUsage.getMax() / 1024 / 1024,
                (double) heapUsage.getUsed() / heapUsage.getMax() * 100);
        
        // 获取GC信息
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        System.out.println("垃圾收集器信息:");
        
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            System.out.printf("  %s: 收集%d次, 耗时%d ms\n",
                    gcBean.getName(),
                    gcBean.getCollectionCount(),
                    gcBean.getCollectionTime());
        }
        System.out.println();
    }
    
    /**
     * 演示Minor GC
     */
    private void demonstrateMinorGC() {
        System.out.println("=== 演示Minor GC ===");
        
        long beforeCount = getTotalGCCount();
        
        // 创建大量短生命周期对象，触发Minor GC
        System.out.println("正在创建大量短生命周期对象...");
        
        for (int i = 0; i < 100; i++) {
            List<YoungObject> batch = new ArrayList<>();
            
            // 每批创建1000个对象
            for (int j = 0; j < 1000; j++) {
                batch.add(new YoungObject(i * 1000 + j));
            }
            
            // 批量处理后丢弃引用，使对象可以被GC
            if (i % 10 == 0) {
                System.out.printf("已创建第%d批对象\n", i + 1);
            }
            
            // 偶尔休眠一下，给GC机会
            if (i % 20 == 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        // 手动建议GC
        System.gc();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long afterCount = getTotalGCCount();
        System.out.printf("GC次数增加: %d\n", afterCount - beforeCount);
        
        displayGCInfo("Minor GC后");
    }
    
    /**
     * 演示Major GC
     */
    private void demonstrateMajorGC() {
        System.out.println("=== 演示Major GC ===");
        
        long beforeCount = getTotalGCCount();
        
        System.out.println("正在创建长生命周期对象...");
        
        // 创建一些长生命周期对象，让它们晋升到老年代
        for (int i = 0; i < 50; i++) {
            // 创建较大的对象
            LongLivedObject obj = new LongLivedObject(i);
            objectContainer.add(obj);
            
            // 创建一些临时对象触发年轻代GC
            for (int j = 0; j < 100; j++) {
                new YoungObject(i * 100 + j);
            }
            
            if (i % 10 == 0) {
                System.gc(); // 建议GC
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        // 创建一些大对象，可能直接进入老年代
        System.out.println("正在创建大对象...");
        for (int i = 0; i < 10; i++) {
            objectContainer.add(new LargeObject(i));
        }
        
        System.gc();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long afterCount = getTotalGCCount();
        System.out.printf("GC次数增加: %d\n", afterCount - beforeCount);
        
        displayGCInfo("Major GC后");
    }
    
    /**
     * 演示对象年龄增长
     */
    private void demonstrateObjectAging() {
        System.out.println("=== 演示对象年龄增长 ===");
        
        // 创建一批对象
        List<AgingObject> agingObjects = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            agingObjects.add(new AgingObject(i));
        }
        
        System.out.println("对象已创建，开始多轮GC增加对象年龄...");
        
        // 进行多轮GC，让对象年龄增长
        for (int round = 1; round <= 10; round++) {
            // 创建一些临时对象触发GC
            for (int i = 0; i < 1000; i++) {
                new YoungObject(round * 1000 + i);
            }
            
            System.gc();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            System.out.printf("完成第%d轮GC\n", round);
        }
        
        // 保持对象引用，防止被回收
        objectContainer.addAll(agingObjects);
        
        displayGCInfo("对象年龄增长后");
    }
    
    /**
     * 演示内存分配压力
     */
    private void demonstrateAllocationPressure() {
        System.out.println("=== 演示内存分配压力 ===");
        
        long startTime = System.currentTimeMillis();
        long beforeCount = getTotalGCCount();
        
        System.out.println("正在施加内存分配压力...");
        
        // 快速分配大量对象，观察GC行为
        for (int i = 0; i < 200; i++) {
            List<PressureObject> batch = new ArrayList<>();
            
            for (int j = 0; j < 500; j++) {
                batch.add(new PressureObject(i * 500 + j));
            }
            
            // 偶尔保留一些对象
            if (i % 20 == 0) {
                objectContainer.addAll(batch.subList(0, 50));
                System.out.printf("第%d批次，保留了50个对象\n", i + 1);
            }
            
            // 短暂休眠
            if (i % 50 == 0) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        long afterCount = getTotalGCCount();
        
        System.out.printf("分配压力测试完成，耗时: %d ms\n", endTime - startTime);
        System.out.printf("触发GC次数: %d\n", afterCount - beforeCount);
        
        displayGCInfo("内存压力测试后");
    }
    
    /**
     * 获取总GC次数
     */
    private long getTotalGCCount() {
        long totalCount = 0;
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            totalCount += gcBean.getCollectionCount();
        }
        return totalCount;
    }
    
    /**
     * 年轻对象类 - 模拟短生命周期对象
     */
    static class YoungObject {
        private final int id;
        private final String data;
        private final long timestamp;
        
        public YoungObject(int id) {
            this.id = id;
            this.data = "YoungObject_" + id + "_" + System.currentTimeMillis();
            this.timestamp = System.currentTimeMillis();
        }
        
        public int getId() { return id; }
        public String getData() { return data; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * 长生命周期对象类 - 模拟老年代对象
     */
    static class LongLivedObject {
        private final int id;
        private final byte[] data;
        private final String description;
        private final long creationTime;
        
        public LongLivedObject(int id) {
            this.id = id;
            this.data = new byte[10 * 1024]; // 10KB
            this.description = "LongLivedObject_" + id + "_persistent_data";
            this.creationTime = System.currentTimeMillis();
        }
        
        public int getId() { return id; }
        public byte[] getData() { return data; }
        public String getDescription() { return description; }
        public long getCreationTime() { return creationTime; }
    }
    
    /**
     * 大对象类 - 可能直接进入老年代
     */
    static class LargeObject {
        private final int id;
        private final byte[] largeData;
        
        public LargeObject(int id) {
            this.id = id;
            this.largeData = new byte[2 * 1024 * 1024]; // 2MB
            
            // 填充一些数据
            for (int i = 0; i < largeData.length; i++) {
                largeData[i] = (byte) (i % 256);
            }
        }
        
        public int getId() { return id; }
        public byte[] getLargeData() { return largeData; }
    }
    
    /**
     * 年龄增长对象类 - 用于演示对象年龄
     */
    static class AgingObject {
        private final int id;
        private final String info;
        private final byte[] payload;
        
        public AgingObject(int id) {
            this.id = id;
            this.info = "AgingObject_" + id;
            this.payload = new byte[1024]; // 1KB
        }
        
        public int getId() { return id; }
        public String getInfo() { return info; }
        public byte[] getPayload() { return payload; }
    }
    
    /**
     * 压力测试对象类 - 用于内存分配压力测试
     */
    static class PressureObject {
        private final int id;
        private final String name;
        private final long[] numbers;
        
        public PressureObject(int id) {
            this.id = id;
            this.name = "PressureObject_" + id;
            this.numbers = new long[64]; // 512字节
            
            for (int i = 0; i < numbers.length; i++) {
                numbers[i] = id * 64L + i;
            }
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        public long[] getNumbers() { return numbers; }
    }
}