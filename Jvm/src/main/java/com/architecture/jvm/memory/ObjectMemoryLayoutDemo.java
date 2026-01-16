package com.architecture.jvm.memory;

/**
 * 对象内存布局深度解析
 *
 * Java对象在内存中的布局分为三部分：
 * ┌──────────────────────────────────────┐
 * │          对象头 (Object Header)       │
 * ├──────────────────────────────────────┤
 * │      Mark Word (标记字)              │ 8字节(64位)
 * │   - hashCode, GC分代年龄, 锁状态等    │
 * ├──────────────────────────────────────┤
 * │      Class Pointer (类型指针)        │ 4/8字节
 * │   - 指向类元数据的指针                │
 * ├──────────────────────────────────────┤
 * │      Array Length (数组长度)         │ 4字节(仅数组)
 * ├──────────────────────────────────────┤
 * │      实例数据 (Instance Data)        │
 * │   - 对象真正存储的有效信息            │
 * │   - 父类字段 + 本类字段              │
 * ├──────────────────────────────────────┤
 * │      对齐填充 (Padding)              │
 * │   - 补齐到8字节的倍数                │
 * └──────────────────────────────────────┘
 *
 * @author Architecture
 */
public class ObjectMemoryLayoutDemo {

    public static void main(String[] args) {
        System.out.println("=== 对象内存布局深度解析 ===\n");

        // 1. 对象头详解
        explainObjectHeader();

        // 2. Mark Word详解
        explainMarkWord();

        // 3. 类型指针
        explainClassPointer();

        // 4. 实例数据
        explainInstanceData();

        // 5. 对齐填充
        explainPadding();

        // 6. 数组对象特殊性
        explainArrayLayout();

        // 7. 压缩指针
        explainCompressedOops();

        // 8. 对象大小计算
        calculateObjectSize();

        // 9. 内存布局示例
        demonstrateMemoryLayout();
    }

    /**
     * 1. 对象头详解
     */
    private static void explainObjectHeader() {
        System.out.println("【1. 对象头 (Object Header)】\n");

        System.out.println("对象头包含两部分 (非数组对象):");
        System.out.println("  1. Mark Word (标记字) - 8字节");
        System.out.println("     存储对象运行时数据:");
        System.out.println("     • hashCode (哈希码)");
        System.out.println("     • GC分代年龄");
        System.out.println("     • 锁状态标志");
        System.out.println("     • 线程持有的锁");
        System.out.println("     • 偏向线程ID");
        System.out.println("     • 偏向时间戳");

        System.out.println("\n  2. Class Pointer (类型指针) - 4/8字节");
        System.out.println("     指向类元数据的指针:");
        System.out.println("     • 指向方法区的Class对象");
        System.out.println("     • 确定对象的类型");
        System.out.println("     • 压缩指针开启时4字节，否则8字节");

        System.out.println("\n数组对象额外包含:");
        System.out.println("  3. Array Length (数组长度) - 4字节");
        System.out.println("     记录数组的长度");

        System.out.println("\n对象头大小:");
        System.out.println("  普通对象: 12字节 (Mark Word 8 + Class Pointer 4)");
        System.out.println("  数组对象: 16字节 (Mark Word 8 + Class Pointer 4 + Length 4)");
        System.out.println("  (以上为开启压缩指针的情况)");
        System.out.println();
    }

