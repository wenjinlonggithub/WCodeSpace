package com.architecture.jvm.execution;

import java.lang.management.*;

/**
 * JIT即时编译器深度演示
 *
 * JIT (Just-In-Time) 编译器是JVM执行引擎的核心组件
 *
 * 执行引擎的两种执行方式：
 * 1. 解释执行 (Interpreter) - 逐行解释字节码
 * 2. 编译执行 (JIT Compiler) - 将热点代码编译为本地机器码
 *
 * HotSpot虚拟机包含两个JIT编译器：
 * - C1编译器 (Client Compiler) - 快速编译，优化较少
 * - C2编译器 (Server Compiler) - 编译慢，优化激进
 *
 * 分层编译 (Tiered Compilation):
 * Level 0: 解释执行
 * Level 1: C1编译，无profiling
 * Level 2: C1编译，仅方法及回边计数器profiling
 * Level 3: C1编译，完全profiling
 * Level 4: C2编译，完全优化
 *
 * @author Architecture
 */
public class JITCompilationDemo {

    private static final int ITERATIONS = 100000;

    public static void main(String[] args) {
        System.out.println("=== JIT即时编译器深度演示 ===\n");

        // 1. JIT编译器简介
        introduceJITCompiler();

        // 2. 热点代码检测
        demonstrateHotspotDetection();

        // 3. 编译优化演示
        demonstrateCompilationOptimization();

        // 4. 逃逸分析
        demonstrateEscapeAnalysis();

        // 5. 方法内联
        demonstrateMethodInlining();

        // 6. 编译器优化技术
        discussOptimizationTechniques();

        // 7. JIT参数配置
        displayJITParameters();

        // 8. 查看编译信息
        displayCompilationInfo();
    }

    /**
     * 1. JIT编译器简介
     */
    private static void introduceJITCompiler() {
        System.out.println("【1. JIT编译器简介】\n");

        System.out.println("执行引擎的工作模式:");
        System.out.println("  解释执行:");
        System.out.println("    字节码 → 解释器 → 逐条执行");
        System.out.println("    优点: 启动快");
        System.out.println("    缺点: 执行慢");

        System.out.println("\n  JIT编译执行:");
        System.out.println("    字节码 → JIT编译器 → 本地机器码 → 执行");
        System.out.println("    优点: 执行快");
        System.out.println("    缺点: 编译有开销");

        System.out.println("\n混合模式 (Mixed Mode):");
        System.out.println("  1. 程序启动时，解释执行");
        System.out.println("  2. 运行过程中，监控方法调用频率");
        System.out.println("  3. 热点代码触发JIT编译");
        System.out.println("  4. 编译后的代码缓存起来");
        System.out.println("  5. 后续调用直接执行本地代码");

        System.out.println("\nHotSpot的两个编译器:");
        System.out.println("  ┌────────────────────────────────────┐");
        System.out.println("  │ C1 (Client Compiler)               │");
        System.out.println("  ├────────────────────────────────────┤");
        System.out.println("  │ • 编译速度快                        │");
        System.out.println("  │ • 优化较少                          │");
        System.out.println("  │ • 适合客户端应用                     │");
        System.out.println("  └────────────────────────────────────┘");

        System.out.println("  ┌────────────────────────────────────┐");
        System.out.println("  │ C2 (Server Compiler)               │");
        System.out.println("  ├────────────────────────────────────┤");
        System.out.println("  │ • 编译速度慢                        │");
        System.out.println("  │ • 激进优化                          │");
        System.out.println("  │ • 适合服务器应用                     │");
        System.out.println("  └────────────────────────────────────┘");

        System.out.println("\n分层编译 (JDK 7+默认开启):");
        System.out.println("  Level 0 → 解释执行");
        System.out.println("  Level 1 → C1编译 (简单优化)");
        System.out.println("  Level 2 → C1编译 + 调用计数器");
        System.out.println("  Level 3 → C1编译 + 完整profiling");
        System.out.println("  Level 4 → C2编译 (完全优化)");
        System.out.println();
    }

