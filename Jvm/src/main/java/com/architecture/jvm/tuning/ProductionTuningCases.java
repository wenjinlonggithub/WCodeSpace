package com.architecture.jvm.tuning;

import java.lang.management.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * 生产环境JVM调优实战案例
 *
 * 涵盖常见的性能问题及解决方案：
 * 1. OOM (Out Of Memory)
 * 2. CPU飙高
 * 3. 频繁GC
 * 4. 内存泄漏
 * 5. 死锁
 * 6. 响应变慢
 *
 * @author Architecture
 */
public class ProductionTuningCases {

    public static void main(String[] args) {
        System.out.println("=== 生产环境JVM调优实战案例 ===\n");

        // 1. OOM问题诊断
        diagnoseOOM();

        // 2. CPU飙高问题
        diagnoseCPUHigh();

        // 3. 频繁GC问题
        diagnoseFrequentGC();

        // 4. 内存泄漏排查
        diagnoseMemoryLeak();

        // 5. 死锁检测
        diagnoseDeadlock();

        // 6. 响应变慢诊断
        diagnoseSlowResponse();

        // 7. JVM参数调优
        tuningParameters();

        // 8. 监控和工具
        monitoringTools();
    }

    /**
     * 1. OOM问题诊断
     */
    private static void diagnoseOOM() {
        System.out.println("【案例1: OutOfMemoryError诊断】\n");

        System.out.println("常见的OOM类型:");

        System.out.println("\n  1. java.lang.OutOfMemoryError: Java heap space");
        System.out.println("     原因:");
        System.out.println("       • 堆内存不足");
        System.out.println("       • 内存泄漏");
        System.out.println("       • 对象过大或过多");
        System.out.println("     排查步骤:");
        System.out.println("       1) 查看堆内存配置: jmap -heap <pid>");
        System.out.println("       2) Dump堆快照: jmap -dump:format=b,file=heap.hprof <pid>");
        System.out.println("       3) 使用MAT分析heap.hprof");
        System.out.println("       4) 查找占用内存最大的对象");
        System.out.println("     解决方案:");
        System.out.println("       • 增加堆大小: -Xmx4g");
        System.out.println("       • 优化代码,减少对象创建");
        System.out.println("       • 修复内存泄漏");

        System.out.println("\n  2. java.lang.OutOfMemoryError: Metaspace");
        System.out.println("     原因:");
        System.out.println("       • 加载的类过多");
        System.out.println("       • 动态代理、CGLib生成大量类");
        System.out.println("       • 类未被卸载");
        System.out.println("     排查步骤:");
        System.out.println("       1) 查看元空间: jstat -gc <pid>");
        System.out.println("       2) 查看类加载: jstat -class <pid>");
        System.out.println("       3) Dump类信息: jcmd <pid> GC.class_histogram");
        System.out.println("     解决方案:");
        System.out.println("       • 增加元空间: -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m");
        System.out.println("       • 检查动态代理使用");
        System.out.println("       • 启用类卸载");

        System.out.println("\n  3. java.lang.OutOfMemoryError: Direct buffer memory");
        System.out.println("     原因:");
        System.out.println("       • DirectByteBuffer分配过多");
        System.out.println("       • NIO使用不当");
        System.out.println("     排查步骤:");
        System.out.println("       1) 查看直接内存: jconsole查看BufferPool");
        System.out.println("       2) 检查NIO代码");
        System.out.println("     解决方案:");
        System.out.println("       • 增加直接内存: -XX:MaxDirectMemorySize=512m");
        System.out.println("       • 及时释放DirectByteBuffer");

        System.out.println("\n  4. java.lang.OutOfMemoryError: unable to create new native thread");
        System.out.println("     原因:");
        System.out.println("       • 线程数超过系统限制");
        System.out.println("       • 线程泄漏");
        System.out.println("     排查步骤:");
        System.out.println("       1) 查看线程数: jstack <pid> | grep 'java.lang.Thread.State' | wc -l");
        System.out.println("       2) 查看系统限制: ulimit -u");
        System.out.println("     解决方案:");
        System.out.println("       • 减少线程数,使用线程池");
        System.out.println("       • 增加系统限制: ulimit -u 10000");
        System.out.println("       • 减小栈大小: -Xss256k");

        System.out.println("\n实战示例:");
        System.out.println("  问题: 电商系统在促销活动时频繁OOM");
        System.out.println("  排查:");
        System.out.println("    1. jmap -heap <pid> 发现堆使用率99%");
        System.out.println("    2. jmap -dump获取堆快照");
        System.out.println("    3. MAT分析发现大量订单对象未释放");
        System.out.println("    4. 定位到缓存逻辑未设置过期时间");
        System.out.println("  解决:");
        System.out.println("    1. 设置缓存过期时间");
        System.out.println("    2. 使用LRU策略限制缓存大小");
        System.out.println("    3. 增加堆大小从2G到4G");
        System.out.println();
    }