    /**
     * 2. Mark Word详解
     */
    private static void explainMarkWord() {
        System.out.println("【2. Mark Word (标记字)】\n");

        System.out.println("Mark Word是对象头的核心，64位JVM中占8字节");

        System.out.println("\nMark Word不同状态下的布局 (64位):");

        System.out.println("\n  无锁状态 (Unlocked):");
        System.out.println("  ┌─────────────────────────────────────────────────────┐");
        System.out.println("  │ unused:25 | hashCode:31 | unused:1 | age:4 | 0 | 01 │");
        System.out.println("  └─────────────────────────────────────────────────────┘");
        System.out.println("     • hashCode: 对象的identity hash code");
        System.out.println("     • age: GC分代年龄 (0-15)");
        System.out.println("     • 01: 无锁标志");

        System.out.println("\n  偏向锁 (Biased):");
        System.out.println("  ┌─────────────────────────────────────────────────────┐");
        System.out.println("  │ thread:54 | epoch:2 | unused:1 | age:4 | 1 | 01     │");
        System.out.println("  └─────────────────────────────────────────────────────┘");
        System.out.println("     • thread: 持有偏向锁的线程ID");
        System.out.println("     • epoch: 偏向时间戳");
        System.out.println("     • 101: 偏向锁标志");

        System.out.println("\n  轻量级锁 (Lightweight Locked):");
        System.out.println("  ┌─────────────────────────────────────────────────────┐");
        System.out.println("  │ pointer to lock record:62                     | 00  │");
        System.out.println("  └─────────────────────────────────────────────────────┘");
        System.out.println("     • pointer: 指向栈中锁记录的指针");
        System.out.println("     • 00: 轻量级锁标志");

        System.out.println("\n  重量级锁 (Heavyweight Locked):");
        System.out.println("  ┌─────────────────────────────────────────────────────┐");
        System.out.println("  │ pointer to monitor:62                         | 10  │");
        System.out.println("  └─────────────────────────────────────────────────────┘");
        System.out.println("     • pointer: 指向监视器(monitor)的指针");
        System.out.println("     • 10: 重量级锁标志");

        System.out.println("\n  GC标记 (Marked for GC):");
        System.out.println("  ┌─────────────────────────────────────────────────────┐");
        System.out.println("  │                                                | 11  │");
        System.out.println("  └─────────────────────────────────────────────────────┘");
        System.out.println("     • 11: GC标记");

        System.out.println("\n锁升级过程:");
        System.out.println("  无锁 → 偏向锁 → 轻量级锁 → 重量级锁");
        System.out.println("  (锁只能升级，不能降级)");
        System.out.println();
    }

    /**
     * 3. 类型指针
     */
    private static void explainClassPointer() {
        System.out.println("【3. 类型指针 (Class Pointer/Klass Pointer)】\n");

        System.out.println("作用:");
        System.out.println("  • 指向方法区中的类元数据");
        System.out.println("  • JVM通过它确定对象的类型");
        System.out.println("  • 用于方法调用、类型检查等");

        System.out.println("\n大小:");
        System.out.println("  未压缩: 8字节 (64位地址)");
        System.out.println("  压缩:   4字节 (启用-XX:+UseCompressedOops)");

        System.out.println("\n压缩指针:");
        System.out.println("  • JDK 6 Update 23开始支持");
        System.out.println("  • 默认开启 (堆 < 32GB)");
        System.out.println("  • 节省内存，提高缓存利用率");

        Object obj = new Object();
        System.out.println("\n示例:");
        System.out.println("  Object obj = new Object();");
        System.out.println("  对象类型: " + obj.getClass().getName());
        System.out.println("  Class对象: " + obj.getClass());
        System.out.println("  (对象头的类型指针指向这个Class对象)");
        System.out.println();
    }

    /**
     * 4. 实例数据
     */
    private static void explainInstanceData() {
        System.out.println("【4. 实例数据 (Instance Data)】\n");

        System.out.println("实例数据包含:");
        System.out.println("  • 对象的所有字段");
        System.out.println("  • 包括从父类继承的字段");
        System.out.println("  • 不包括static字段 (存储在方法区)");

        System.out.println("\n字段存储顺序:");
        System.out.println("  1. 父类字段在子类字段之前");
        System.out.println("  2. 相同宽度的字段分配在一起");
        System.out.println("  3. 顺序: long/double > int/float > short/char > byte/boolean > reference");

        System.out.println("\n基本类型大小:");
        System.out.println("  byte:    1字节");
        System.out.println("  short:   2字节");
        System.out.println("  char:    2字节");
        System.out.println("  int:     4字节");
        System.out.println("  float:   4字节");
        System.out.println("  long:    8字节");
        System.out.println("  double:  8字节");
        System.out.println("  boolean: 1字节 (JVM规范允许4字节)");
        System.out.println("  引用:    4/8字节 (取决于是否压缩)");

        System.out.println("\n示例:");
        SampleObject sample = new SampleObject();
        System.out.println("  class SampleObject {");
        System.out.println("    byte b;      // 1字节");
        System.out.println("    int i;       // 4字节");
        System.out.println("    long l;      // 8字节");
        System.out.println("    Object obj;  // 4字节(压缩指针)");
        System.out.println("  }");
        System.out.println("\n  实例数据大小: 1 + 4 + 8 + 4 = 17字节");
        System.out.println("  (实际会有padding对齐)");
        System.out.println();
    }

