package com.jvm.internals;

import java.lang.management.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * JVM内部机制和字节码分析
 * 
 * 深入分析：
 * 1. JVM内存结构详解
 * 2. 类加载机制和双亲委派模型
 * 3. 字节码指令分析
 * 4. JIT编译优化
 * 5. 方法调用和栈帧结构
 * 6. 对象创建和内存分配
 * 7. JVM参数调优
 */

public class JVMInternalsAnalysis {
    
    /**
     * JVM内存结构分析
     * 
     * 内存区域：
     * 1. 程序计数器 (PC Register)
     * 2. Java虚拟机栈 (JVM Stack)
     * 3. 本地方法栈 (Native Method Stack)
     * 4. 堆内存 (Heap)
     * 5. 方法区 (Method Area) / 元空间 (Metaspace)
     * 6. 直接内存 (Direct Memory)
     */
    
    static class MemoryStructureAnalysis {
        
        /**
         * 堆内存分析
         */
        public static void analyzeHeapMemory() {
            System.out.println("=== JVM Heap Memory Analysis ===");
            
            // 获取内存管理Bean
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
            
            // 1. 堆内存总体信息
            System.out.println("\n--- Heap Memory Overview ---");
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            
            System.out.println("Heap Memory Usage:");
            System.out.println("  Initial: " + formatBytes(heapUsage.getInit()));
            System.out.println("  Used: " + formatBytes(heapUsage.getUsed()));
            System.out.println("  Committed: " + formatBytes(heapUsage.getCommitted()));
            System.out.println("  Max: " + formatBytes(heapUsage.getMax()));
            
            // 2. 各个内存池详细信息
            System.out.println("\n--- Memory Pools Detail ---");
            for (MemoryPoolMXBean pool : memoryPools) {
                if (pool.getType() == MemoryType.HEAP) {
                    MemoryUsage usage = pool.getUsage();
                    System.out.println("Pool: " + pool.getName());
                    System.out.println("  Type: " + pool.getType());
                    System.out.println("  Used: " + formatBytes(usage.getUsed()));
                    System.out.println("  Max: " + formatBytes(usage.getMax()));
                    System.out.println("  Usage: " + String.format("%.2f%%", 
                        (usage.getUsed() * 100.0) / usage.getMax()));
                    System.out.println();
                }
            }
            
            // 3. 对象分配演示
            System.out.println("--- Object Allocation Demo ---");
            
            long beforeAllocation = heapUsage.getUsed();
            
            // 分配大量对象
            List<byte[]> allocations = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                allocations.add(new byte[1024]); // 1KB per object
            }
            
            // 强制更新内存使用信息
            System.gc();
            try {
                Thread.sleep(100); // 等待GC完成
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            heapUsage = memoryBean.getHeapMemoryUsage();
            long afterAllocation = heapUsage.getUsed();
            
            System.out.println("Memory before allocation: " + formatBytes(beforeAllocation));
            System.out.println("Memory after allocation: " + formatBytes(afterAllocation));
            System.out.println("Memory increase: " + formatBytes(afterAllocation - beforeAllocation));
            
            // 清理对象
            allocations.clear();
            System.gc();
        }
        
        /**
         * 非堆内存分析
         */
        public static void analyzeNonHeapMemory() {
            System.out.println("\n=== Non-Heap Memory Analysis ===");
            
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
            
            // 1. 非堆内存总体信息
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            
            System.out.println("Non-Heap Memory Usage:");
            System.out.println("  Initial: " + formatBytes(nonHeapUsage.getInit()));
            System.out.println("  Used: " + formatBytes(nonHeapUsage.getUsed()));
            System.out.println("  Committed: " + formatBytes(nonHeapUsage.getCommitted()));
            System.out.println("  Max: " + formatBytes(nonHeapUsage.getMax()));
            
            // 2. 方法区/元空间详细信息
            System.out.println("\n--- Method Area / Metaspace ---");
            for (MemoryPoolMXBean pool : memoryPools) {
                if (pool.getType() == MemoryType.NON_HEAP) {
                    MemoryUsage usage = pool.getUsage();
                    System.out.println("Pool: " + pool.getName());
                    System.out.println("  Used: " + formatBytes(usage.getUsed()));
                    System.out.println("  Committed: " + formatBytes(usage.getCommitted()));
                    if (usage.getMax() > 0) {
                        System.out.println("  Max: " + formatBytes(usage.getMax()));
                        System.out.println("  Usage: " + String.format("%.2f%%", 
                            (usage.getUsed() * 100.0) / usage.getMax()));
                    }
                    System.out.println();
                }
            }
        }
        
