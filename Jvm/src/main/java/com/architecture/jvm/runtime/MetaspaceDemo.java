package com.architecture.jvm.runtime;

import java.lang.management.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * 方法区/元空间深度演示
 *
 * 方法区是JVM规范定义的概念，用于存储：
 * 1. 类的元数据 (类型信息、方法信息、字段信息)
 * 2. 运行时常量池
 * 3. 静态变量
 * 4. 即时编译器编译后的代码
 *
 * 实现方式变迁：
 * - JDK 7之前: 永久代(PermGen) - 使用堆内存
 * - JDK 8+: 元空间(Metaspace) - 使用本地内存
 *
 * 为什么改为元空间？
 * 1. 永久代大小难以确定，容易OOM
 * 2. 永久代会为GC带来不必要的复杂度
 * 3. 元空间使用本地内存，大小可以动态调整
 *
 * @author Architecture
 */
public class MetaspaceDemo {

    // 静态变量存储在方法区
    private static int staticInt = 100;
    private static String staticString = "存储在方法区";
    private static final String CONSTANT = "常量也在方法区";

    public static void main(String[] args) {
        System.out.println("=== 方法区/元空间深度演示 ===\n");

        // 1. 元空间基本信息
        displayMetaspaceInfo();

        // 2. 类元数据演示
        demonstrateClassMetadata();

        // 3. 运行时常量池演示
        demonstrateConstantPool();

        // 4. 静态变量演示
        demonstrateStaticVariables();

        // 5. 类加载与元空间
        demonstrateClassLoading();

        // 6. 元空间OOM演示
        // demonstrateMetaspaceOOM(); // 注释掉，避免实际运行时OOM

        // 7. 元空间参数
        displayMetaspaceParameters();
    }

    /**
     * 1. 显示元空间基本信息
     */
    private static void displayMetaspaceInfo() {
        System.out.println("【1. 元空间基本信息】\n");

        System.out.println("方法区的实现:");
        System.out.println("  JDK 7-  : 永久代 (PermGen)");
        System.out.println("            └─ 使用堆内存");
        System.out.println("            └─ 默认大小: 64MB (32位) / 85MB (64位)");
        System.out.println("            └─ 容易发生 java.lang.OutOfMemoryError: PermGen space");
        System.out.println();
        System.out.println("  JDK 8+  : 元空间 (Metaspace)");
        System.out.println("            └─ 使用本地内存 (Native Memory)");
        System.out.println("            └─ 默认大小: 不限制 (受限于系统内存)");
        System.out.println("            └─ 自动扩展，更灵活");

        // 获取元空间使用情况
        System.out.println("\n当前元空间使用情况:");
        List<MemoryPoolMXBean> memoryPools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : memoryPools) {
            if (pool.getName().contains("Metaspace")) {
                MemoryUsage usage = pool.getUsage();
                System.out.println("  " + pool.getName() + ":");
                System.out.println("    已使用  : " + formatBytes(usage.getUsed()));
                System.out.println("    已提交  : " + formatBytes(usage.getCommitted()));
                if (usage.getMax() > 0) {
                    System.out.println("    最大值  : " + formatBytes(usage.getMax()));
                    System.out.println("    使用率  : " + String.format("%.2f%%",
                        usage.getUsed() * 100.0 / usage.getMax()));
                } else {
                    System.out.println("    最大值  : 不限制");
                }
            }
        }