    static class SampleObject {
        byte b;
        int i;
        long l;
        Object obj;
    }

    /**
     * 5. 对齐填充
     */
    private static void explainPadding() {
        System.out.println("【5. 对齐填充 (Padding)】\n");

        System.out.println("为什么需要对齐？");
        System.out.println("  • HotSpot要求对象大小必须是8字节的倍数");
        System.out.println("  • CPU访问对齐的数据更高效");
        System.out.println("  • 简化内存管理");

        System.out.println("\n对齐规则:");
        System.out.println("  • 对象总大小 = 对象头 + 实例数据 + 填充");
        System.out.println("  • 总大小必须是8的倍数");
        System.out.println("  • 不足的部分用padding填充");

        System.out.println("\n示例:");
        System.out.println("  class TinyObject {");
        System.out.println("    byte b;  // 1字节");
        System.out.println("  }");
        System.out.println("\n  对象头: 12字节");
        System.out.println("  实例数据: 1字节");
        System.out.println("  小计: 13字节");
        System.out.println("  填充: 3字节 (补齐到16字节)");
        System.out.println("  总计: 16字节");

        System.out.println("\n  class EmptyObject {}");
        System.out.println("\n  对象头: 12字节");
        System.out.println("  实例数据: 0字节");
        System.out.println("  填充: 4字节 (补齐到16字节)");
        System.out.println("  总计: 16字节");
        System.out.println("  (空对象也占用16字节!)");
        System.out.println();
    }

    /**
     * 6. 数组对象特殊性
     */
    private static void explainArrayLayout() {
        System.out.println("【6. 数组对象的内存布局】\n");

        System.out.println("数组对象结构:");
        System.out.println("  ┌──────────────────────┐");
        System.out.println("  │ Mark Word (8字节)    │");
        System.out.println("  ├──────────────────────┤");
        System.out.println("  │ Class Pointer (4字节)│");
        System.out.println("  ├──────────────────────┤");
        System.out.println("  │ Array Length (4字节) │ ← 特有");
        System.out.println("  ├──────────────────────┤");
        System.out.println("  │ Array Data (元素)    │");
        System.out.println("  ├──────────────────────┤");
        System.out.println("  │ Padding              │");
        System.out.println("  └──────────────────────┘");

        System.out.println("\n数组对象头: 16字节");
        System.out.println("  • Mark Word: 8字节");
        System.out.println("  • Class Pointer: 4字节");
        System.out.println("  • Array Length: 4字节");

        System.out.println("\n示例:");
        int[] arr = new int[10];
        System.out.println("  int[] arr = new int[10];");
        System.out.println("\n  对象头: 16字节");
        System.out.println("  元素数据: 10 × 4 = 40字节");
        System.out.println("  总计: 56字节 (已是8的倍数,无需填充)");

        byte[] byteArr = new byte[5];
        System.out.println("\n  byte[] byteArr = new byte[5];");
        System.out.println("\n  对象头: 16字节");
        System.out.println("  元素数据: 5 × 1 = 5字节");
        System.out.println("  填充: 3字节");
        System.out.println("  总计: 24字节");
        System.out.println();
    }

    /**
     * 7. 压缩指针
     */
    private static void explainCompressedOops() {
        System.out.println("【7. 压缩指针 (Compressed Oops)】\n");

        System.out.println("什么是压缩指针？");
        System.out.println("  Oops = Ordinary Object Pointers");
        System.out.println("  将64位指针压缩为32位");

        System.out.println("\n压缩范围:");
        System.out.println("  • Class Pointer (类型指针)");
        System.out.println("  • 对象引用字段");
        System.out.println("  • 引用类型数组元素");

        System.out.println("\n压缩原理:");
        System.out.println("  • 对象按8字节对齐");
        System.out.println("  • 地址最低3位始终为0");
        System.out.println("  • 32位可以表示 2^32 × 8 = 32GB地址空间");

        System.out.println("\n启用条件:");
        System.out.println("  • 堆 < 32GB (默认开启)");
        System.out.println("  • 堆 >= 32GB (自动关闭)");

        System.out.println("\nJVM参数:");
        System.out.println("  -XX:+UseCompressedOops     启用压缩指针 (默认)");
        System.out.println("  -XX:-UseCompressedOops     禁用压缩指针");
        System.out.println("  -XX:+UseCompressedClassPointers  压缩类指针");

        System.out.println("\n优势:");
        System.out.println("  ✓ 减少内存占用 (约30%)");
        System.out.println("  ✓ 提高CPU缓存利用率");
        System.out.println("  ✓ 减少GC压力");

        System.out.println("\n劣势:");
        System.out.println("  ✗ 地址转换有微小性能开销");
        System.out.println("  ✗ 限制堆大小 < 32GB");
        System.out.println();
    }

