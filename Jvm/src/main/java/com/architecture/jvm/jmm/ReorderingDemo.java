package com.architecture.jvm.jmm;

/**
 * 指令重排序演示
 *
 * 演示经典的双重检查锁定问题（DCL）
 *
 * @author Architecture
 */
public class ReorderingDemo {

    public static void main(String[] args) {
        System.out.println("=== 指令重排序演示 ===\n");

        // 演示1: 错误的双重检查锁定
        demonstrateBrokenDCL();

        System.out.println("\n" + "=".repeat(80) + "\n");

        // 演示2: 正确的双重检查锁定
        demonstrateCorrectDCL();

        System.out.println("\n" + "=".repeat(80) + "\n");

        // 演示3: 其他重排序场景
        demonstrateOtherReordering();
    }

    /**
     * 演示错误的双重检查锁定 (DCL - Double-Checked Locking)
     */
    private static void demonstrateBrokenDCL() {
        System.out.println("【演示1: 错误的双重检查锁定】\n");

        System.out.println("❌ 错误的单例实现:");
        System.out.println("```java");
        System.out.println("class Singleton {");
        System.out.println("    private static Singleton instance;");
        System.out.println("    ");
        System.out.println("    public static Singleton getInstance() {");
        System.out.println("        if (instance == null) {              // 第一次检查");
        System.out.println("            synchronized (Singleton.class) {");
        System.out.println("                if (instance == null) {      // 第二次检查");
        System.out.println("                    instance = new Singleton(); // 问题所在!");
        System.out.println("                }");
        System.out.println("            }");
        System.out.println("        }");
        System.out.println("        return instance;");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("```");

        System.out.println("\n问题分析:");
        System.out.println("  instance = new Singleton() 并非原子操作，实际分为三步:");
        System.out.println("    1. memory = allocate();      // 分配内存空间");
        System.out.println("    2. ctorInstance(memory);     // 初始化对象");
        System.out.println("    3. instance = memory;        // 设置instance指向内存");

        System.out.println("\n  由于指令重排序，可能变成:");
        System.out.println("    1. memory = allocate();      // 分配内存空间");
        System.out.println("    3. instance = memory;        // 设置instance指向内存 (重排!)");
        System.out.println("    2. ctorInstance(memory);     // 初始化对象");

        System.out.println("\n并发问题:");
        System.out.println("  时刻  Thread A                  Thread B");
        System.out.println("  T1    if (instance == null)");
        System.out.println("  T2    synchronized块");
        System.out.println("  T3    分配内存");
        System.out.println("  T4    设置instance指向内存");
        System.out.println("        (此时instance != null)");
        System.out.println("  T5                              if (instance == null)");
        System.out.println("                                  // false，直接返回");
        System.out.println("  T6                              return instance;");
        System.out.println("                                  // ❌ 返回未初始化的对象!");
        System.out.println("  T7    初始化对象");

        System.out.println("\n后果:");
        System.out.println("  • Thread B获得了未完全初始化的对象");
        System.out.println("  • 使用该对象可能导致空指针异常或其他错误");
        System.out.println("  • 这是一个非常隐蔽的并发Bug!");
    }

    /**
     * 演示正确的双重检查锁定
     */
    private static void demonstrateCorrectDCL() {
        System.out.println("【演示2: 正确的双重检查锁定】\n");

        System.out.println("✓ 正确的单例实现 (使用volatile):");
        System.out.println("```java");
        System.out.println("class Singleton {");
        System.out.println("    private static volatile Singleton instance; // volatile!");
        System.out.println("    ");
        System.out.println("    public static Singleton getInstance() {");
        System.out.println("        if (instance == null) {");
        System.out.println("            synchronized (Singleton.class) {");
        System.out.println("                if (instance == null) {");
        System.out.println("                    instance = new Singleton();");
        System.out.println("                }");
        System.out.println("            }");
        System.out.println("        }");
        System.out.println("        return instance;");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("```");

        System.out.println("\nvolatile如何解决问题:");
        System.out.println("  volatile禁止重排序规则:");
        System.out.println("    • 禁止步骤2和步骤3重排序");
        System.out.println("    • 保证对象完全初始化后，才对其他线程可见");

        System.out.println("\n  内存屏障:");
        System.out.println("    1. memory = allocate();");
        System.out.println("    2. ctorInstance(memory);");
        System.out.println("       StoreStore屏障              // 禁止2和3重排序");
        System.out.println("    3. instance = memory; (volatile写)");
        System.out.println("       StoreLoad屏障");

        System.out.println("\n其他解决方案:");
        System.out.println("  方案1: 静态内部类 (推荐)");
        System.out.println("    class Singleton {");
        System.out.println("        private static class Holder {");
        System.out.println("            static final Singleton INSTANCE = new Singleton();");
        System.out.println("        }");
        System.out.println("        public static Singleton getInstance() {");
        System.out.println("            return Holder.INSTANCE;");
        System.out.println("        }");
        System.out.println("    }");

        System.out.println("\n  方案2: 枚举 (最佳)");
        System.out.println("    enum Singleton {");
        System.out.println("        INSTANCE;");
        System.out.println("    }");
    }