    /**
     * 2. CPU飙高问题
     */
    private static void diagnoseCPUHigh() {
        System.out.println("【案例2: CPU使用率飙高】\n");

        System.out.println("可能原因:");
        System.out.println("  • 死循环");
        System.out.println("  • 频繁GC");
        System.out.println("  • 大量计算");
        System.out.println("  • 正则表达式回溯");

        System.out.println("\n排查步骤:");
        System.out.println("  1. 定位进程");
        System.out.println("     top -c");
        System.out.println("     找到CPU高的Java进程PID");

        System.out.println("\n  2. 定位线程");
        System.out.println("     top -Hp <pid>");
        System.out.println("     找到CPU高的线程TID");

        System.out.println("\n  3. 转换线程ID");
        System.out.println("     printf '%x\\n' <tid>");
        System.out.println("     将十进制TID转为十六进制");

        System.out.println("\n  4. 导出线程栈");
        System.out.println("     jstack <pid> | grep -A 50 <hex_tid>");
        System.out.println("     查看该线程的栈信息");

        System.out.println("\n  5. 分析代码");
        System.out.println("     根据栈信息定位问题代码");

        System.out.println("\n实战示例:");
        System.out.println("  问题: Web服务CPU持续100%");
        System.out.println("  排查:");
        System.out.println("    1. top找到Java进程PID: 12345");
        System.out.println("    2. top -Hp 12345找到线程TID: 12356");
        System.out.println("    3. printf '%x' 12356得到: 304c");
        System.out.println("    4. jstack 12345 | grep 304c -A 50");
        System.out.println("       发现在正则匹配方法中循环");
        System.out.println("    5. 检查代码,发现正则表达式回溯问题");
        System.out.println("  解决:");
        System.out.println("    • 优化正则表达式");
        System.out.println("    • 添加超时机制");
        System.out.println();
    }

    /**
     * 3. 频繁GC问题
     */
    private static void diagnoseFrequentGC() {
        System.out.println("【案例3: 频繁GC导致性能下降】\n");

        System.out.println("GC问题分类:");

        System.out.println("\n  1. Young GC频繁");
        System.out.println("     现象: Minor GC每秒多次");
        System.out.println("     原因:");
        System.out.println("       • 新生代太小");
        System.out.println("       • 短生命周期对象过多");
        System.out.println("     排查:");
        System.out.println("       jstat -gc <pid> 1000");
        System.out.println("       观察YGC频率和Eden区使用");
        System.out.println("     解决:");
        System.out.println("       • 增大新生代: -Xmn2g");
        System.out.println("       • 调整Eden/Survivor比例: -XX:SurvivorRatio=8");

        System.out.println("\n  2. Full GC频繁");
        System.out.println("     现象: Full GC每分钟多次");
        System.out.println("     原因:");
        System.out.println("       • 老年代空间不足");
        System.out.println("       • 元空间不足");
        System.out.println("       • System.gc()调用");
        System.out.println("     排查:");
        System.out.println("       jstat -gcutil <pid> 1000");
        System.out.println("       观察Old区使用率");
        System.out.println("     解决:");
        System.out.println("       • 增大堆大小: -Xmx4g");
        System.out.println("       • 优化对象生命周期");
        System.out.println("       • 禁用System.gc(): -XX:+DisableExplicitGC");

        System.out.println("\n  3. GC时间过长");
        System.out.println("     现象: 单次GC耗时超过1秒");
        System.out.println("     原因:");
        System.out.println("       • 堆过大");
        System.out.println("       • 对象过多");
        System.out.println("       • GC算法不合适");
        System.out.println("     排查:");
        System.out.println("       -XX:+PrintGCDetails -XX:+PrintGCTimeStamps");
        System.out.println("       分析GC日志");
        System.out.println("     解决:");
        System.out.println("       • 使用G1/ZGC: -XX:+UseG1GC");
        System.out.println("       • 调整GC线程数: -XX:ParallelGCThreads=8");

        System.out.println("\n实战示例:");
        System.out.println("  问题: 订单系统每分钟Full GC 5次");
        System.out.println("  排查:");
        System.out.println("    jstat -gcutil <pid> 1000");
        System.out.println("    S0     S1     E      O      M     CCS    YGC  YGCT   FGC  FGCT   GCT");
        System.out.println("    0.00  99.99  45.50  98.50  95.2  90.5   1500  15.2   300  50.5  65.7");
        System.out.println("    发现: Old区持续98%,频繁Full GC");
        System.out.println("  分析:");
        System.out.println("    1. Dump堆快照分析");
        System.out.println("    2. 发现大量订单缓存未释放");
        System.out.println("  解决:");
        System.out.println("    1. 增大堆大小: -Xmx4g (原2g)");
        System.out.println("    2. 优化缓存策略,添加过期时间");
        System.out.println("    3. 使用G1 GC: -XX:+UseG1GC");
        System.out.println();
    }