        /**
         * 直接内存分析
         */
        public static void analyzeDirectMemory() {
            System.out.println("\n=== Direct Memory Analysis ===");
            
            // 直接内存不在JVM堆中，通过MXBean无法直接获取
            // 这里演示直接内存的使用
            
            List<java.nio.ByteBuffer> directBuffers = new ArrayList<>();
            
            try {
                System.out.println("Allocating direct memory buffers...");
                
                // 分配直接内存
                for (int i = 0; i < 100; i++) {
                    java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocateDirect(1024 * 1024); // 1MB
                    directBuffers.add(buffer);
                    
                    if (i % 20 == 19) {
                        System.out.println("Allocated " + (i + 1) + " direct buffers (" + 
                            ((i + 1) * 1024 * 1024 / 1024 / 1024) + " MB)");
                    }
                }
                
                System.out.println("Total direct memory allocated: " + 
                    (directBuffers.size() * 1024 * 1024 / 1024 / 1024) + " MB");
                
            } catch (OutOfMemoryError e) {
                System.out.println("Direct memory exhausted: " + e.getMessage());
            } finally {
                // 清理直接内存
                directBuffers.clear();
                System.gc(); // 触发GC，可能会清理直接内存
            }
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
     * 类加载机制分析
     */
    
    static class ClassLoadingAnalysis {
        
        /**
         * 双亲委派模型演示
         */
        public static void analyzeDelegationModel() {
            System.out.println("\n=== Class Loading Delegation Model ===");
            
            // 1. 类加载器层次结构
            System.out.println("\n--- ClassLoader Hierarchy ---");
            
            ClassLoader currentLoader = ClassLoadingAnalysis.class.getClassLoader();
            int level = 0;
            
            while (currentLoader != null) {
                System.out.println("Level " + level + ": " + currentLoader.getClass().getName());
                System.out.println("  " + currentLoader.toString());
                currentLoader = currentLoader.getParent();
                level++;
            }
            System.out.println("Level " + level + ": Bootstrap ClassLoader (null)");
            
            // 2. 不同类的加载器
            System.out.println("\n--- Different Class Loaders ---");
            
            // 系统类
            Class<?> stringClass = String.class;
            System.out.println("String class loader: " + stringClass.getClassLoader()); // null (Bootstrap)
            
            // 扩展类
            Class<?> managementFactory = ManagementFactory.class;
            System.out.println("ManagementFactory class loader: " + managementFactory.getClassLoader());
            
            // 应用类
            Class<?> thisClass = ClassLoadingAnalysis.class;
            System.out.println("This class loader: " + thisClass.getClassLoader());
            
            // 3. 类加载过程演示
            System.out.println("\n--- Class Loading Process ---");
            
            try {
                // 动态加载类
                ClassLoader appLoader = ClassLoader.getSystemClassLoader();
                Class<?> loadedClass = appLoader.loadClass("java.util.HashMap");
                
                System.out.println("Loaded class: " + loadedClass.getName());
                System.out.println("Loaded by: " + loadedClass.getClassLoader());
                
                // 验证类是否已经加载
                Class<?> sameClass = appLoader.loadClass("java.util.HashMap");
                System.out.println("Same class instance: " + (loadedClass == sameClass));
                
            } catch (ClassNotFoundException e) {
                System.err.println("Class not found: " + e.getMessage());
            }
        }
        
        /**
         * 自定义类加载器演示
         */
        public static void demonstrateCustomClassLoader() {
            System.out.println("\n--- Custom ClassLoader Demo ---");
            
            CustomClassLoader customLoader = new CustomClassLoader();
            
            try {
                // 使用自定义类加载器加载类
                Class<?> customClass = customLoader.findClass("com.example.TestClass");
                System.out.println("Custom loaded class: " + customClass.getName());
                System.out.println("Loaded by: " + customClass.getClassLoader());
                
            } catch (ClassNotFoundException e) {
                System.out.println("Custom class loading demo (simulated): " + e.getMessage());
            }
        }
        
        /**
         * 类初始化顺序演示
         */
        public static void demonstrateClassInitialization() {
            System.out.println("\n--- Class Initialization Order ---");
            
            System.out.println("Creating Parent instance:");
            Parent parent = new Parent();
            
            System.out.println("\nCreating Child instance:");
            Child child = new Child();
        }
    }
    
    // 自定义类加载器
    static class CustomClassLoader extends ClassLoader {
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            System.out.println("CustomClassLoader.findClass: " + name);
            
            // 模拟类加载过程
            if (name.equals("com.example.TestClass")) {
                // 在实际实现中，这里会读取.class文件的字节码
                // 然后调用defineClass方法
                System.out.println("Would load bytecode for: " + name);
            }
            
            throw new ClassNotFoundException("Class not found: " + name);
        }
    }
    