        // 类加载统计
        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
        System.out.println("\n类加载统计:");
        System.out.println("  当前加载: " + classLoadingMXBean.getLoadedClassCount() + " 个类");
        System.out.println("  累计加载: " + classLoadingMXBean.getTotalLoadedClassCount() + " 个类");
        System.out.println("  已卸载  : " + classLoadingMXBean.getUnloadedClassCount() + " 个类");
        System.out.println();
    }

    /**
     * 2. 类元数据演示
     * 类的元数据包括：类型信息、方法信息、字段信息等
     */
    private static void demonstrateClassMetadata() {
        System.out.println("【2. 类元数据演示】\n");

        System.out.println("类元数据存储内容:");
        System.out.println("  1. 类型信息:");
        System.out.println("     • 类的全限定名");
        System.out.println("     • 父类的全限定名");
        System.out.println("     • 实现的接口");
        System.out.println("     • 访问修饰符 (public, abstract, final等)");
        System.out.println("  2. 方法信息:");
        System.out.println("     • 方法名称、返回类型、参数列表");
        System.out.println("     • 方法修饰符");
        System.out.println("     • 方法的字节码、操作数栈、局部变量表");
        System.out.println("     • 异常表");
        System.out.println("  3. 字段信息:");
        System.out.println("     • 字段名称、类型");
        System.out.println("     • 字段修饰符");

        // 使用反射获取类元数据
        Class<SampleClass> clazz = SampleClass.class;
        System.out.println("\n演示：获取SampleClass的元数据");
        System.out.println("类名: " + clazz.getName());
        System.out.println("父类: " + clazz.getSuperclass().getName());

        System.out.println("\n字段信息:");
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            System.out.println("  " + Modifier.toString(field.getModifiers()) +
                             " " + field.getType().getSimpleName() +
                             " " + field.getName());
        }

        System.out.println("\n方法信息:");
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            System.out.println("  " + Modifier.toString(method.getModifiers()) +
                             " " + method.getReturnType().getSimpleName() +
                             " " + method.getName() + "()");
        }

        System.out.println("\n这些元数据都存储在元空间中");
        System.out.println();
    }

    /**
     * 3. 运行时常量池演示
     * 运行时常量池是方法区的一部分
     */
    private static void demonstrateConstantPool() {
        System.out.println("【3. 运行时常量池演示】\n");

        System.out.println("常量池包含:");
        System.out.println("  • 编译期生成的字面量");
        System.out.println("    - 文本字符串");
        System.out.println("    - 声明为final的常量值");
        System.out.println("  • 编译期生成的符号引用");
        System.out.println("    - 类和接口的全限定名");
        System.out.println("    - 字段的名称和描述符");
        System.out.println("    - 方法的名称和描述符");

        System.out.println("\nJDK 6 vs JDK 7+ 常量池位置变化:");
        System.out.println("  JDK 6  : 字符串常量池在方法区 (永久代)");
        System.out.println("  JDK 7+ : 字符串常量池移到堆中");
        System.out.println("           (但符号引用等仍在元空间)");

        // 字符串常量池演示
        System.out.println("\n演示：字符串常量池");

        String str1 = "Hello";           // 字面量，放入常量池
        String str2 = "Hello";           // 复用常量池中的对象
        String str3 = new String("Hello"); // 在堆中创建新对象
        String str4 = str3.intern();     // intern()返回常量池中的引用

        System.out.println("String str1 = \"Hello\";        // 字面量 → 常量池");
        System.out.println("String str2 = \"Hello\";        // 复用常量池");
        System.out.println("String str3 = new String(\"Hello\"); // 堆中新对象");
        System.out.println("String str4 = str3.intern();   // 返回常量池引用");

        System.out.println("\n引用比较:");
        System.out.println("  str1 == str2: " + (str1 == str2) + " (都指向常量池)");
        System.out.println("  str1 == str3: " + (str1 == str3) + " (str3是堆中对象)");
        System.out.println("  str1 == str4: " + (str1 == str4) + " (intern返回常量池引用)");

        // intern()方法的作用
        System.out.println("\nintern()方法:");
        System.out.println("  • 如果常量池中已存在该字符串，返回常量池引用");
        System.out.println("  • 如果常量池中不存在，将该字符串加入常量池，返回引用");
        System.out.println("  • JDK 7+，不会复制对象，只存储引用");

        // 数值常量
        System.out.println("\n基本类型包装类常量池 (IntegerCache等):");
        Integer i1 = 127;  // 自动装箱，使用缓存
        Integer i2 = 127;
        Integer i3 = 128;  // 超出缓存范围
        Integer i4 = 128;

        System.out.println("  Integer i1 = 127; Integer i2 = 127;");
        System.out.println("  i1 == i2: " + (i1 == i2) + " (127在缓存范围内)");
        System.out.println("  Integer i3 = 128; Integer i4 = 128;");
        System.out.println("  i3 == i4: " + (i3 == i4) + " (128超出缓存范围)");
        System.out.println("  IntegerCache默认范围: -128 ~ 127");
        System.out.println();
    }

    /**
     * 4. 静态变量演示
     * JDK 7之前，静态变量存储在永久代
     * JDK 7+，静态变量存储在堆中，但类的元数据在元空间
     */
    private static void demonstrateStaticVariables() {
        System.out.println("【4. 静态变量演示】\n");

        System.out.println("静态变量存储位置变化:");
        System.out.println("  JDK 6  : 静态变量存储在方法区 (永久代)");
        System.out.println("  JDK 7+ : 静态变量存储在堆中");
        System.out.println("           (但静态变量的元数据在元空间)");

        System.out.println("\n演示：访问静态变量");
        System.out.println("  staticInt = " + staticInt);
        System.out.println("  staticString = " + staticString);
        System.out.println("  CONSTANT = " + CONSTANT);

        // 修改静态变量
        staticInt = 200;
        staticString = "已修改";
        System.out.println("\n修改后:");
        System.out.println("  staticInt = " + staticInt);
        System.out.println("  staticString = " + staticString);

        // 静态变量的特点
        System.out.println("\n静态变量特点:");
        System.out.println("  • 属于类，不属于任何实例");
        System.out.println("  • 所有实例共享同一个静态变量");
        System.out.println("  • 在类加载的准备阶段分配内存并初始化默认值");
        System.out.println("  • 在初始化阶段赋予初始值");

        // 静态块
        System.out.println("\n静态初始化块:");
        System.out.println("  • 在类加载时执行");
        System.out.println("  • 用于初始化静态变量");
        System.out.println("  • 执行顺序: 父类静态块 → 子类静态块");
        System.out.println();
    }

    /**
     * 5. 类加载与元空间
     * 类加载时，类的元数据被加载到元空间
     */
    private static void demonstrateClassLoading() {
        System.out.println("【5. 类加载与元空间】\n");

        ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();

        long beforeCount = classLoadingMXBean.getLoadedClassCount();
        System.out.println("加载前类数量: " + beforeCount);

        // 触发类加载
        System.out.println("\n触发类加载...");
        DynamicLoadClass obj1 = new DynamicLoadClass();
        DynamicLoadClass obj2 = new DynamicLoadClass();

        long afterCount = classLoadingMXBean.getLoadedClassCount();
        System.out.println("加载后类数量: " + afterCount);
        System.out.println("新加载: " + (afterCount - beforeCount) + " 个类");

        System.out.println("\n类加载过程:");
        System.out.println("  1. 加载 (Loading)");
        System.out.println("     • 通过类的全限定名获取二进制字节流");
        System.out.println("     • 将字节流转化为方法区的运行时数据结构");
        System.out.println("     • 在堆中生成Class对象");
        System.out.println("  2. 链接 (Linking)");
        System.out.println("     • 验证: 确保字节码符合规范");
        System.out.println("     • 准备: 为静态变量分配内存，设置默认值");
        System.out.println("     • 解析: 将符号引用转换为直接引用");
        System.out.println("  3. 初始化 (Initialization)");
        System.out.println("     • 执行类构造器<clinit>()");
        System.out.println("     • 初始化静态变量和静态代码块");

        System.out.println("\n元空间增长:");
        System.out.println("  • 每加载一个类，元空间使用量增加");
        System.out.println("  • 类越大，占用元空间越多");
        System.out.println("  • 匿名内部类、动态代理会产生大量类");
        System.out.println();
    }

    /**
     * 6. 元空间OOM演示 (注释掉，仅供参考)
     * -XX:MetaspaceSize=10m -XX:MaxMetaspaceSize=10m
     */
    @SuppressWarnings("unused")
    private static void demonstrateMetaspaceOOM() {
        System.out.println("【6. 元空间OOM演示】\n");
        System.out.println("运行参数: -XX:MetaspaceSize=10m -XX:MaxMetaspaceSize=10m\n");

        System.out.println("不断生成类，直到元空间溢出...");

        // 使用动态代理生成大量类
        int count = 0;
        try {
            while (true) {
                // 使用ASM或CGLib等工具动态生成类
                // 这里简化，实际需要使用字节码生成工具
                count++;

                if (count % 1000 == 0) {
                    System.out.println("已生成 " + count + " 个类");
                }
            }
        } catch (OutOfMemoryError e) {
            System.out.println("\n捕获 OutOfMemoryError!");
            System.out.println("生成了 " + count + " 个类后发生OOM");
            System.out.println("错误信息: " + e.getMessage());
        }
    }

    /**
     * 7. 元空间参数
     */
    private static void displayMetaspaceParameters() {
        System.out.println("【7. 元空间JVM参数】\n");

        System.out.println("元空间大小设置:");
        System.out.println("  -XX:MetaspaceSize=<size>");
        System.out.println("    • 设置元空间初始大小");
        System.out.println("    • 默认: 约21MB (平台相关)");
        System.out.println("    • 达到此值会触发Full GC");
        System.out.println();
        System.out.println("  -XX:MaxMetaspaceSize=<size>");
        System.out.println("    • 设置元空间最大大小");
        System.out.println("    • 默认: 不限制 (受限于系统内存)");
        System.out.println("    • 达到此值会抛出OOM");

        System.out.println("\n压缩类空间 (Compressed Class Space):");
        System.out.println("  -XX:+UseCompressedClassPointers");
        System.out.println("    • 启用类指针压缩 (默认开启)");
        System.out.println("    • 64位平台，压缩类指针以节省空间");
        System.out.println();
        System.out.println("  -XX:CompressedClassSpaceSize=<size>");
        System.out.println("    • 设置压缩类空间大小");
        System.out.println("    • 默认: 1GB");

        System.out.println("\n类卸载相关:");
        System.out.println("  -XX:+CMSClassUnloadingEnabled");
        System.out.println("    • CMS收集器启用类卸载");
        System.out.println();
        System.out.println("  -XX:+ClassUnloadingWithConcurrentMark");
        System.out.println("    • G1收集器启用类卸载");

        System.out.println("\n推荐配置:");
        System.out.println("  开发环境:");
        System.out.println("    -XX:MetaspaceSize=128m");
        System.out.println("    -XX:MaxMetaspaceSize=256m");
        System.out.println();
        System.out.println("  生产环境:");
        System.out.println("    -XX:MetaspaceSize=256m");
        System.out.println("    -XX:MaxMetaspaceSize=512m");
        System.out.println("    (根据应用实际情况调整)");

        System.out.println("\n监控元空间:");
        System.out.println("  jstat -gc <pid>           查看元空间使用情况");
        System.out.println("  jmap -clstats <pid>       查看类加载统计");
        System.out.println("  jcmd <pid> GC.class_stats 查看详细类统计");
        System.out.println();
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
     * 示例类 - 用于演示类元数据
     */
    static class SampleClass {
        private int instanceVar;
        private static int staticVar;
        public String publicField;
        protected double protectedField;

        public void publicMethod() {
        }

        private void privateMethod() {
        }

        static void staticMethod() {
        }
    }

    /**
     * 动态加载类示例
     */
    static class DynamicLoadClass {
        static {
            // 静态初始化块，类加载时执行
        }

        private int value = 42;

        public DynamicLoadClass() {
            // 构造方法
        }
    }
}
