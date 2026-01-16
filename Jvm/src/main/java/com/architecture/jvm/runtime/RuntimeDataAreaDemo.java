package com.architecture.jvm.runtime;

import java.lang.management.*;
import java.util.*;

/**
 * JVM运行时数据区完整演示
 *
 * JVM运行时数据区分为：
 * 1. 程序计数器 (Program Counter Register) - 线程私有
 * 2. Java虚拟机栈 (VM Stack) - 线程私有
 * 3. 本地方法栈 (Native Method Stack) - 线程私有
 * 4. 堆 (Heap) - 线程共享
 * 5. 方法区/元空间 (Method Area/Metaspace) - 线程共享
 * 6. 直接内存 (Direct Memory) - 非JVM规范定义，但实际使用
 *
 * @author Architecture
 */
public class RuntimeDataAreaDemo {

    public static void main(String[] args) {
        System.out.println("=== JVM运行时数据区完整演示 ===\n");

        // 1. 显示JVM内存布局
        displayMemoryLayout();

        // 2. 演示各个区域
        demonstrateProgramCounter();
        demonstrateVMStack();
        demonstrateHeap();
        demonstrateMetaspace();
        demonstrateDirectMemory();

        // 3. 内存使用总结
        displayMemorySummary();
    }

    /**
     * 显示JVM内存布局
     */
    private static void displayMemoryLayout() {
        System.out.println("【1. JVM内存布局】");
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.println("│         JVM运行时数据区结构              │");
        System.out.println("├─────────────────────────────────────────┤");
        System.out.println("│  线程私有区域:                           │");
        System.out.println("│    • 程序计数器 (PC Register)           │");
        System.out.println("│    • 虚拟机栈 (VM Stack)                │");
        System.out.println("│    • 本地方法栈 (Native Method Stack)   │");
        System.out.println("├─────────────────────────────────────────┤");
        System.out.println("│  线程共享区域:                           │");
        System.out.println("│    • 堆 (Heap)                          │");
        System.out.println("│      - 新生代 (Young Generation)        │");
        System.out.println("│        * Eden区                         │");
        System.out.println("│        * Survivor0 (From)               │");
        System.out.println("│        * Survivor1 (To)                 │");
        System.out.println("│      - 老年代 (Old Generation)          │");
        System.out.println("│    • 方法区/元空间 (Metaspace)          │");
        System.out.println("│      - 类元数据                          │");
        System.out.println("│      - 常量池                            │");
        System.out.println("│      - 静态变量                          │");
        System.out.println("├─────────────────────────────────────────┤");
        System.out.println("│  非JVM规范区域:                          │");
        System.out.println("│    • 直接内存 (Direct Memory)           │");
        System.out.println("│    • 代码缓存 (Code Cache)              │");
        System.out.println("└─────────────────────────────────────────┘");
        System.out.println();
    }

    /**
     * 演示程序计数器
     * PC寄存器是线程私有的，记录当前线程执行的字节码指令地址
     */
    private static void demonstrateProgramCounter() {
        System.out.println("【2. 程序计数器 (PC Register)】");
        System.out.println("特点：");
        System.out.println("  • 线程私有，每个线程都有独立的PC");
        System.out.println("  • 记录当前线程正在执行的字节码指令地址");
        System.out.println("  • 如果执行Native方法，PC值为undefined");
        System.out.println("  • 唯一不会发生OutOfMemoryError的区域");

        // 多线程演示PC的独立性
        System.out.println("\n演示：多线程中PC的独立性");
        for (int i = 1; i <= 3; i++) {
            final int threadNum = i;
            new Thread(() -> {
                // 每个线程都有自己的PC，记录自己的执行位置
                executeInstructions(threadNum);
            }, "Thread-" + i).start();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void executeInstructions(int threadNum) {
        System.out.println(Thread.currentThread().getName() +
                         " 执行中 (每个线程有独立的PC记录执行位置)");
        // 模拟执行多条指令
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            sum += i; // 每次循环PC都会改变，记录下一条指令位置
        }
    }

    /**
     * 演示虚拟机栈
     * 线程私有，描述Java方法执行的内存模型
     */
    private static void demonstrateVMStack() {
        System.out.println("【3. Java虚拟机栈 (VM Stack)】");
        System.out.println("特点：");
        System.out.println("  • 线程私有，生命周期与线程相同");
        System.out.println("  • 描述Java方法执行的内存模型");
        System.out.println("  • 每个方法执行时创建一个栈帧(Stack Frame)");
        System.out.println("  • 栈帧包含：局部变量表、操作数栈、动态链接、方法返回地址");
        System.out.println("  • 可能异常：StackOverflowError、OutOfMemoryError");

        System.out.println("\n栈帧结构：");
        System.out.println("  ┌──────────────────┐");
        System.out.println("  │ 方法返回地址      │");
        System.out.println("  ├──────────────────┤");
        System.out.println("  │ 动态链接          │");
        System.out.println("  ├──────────────────┤");
        System.out.println("  │ 操作数栈          │");
        System.out.println("  ├──────────────────┤");
        System.out.println("  │ 局部变量表        │");
        System.out.println("  └──────────────────┘");

        System.out.println("\n演示：方法调用与栈帧");
        methodA();

        // 获取当前线程栈信息
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] threadIds = threadMXBean.getAllThreadIds();
        System.out.println("\n当前JVM线程数: " + threadIds.length);
        System.out.println("(每个线程都有自己的虚拟机栈)");
        System.out.println();
    }