    /**
     * 2. 热点代码检测
     */
    private static void demonstrateHotspotDetection() {
        System.out.println("【2. 热点代码检测】\n");

        System.out.println("什么是热点代码？");
        System.out.println("  • 被多次调用的方法");
        System.out.println("  • 被多次执行的循环体");

        System.out.println("\n热点检测方式:");
        System.out.println("  1. 基于采样 (Sample Based)");
        System.out.println("     • 周期性检查线程栈顶方法");
        System.out.println("     • 统计出现频率高的方法");
        System.out.println("     • 简单但不精确");

        System.out.println("\n  2. 基于计数器 (Counter Based) ← HotSpot使用");
        System.out.println("     • 方法调用计数器 (Invocation Counter)");
        System.out.println("       - 统计方法调用次数");
        System.out.println("       - 默认阈值: Client 1500, Server 10000");
        System.out.println("     • 回边计数器 (Back Edge Counter)");
        System.out.println("       - 统计循环体执行次数");
        System.out.println("       - 用于OSR (On Stack Replacement)");

        System.out.println("\n热点代码编译流程:");
        System.out.println("  1. 方法被调用/循环执行");
        System.out.println("  2. 计数器增加");
        System.out.println("  3. 达到阈值");
        System.out.println("  4. 提交编译请求到编译队列");
        System.out.println("  5. 编译线程异步编译");
        System.out.println("  6. 编译完成，替换解释执行");

        // 演示热点方法
        System.out.println("\n演示：触发JIT编译");
        long start = System.currentTimeMillis();

        for (int i = 0; i < ITERATIONS; i++) {
            hotMethod(i); // 热点方法，会被JIT编译
        }

        long end = System.currentTimeMillis();
        System.out.println("执行 " + ITERATIONS + " 次热点方法");
        System.out.println("耗时: " + (end - start) + " ms");
        System.out.println("(多次调用后，JIT会将其编译为本地代码)");
        System.out.println();
    }

    private static int hotMethod(int n) {
        return n * 2 + 1;
    }

    /**
     * 3. 编译优化演示
     */
    private static void demonstrateCompilationOptimization() {
        System.out.println("【3. 编译优化演示】\n");

        System.out.println("JIT编译器的优化技术:");

        // 演示1: 常量折叠
        System.out.println("1. 常量折叠 (Constant Folding):");
        System.out.println("   优化前: int a = 10 * 20 + 30;");
        System.out.println("   优化后: int a = 230;");

        // 演示2: 死代码消除
        System.out.println("\n2. 死代码消除 (Dead Code Elimination):");
        System.out.println("   优化前: if (false) { ... }");
        System.out.println("   优化后: (代码被完全移除)");

        // 演示3: 循环展开
        System.out.println("\n3. 循环展开 (Loop Unrolling):");
        System.out.println("   优化前:");
        System.out.println("     for (int i = 0; i < 4; i++) {");
        System.out.println("       sum += array[i];");
        System.out.println("     }");
        System.out.println("   优化后:");
        System.out.println("     sum += array[0];");
        System.out.println("     sum += array[1];");
        System.out.println("     sum += array[2];");
        System.out.println("     sum += array[3];");

        // 演示4: 公共子表达式消除
        System.out.println("\n4. 公共子表达式消除 (CSE):");
        System.out.println("   优化前:");
        System.out.println("     int a = x * y + 1;");
        System.out.println("     int b = x * y + 2;");
        System.out.println("   优化后:");
        System.out.println("     int temp = x * y;");
        System.out.println("     int a = temp + 1;");
        System.out.println("     int b = temp + 2;");

        System.out.println("\n性能对比:");
        benchmarkOptimization();
        System.out.println();
    }

    private static void benchmarkOptimization() {
        int iterations = 10000000;

        // 未优化版本
        long start1 = System.currentTimeMillis();
        long sum1 = 0;
        for (int i = 0; i < iterations; i++) {
            sum1 += unoptimizedMethod(i);
        }
        long time1 = System.currentTimeMillis() - start1;

        // 优化版本
        long start2 = System.currentTimeMillis();
        long sum2 = 0;
        for (int i = 0; i < iterations; i++) {
            sum2 += optimizedMethod(i);
        }
        long time2 = System.currentTimeMillis() - start2;

        System.out.println("  未优化版本: " + time1 + " ms");
        System.out.println("  优化版本  : " + time2 + " ms");
        System.out.println("  性能提升  : " + String.format("%.2f%%",
            (time1 - time2) * 100.0 / time1));
    }