    /**
     * 4. 内存泄漏排查
     */
    private static void diagnoseMemoryLeak() {
        System.out.println("【案例4: 内存泄漏排查】\n");

        System.out.println("什么是内存泄漏？");
        System.out.println("  • 不再使用的对象无法被GC回收");
        System.out.println("  • 导致内存持续增长");
        System.out.println("  • 最终引发OOM");

        System.out.println("\n常见的内存泄漏场景:");
        System.out.println("  1. 静态集合持有对象引用");
        System.out.println("  2. 监听器未移除");
        System.out.println("  3. 内部类持有外部类引用");
        System.out.println("  4. ThreadLocal未清理");
        System.out.println("  5. 数据库连接/IO流未关闭");
        System.out.println("  6. 缓存无限增长");

        System.out.println("\n排查步骤:");
        System.out.println("  1. 监控内存趋势");
        System.out.println("     jstat -gcutil <pid> 1000 100");
        System.out.println("     观察Old区是否持续增长");

        System.out.println("\n  2. 获取两次堆快照");
        System.out.println("     jmap -dump:format=b,file=heap1.hprof <pid>");
        System.out.println("     (运行一段时间后)");
        System.out.println("     jmap -dump:format=b,file=heap2.hprof <pid>");

        System.out.println("\n  3. MAT对比分析");
        System.out.println("     • 打开heap1.hprof,生成Leak Suspects报告");
        System.out.println("     • 打开heap2.hprof,对比差异");
        System.out.println("     • 查找持续增长的对象");

        System.out.println("\n  4. 分析对象引用链");
        System.out.println("     • Path to GC Roots");
        System.out.println("     • 找到阻止对象被回收的引用");

        System.out.println("\n实战示例:");
        System.out.println("  问题: 应用运行几天后OOM");
        System.out.println("  现象:");
        System.out.println("    • 内存持续增长");
        System.out.println("    • Full GC无法回收");
        System.out.println("  排查:");
        System.out.println("    1. MAT分析heap dump");
        System.out.println("    2. 发现HashMap持有大量User对象");
        System.out.println("    3. 追踪GC Root,发现是静态缓存");
        System.out.println("    4. 代码审查,缓存只添加不删除");
        System.out.println("  解决:");
        System.out.println("    1. 使用WeakHashMap或软引用");
        System.out.println("    2. 添加缓存过期机制");
        System.out.println("    3. 限制缓存大小(LRU策略)");

        System.out.println("\n防止内存泄漏的最佳实践:");
        System.out.println("  ✓ 及时清理集合中的对象");
        System.out.println("  ✓ 使用try-with-resources");
        System.out.println("  ✓ 监听器记得移除");
        System.out.println("  ✓ ThreadLocal使用后remove()");
        System.out.println("  ✓ 使用弱引用/软引用缓存");
        System.out.println("  ✓ 定期review静态变量");
        System.out.println();
    }

