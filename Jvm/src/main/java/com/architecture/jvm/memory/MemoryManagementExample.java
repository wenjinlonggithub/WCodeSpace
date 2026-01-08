package com.architecture.jvm.memory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;

/**
 * JVM内存管理机制示例
 * 
 * 演示堆内存、非堆内存的分配和使用情况
 */
public class MemoryManagementExample {
    
    private static final List<Object> objects = new ArrayList<>();
    
    public static void main(String[] args) {
        MemoryManagementExample example = new MemoryManagementExample();
        
        System.out.println("=== JVM内存管理示例 ===\n");
        
        // 获取内存管理Bean
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        // 1. 显示初始内存状态
        example.displayMemoryInfo("初始状态", memoryBean);
        
        // 2. 演示堆内存分配
        example.demonstrateHeapMemory();
        example.displayMemoryInfo("堆内存分配后", memoryBean);
        
        // 3. 演示方法区内存
        example.demonstrateMethodArea();
        example.displayMemoryInfo("方法区使用后", memoryBean);
        
        // 4. 手动触发GC
        System.gc();
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
        example.displayMemoryInfo("GC后", memoryBean);
        
        // 5. 演示内存溢出场景（注释掉避免实际溢出）
        // example.demonstrateOutOfMemory();
    }
    
    /**
     * 显示内存信息
     */
    private void displayMemoryInfo(String phase, MemoryMXBean memoryBean) {
        System.out.println("=== " + phase + " ===");
        
        // 堆内存信息
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        System.out.println("堆内存:");
        System.out.printf("  初始化: %d MB\n", heapUsage.getInit() / 1024 / 1024);
        System.out.printf("  已使用: %d MB\n", heapUsage.getUsed() / 1024 / 1024);
        System.out.printf("  已提交: %d MB\n", heapUsage.getCommitted() / 1024 / 1024);
        System.out.printf("  最大值: %d MB\n", heapUsage.getMax() / 1024 / 1024);
        System.out.printf("  使用率: %.2f%%\n", 
                (double) heapUsage.getUsed() / heapUsage.getMax() * 100);
        
        // 非堆内存信息
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        System.out.println("非堆内存:");
        System.out.printf("  已使用: %d MB\n", nonHeapUsage.getUsed() / 1024 / 1024);
        System.out.printf("  已提交: %d MB\n", nonHeapUsage.getCommitted() / 1024 / 1024);
        System.out.printf("  最大值: %d MB\n", 
                nonHeapUsage.getMax() > 0 ? nonHeapUsage.getMax() / 1024 / 1024 : -1);
        
        System.out.println();
    }
    
    /**
     * 演示堆内存分配
     */
    private void demonstrateHeapMemory() {
        System.out.println("正在分配堆内存对象...");
        
        // 分配大量对象到Eden区
        for (int i = 0; i < 10000; i++) {
            objects.add(new LargeObject(i));
        }
        
        // 创建一些大对象，可能直接进入老年代
        for (int i = 0; i < 5; i++) {
            objects.add(new byte[1024 * 1024]); // 1MB对象
        }
        
        System.out.println("堆内存对象分配完成\n");
    }
    
    /**
     * 演示方法区内存使用
     */
    private void demonstrateMethodArea() {
        System.out.println("正在加载类到方法区...");
        
        // 动态生成类增加方法区使用
        try {
            for (int i = 0; i < 100; i++) {
                String className = "DynamicClass" + i;
                generateClass(className);
            }
        } catch (Exception e) {
            System.out.println("类加载异常: " + e.getMessage());
        }
        
        System.out.println("方法区类加载完成\n");
    }
    
    /**
     * 动态生成类
     */
    private void generateClass(String className) throws Exception {
        // 这里简化实现，实际可以使用ASM或Javassist动态生成类
        ClassLoader classLoader = new ClassLoader() {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                if (name.equals(className)) {
                    // 简单的类字节码（实际应该生成正确的字节码）
                    byte[] classBytes = generateClassBytes(className);
                    return defineClass(name, classBytes, 0, classBytes.length);
                }
                return super.findClass(name);
            }
        };
        
        classLoader.loadClass(className);
    }
    
    /**
     * 生成简单的类字节码（模拟）
     */
    private byte[] generateClassBytes(String className) {
        // 这里返回一个简单的字节码数组
        // 实际应该生成符合JVM规范的字节码
        return ("class " + className + " {}").getBytes();
    }
    
    /**
     * 演示内存溢出场景（谨慎使用）
     */
    @SuppressWarnings("unused")
    private void demonstrateOutOfMemory() {
        System.out.println("演示内存溢出...");
        
        List<byte[]> memoryEater = new ArrayList<>();
        try {
            while (true) {
                // 不断分配内存直到OOM
                memoryEater.add(new byte[1024 * 1024]); // 1MB
                System.out.println("已分配内存块: " + memoryEater.size() + " MB");
            }
        } catch (OutOfMemoryError e) {
            System.out.println("发生内存溢出: " + e.getMessage());
            System.out.println("总共分配了 " + memoryEater.size() + " MB内存");
        }
    }
    
    /**
     * 大对象类，用于演示内存分配
     */
    static class LargeObject {
        private final int id;
        private final byte[] data;
        private final String description;
        
        public LargeObject(int id) {
            this.id = id;
            this.data = new byte[1024]; // 1KB数据
            this.description = "Large Object #" + id + " with some description data";
        }
        
        public int getId() { return id; }
        public byte[] getData() { return data; }
        public String getDescription() { return description; }
    }
}