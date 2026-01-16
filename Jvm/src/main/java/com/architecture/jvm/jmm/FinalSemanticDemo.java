package com.architecture.jvm.jmm;

/**
 * final语义演示
 *
 * 演示final字段的内存语义和线程安全性
 *
 * @author Architecture
 */
public class FinalSemanticDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== final语义演示 ===\n");

        // 演示1: final字段的初始化安全性
        demonstrateFinalInitialization();

        System.out.println("\n" + "=".repeat(80) + "\n");

        // 演示2: 普通字段的不安全发布
        demonstrateUnsafePublication();

        System.out.println("\n" + "=".repeat(80) + "\n");

        // 演示3: final引用类型的深度不可变性
        demonstrateFinalReference();
    }

    /**
     * 演示1: final字段的初始化安全性
     */
    private static void demonstrateFinalInitialization() throws InterruptedException {
        System.out.println("【演示1: final字段的初始化安全性】\n");

        System.out.println("场景: 多线程环境下创建和访问对象\n");

        for (int i = 0; i < 5; i++) {
            System.out.println("第 " + (i + 1) + " 次测试:");

            // 创建线程来发布对象
            Thread publisher = new Thread(() -> {
                SafeObject.instance = new SafeObject(42, 100);
            });

            // 创建线程来读取对象
            Thread reader = new Thread(() -> {
                SafeObject obj = SafeObject.instance;
                if (obj != null) {
                    System.out.println("  [Reader] finalValue = " + obj.finalValue
                        + ", normalValue = " + obj.normalValue);

                    if (obj.finalValue == 42 && obj.normalValue != 100) {
                        System.out.println("  ⚠️  final字段正确初始化，但普通字段未初始化!");
                    }
                }
            });

            publisher.start();
            reader.start();

            publisher.join();
            reader.join();

            SafeObject.instance = null; // 重置
            Thread.sleep(10);
        }

        System.out.println("\n分析:");
        System.out.println("  • final字段: 保证构造函数完成前，其他线程看不到对象");
        System.out.println("  • 普通字段: 可能在对象未完全初始化时就对其他线程可见");
        System.out.println("  • final提供\"初始化安全性\"保证");
    }

    /**
     * 安全的对象（使用final）
     */
    static class SafeObject {
        static volatile SafeObject instance;

        final int finalValue;    // final字段
        int normalValue;         // 普通字段

        public SafeObject(int finalValue, int normalValue) {
            this.finalValue = finalValue;
            this.normalValue = normalValue;
        }
    }

    /**
     * 演示2: 普通字段的不安全发布
     */
    private static void demonstrateUnsafePublication() {
        System.out.println("【演示2: 对象逃逸与final的保护】\n");

        System.out.println("❌ 不安全的对象发布:");
        System.out.println("```java");
        System.out.println("class UnsafeObject {");
        System.out.println("    static UnsafeObject instance;");
        System.out.println("    int value;");
        System.out.println("    ");
        System.out.println("    public UnsafeObject(int value) {");
        System.out.println("        instance = this;  // 对象逃逸!");
        System.out.println("        this.value = value;");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("```");

        System.out.println("\n问题:");
        System.out.println("  • 构造函数中将this赋值给静态变量（对象逃逸）");
        System.out.println("  • 其他线程可能看到未初始化完成的对象");
        System.out.println("  • this.value可能还未赋值，其他线程就访问了");

        System.out.println("\n✓ 使用final修复:");
        System.out.println("```java");
        System.out.println("class SafeObject {");
        System.out.println("    static SafeObject instance;");
        System.out.println("    final int value;  // 使用final");
        System.out.println("    ");
        System.out.println("    public SafeObject(int value) {");
        System.out.println("        this.value = value;");
        System.out.println("        instance = this;  // final保证value已初始化");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("```");

        System.out.println("\nfinal的重排序规则:");
        System.out.println("  规则1: final字段的写入，禁止重排序到构造函数外");
        System.out.println("    • 保证构造函数返回前，final字段已正确初始化");
        System.out.println();
        System.out.println("  规则2: 初次读取包含final字段的对象引用，");
        System.out.println("         禁止重排序到读取final字段之后");
        System.out.println("    • 保证读取对象时，能看到正确初始化的final字段");

        System.out.println("\n内存屏障:");
        System.out.println("  构造函数:");
        System.out.println("    this.finalField = value;");
        System.out.println("    StoreStore屏障           // JMM插入");
        System.out.println("    return;                  // 构造函数返回");
        System.out.println();
        System.out.println("  读取线程:");
        System.out.println("    obj = SharedObject.instance;  // 读取对象引用");
        System.out.println("    LoadLoad屏障                  // JMM插入");
        System.out.println("    int x = obj.finalField;       // 读取final字段");
    }

    /**
     * 演示3: final引用类型
     */
    private static void demonstrateFinalReference() {
        System.out.println("【演示3: final引用类型】\n");

        System.out.println("final引用的含义:");
        System.out.println("  • final修饰引用类型时，引用不可变");
        System.out.println("  • 但引用指向的对象内容仍可变");

        System.out.println("\n示例:");
        System.out.println("```java");
        System.out.println("class Container {");
        System.out.println("    final int[] array;  // final引用");
        System.out.println("    ");
        System.out.println("    public Container() {");
        System.out.println("        array = new int[10];");
        System.out.println("        array[0] = 42;");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("```");

        System.out.println("\nfinal的保证:");
        System.out.println("  ✓ 保证: 其他线程看到Container对象时");
        System.out.println("         array引用一定不为null");
        System.out.println("  ✓ 保证: array[0]的初始值42一定可见");
        System.out.println("  ❌ 不保证: 后续对array[0]的修改对其他线程立即可见");

        System.out.println("\n深度不可变性:");
        System.out.println("  要实现真正的不可变对象，需要:");
        System.out.println("    1. 所有字段都是final");
        System.out.println("    2. 字段类型都是基本类型或不可变类型");
        System.out.println("    3. 不提供setter方法");
        System.out.println("    4. 不让可变字段逃逸");

        System.out.println("\n✓ 真正的不可变对象:");
        System.out.println("```java");
        System.out.println("final class ImmutableObject {");
        System.out.println("    private final int value;");
        System.out.println("    private final String name;");
        System.out.println("    private final ImmutableList<Integer> list;");
        System.out.println("    ");
        System.out.println("    public ImmutableObject(int value, String name, ");
        System.out.println("                          ImmutableList<Integer> list) {");
        System.out.println("        this.value = value;");
        System.out.println("        this.name = name;");
        System.out.println("        this.list = list;");
        System.out.println("    }");
        System.out.println("    ");
        System.out.println("    // 只提供getter，不提供setter");
        System.out.println("    public int getValue() { return value; }");
        System.out.println("    public String getName() { return name; }");
        System.out.println("    public ImmutableList<Integer> getList() { return list; }");
        System.out.println("}");
        System.out.println("```");

        System.out.println("\n不可变对象的优势:");
        System.out.println("  • 天然线程安全，无需同步");
        System.out.println("  • 可以安全地共享");
        System.out.println("  • 简化并发编程");
        System.out.println("  • 可以作为HashMap的key");

        System.out.println("\nJava中的不可变类:");
        System.out.println("  • String");
        System.out.println("  • Integer, Long等包装类");
        System.out.println("  • BigInteger, BigDecimal");
        System.out.println("  • LocalDate, LocalDateTime等时间类");
        System.out.println("  • Collections.unmodifiableXxx()返回的集合");
    }
}