    private static void methodA() {
        System.out.println("  → methodA() 入栈");
        int localVarA = 10; // 局部变量存储在栈帧的局部变量表中
        methodB();
        System.out.println("  ← methodA() 出栈");
    }

    private static void methodB() {
        System.out.println("    → methodB() 入栈");
        int localVarB = 20;
        methodC();
        System.out.println("    ← methodB() 出栈");
    }

    private static void methodC() {
        System.out.println("      → methodC() 入栈");
        int localVarC = 30;
        System.out.println("      ← methodC() 出栈");
    }

    /**
     * 演示堆内存
     * 所有线程共享，存储对象实例和数组
     */
    private static void demonstrateHeap() {
        System.out.println("【4. 堆内存 (Heap)】");
        System.out.println("特点：");
        System.out.println("  • 所有线程共享");
        System.out.println("  • 存储对象实例和数组");
        System.out.println("  • 垃圾回收器管理的主要区域");
        System.out.println("  • 分代结构：新生代(Eden + 2个Survivor) + 老年代");

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();

        System.out.println("\n当前堆内存状态：");
        System.out.println("  初始大小: " + formatBytes(heapMemory.getInit()));
        System.out.println("  已使用  : " + formatBytes(heapMemory.getUsed()));
        System.out.println("  已提交  : " + formatBytes(heapMemory.getCommitted()));
        System.out.println("  最大值  : " + formatBytes(heapMemory.getMax()));

        // 演示对象分配
        System.out.println("\n演示：对象在堆中的分配");
        List<String> list = new ArrayList<>(); // 对象分配在堆上
        for (int i = 0; i < 5; i++) {
            list.add("Object-" + i);
            System.out.println("  创建对象 " + i + " (分配在堆的Eden区)");
        }

        // 显示各代内存池
        System.out.println("\n堆内存池详情：");
        List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : memoryPools) {
            if (pool.getType() == MemoryType.HEAP) {
                MemoryUsage usage = pool.getUsage();
                System.out.println("  " + pool.getName() + ":");
                System.out.println("    已使用: " + formatBytes(usage.getUsed()) +
                                 " / " + formatBytes(usage.getMax()));
            }
        }
        System.out.println();
    }

    /**
     * 演示方法区/元空间
     * 存储类的元数据、常量、静态变量等
     */
    private static void demonstrateMetaspace() {
        System.out.println("【5. 方法区/元空间 (Metaspace)】");
        System.out.println("特点：");
        System.out.println("  • 所有线程共享");
        System.out.println("  • 存储类的元数据、常量池、静态变量");
        System.out.println("  • JDK 8之前叫永久代(PermGen)，使用堆内存");
        System.out.println("  • JDK 8+改为元空间(Metaspace)，使用本地内存");
        System.out.println("  • 类卸载、常量池回收在此区域进行");

        // 获取元空间信息
        List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
        System.out.println("\n元空间使用情况：");
        for (MemoryPoolMXBean pool : memoryPools) {
            if (pool.getName().contains("Metaspace")) {
                MemoryUsage usage = pool.getUsage();
                System.out.println("  " + pool.getName() + ":");
                System.out.println("    已使用: " + formatBytes(usage.getUsed()));
                System.out.println("    已提交: " + formatBytes(usage.getCommitted()));
                if (usage.getMax() > 0) {
                    System.out.println("    最大值: " + formatBytes(usage.getMax()));
                } else {
                    System.out.println("    最大值: 不限制(受限于系统内存)");
                }
            }
        }

        // 演示类加载
        System.out.println("\n演示：类元数据加载到方法区");
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        System.out.println("  当前加载类数量: " + classLoadingMXBean.getLoadedClassCount());
        System.out.println("  累计加载类数量: " + classLoadingMXBean.getTotalLoadedClassCount());
        System.out.println("  已卸载类数量  : " + classLoadingMXBean.getUnloadedClassCount());

        // 演示常量池
        System.out.println("\n演示：字符串常量池（在堆中，元数据在Metaspace）");
        String str1 = "Hello"; // 字面量，存入常量池
        String str2 = "Hello"; // 复用常量池中的对象
        String str3 = new String("Hello"); // 在堆中创建新对象

        System.out.println("  str1 == str2: " + (str1 == str2) + " (都指向常量池)");
        System.out.println("  str1 == str3: " + (str1 == str3) + " (str3是新对象)");
        System.out.println("  str1.equals(str3): " + str1.equals(str3) + " (内容相同)");
        System.out.println();
    }

    /**
     * 演示直接内存
     * 不是JVM运行时数据区的一部分，但被频繁使用
     */
    private static void demonstrateDirectMemory() {
        System.out.println("【6. 直接内存 (Direct Memory)】");
        System.out.println("特点：");
        System.out.println("  • 不是JVM规范定义的内存区域");
        System.out.println("  • 在Java堆外分配内存");
        System.out.println("  • 通过Native函数库直接分配");
        System.out.println("  • 主要用于NIO的DirectByteBuffer");
        System.out.println("  • 避免Java堆和Native堆之间的数据复制");
        System.out.println("  • 可通过-XX:MaxDirectMemorySize参数限制");

        System.out.println("\n优势：");
        System.out.println("  • 避免数据在Java堆和本地内存之间复制");
        System.out.println("  • 提高IO操作性能");
        System.out.println("  • 不受GC影响");

        System.out.println("\n劣势：");
        System.out.println("  • 分配和回收成本较高");
        System.out.println("  • 不受JVM内存管理，容易造成内存泄漏");
        System.out.println("  • 调试困难");

        // 获取BufferPool信息
        System.out.println("\nBuffer Pool信息：");
        List<BufferPoolMXBean> bufferPools = ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
        for (BufferPoolMXBean pool : bufferPools) {
            System.out.println("  " + pool.getName() + ":");
            System.out.println("    数量: " + pool.getCount());
            System.out.println("    容量: " + formatBytes(pool.getTotalCapacity()));
            System.out.println("    已使用: " + formatBytes(pool.getMemoryUsed()));
        }
        System.out.println();
    }

    /**
     * 显示内存使用总结
     */
    private static void displayMemorySummary() {
        System.out.println("【7. 内存使用总结】");

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryMXBean.getNonHeapMemoryUsage();

        System.out.println("堆内存 (Heap):");
        System.out.println("  已使用: " + formatBytes(heapMemory.getUsed()));
        System.out.println("  已提交: " + formatBytes(heapMemory.getCommitted()));
        System.out.println("  最大值: " + formatBytes(heapMemory.getMax()));

        System.out.println("\n非堆内存 (Non-Heap，包括Metaspace、Code Cache等):");
        System.out.println("  已使用: " + formatBytes(nonHeapMemory.getUsed()));
        System.out.println("  已提交: " + formatBytes(nonHeapMemory.getCommitted()));
        if (nonHeapMemory.getMax() > 0) {
            System.out.println("  最大值: " + formatBytes(nonHeapMemory.getMax()));
        }

        System.out.println("\nJVM总内存: " +
            formatBytes(heapMemory.getUsed() + nonHeapMemory.getUsed()));

        // 运行时信息
        Runtime runtime = Runtime.getRuntime();
        System.out.println("\nRuntime内存信息:");
        System.out.println("  总内存: " + formatBytes(runtime.totalMemory()));
        System.out.println("  空闲内存: " + formatBytes(runtime.freeMemory()));
        System.out.println("  最大内存: " + formatBytes(runtime.maxMemory()));
        System.out.println("  已用内存: " + formatBytes(runtime.totalMemory() - runtime.freeMemory()));

        // GC信息
        System.out.println("\nGC信息:");
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gc : gcBeans) {
            System.out.println("  " + gc.getName() + ":");
            System.out.println("    回收次数: " + gc.getCollectionCount());
            System.out.println("    回收耗时: " + gc.getCollectionTime() + "ms");
        }
    }

    /**
     * 格式化字节数
     */
    private static String formatBytes(long bytes) {
        if (bytes < 0) return "N/A";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