    /**
     * 5. 死锁检测
     */
    private static void diagnoseDeadlock() {
        System.out.println("【案例5: 死锁检测与解决】\n");

        System.out.println("什么是死锁？");
        System.out.println("  两个或多个线程互相持有对方需要的资源,");
        System.out.println("  导致所有线程都无法继续执行");

        System.out.println("\n死锁的四个必要条件:");
        System.out.println("  1. 互斥条件 - 资源不能共享");
        System.out.println("  2. 持有并等待 - 持有资源的同时等待其他资源");
        System.out.println("  3. 不可剥夺 - 资源不能被强制释放");
        System.out.println("  4. 循环等待 - 形成等待环路");

        System.out.println("\n检测死锁:");
        System.out.println("  1. jstack检测");
        System.out.println("     jstack <pid>");
        System.out.println("     查找\"Found one Java-level deadlock:\"");

        System.out.println("\n  2. jconsole可视化");
        System.out.println("     连接JVM,点击\"线程\"标签");
        System.out.println("     点击\"检测死锁\"按钮");

        System.out.println("\n  3. ThreadMXBean编程检测");
        System.out.println("     ThreadMXBean.findDeadlockedThreads()");

        // 演示死锁检测
        System.out.println("\n编程检测死锁:");
        detectDeadlock();

        System.out.println("\n解决死锁:");
        System.out.println("  预防:");
        System.out.println("    • 避免嵌套锁");
        System.out.println("    • 固定加锁顺序");
        System.out.println("    • 使用tryLock()带超时");
        System.out.println("    • 使用并发工具类(Lock、Semaphore)");

        System.out.println("\n  恢复:");
        System.out.println("    • 重启应用(临时方案)");
        System.out.println("    • 释放部分资源");
        System.out.println("    • 杀死死锁线程");
        System.out.println();
    }

    private static void detectDeadlock() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();