    private static int unoptimizedMethod(int n) {
        return n * n + n * n; // 重复计算
    }

    private static int optimizedMethod(int n) {
        int temp = n * n; // 消除重复计算
        return temp + temp;
    }

    /**
     * 4. 逃逸分析
     */
    private static void demonstrateEscapeAnalysis() {
        System.out.println("【4. 逃逸分析 (Escape Analysis)】\n");

        System.out.println("什么是逃逸分析？");
        System.out.println("  分析对象的作用域,判断对象是否会逃逸出方法或线程");

        System.out.println("\n逃逸状态:");
        System.out.println("  1. NoEscape - 对象不逃逸");
        System.out.println("     • 对象只在方法内使用");
        System.out.println("     • 不被外部引用");
        System.out.println("  2. ArgEscape - 参数逃逸");
        System.out.println("     • 对象作为参数传递");
        System.out.println("     • 但不会被外部保存");
        System.out.println("  3. GlobalEscape - 全局逃逸");
        System.out.println("     • 对象逃逸出方法");
        System.out.println("     • 可能被其他线程访问");

        System.out.println("\n基于逃逸分析的优化:");
        System.out.println("  1. 栈上分配 (Stack Allocation)");
        System.out.println("     • 不逃逸的对象分配在栈上");
        System.out.println("     • 方法结束自动回收");
        System.out.println("     • 减轻GC压力");

        System.out.println("\n  2. 标量替换 (Scalar Replacement)");
        System.out.println("     • 将对象拆分为基本类型");
        System.out.println("     • 直接使用局部变量");
        System.out.println("     • 避免对象创建");

        System.out.println("\n  3. 锁消除 (Lock Elimination)");
        System.out.println("     • 消除不可能竞争的锁");
        System.out.println("     • 提高同步性能");

        // 演示
        System.out.println("\n演示：不逃逸对象");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            noEscapeMethod(); // 对象不逃逸，可能被栈上分配
        }
        long time = System.currentTimeMillis() - start;
        System.out.println("  执行1000万次，耗时: " + time + " ms");
        System.out.println("  (启用逃逸分析后，对象可能在栈上分配)");
        System.out.println();
    }

    private static void noEscapeMethod() {
        Point p = new Point(1, 2); // 对象不逃逸
        int sum = p.x + p.y;
    }

    static class Point {
        int x, y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * 5. 方法内联
     */
    private static void demonstrateMethodInlining() {
        System.out.println("【5. 方法内联 (Method Inlining)】\n");

        System.out.println("什么是方法内联？");
        System.out.println("  将方法调用替换为方法体代码");

        System.out.println("\n优化前:");
        System.out.println("  int result = add(a, b);");
        System.out.println("  ");
        System.out.println("  int add(int x, int y) {");
        System.out.println("    return x + y;");
        System.out.println("  }");

        System.out.println("\n优化后 (内联):");
        System.out.println("  int result = a + b; // 直接展开");

        System.out.println("\n内联的好处:");
        System.out.println("  • 消除方法调用开销");
        System.out.println("  • 减少栈帧创建");
        System.out.println("  • 为进一步优化创造机会");

        System.out.println("\n内联条件:");
        System.out.println("  • 方法体较小 (默认 < 35字节码)");
        System.out.println("  • 调用频率高");
        System.out.println("  • 非虚方法或可去虚化的方法");

        System.out.println("\n内联限制:");
        System.out.println("  • 递归方法");
        System.out.println("  • 包含异常处理");
        System.out.println("  • 方法体过大");

        // 演示
        System.out.println("\n演示：方法内联优化");
        long start = System.currentTimeMillis();
        int sum = 0;
        for (int i = 0; i < 100000000; i++) {
            sum += inlineableMethod(i); // 小方法，会被内联
        }
        long time = System.currentTimeMillis() - start;
        System.out.println("  执行1亿次，耗时: " + time + " ms");
        System.out.println("  (小方法会被JIT内联，避免方法调用开销)");
        System.out.println();
    }

    private static int inlineableMethod(int n) {
        return n + 1; // 简单方法，容易被内联
    }

    /**
     * 6. 编译器优化技术总结
     */
    private static void discussOptimizationTechniques() {
        System.out.println("【6. JIT编译器优化技术】\n");

        System.out.println("数据流优化:");
        System.out.println("  • 常量传播");
        System.out.println("  • 常量折叠");
        System.out.println("  • 复写传播");
        System.out.println("  • 公共子表达式消除");
        System.out.println("  • 死代码消除");

        System.out.println("\n控制流优化:");
        System.out.println("  • 循环展开");
        System.out.println("  • 循环不变量外提");
        System.out.println("  • 循环倒置");
        System.out.println("  • 范围检查消除");

        System.out.println("\n对象优化:");
        System.out.println("  • 逃逸分析");
        System.out.println("  • 栈上分配");
        System.out.println("  • 标量替换");
        System.out.println("  • 锁消除/锁粗化");

        System.out.println("\n方法优化:");
        System.out.println("  • 方法内联");
        System.out.println("  • 去虚化 (Devirtualization)");
        System.out.println("  • 投机优化 (Speculative Optimization)");

        System.out.println("\n其他优化:");
        System.out.println("  • 空值检查消除");
        System.out.println("  • 类型检查消除");
        System.out.println("  • 数组边界检查消除");
        System.out.println("  • SIMD指令使用");
        System.out.println();
    }

    /**
     * 7. JIT参数配置
     */
    private static void displayJITParameters() {
        System.out.println("【7. JIT参数配置】\n");

        System.out.println("编译模式:");
        System.out.println("  -Xint                      纯解释模式");
        System.out.println("  -Xcomp                     纯编译模式");
        System.out.println("  -Xmixed                    混合模式 (默认)");

        System.out.println("\n编译器选择:");
        System.out.println("  -client                    使用C1编译器");
        System.out.println("  -server                    使用C2编译器");
        System.out.println("  -XX:+TieredCompilation     分层编译 (默认)");

        System.out.println("\n编译阈值:");
        System.out.println("  -XX:CompileThreshold=10000 方法调用次数阈值");
        System.out.println("  -XX:BackEdgeThreshold=100000 回边计数阈值");

        System.out.println("\n内联控制:");
        System.out.println("  -XX:MaxInlineSize=35       最大内联字节码大小");
        System.out.println("  -XX:FreqInlineSize=325     频繁调用方法内联大小");
        System.out.println("  -XX:+PrintInlining         打印内联信息");

        System.out.println("\n逃逸分析:");
        System.out.println("  -XX:+DoEscapeAnalysis      启用逃逸分析 (默认)");
        System.out.println("  -XX:+EliminateAllocations  启用标量替换");
        System.out.println("  -XX:+EliminateLocks        启用锁消除");

        System.out.println("\n编译日志:");
        System.out.println("  -XX:+PrintCompilation      打印编译信息");
        System.out.println("  -XX:+UnlockDiagnosticVMOptions");
        System.out.println("  -XX:+PrintInlining         打印内联决策");
        System.out.println("  -XX:+PrintAssembly         打印汇编代码(需hsdis)");
        System.out.println();
    }

    /**
     * 8. 查看编译信息
     */
    private static void displayCompilationInfo() {
        System.out.println("【8. 编译器信息】\n");

        CompilationMXBean compilationMXBean = ManagementFactory.getCompilationMXBean();
        if (compilationMXBean != null) {
            System.out.println("JIT编译器信息:");
            System.out.println("  编译器名称: " + compilationMXBean.getName());
            System.out.println("  总编译耗时: " + compilationMXBean.getTotalCompilationTime() + " ms");
        }

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        System.out.println("\nJVM信息:");
        System.out.println("  JVM名称: " + runtimeMXBean.getVmName());
        System.out.println("  JVM版本: " + runtimeMXBean.getVmVersion());
        System.out.println("  JVM供应商: " + runtimeMXBean.getVmVendor());

        System.out.println("\n如何查看JIT编译详情？");
        System.out.println("  1. 添加JVM参数: -XX:+PrintCompilation");
        System.out.println("  2. 输出格式: timestamp compilation_id attributes method_name size deopt");
        System.out.println("  3. 使用JITWatch工具可视化分析");
        System.out.println();
    }
}