    /**
     * 8. 对象大小计算
     */
    private static void calculateObjectSize() {
        System.out.println("【8. 对象大小计算】\n");

        System.out.println("计算公式 (压缩指针开启):");
        System.out.println("  对象大小 = 对象头 + 实例数据 + 对齐填充");
        System.out.println("  对象头 = 12字节 (普通对象) / 16字节 (数组)");

        System.out.println("\n示例1: 空对象");
        System.out.println("  class Empty {}");
        System.out.println("  大小 = 12 + 0 + 4 = 16字节");

        System.out.println("\n示例2: 包含基本类型");
        System.out.println("  class WithInt { int x; }");
        System.out.println("  大小 = 12 + 4 + 0 = 16字节");

        System.out.println("\n示例3: 包含多个字段");
        System.out.println("  class Multi {");
        System.out.println("    int x;      // 4字节");
        System.out.println("    long y;     // 8字节");
        System.out.println("    byte z;     // 1字节");
        System.out.println("  }");
        System.out.println("  大小 = 12 + (4+8+1) + 3 = 28 → 32字节");

        System.out.println("\n示例4: 包含引用");
        System.out.println("  class WithRef {");
        System.out.println("    Object obj1;  // 4字节(压缩)");
        System.out.println("    Object obj2;  // 4字节(压缩)");
        System.out.println("  }");
        System.out.println("  大小 = 12 + 8 + 4 = 24字节");

        System.out.println("\n示例5: 数组");
        System.out.println("  int[] arr = new int[3];");
        System.out.println("  大小 = 16 + (3×4) + 4 = 32字节");

        System.out.println("\n注意:");
        System.out.println("  • 引用只占用4/8字节，实际对象另外计算");
        System.out.println("  • 继承会增加父类字段的大小");
        System.out.println("  • 字段重排序可能影响大小");
        System.out.println();
    }

    /**
     * 9. 内存布局示例
     */
    private static void demonstrateMemoryLayout() {
        System.out.println("【9. 完整示例】\n");

        System.out.println("class Person {");
        System.out.println("  private String name;   // 引用: 4字节");
        System.out.println("  private int age;       // int:  4字节");
        System.out.println("  private boolean male;  // bool: 1字节");
        System.out.println("}");

        System.out.println("\n内存布局 (启用压缩指针):");
        System.out.println("┌─────────────────────────────────┐");
        System.out.println("│ Mark Word                8字节  │  对象头");
        System.out.println("├─────────────────────────────────┤");
        System.out.println("│ Class Pointer            4字节  │  对象头");
        System.out.println("├─────────────────────────────────┤");
        System.out.println("│ name (String引用)        4字节  │  实例数据");
        System.out.println("├─────────────────────────────────┤");
        System.out.println("│ age (int)                4字节  │  实例数据");
        System.out.println("├─────────────────────────────────┤");
        System.out.println("│ male (boolean)           1字节  │  实例数据");
        System.out.println("├─────────────────────────────────┤");
        System.out.println("│ Padding                  3字节  │  对齐填充");
        System.out.println("└─────────────────────────────────┘");
        System.out.println("总计: 24字节");

        System.out.println("\n如何查看对象内存布局？");
        System.out.println("  1. 使用JOL (Java Object Layout) 工具");
        System.out.println("     • 依赖: org.openjdk.jol:jol-core");
        System.out.println("     • ClassLayout.parseClass(XXX.class).toPrintable()");

        System.out.println("\n  2. 使用HSDB (HotSpot Debugger)");
        System.out.println("     • java -cp sa-jdi.jar sun.jvm.hotspot.HSDB");

        System.out.println("\n  3. 使用JVM参数");
        System.out.println("     • -XX:+PrintFieldLayout (需要debug版本JVM)");
        System.out.println();
    }
}
