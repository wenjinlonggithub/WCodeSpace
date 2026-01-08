package com.architecture.jvm.tuning;

import java.lang.management.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JVM监控工具集合
 * 
 * 展示各种JVM监控和诊断工具的使用方法
 */
public class JVMMonitoringTools {
    
    public static void main(String[] args) {
        JVMMonitoringTools tools = new JVMMonitoringTools();
        
        System.out.println("=== JVM监控工具演示 ===\n");
        
        // 1. 内存监控
        tools.memoryMonitoring();
        
        // 2. GC监控
        tools.gcMonitoring();
        
        // 3. 线程监控
        tools.threadMonitoring();
        
        // 4. 类加载监控
        tools.classLoadingMonitoring();
        
        // 5. JIT编译监控
        tools.compilationMonitoring();
        
        // 6. 系统属性监控
        tools.systemPropertiesMonitoring();
        
        // 7. 命令行工具演示
        tools.commandLineToolsDemo();
    }
    
    /**
     * 内存监控
     */
    private void memoryMonitoring() {
        System.out.println("=== 内存监控 ===");
        
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
        
        // 堆内存监控
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        System.out.println("堆内存使用情况:");
        printMemoryUsage("堆内存", heapUsage);
        
        // 非堆内存监控
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        System.out.println("\n非堆内存使用情况:");
        printMemoryUsage("非堆内存", nonHeapUsage);
        
        // 各个内存池详细信息
        System.out.println("\n各内存池详细信息:");
        for (MemoryPoolMXBean pool : memoryPools) {
            System.out.println("内存池: " + pool.getName());
            System.out.println("  类型: " + pool.getType());
            
            MemoryUsage usage = pool.getUsage();
            if (usage != null) {
                System.out.printf("  使用量: %d MB / %d MB (%.2f%%)\n",
                        usage.getUsed() / 1024 / 1024,
                        usage.getMax() > 0 ? usage.getMax() / 1024 / 1024 : -1,
                        usage.getMax() > 0 ? (double) usage.getUsed() / usage.getMax() * 100 : 0);
            }
            
            // 使用阈值监控
            if (pool.isUsageThresholdSupported()) {
                System.out.println("  支持使用阈值监控");
                System.out.println("  阈值: " + pool.getUsageThreshold() / 1024 / 1024 + " MB");
                System.out.println("  超出次数: " + pool.getUsageThresholdCount());
            }
        }
        
        System.out.println();
    }
    
    /**
     * GC监控
     */
    private void gcMonitoring() {
        System.out.println("=== GC监控 ===");
        
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        
        System.out.println("垃圾收集器信息:");
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            System.out.println("收集器: " + gcBean.getName());
            System.out.println("  收集次数: " + gcBean.getCollectionCount());
            System.out.println("  累计收集时间: " + gcBean.getCollectionTime() + " ms");
            
            // 计算平均收集时间
            if (gcBean.getCollectionCount() > 0) {
                double avgTime = (double) gcBean.getCollectionTime() / gcBean.getCollectionCount();
                System.out.printf("  平均收集时间: %.2f ms\n", avgTime);
            }
            
            // 管理的内存池
            String[] poolNames = gcBean.getMemoryPoolNames();
            System.out.println("  管理的内存池: " + String.join(", ", poolNames));
            System.out.println("  是否有效: " + gcBean.isValid());
            System.out.println();
        }
        
        // GC效率分析
        analyzeGCEfficiency();
        