        if (deadlockedThreads == null || deadlockedThreads.length == 0) {
            System.out.println("  未检测到死锁");
        } else {
            System.out.println("  检测到死锁! 涉及 " + deadlockedThreads.length + " 个线程");
            ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(deadlockedThreads);
            for (ThreadInfo info : threadInfos) {
                System.out.println("  线程: " + info.getThreadName());
                System.out.println("  状态: " + info.getThreadState());
            }
        }
    }

    /**
     * 6. 响应变慢诊断
     */
    private static void diagnoseSlowResponse() {
        System.out.println("【案例6: 响应时间变慢】\n");

        System.out.println("可能原因:");
        System.out.println("  • 频繁GC导致STW");
        System.out.println("  • 数据库慢查询");
        System.out.println("  • 网络延迟");
        System.out.println("  • 锁竞争");
        System.out.println("  • 线程池满");

        System.out.println("\n诊断步骤:");
        System.out.println("  1. 查看GC情况");
        System.out.println("     jstat -gcutil <pid> 1000");
        System.out.println("     关注FGCT(Full GC总耗时)");

        System.out.println("\n  2. 查看线程状态");
        System.out.println("     jstack <pid>");
        System.out.println("     统计BLOCKED、WAITING线程数");

        System.out.println("\n  3. 分析慢方法");
        System.out.println("     使用JProfiler/Arthas profiler");
        System.out.println("     找出耗时最长的方法");

        System.out.println("\n  4. 检查数据库");
        System.out.println("     开启慢查询日志");
        System.out.println("     分析执行计划");

        System.out.println("\n实战示例:");
        System.out.println("  问题: API响应从50ms增加到500ms");
        System.out.println("  排查:");
        System.out.println("    1. jstat发现Young GC每秒3次,每次50ms");
        System.out.println("    2. jstack发现大量线程WAITING");
        System.out.println("    3. profiler显示JSON序列化耗时高");
        System.out.println("  分析:");
        System.out.println("    • 新生代太小,频繁Young GC");
        System.out.println("    • 对象创建过多");
        System.out.println("    • JSON库性能差");
        System.out.println("  解决:");
        System.out.println("    1. 增大新生代: -Xmn1g");
        System.out.println("    2. 对象复用(池化)");
        System.out.println("    3. 更换JSON库(Jackson→fastjson)");
        System.out.println();
    }

    /**
     * 7. JVM参数调优
     */
    private static void tuningParameters() {
        System.out.println("【7. JVM参数调优建议】\n");

        System.out.println("堆内存配置:");
        System.out.println("  -Xms4g -Xmx4g           # 初始和最大堆相同,避免扩容");
        System.out.println("  -Xmn2g                  # 新生代1/2到1/3堆大小");
        System.out.println("  -XX:SurvivorRatio=8     # Eden:Survivor = 8:1");

        System.out.println("\nGC选择:");
        System.out.println("  # 响应时间优先(Web应用)");
        System.out.println("  -XX:+UseG1GC");
        System.out.println("  -XX:MaxGCPauseMillis=200");
        System.out.println("  -XX:G1HeapRegionSize=16m");

        System.out.println("\n  # 吞吐量优先(批处理)");
        System.out.println("  -XX:+UseParallelGC");
        System.out.println("  -XX:ParallelGCThreads=8");
        System.out.println("  -XX:GCTimeRatio=99");

        System.out.println("\n  # 超大堆/低延迟");
        System.out.println("  -XX:+UseZGC");
        System.out.println("  -XX:ZCollectionInterval=120");

        System.out.println("\nGC日志:");
        System.out.println("  -XX:+PrintGCDetails");
        System.out.println("  -XX:+PrintGCTimeStamps");
        System.out.println("  -XX:+PrintGCDateStamps");
        System.out.println("  -Xloggc:/path/to/gc.log");
        System.out.println("  -XX:+UseGCLogFileRotation");
        System.out.println("  -XX:NumberOfGCLogFiles=10");
        System.out.println("  -XX:GCLogFileSize=100M");

        System.out.println("\nOOM时Dump:");
        System.out.println("  -XX:+HeapDumpOnOutOfMemoryError");
        System.out.println("  -XX:HeapDumpPath=/path/to/dumps/");

        System.out.println("\n元空间:");
        System.out.println("  -XX:MetaspaceSize=256m");
        System.out.println("  -XX:MaxMetaspaceSize=512m");

        System.out.println("\n直接内存:");
        System.out.println("  -XX:MaxDirectMemorySize=512m");

        System.out.println("\n线程栈:");
        System.out.println("  -Xss512k                # 减小栈大小可增加线程数");

        System.out.println("\n完整示例 (8G内存Web应用):");
        System.out.println("  java \\");
        System.out.println("    -Xms4g -Xmx4g \\");
        System.out.println("    -Xmn2g \\");
        System.out.println("    -XX:SurvivorRatio=8 \\");
        System.out.println("    -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m \\");
        System.out.println("    -XX:+UseG1GC \\");
        System.out.println("    -XX:MaxGCPauseMillis=200 \\");
        System.out.println("    -XX:+PrintGCDetails \\");
        System.out.println("    -Xloggc:/var/log/app/gc.log \\");
        System.out.println("    -XX:+HeapDumpOnOutOfMemoryError \\");
        System.out.println("    -XX:HeapDumpPath=/var/log/app/dumps/ \\");
        System.out.println("    -jar app.jar");
        System.out.println();
    }

    /**
     * 8. 监控和工具
     */
    private static void monitoringTools() {
        System.out.println("【8. JVM监控和诊断工具】\n");

        System.out.println("命令行工具:");
        System.out.println("  jps        # 查看Java进程");
        System.out.println("  jstat      # 查看GC统计");
        System.out.println("  jmap       # Dump堆,查看堆信息");
        System.out.println("  jstack     # Dump线程栈");
        System.out.println("  jinfo      # 查看JVM配置");
        System.out.println("  jcmd       # 多功能诊断工具");

        System.out.println("\n可视化工具:");
        System.out.println("  JConsole   # JDK自带监控工具");
        System.out.println("  VisualVM   # 功能强大的Profile工具");
        System.out.println("  JProfiler  # 商业Profile工具");
        System.out.println("  MAT        # 内存分析工具");
        System.out.println("  GCViewer   # GC日志分析");
        System.out.println("  Arthas     # 阿里开源诊断工具");

        System.out.println("\n在线监控:");
        System.out.println("  Prometheus + Grafana  # 指标监控");
        System.out.println("  SkyWalking           # APM系统");
        System.out.println("  Pinpoint             # APM系统");

        System.out.println("\n常用诊断命令:");
        System.out.println("  # 查看GC情况");
        System.out.println("  jstat -gcutil <pid> 1000");

        System.out.println("\n  # Dump堆快照");
        System.out.println("  jmap -dump:live,format=b,file=heap.hprof <pid>");

        System.out.println("\n  # 导出线程栈");
        System.out.println("  jstack <pid> > thread.txt");

        System.out.println("\n  # 查看JVM参数");
        System.out.println("  jinfo -flags <pid>");

        System.out.println("\n  # 强制Full GC");
        System.out.println("  jcmd <pid> GC.run");

        System.out.println("\n  # 查看类统计");
        System.out.println("  jcmd <pid> GC.class_histogram");
        System.out.println();
    }
}
