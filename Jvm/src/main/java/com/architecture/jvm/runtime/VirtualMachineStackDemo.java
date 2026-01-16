package com.architecture.jvm.runtime;

/**
 * Java虚拟机栈深度演示
 *
 * 虚拟机栈是线程私有的，描述Java方法执行的内存模型
 * 每个方法执行时会创建一个栈帧(Stack Frame)
 *
 * 栈帧包含：
 * 1. 局部变量表 (Local Variable Table) - 存储方法参数和局部变量
 * 2. 操作数栈 (Operand Stack) - 方法执行过程中的操作数
 * 3. 动态链接 (Dynamic Linking) - 指向运行时常量池的方法引用
 * 4. 方法返回地址 (Return Address) - 方法正常/异常退出的地址
 *
 * @author Architecture
 */
public class VirtualMachineStackDemo {

    private static int stackDepth = 0;
    private static final int MAX_DEPTH_TO_PRINT = 10;

    public static void main(String[] args) {
        System.out.println("=== Java虚拟机栈深度演示 ===\n");

        // 1. 栈帧结构演示
        demonstrateStackFrame();

        // 2. 局部变量表演示
        demonstrateLocalVariableTable();

        // 3. 操作数栈演示
        demonstrateOperandStack();

        // 4. StackOverflowError演示
        demonstrateStackOverflow();

        // 5. 方法递归与栈深度
        demonstrateRecursion();

        // 6. 异常处理与栈帧
        demonstrateExceptionHandling();
    }

    /**
     * 1. 栈帧结构演示
     */
    private static void demonstrateStackFrame() {
        System.out.println("【1. 栈帧结构演示】");
        System.out.println("每个方法调用都会创建一个栈帧\n");

        System.out.println("调用链: main() → method1() → method2() → method3()");
        System.out.println();
        method1();
        System.out.println("\n所有方法执行完毕，栈帧全部出栈");
        System.out.println();
    }

    private static void method1() {
        String localVar = "method1的局部变量";
        System.out.println("→ method1() 栈帧入栈");
        System.out.println("  局部变量表: [localVar=\"" + localVar + "\"]");
        method2();
        System.out.println("← method1() 栈帧出栈");
    }

    private static void method2() {
        int localVar = 200;
        System.out.println("  → method2() 栈帧入栈");
        System.out.println("    局部变量表: [localVar=" + localVar + "]");
        method3();
        System.out.println("  ← method2() 栈帧出栈");
    }

    private static void method3() {
        double localVar = 3.14;
        System.out.println("    → method3() 栈帧入栈");
        System.out.println("      局部变量表: [localVar=" + localVar + "]");
        System.out.println("      (此时栈中有4个栈帧: main、method1、method2、method3)");
        System.out.println("    ← method3() 栈帧出栈");
    }

    /**
     * 2. 局部变量表演示
     * 局部变量表是一组变量值存储空间，存放方法参数和方法内部定义的局部变量
     * 以变量槽(Slot)为单位，每个Slot可以存放一个32位以内的数据类型
     */
    private static void demonstrateLocalVariableTable() {
        System.out.println("【2. 局部变量表演示】");
        System.out.println("局部变量表存储：方法参数 + 局部变量");
        System.out.println("基本数据类型和对象引用都存储在局部变量表中\n");

        // 各种类型的局部变量
        byte byteVar = 1;           // 占用1个slot
        short shortVar = 2;         // 占用1个slot
        int intVar = 3;             // 占用1个slot
        long longVar = 4L;          // 占用2个slot (64位)
        float floatVar = 5.0f;      // 占用1个slot
        double doubleVar = 6.0;     // 占用2个slot (64位)
        char charVar = 'A';         // 占用1个slot
        boolean boolVar = true;     // 占用1个slot
        String strVar = "Hello";    // 对象引用，占用1个slot

        System.out.println("局部变量表内容 (以Slot为单位):");
        System.out.println("  Slot 0: this (如果是实例方法)");
        System.out.println("  Slot 1: byteVar = " + byteVar + " (1个slot)");
        System.out.println("  Slot 2: shortVar = " + shortVar + " (1个slot)");
        System.out.println("  Slot 3: intVar = " + intVar + " (1个slot)");
        System.out.println("  Slot 4-5: longVar = " + longVar + " (2个slot, 64位)");
        System.out.println("  Slot 6: floatVar = " + floatVar + " (1个slot)");
        System.out.println("  Slot 7-8: doubleVar = " + doubleVar + " (2个slot, 64位)");
        System.out.println("  Slot 9: charVar = " + charVar + " (1个slot)");
        System.out.println("  Slot 10: boolVar = " + boolVar + " (1个slot)");
        System.out.println("  Slot 11: strVar引用 = " + strVar + " (1个slot，实际对象在堆中)");

        System.out.println("\n注意：");
        System.out.println("  • long和double占用2个Slot");
        System.out.println("  • 对象引用占用1个Slot，实际对象在堆中");
        System.out.println("  • 局部变量表在编译期确定大小");
        System.out.println();
    }

    /**
     * 3. 操作数栈演示
     * 操作数栈用于存放方法执行过程中的中间计算结果
     */
    private static void demonstrateOperandStack() {
        System.out.println("【3. 操作数栈演示】");
        System.out.println("操作数栈用于存放方法执行过程中的操作数\n");

        System.out.println("执行: int c = a + b;");
        System.out.println("字节码执行过程:");
        System.out.println("  1. iload_1      // 将局部变量表slot 1的值(a)压入操作数栈");
        System.out.println("  2. iload_2      // 将局部变量表slot 2的值(b)压入操作数栈");
        System.out.println("  3. iadd         // 弹出栈顶两个值，相加，结果压入栈");
        System.out.println("  4. istore_3     // 弹出栈顶值，存入局部变量表slot 3(c)");

        int a = 10;
        int b = 20;
        int c = a + b; // 这个操作会使用操作数栈

        System.out.println("\n结果: " + c);
        System.out.println("\n操作数栈可视化:");
        System.out.println("  步骤1后: [10]");
        System.out.println("  步骤2后: [10, 20]");
        System.out.println("  步骤3后: [30]");
        System.out.println("  步骤4后: []");
        System.out.println();
    }