    /**
     * 演示其他重排序场景
     */
    private static void demonstrateOtherReordering() {
        System.out.println("【演示3: 其他重排序场景】\n");

        System.out.println("场景1: 经典的重排序示例");
        System.out.println("  初始状态: a = 0, b = 0, x = 0, y = 0\n");
        System.out.println("  Thread 1              Thread 2");
        System.out.println("  --------              --------");
        System.out.println("  a = 1;      (1)       b = 1;      (3)");
        System.out.println("  x = b;      (2)       y = a;      (4)");

        System.out.println("\n  可能的执行顺序和结果:");
        System.out.println("    顺序           x    y    说明");
        System.out.println("    1234           0    1    正常顺序");
        System.out.println("    1324           1    1    正常顺序");
        System.out.println("    3412           1    0    正常顺序");
        System.out.println("    3142           1    1    正常顺序");
        System.out.println("    2143或类似     0    0    ❌ 重排序导致!");

        System.out.println("\n  重排序的可能:");
        System.out.println("    • (1)和(2)可能被重排序为: x=b; a=1;");
        System.out.println("    • (3)和(4)可能被重排序为: y=a; b=1;");
        System.out.println("    • 如果两个线程都发生重排序，就会出现 x=0, y=0");

        System.out.println("\n场景2: 数据依赖性重排序");
        System.out.println("  int a = 1;    // (1)");
        System.out.println("  int b = 2;    // (2)");
        System.out.println("  int c = a + b; // (3)");

        System.out.println("\n  可能的重排序:");
        System.out.println("    • (1)和(2)可以重排序 (无数据依赖)");
        System.out.println("    • (3)不能排到(1)或(2)之前 (有数据依赖)");
        System.out.println("    • as-if-serial语义: 单线程内，重排序不能改变执行结果");

        System.out.println("\n场景3: 控制依赖性重排序");
        System.out.println("  boolean flag = false;");
        System.out.println("  int value = 0;");
        System.out.println();
        System.out.println("  // Thread 1");
        System.out.println("  value = 42;        // (1)");
        System.out.println("  flag = true;       // (2)");
        System.out.println();
        System.out.println("  // Thread 2");
        System.out.println("  if (flag) {        // (3)");
        System.out.println("    int x = value;   // (4) 可能看不到42!");
        System.out.println("  }");

        System.out.println("\n  问题:");
        System.out.println("    • (1)和(2)可能被重排序");
        System.out.println("    • Thread2可能看到flag=true，但value仍是0");
        System.out.println("    • 解决: 将flag声明为volatile");

        System.out.println("\n如何避免重排序问题:");
        System.out.println("  1. 使用volatile关键字");
        System.out.println("     • 禁止volatile变量前后的指令重排序");
        System.out.println();
        System.out.println("  2. 使用synchronized同步块");
        System.out.println("     • 禁止同步块内外的重排序");
        System.out.println();
        System.out.println("  3. 使用Lock锁");
        System.out.println("     • 提供与synchronized类似的内存语义");
        System.out.println();
        System.out.println("  4. 使用final关键字");
        System.out.println("     • 禁止final字段的初始化重排序到构造函数外");
        System.out.println();
        System.out.println("  5. 依赖happens-before规则");
        System.out.println("     • 正确使用JMM提供的happens-before保证");
    }

    /**
     * 错误的单例实现（仅用于演示，实际不要使用）
     */
    static class BrokenSingleton {
        private static BrokenSingleton instance;

        public static BrokenSingleton getInstance() {
            if (instance == null) {
                synchronized (BrokenSingleton.class) {
                    if (instance == null) {
                        instance = new BrokenSingleton(); // 可能发生指令重排序
                    }
                }
            }
            return instance;
        }
    }

    /**
     * 正确的单例实现（使用volatile）
     */
    static class CorrectSingleton {
        private static volatile CorrectSingleton instance;

        public static CorrectSingleton getInstance() {
            if (instance == null) {
                synchronized (CorrectSingleton.class) {
                    if (instance == null) {
                        instance = new CorrectSingleton(); // volatile禁止重排序
                    }
                }
            }
            return instance;
        }
    }

    /**
     * 静态内部类实现（推荐）
     */
    static class StaticInnerSingleton {
        private StaticInnerSingleton() {}

        private static class Holder {
            private static final StaticInnerSingleton INSTANCE = new StaticInnerSingleton();
        }

        public static StaticInnerSingleton getInstance() {
            return Holder.INSTANCE;
        }
    }

    /**
     * 枚举实现（最佳）
     */
    enum EnumSingleton {
        INSTANCE;

        public void doSomething() {
            // 业务方法
        }
    }
}