    // 类初始化演示类
    static class Parent {
        static {
            System.out.println("Parent static block");
        }
        
        {
            System.out.println("Parent instance block");
        }
        
        public Parent() {
            System.out.println("Parent constructor");
        }
    }
    
    static class Child extends Parent {
        static {
            System.out.println("Child static block");
        }
        
        {
            System.out.println("Child instance block");
        }
        
        public Child() {
            System.out.println("Child constructor");
        }
    }
    
    /**
     * 字节码分析
     */
    
    static class BytecodeAnalysis {
        
        /**
         * 方法调用字节码分析
         */
        public static void analyzeMethodInvocation() {
            System.out.println("\n=== Bytecode Analysis ===");
            
            System.out.println("\n--- Method Invocation Types ---");
            
            // 1. 静态方法调用 (invokestatic)
            staticMethod();
            
            // 2. 实例方法调用 (invokevirtual)
            BytecodeAnalysis instance = new BytecodeAnalysis();
            instance.instanceMethod();
            
            // 3. 接口方法调用 (invokeinterface)
            List<String> list = new ArrayList<>();
            list.add("test"); // invokeinterface
            
            // 4. 特殊方法调用 (invokespecial)
            // 构造函数、私有方法、super方法调用
            new BytecodeAnalysis(); // invokespecial <init>
            
            System.out.println("Method invocation types demonstrated");
            System.out.println("Use 'javap -c ClassName' to view actual bytecode");
        }
        
        private static void staticMethod() {
            System.out.println("Static method called (invokestatic)");
        }
        
        private void instanceMethod() {
            System.out.println("Instance method called (invokevirtual)");
        }
        
        /**
         * 字段访问字节码分析
         */
        public static void analyzeFieldAccess() {
            System.out.println("\n--- Field Access Bytecode ---");
            
            BytecodeDemo demo = new BytecodeDemo();
            
            // 实例字段访问 (getfield/putfield)
            demo.instanceField = 42;
            int value = demo.instanceField;
            
            // 静态字段访问 (getstatic/putstatic)
            BytecodeDemo.staticField = 100;
            int staticValue = BytecodeDemo.staticField;
            
            System.out.println("Instance field: " + value);
            System.out.println("Static field: " + staticValue);
        }
        
        /**
         * 数组操作字节码分析
         */
        public static void analyzeArrayOperations() {
            System.out.println("\n--- Array Operations Bytecode ---");
            
            // 数组创建 (newarray/anewarray)
            int[] intArray = new int[10];
            String[] stringArray = new String[5];
            
            // 数组访问 (iaload/iastore, aaload/aastore)
            intArray[0] = 42;
            int value = intArray[0];
            
            stringArray[0] = "Hello";
            String str = stringArray[0];
            
            // 数组长度 (arraylength)
            int length = intArray.length;
            
            System.out.println("Array operations completed");
            System.out.println("Int array[0]: " + value);
            System.out.println("String array[0]: " + str);
            System.out.println("Array length: " + length);
        }
    }
    
    static class BytecodeDemo {
        public int instanceField;
        public static int staticField;
    }
    
    /**
     * JIT编译优化分析
     */
    
    static class JITCompilationAnalysis {
        
