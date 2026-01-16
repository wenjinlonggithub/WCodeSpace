package com.architecture.jvm.runtime;

import java.lang.management.*;
import java.util.*;

/**
 * 堆内存深度解析
 *
 * 堆是Java虚拟机管理的最大一块内存区域，所有线程共享
 *
 * 堆的结构（分代模型）：
 * ┌─────────────────────────────────────┐
 * │           Java Heap                 │
 * ├─────────────────┬───────────────────┤
 * │  Young Gen      │    Old Gen        │
 * │  (新生代)        │   (老年代)        │
 * ├────┬────┬───────┼───────────────────┤
 * │Eden│S0  │S1     │                   │
 * │    │    │       │                   │
 * └────┴────┴───────┴───────────────────┘
 *
 * 对象分配流程：
 * 1. 新对象优先在Eden区分配
 * 2. Eden区满时触发Minor GC
 * 3. 存活对象复制到Survivor区
 * 4. Survivor区对象年龄+1
 * 5. 年龄达到阈值(默认15)晋升到老年代
 * 6. 大对象直接进入老年代
 *
 * @author Architecture
 */
public class HeapMemoryDeepDive {

    public static void main(String[] args) {
        System.out.println("=== 堆内存深度解析 ===\n");

        // 1. 堆内存结构
        displayHeapStructure();

        // 2. 对象分配演示
        demonstrateObjectAllocation();

        // 3. Eden区分配
        demonstrateEdenAllocation();

        // 4. 大对象直接进入老年代
        demonstrateLargeObjectAllocation();

        // 5. 对象晋升演示
        demonstrateObjectPromotion();

        // 6. 堆内存监控
        monitorHeapMemory();

        // 7. 堆内存参数
        displayHeapParameters();
    }

    /**
     * 1. 显示堆内存结构
     */
    private static void displayHeapStructure() {
        System.out.println("【1. 堆内存结构】\n");

        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();

        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.println("│              堆内存分代结构                  │");
        System.out.println("├─────────────────────────────────────────────┤");
        System.out.println("│  新生代 (Young Generation)                  │");
        System.out.println("│  ├─ Eden区 (伊甸园)                         │");
        System.out.println("│  │   • 新对象分配的地方                     │");
        System.out.println("│  │   • 占新生代的80% (默认比例 8:1:1)       │");
        System.out.println("│  ├─ Survivor0 (From区)                      │");
        System.out.println("│  │   • 存放经过一次GC后存活的对象           │");
        System.out.println("│  │   • 占新生代的10%                        │");
        System.out.println("│  └─ Survivor1 (To区)                        │");
        System.out.println("│      • 复制算法的目标区域                   │");
        System.out.println("│      • 占新生代的10%                        │");
        System.out.println("├─────────────────────────────────────────────┤");
        System.out.println("│  老年代 (Old Generation/Tenured)            │");
        System.out.println("│  • 存放长期存活的对象                        │");
        System.out.println("│  • 存放大对象                                │");
        System.out.println("│  • 占堆内存的2/3 (默认比例 1:2)             │");
        System.out.println("└─────────────────────────────────────────────┘");

        System.out.println("\n当前堆内存信息:");
        System.out.println("  初始大小 (init)    : " + formatBytes(heapMemory.getInit()));
        System.out.println("  已使用 (used)      : " + formatBytes(heapMemory.getUsed()));
        System.out.println("  已提交 (committed) : " + formatBytes(heapMemory.getCommitted()));
        System.out.println("  最大值 (max)       : " + formatBytes(heapMemory.getMax()));
        System.out.println();
    }

    /**
     * 2. 对象分配演示
     */
    private static void demonstrateObjectAllocation() {
        System.out.println("【2. 对象分配演示】\n");

        System.out.println("对象分配的优先级顺序:");
        System.out.println("  1. 栈上分配 (逃逸分析优化)");
        System.out.println("  2. TLAB分配 (Thread Local Allocation Buffer)");
        System.out.println("  3. Eden区分配");
        System.out.println("  4. 老年代分配 (大对象或晋升对象)");

        System.out.println("\n演示：创建对象");
        List<Object> objects = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            // 创建小对象，优先在Eden区分配
            Object obj = new Object();
            objects.add(obj);
            System.out.println("  创建对象 " + (i + 1) + " → Eden区");
        }