        System.out.println();
    }
    
    /**
     * 线程监控
     */
    private void threadMonitoring() {
        System.out.println("=== 线程监控 ===");
        
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        
        // 基本线程信息
        System.out.println("线程基本信息:");
        System.out.println("  当前线程数: " + threadBean.getThreadCount());
        System.out.println("  峰值线程数: " + threadBean.getPeakThreadCount());
        System.out.println("  守护线程数: " + threadBean.getDaemonThreadCount());
        System.out.println("  总启动线程数: " + threadBean.getTotalStartedThreadCount());
        
        // 死锁检测
        long[] deadlockedThreads = threadBean.findDeadlockedThreads();
        if (deadlockedThreads != null) {
            System.out.println("  检测到死锁线程数: " + deadlockedThreads.length);
        } else {
            System.out.println("  无死锁线程");
        }
        
        // CPU时间监控（如果支持）
        if (threadBean.isThreadCpuTimeSupported()) {
            System.out.println("\nCPU时间监控:");
            System.out.println("  支持线程CPU时间监控: " + threadBean.isThreadCpuTimeEnabled());
            
            // 获取当前线程CPU时间
            long currentThreadId = Thread.currentThread().getId();
            long cpuTime = threadBean.getThreadCpuTime(currentThreadId);
            long userTime = threadBean.getThreadUserTime(currentThreadId);
            
            System.out.printf("  当前线程CPU时间: %d ms\n", cpuTime / 1000000);
            System.out.printf("  当前线程用户时间: %d ms\n", userTime / 1000000);
        }
        
        // 线程争用监控
        if (threadBean.isThreadContentionMonitoringSupported()) {
            System.out.println("\n线程争用监控:");
            System.out.println("  支持线程争用监控: " + threadBean.isThreadContentionMonitoringEnabled());
        }
        
        System.out.println();
    }
    
    /**
     * 类加载监控
     */
    private void classLoadingMonitoring() {
        System.out.println("=== 类加载监控 ===");
        
        ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
        
        System.out.println("类加载统计:");
        System.out.println("  当前已加载类数量: " + classLoadingBean.getLoadedClassCount());
        System.out.println("  累计已加载类数量: " + classLoadingBean.getTotalLoadedClassCount());
        System.out.println("  累计卸载类数量: " + classLoadingBean.getUnloadedClassCount());
        System.out.println("  详细输出启用: " + classLoadingBean.isVerbose());
        
        // 计算类加载效率
        long totalLoaded = classLoadingBean.getTotalLoadedClassCount();
        long currentLoaded = classLoadingBean.getLoadedClassCount();
        long unloaded = classLoadingBean.getUnloadedClassCount();
        
        System.out.printf("  类卸载率: %.2f%%\n", 
                (double) unloaded / totalLoaded * 100);
        System.out.printf("  当前类保留率: %.2f%%\n",
                (double) currentLoaded / totalLoaded * 100);
        
        System.out.println();
    }
    
    /**
     * JIT编译监控
     */
    private void compilationMonitoring() {
        System.out.println("=== JIT编译监控 ===");
        
        CompilationMXBean compilationBean = ManagementFactory.getCompilationMXBean();
        
        if (compilationBean != null) {
            System.out.println("编译器信息:");
            System.out.println("  编译器名称: " + compilationBean.getName());
            
            if (compilationBean.isCompilationTimeMonitoringSupported()) {
                System.out.println("  累计编译时间: " + compilationBean.getTotalCompilationTime() + " ms");
                
                // 估算编译效率
                Runtime runtime = Runtime.getRuntime();
                long totalMemory = runtime.totalMemory();
                long usedMemory = totalMemory - runtime.freeMemory();
                
                System.out.println("  JVM运行时信息:");
                System.out.println("    可用处理器数: " + runtime.availableProcessors());
                System.out.printf("    已用内存: %d MB\n", usedMemory / 1024 / 1024);
                System.out.printf("    总内存: %d MB\n", totalMemory / 1024 / 1024);
                System.out.printf("    最大内存: %d MB\n", runtime.maxMemory() / 1024 / 1024);
            } else {
                System.out.println("  不支持编译时间监控");
            }
        } else {
            System.out.println("JIT编译器不可用");
        }
        
        System.out.println();
    }
    
    /**
     * 系统属性监控
     */
    private void systemPropertiesMonitoring() {
        System.out.println("=== 系统属性监控 ===");
        
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        
        // JVM运行时信息
        System.out.println("JVM运行时信息:");
        System.out.println("  JVM名称: " + runtimeBean.getVmName());
        System.out.println("  JVM版本: " + runtimeBean.getVmVersion());
        System.out.println("  JVM厂商: " + runtimeBean.getVmVendor());
        System.out.println("  启动时间: " + new java.util.Date(runtimeBean.getStartTime()));
        System.out.println("  运行时长: " + runtimeBean.getUptime() / 1000 + " 秒");
        
        // JVM参数
        System.out.println("\nJVM输入参数:");
        List<String> inputArguments = runtimeBean.getInputArguments();
        for (String arg : inputArguments) {
            System.out.println("  " + arg);
        }
        
        // 操作系统信息
        System.out.println("\n操作系统信息:");
        System.out.println("  操作系统名称: " + osBean.getName());
        System.out.println("  操作系统版本: " + osBean.getVersion());
        System.out.println("  操作系统架构: " + osBean.getArch());
        System.out.println("  可用处理器数: " + osBean.getAvailableProcessors());
        
        // 系统负载（如果支持）
        double systemLoadAverage = osBean.getSystemLoadAverage();
        if (systemLoadAverage >= 0) {
            System.out.printf("  系统负载平均值: %.2f\n", systemLoadAverage);
        }
        
        System.out.println();
    }
    
    /**
     * 命令行工具演示
     */
    private void commandLineToolsDemo() {
        System.out.println("=== 常用命令行工具 ===");
        
        // 获取当前进程ID
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        String pid = processName.split("@")[0];
        
        System.out.println("当前进程ID: " + pid);
        System.out.println("\n常用命令行工具使用方法:");
        
        // jstat工具
        System.out.println("1. jstat - JVM统计信息工具");
        System.out.println("   jstat -gc " + pid + " 1s        # 每秒显示GC信息");
        System.out.println("   jstat -gcutil " + pid + " 1s    # 显示GC利用率");
        System.out.println("   jstat -class " + pid + "        # 显示类加载信息");
        System.out.println("   jstat -compiler " + pid + "     # 显示JIT编译信息");
        
        // jmap工具
        System.out.println("\n2. jmap - 内存映射工具");
        System.out.println("   jmap -heap " + pid + "          # 显示堆信息");
        System.out.println("   jmap -histo " + pid + "         # 显示对象统计");
        System.out.println("   jmap -dump:format=b,file=heap.hprof " + pid + "  # 生成堆转储");
        
        // jstack工具
        System.out.println("\n3. jstack - 线程堆栈工具");
        System.out.println("   jstack " + pid + "              # 显示线程堆栈");
        System.out.println("   jstack -l " + pid + "           # 显示锁信息");
        
        // jinfo工具
        System.out.println("\n4. jinfo - 配置信息工具");
        System.out.println("   jinfo " + pid + "               # 显示JVM参数");
        System.out.println("   jinfo -flags " + pid + "        # 显示JVM标志");
        System.out.println("   jinfo -sysprops " + pid + "     # 显示系统属性");
        
        // jcmd工具
        System.out.println("\n5. jcmd - 诊断命令工具");
        System.out.println("   jcmd " + pid + " help           # 显示可用命令");
        System.out.println("   jcmd " + pid + " VM.flags       # 显示JVM标志");
        System.out.println("   jcmd " + pid + " GC.run         # 执行GC");
        System.out.println("   jcmd " + pid + " Thread.print   # 打印线程信息");
        
        // VisualVM
        System.out.println("\n6. 图形化工具");
        System.out.println("   jvisualvm                       # 启动VisualVM");
        System.out.println("   jconsole                        # 启动JConsole");
        
        System.out.println();
    }
    
    /**
     * 分析GC效率
     */
    private void analyzeGCEfficiency() {
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        
        long totalGCTime = 0;
        long totalGCCount = 0;
        
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            totalGCTime += gcBean.getCollectionTime();
            totalGCCount += gcBean.getCollectionCount();
        }
        
        // 计算GC效率指标
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        long uptime = runtimeBean.getUptime();
        
        double gcTimePercentage = (double) totalGCTime / uptime * 100;
        
        System.out.println("GC效率分析:");
        System.out.printf("  总GC时间: %d ms\n", totalGCTime);
        System.out.printf("  总GC次数: %d\n", totalGCCount);
        System.out.printf("  JVM运行时间: %d ms\n", uptime);
        System.out.printf("  GC时间占比: %.2f%%\n", gcTimePercentage);
        
        if (totalGCCount > 0) {
            double avgGCTime = (double) totalGCTime / totalGCCount;
            System.out.printf("  平均GC时间: %.2f ms\n", avgGCTime);
        }
        
        // 给出建议
        System.out.println("  效率评估:");
        if (gcTimePercentage < 5) {
            System.out.println("    ✓ GC效率良好");
        } else if (gcTimePercentage < 10) {
            System.out.println("    ⚠ GC效率一般，建议优化");
        } else {
            System.out.println("    ✗ GC效率差，急需优化");
        }
    }
    
    /**
     * 打印内存使用信息
     */
    private void printMemoryUsage(String name, MemoryUsage usage) {
        System.out.printf("%s:\n", name);
        System.out.printf("  初始: %d MB\n", usage.getInit() / 1024 / 1024);
        System.out.printf("  已用: %d MB\n", usage.getUsed() / 1024 / 1024);
        System.out.printf("  已提交: %d MB\n", usage.getCommitted() / 1024 / 1024);
        
        if (usage.getMax() > 0) {
            System.out.printf("  最大: %d MB\n", usage.getMax() / 1024 / 1024);
            System.out.printf("  使用率: %.2f%%\n", 
                    (double) usage.getUsed() / usage.getMax() * 100);
        } else {
            System.out.println("  最大: 未设定");
        }
    }
}