    /**
     * 4. StackOverflowError演示
     * 当线程请求的栈深度大于虚拟机允许的深度时，抛出StackOverflowError
     */
    private static void demonstrateStackOverflow() {
        System.out.println("【4. StackOverflowError演示】");
        System.out.println("当栈深度超过限制时，会抛出StackOverflowError");
        System.out.println("可以通过-Xss参数设置栈大小\n");

        stackDepth = 0;
        try {
            recursiveMethod();
        } catch (StackOverflowError e) {
            System.out.println("\n捕获StackOverflowError!");
            System.out.println("栈深度达到: " + stackDepth);
            System.out.println("这就是为什么无限递归会导致栈溢出");
        }
        System.out.println();
    }

    private static void recursiveMethod() {
        stackDepth++;
        if (stackDepth <= MAX_DEPTH_TO_PRINT) {
            System.out.println("递归深度: " + stackDepth + " (每次调用创建新栈帧)");
        } else if (stackDepth == MAX_DEPTH_TO_PRINT + 1) {
            System.out.println("... (继续递归，不再打印) ...");
        }
        recursiveMethod(); // 无限递归，最终导致栈溢出
    }

    /**
     * 5. 方法递归与栈深度
     * 正常的递归调用，每次都会创建栈帧
     */
    private static void demonstrateRecursion() {
        System.out.println("【5. 方法递归与栈深度】");
        System.out.println("计算阶乘: 5! = 5 × 4 × 3 × 2 × 1\n");

        int result = factorial(5, 1);
        System.out.println("\n结果: 5! = " + result);
        System.out.println("\n每次递归调用都会创建新的栈帧");
        System.out.println("栈最深时有6个factorial方法的栈帧 (5, 4, 3, 2, 1, 0)");
        System.out.println();
    }

    private static int factorial(int n, int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + "→ factorial(" + n + ") 入栈");

        int result;
        if (n <= 1) {
            result = 1;
            System.out.println(indent + "  返回 1");
        } else {
            result = n * factorial(n - 1, depth + 1);
            System.out.println(indent + "  返回 " + n + " × factorial(" + (n-1) + ") = " + result);
        }

        System.out.println(indent + "← factorial(" + n + ") 出栈");
        return result;
    }

    /**
     * 6. 异常处理与栈帧
     * 异常发生时，会在栈帧中查找异常处理器
     */
    private static void demonstrateExceptionHandling() {
        System.out.println("【6. 异常处理与栈帧】");
        System.out.println("异常发生时的栈帧处理\n");

        try {
            exceptionMethod1();
        } catch (Exception e) {
            System.out.println("\n在main方法中捕获异常: " + e.getMessage());
            System.out.println("\n异常栈追踪 (显示异常传播路径):");
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (int i = 0; i < Math.min(stackTrace.length, 5); i++) {
                System.out.println("  " + stackTrace[i]);
            }
        }

        System.out.println("\n异常处理机制:");
        System.out.println("  1. 异常发生时，在当前栈帧查找异常处理器");
        System.out.println("  2. 如果找到，执行异常处理代码");
        System.out.println("  3. 如果未找到，栈帧出栈，在调用者栈帧中查找");
        System.out.println("  4. 重复步骤，直到找到处理器或到达栈底");
        System.out.println();
    }

    private static void exceptionMethod1() throws Exception {
        System.out.println("→ exceptionMethod1() 入栈");
        exceptionMethod2();
        System.out.println("← exceptionMethod1() 出栈");
    }

    private static void exceptionMethod2() throws Exception {
        System.out.println("  → exceptionMethod2() 入栈");
        exceptionMethod3();
        System.out.println("  ← exceptionMethod2() 出栈");
    }

    private static void exceptionMethod3() throws Exception {
        System.out.println("    → exceptionMethod3() 入栈");
        System.out.println("    抛出异常!");
        throw new Exception("在exceptionMethod3中抛出的异常");
    }

    /**
     * 栈帧大小分析
     */
    @SuppressWarnings("unused")
    public static void analyzeStackFrameSize() {
        System.out.println("【栈帧大小分析】");
        System.out.println("栈帧大小 = 局部变量表大小 + 操作数栈大小 + 其他信息");
        System.out.println("\n局部变量表大小在编译时确定:");

        // 大量局部变量
        int var1 = 1, var2 = 2, var3 = 3, var4 = 4, var5 = 5;
        int var6 = 6, var7 = 7, var8 = 8, var9 = 9, var10 = 10;

        System.out.println("  • 此方法有10个int变量，需要10个slot");
        System.out.println("  • 加上this引用(实例方法)，共11个slot");
        System.out.println("  • 每个slot通常4字节，局部变量表约44字节");

        System.out.println("\n操作数栈深度也在编译时确定:");
        System.out.println("  • 根据字节码指令分析得出");
        System.out.println("  • 复杂表达式可能需要更深的操作数栈");

        // 复杂表达式
        int result = (var1 + var2) * (var3 + var4) / (var5 - var6);
        System.out.println("  • 此表达式需要的操作数栈深度: 约4-6");
    }
}