        System.out.println("\n对象引用存储:");
        System.out.println("  • 对象本身: 存储在堆中");
        System.out.println("  • 对象引用: 存储在栈的局部变量表中");
        System.out.println("  • 实例变量: 存储在堆的对象内部");
        System.out.println();
    }

    /**
     * 3. Eden区分配演示
     */
    private static void demonstrateEdenAllocation() {
        System.out.println("【3. Eden区分配演示】\n");

        // 获取年轻代内存池
        List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
        MemoryPoolMXBean edenPool = null;
        MemoryPoolMXBean survivorPool = null;
        MemoryPoolMXBean oldGenPool = null;

        for (MemoryPoolMXBean pool : memoryPools) {
            String name = pool.getName();
            if (name.contains("Eden")) {
                edenPool = pool;
            } else if (name.contains("Survivor")) {
                survivorPool = pool;
            } else if (name.contains("Old") || name.contains("Tenured")) {
                oldGenPool = pool;
            }
        }

        System.out.println("分配前的内存状态:");
        if (edenPool != null) {
            printMemoryPoolInfo("Eden区", edenPool);
        }
        if (survivorPool != null) {
            printMemoryPoolInfo("Survivor区", survivorPool);
        }
        if (oldGenPool != null) {
            printMemoryPoolInfo("老年代", oldGenPool);
        }

        // 分配一些对象
        System.out.println("\n开始分配对象...");
        List<byte[]> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            // 分配1KB的小对象
            byte[] bytes = new byte[1024];
            list.add(bytes);
        }

        System.out.println("分配了10个1KB的对象");

        System.out.println("\n分配后的内存状态:");
        if (edenPool != null) {
            printMemoryPoolInfo("Eden区", edenPool);
        }
        System.out.println();
    }

    /**
     * 4. 大对象直接进入老年代
     * -XX:PretenureSizeThreshold 参数可以设置大对象阈值
     */
    private static void demonstrateLargeObjectAllocation() {
        System.out.println("【4. 大对象直接进入老年代】\n");

        System.out.println("大对象特征:");
        System.out.println("  • 需要大量连续内存空间的对象");
        System.out.println("  • 典型例子：大数组、大字符串");
        System.out.println("  • 通过-XX:PretenureSizeThreshold设置阈值");
        System.out.println("  • 默认情况下，某些收集器会自动判断");

        System.out.println("\n为什么大对象直接进入老年代？");
        System.out.println("  1. 避免在Eden和Survivor之间大量复制");
        System.out.println("  2. 避免提前触发GC");
        System.out.println("  3. Eden区空间有限，可能无法容纳大对象");

        // 获取老年代内存池
        List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
        MemoryPoolMXBean oldGenPool = null;
        for (MemoryPoolMXBean pool : memoryPools) {
            if (pool.getName().contains("Old") || pool.getName().contains("Tenured")) {
                oldGenPool = pool;
                break;
            }
        }

        if (oldGenPool != null) {
            System.out.println("\n分配大对象前:");
            printMemoryPoolInfo("老年代", oldGenPool);
        }

        // 分配大对象（10MB）
        System.out.println("\n分配大对象 (10MB数组)...");
        byte[] largeObject = new byte[10 * 1024 * 1024];
        System.out.println("大对象已分配");

        if (oldGenPool != null) {
            System.out.println("\n分配大对象后:");
            printMemoryPoolInfo("老年代", oldGenPool);
            System.out.println("(注意：老年代使用量增加，大对象直接进入老年代)");
        }

        // 保持引用，防止被GC
        largeObject[0] = 1;
        System.out.println();
    }

    /**
     * 5. 对象晋升演示
     * 对象在Survivor区每经过一次Minor GC，年龄+1
     * 当年龄达到阈值(默认15)时，晋升到老年代
     */
    private static void demonstrateObjectPromotion() {
        System.out.println("【5. 对象晋升演示】\n");

        System.out.println("对象晋升条件:");
        System.out.println("  1. 对象年龄达到阈值 (默认15，通过-XX:MaxTenuringThreshold设置)");
        System.out.println("  2. Survivor区空间不足");
        System.out.println("  3. 动态年龄判断 (相同年龄对象占Survivor一半以上)");

        System.out.println("\n对象年龄增长过程:");
        System.out.println("  新创建       → Eden区 (age=0)");
        System.out.println("  第1次Minor GC → Survivor区 (age=1)");
        System.out.println("  第2次Minor GC → Survivor区 (age=2)");
        System.out.println("  ...");
        System.out.println("  第15次Minor GC → 老年代 (age=15，晋升)");

        System.out.println("\n晋升流程:");
        System.out.println("  ┌────────┐  Minor GC   ┌──────────┐");
        System.out.println("  │ Eden区 │ ─────────→  │ Survivor │");
        System.out.println("  └────────┘             └──────────┘");
        System.out.println("                              │");
        System.out.println("                              │ age达到阈值");
        System.out.println("                              ↓");
        System.out.println("                         ┌─────────┐");
        System.out.println("                         │ 老年代   │");
        System.out.println("                         └─────────┘");

        // 创建一些对象，模拟晋升
        System.out.println("\n创建持久对象 (模拟长期存活):");
        List<Object> longLivedObjects = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Object obj = new Object();
            longLivedObjects.add(obj);
            System.out.println("  对象 " + (i + 1) + " 创建 (将在多次GC后晋升到老年代)");
        }
        System.out.println();
    }

    /**
     * 6. 堆内存监控
     */
    private static void monitorHeapMemory() {
        System.out.println("【6. 堆内存监控】\n");

        List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();

        System.out.println("各代内存详细信息:");
        for (MemoryPoolMXBean pool : memoryPools) {
            if (pool.getType() == MemoryType.HEAP) {
                System.out.println("\n" + pool.getName() + ":");
                MemoryUsage usage = pool.getUsage();
                MemoryUsage peakUsage = pool.getPeakUsage();

                System.out.println("  当前使用:");
                System.out.println("    已使用: " + formatBytes(usage.getUsed()));
                System.out.println("    已提交: " + formatBytes(usage.getCommitted()));
                System.out.println("    最大值: " + formatBytes(usage.getMax()));
                System.out.println("    使用率: " + String.format("%.2f%%",
                    usage.getUsed() * 100.0 / usage.getMax()));

                System.out.println("  峰值使用:");
                System.out.println("    峰值: " + formatBytes(peakUsage.getUsed()));
            }
        }

        // GC统计
        System.out.println("\nGC统计:");
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gc : gcBeans) {
            System.out.println("  " + gc.getName() + ":");
            System.out.println("    回收次数: " + gc.getCollectionCount());
            System.out.println("    回收耗时: " + gc.getCollectionTime() + "ms");
            String[] poolNames = gc.getMemoryPoolNames();
            System.out.println("    管理内存池: " + String.join(", ", poolNames));
        }
        System.out.println();
    }

    /**
     * 7. 堆内存参数
     */
    private static void displayHeapParameters() {
        System.out.println("【7. 常用堆内存参数】\n");

        System.out.println("堆大小设置:");
        System.out.println("  -Xms<size>              设置初始堆大小");
        System.out.println("  -Xmx<size>              设置最大堆大小");
        System.out.println("  -Xmn<size>              设置新生代大小");
        System.out.println("  -XX:NewRatio=<ratio>    设置老年代/新生代比例 (默认2)");
        System.out.println("  -XX:SurvivorRatio=<ratio> 设置Eden/Survivor比例 (默认8)");

        System.out.println("\n对象晋升参数:");
        System.out.println("  -XX:MaxTenuringThreshold=<n>      对象晋升年龄阈值 (默认15)");
        System.out.println("  -XX:PretenureSizeThreshold=<size> 大对象直接进入老年代的阈值");
        System.out.println("  -XX:TargetSurvivorRatio=<percent> Survivor区目标使用率");

        System.out.println("\n内存分配参数:");
        System.out.println("  -XX:+UseTLAB            启用TLAB (默认开启)");
        System.out.println("  -XX:TLABSize=<size>     设置TLAB大小");

        System.out.println("\n当前JVM参数:");
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMXBean.getInputArguments();
        if (arguments.isEmpty()) {
            System.out.println("  (使用默认参数)");
        } else {
            for (String arg : arguments) {
                if (arg.startsWith("-X") || arg.startsWith("-XX:")) {
                    System.out.println("  " + arg);
                }
            }
        }

        System.out.println("\n推荐配置:");
        System.out.println("  生产环境:");
        System.out.println("    -Xms4g -Xmx4g          (堆初始和最大值设置相同，避免动态扩容)");
        System.out.println("    -Xmn2g                 (新生代占堆的1/2到1/3)");
        System.out.println("    -XX:SurvivorRatio=8    (Eden:Survivor = 8:1)");
        System.out.println("    -XX:MaxTenuringThreshold=15  (默认值)");
        System.out.println();
    }

    /**
     * 打印内存池信息
     */
    private static void printMemoryPoolInfo(String name, MemoryPoolMXBean pool) {
        MemoryUsage usage = pool.getUsage();
        System.out.println("  " + name + ":");
        System.out.println("    已使用: " + formatBytes(usage.getUsed()) +
                         " / " + formatBytes(usage.getMax()) +
                         " (" + String.format("%.2f%%", usage.getUsed() * 100.0 / usage.getMax()) + ")");
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

    /**
     * TLAB (Thread Local Allocation Buffer) 演示
     * TLAB是线程私有的分配缓冲区，可以避免多线程竞争
     */
    public static void demonstrateTLAB() {
        System.out.println("【TLAB演示】\n");

        System.out.println("TLAB (Thread Local Allocation Buffer):");
        System.out.println("  • 每个线程在Eden区独占一块缓冲区");
        System.out.println("  • 避免多线程分配对象时的同步开销");
        System.out.println("  • 默认开启 (-XX:+UseTLAB)");
        System.out.println("  • 提高对象分配效率");

        System.out.println("\n对象分配流程 (启用TLAB):");
        System.out.println("  1. 线程尝试在自己的TLAB中分配对象");
        System.out.println("  2. 如果TLAB空间足够，直接分配 (无需同步)");
        System.out.println("  3. 如果TLAB空间不足，申请新的TLAB");
        System.out.println("  4. 如果申请失败，在Eden区共享空间分配 (需要同步)");

        // 多线程分配对象
        int threadCount = 3;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                List<Object> objects = new ArrayList<>();
                for (int j = 0; j < 1000; j++) {
                    objects.add(new Object()); // 在线程的TLAB中分配
                }
                System.out.println("线程 " + threadId + " 分配了1000个对象 (使用TLAB)");
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\nTLAB的优势:");
        System.out.println("  • 减少线程同步开销");
        System.out.println("  • 提高内存分配速度");
        System.out.println("  • 改善多线程程序性能");
    }
}