        /**
         * 热点代码和JIT编译演示
         */
        public static void demonstrateJITCompilation() {
            System.out.println("\n=== JIT Compilation Analysis ===");
            
            // 获取编译器MXBean
            List<CompilationMXBean> compilationBeans = ManagementFactory.getPlatformMXBeans(CompilationMXBean.class);
            
            if (!compilationBeans.isEmpty()) {
                CompilationMXBean compilationBean = compilationBeans.get(0);
                System.out.println("JIT Compiler: " + compilationBean.getName());
                System.out.println("Compilation time supported: " + compilationBean.isCompilationTimeMonitoringSupported());
                
                if (compilationBean.isCompilationTimeMonitoringSupported()) {
                    long initialCompilationTime = compilationBean.getTotalCompilationTime();
                    System.out.println("Initial compilation time: " + initialCompilationTime + " ms");
                    
                    // 执行热点代码
                    System.out.println("\nExecuting hot code to trigger JIT compilation...");
                    
                    long result = 0;
                    int iterations = 100000;
                    
                    // 第一次执行（解释执行）
                    long startTime = System.nanoTime();
                    for (int i = 0; i < iterations; i++) {
                        result += hotMethod(i);
                    }
                    long firstRunTime = System.nanoTime() - startTime;
                    
                    // 第二次执行（可能已经JIT编译）
                    startTime = System.nanoTime();
                    for (int i = 0; i < iterations; i++) {
                        result += hotMethod(i);
                    }
                    long secondRunTime = System.nanoTime() - startTime;
                    
                    // 第三次执行（应该已经JIT编译）
                    startTime = System.nanoTime();
                    for (int i = 0; i < iterations; i++) {
                        result += hotMethod(i);
                    }
                    long thirdRunTime = System.nanoTime() - startTime;
                    
                    System.out.println("First run time: " + firstRunTime / 1_000_000.0 + " ms");
                    System.out.println("Second run time: " + secondRunTime / 1_000_000.0 + " ms");
                    System.out.println("Third run time: " + thirdRunTime / 1_000_000.0 + " ms");
                    System.out.println("Result: " + result);
                    
                    long finalCompilationTime = compilationBean.getTotalCompilationTime();
                    System.out.println("Final compilation time: " + finalCompilationTime + " ms");
                    System.out.println("Compilation time increase: " + (finalCompilationTime - initialCompilationTime) + " ms");
                }
            }
        }
        
        // 热点方法，会被JIT编译
        private static long hotMethod(int x) {
            long result = x;
            result = result * 2 + 1;
            result = result % 1000;
            return result;
        }
        
        /**
         * 内联优化演示
         */
        public static void demonstrateInlining() {
            System.out.println("\n--- Method Inlining Demo ---");
            
            int iterations = 1000000;
            
            // 小方法调用，容易被内联
            long startTime = System.nanoTime();
            long sum = 0;
            for (int i = 0; i < iterations; i++) {
                sum += smallMethod(i);
            }
            long inlineTime = System.nanoTime() - startTime;
            
            // 大方法调用，不容易被内联
            startTime = System.nanoTime();
            long sum2 = 0;
            for (int i = 0; i < iterations; i++) {
                sum2 += largeMethod(i);
            }
            long noInlineTime = System.nanoTime() - startTime;
            
            System.out.println("Small method (likely inlined) time: " + inlineTime / 1_000_000.0 + " ms");
            System.out.println("Large method (unlikely inlined) time: " + noInlineTime / 1_000_000.0 + " ms");
            System.out.println("Results: " + sum + ", " + sum2);
        }
        
        private static int smallMethod(int x) {
            return x + 1; // 简单操作，容易被内联
        }
        
        private static int largeMethod(int x) {
            // 复杂操作，不容易被内联
            int result = x;
            for (int i = 0; i < 10; i++) {
                result = result * 2 + 1;
                result = result % 1000;
            }
            return result;
        }
    }
    
    public static void main(String[] args) {
        MemoryStructureAnalysis.analyzeHeapMemory();
        MemoryStructureAnalysis.analyzeNonHeapMemory();
        MemoryStructureAnalysis.analyzeDirectMemory();
        
        ClassLoadingAnalysis.analyzeDelegationModel();
        ClassLoadingAnalysis.demonstrateCustomClassLoader();
        ClassLoadingAnalysis.demonstrateClassInitialization();
        
        BytecodeAnalysis.analyzeMethodInvocation();
        BytecodeAnalysis.analyzeFieldAccess();
        BytecodeAnalysis.analyzeArrayOperations();
        
        JITCompilationAnalysis.demonstrateJITCompilation();
        JITCompilationAnalysis.demonstrateInlining();
    }
}