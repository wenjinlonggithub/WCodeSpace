package com.architecture.jvm.bytecode;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 字节码分析示例
 * 
 * 展示Java字节码的结构和分析方法
 */
public class BytecodeAnalysisExample {
    
    // 用于演示的字段
    private int instanceField = 42;
    private static String staticField = "Hello";
    
    public static void main(String[] args) {
        BytecodeAnalysisExample example = new BytecodeAnalysisExample();
        
        System.out.println("=== Java字节码分析示例 ===\n");
        
        // 1. 字节码基础概念
        example.explainBytecodeBasics();
        
        // 2. 常见字节码指令演示
        example.demonstrateCommonInstructions();
        
        // 3. 方法调用的字节码
        example.analyzeMethodInvocation();
        
        // 4. 控制流的字节码
        example.analyzeControlFlow();
        
        // 5. 异常处理的字节码
        example.analyzeExceptionHandling();
        
        // 6. 字节码操作工具
        example.bytecodeManipulationTools();
    }
    
    /**
     * 解释字节码基础概念
     */
    private void explainBytecodeBasics() {
        System.out.println("=== 字节码基础概念 ===");
        
        System.out.println("1. 字节码定义:");
        System.out.println("   • Java源码编译后的中间代码");
        System.out.println("   • 平台无关的指令集合");
        System.out.println("   • 存储在.class文件中");
        System.out.println("   • 由JVM解释执行或编译为本地代码");
        
        System.out.println("\n2. 字节码文件结构:");
        System.out.println("   magic number (魔数)           4字节  0xCAFEBABE");
        System.out.println("   minor_version (次版本号)       2字节");
        System.out.println("   major_version (主版本号)       2字节"); 
        System.out.println("   constant_pool_count (常量池数量) 2字节");
        System.out.println("   constant_pool (常量池)         变长");
        System.out.println("   access_flags (访问标志)        2字节");
        System.out.println("   this_class (当前类)           2字节");
        System.out.println("   super_class (父类)            2字节");
        System.out.println("   interfaces_count (接口数量)    2字节");
        System.out.println("   interfaces (接口索引集合)      变长");
        System.out.println("   fields_count (字段数量)        2字节");
        System.out.println("   fields (字段集合)             变长");
        System.out.println("   methods_count (方法数量)       2字节");
        System.out.println("   methods (方法集合)            变长");
        System.out.println("   attributes_count (属性数量)    2字节");
        System.out.println("   attributes (属性集合)         变长");
        
        System.out.println("\n3. 操作数栈和局部变量表:");
        System.out.println("   • 操作数栈: 用于存放操作数");
        System.out.println("   • 局部变量表: 存放方法参数和局部变量");
        System.out.println("   • 栈帧: 每个方法调用都有独立的栈帧");
        System.out.println("   • 程序计数器: 指向当前执行的字节码指令");
        
        System.out.println();
    }
    
    /**
     * 演示常见字节码指令
     */
    private void demonstrateCommonInstructions() {
        System.out.println("=== 常见字节码指令演示 ===");
        
        System.out.println("1. 局部变量操作指令:");
        System.out.println("   load指令 - 从局部变量表加载到操作栈");
        System.out.println("   • iload_0, iload_1, iload_2, iload_3  (int)");
        System.out.println("   • lload_0, lload_1, lload_2, lload_3  (long)");
        System.out.println("   • fload_0, fload_1, fload_2, fload_3  (float)");
        System.out.println("   • dload_0, dload_1, dload_2, dload_3  (double)");
        System.out.println("   • aload_0, aload_1, aload_2, aload_3  (reference)");
        
        System.out.println("\n   store指令 - 从操作栈存储到局部变量表");
        System.out.println("   • istore, lstore, fstore, dstore, astore");
        
        // 演示方法
        simpleArithmeticMethod();
        
        System.out.println("\n2. 操作数栈操作指令:");
        System.out.println("   • push指令: iconst_0~5, bipush, sipush");
        System.out.println("   • pop指令: pop, pop2");
        System.out.println("   • dup指令: dup, dup_x1, dup_x2, dup2");
        
        System.out.println("\n3. 算术指令:");
        System.out.println("   • 加法: iadd, ladd, fadd, dadd");
        System.out.println("   • 减法: isub, lsub, fsub, dsub");
        System.out.println("   • 乘法: imul, lmul, fmul, dmul");
        System.out.println("   • 除法: idiv, ldiv, fdiv, ddiv");
        System.out.println("   • 求余: irem, lrem, frem, drem");
        System.out.println("   • 取负: ineg, lneg, fneg, dneg");
        
        System.out.println("\n4. 类型转换指令:");
        System.out.println("   • 宽化转换: i2l, i2f, i2d, l2f, l2d, f2d");
        System.out.println("   • 窄化转换: i2b, i2c, i2s, l2i, f2i, f2l, d2i, d2l, d2f");
        
        System.out.println();
    }
    
    /**
     * 简单算术方法（用于演示字节码）
     */
    private void simpleArithmeticMethod() {
        /*
         * 对应的字节码（通过javap -c查看）:
         * 
         * private void simpleArithmeticMethod();
         *   Code:
         *    0: iconst_5        // 将常量5压入操作栈
         *    1: istore_1        // 存储到局部变量表索引1
         *    2: iconst_3        // 将常量3压入操作栈  
         *    3: istore_2        // 存储到局部变量表索引2
         *    4: iload_1         // 加载局部变量1到操作栈
         *    5: iload_2         // 加载局部变量2到操作栈
         *    6: iadd            // 执行整数加法
         *    7: istore_3        // 存储结果到局部变量表索引3
         *    8: return          // 返回
         */
        
        int a = 5;      // iconst_5, istore_1
        int b = 3;      // iconst_3, istore_2  
        int c = a + b;  // iload_1, iload_2, iadd, istore_3
        
        System.out.println("   简单算术运算: " + a + " + " + b + " = " + c);
    }
    
    /**
     * 分析方法调用的字节码
     */
    private void analyzeMethodInvocation() {
        System.out.println("=== 方法调用字节码分析 ===");
        
        System.out.println("1. 方法调用指令:");
        System.out.println("   • invokestatic - 调用静态方法");
        System.out.println("   • invokespecial - 调用构造方法、私有方法、父类方法");
        System.out.println("   • invokevirtual - 调用实例方法（动态分派）");
        System.out.println("   • invokeinterface - 调用接口方法");
        System.out.println("   • invokedynamic - 调用动态方法（Lambda、方法引用）");
        
        // 演示不同类型的方法调用
        System.out.println("\n2. 方法调用示例:");
        
        // 静态方法调用
        staticMethodExample();  // invokestatic
        
        // 实例方法调用  
        instanceMethodExample(); // invokevirtual
        
        // 构造方法调用
        Object obj = new Object(); // new, dup, invokespecial <init>
        
        // 接口方法调用演示
        interfaceMethodExample();
        
        System.out.println("\n3. 方法调用过程:");
        System.out.println("   1. 参数压入操作栈");
        System.out.println("   2. 执行invoke指令");
        System.out.println("   3. 创建新栈帧");
        System.out.println("   4. 参数传递到局部变量表");
        System.out.println("   5. 执行方法体");
        System.out.println("   6. 返回结果到调用者操作栈");
        System.out.println("   7. 恢复调用者栈帧");
        
        System.out.println();
    }
    
    private static void staticMethodExample() {
        System.out.println("   静态方法调用示例");
    }
    
    private void instanceMethodExample() {
        System.out.println("   实例方法调用示例");
    }
    
    private void interfaceMethodExample() {
        Runnable task = () -> System.out.println("   Lambda表达式示例"); // invokedynamic
        task.run(); // invokeinterface
    }
    
    /**
     * 分析控制流的字节码
     */
    private void analyzeControlFlow() {
        System.out.println("=== 控制流字节码分析 ===");
        
        System.out.println("1. 条件分支指令:");
        System.out.println("   • 条件跳转: ifeq, ifne, iflt, ifle, ifgt, ifge");
        System.out.println("   • 比较跳转: if_icmpeq, if_icmpne, if_icmplt, if_icmple, if_icmpgt, if_icmpge");
        System.out.println("   • 引用比较: if_acmpeq, if_acmpne");
        System.out.println("   • 空值检查: ifnull, ifnonnull");
        
        // if-else示例
        conditionalExample(5);
        
        System.out.println("\n2. 循环指令:");
        System.out.println("   • 无条件跳转: goto");
        System.out.println("   • 子程序调用: jsr, ret (已废弃)");
        
        // 循环示例
        loopExample();
        
        System.out.println("\n3. switch语句:");
        System.out.println("   • lookupswitch - 稀疏case值");
        System.out.println("   • tableswitch - 连续case值");
        
        // switch示例
        switchExample(2);
        
        System.out.println();
    }
    
    private void conditionalExample(int value) {
        /*
         * if-else的字节码结构:
         * 
         * iload_1          // 加载参数value
         * iconst_0         // 加载常量0
         * if_icmple L1     // 如果value <= 0跳转到L1
         * // if代码块
         * goto L2          // 跳转到L2
         * L1:
         * // else代码块  
         * L2:
         * // 后续代码
         */
        
        if (value > 0) {
            System.out.println("   条件为真: " + value);
        } else {
            System.out.println("   条件为假: " + value);
        }
    }
    
    private void loopExample() {
        /*
         * for循环的字节码结构:
         * 
         * iconst_0         // i = 0
         * istore_1
         * goto L2          // 跳转到条件检查
         * L1:              // 循环体
         * // 循环体代码
         * iinc 1, 1        // i++
         * L2:              // 条件检查
         * iload_1          // 加载i
         * iconst_3         // 加载3
         * if_icmplt L1     // 如果i < 3继续循环
         */
        
        for (int i = 0; i < 3; i++) {
            System.out.println("   循环迭代: " + i);
        }
    }
    
    private void switchExample(int value) {
        /*
         * switch的字节码（tableswitch示例）:
         * 
         * iload_1          // 加载value
         * tableswitch {    // switch跳转表
         *   default: L4    // 默认分支
         *   1: L1          // case 1
         *   2: L2          // case 2  
         *   3: L3          // case 3
         * }
         * L1: // case 1代码
         * goto L5
         * L2: // case 2代码  
         * goto L5
         * L3: // case 3代码
         * goto L5
         * L4: // default代码
         * L5: // switch后续代码
         */
        
        switch (value) {
            case 1:
                System.out.println("   Switch case 1");
                break;
            case 2:
                System.out.println("   Switch case 2");
                break;
            case 3:
                System.out.println("   Switch case 3");
                break;
            default:
                System.out.println("   Switch default");
                break;
        }
    }
    
    /**
     * 分析异常处理的字节码
     */
    private void analyzeExceptionHandling() {
        System.out.println("=== 异常处理字节码分析 ===");
        
        System.out.println("1. 异常处理指令:");
        System.out.println("   • athrow - 抛出异常");
        System.out.println("   • Exception Table - 异常处理表");
        System.out.println("   • finally块通过jsr/ret实现（JDK6前）或代码复制");
        
        System.out.println("\n2. try-catch-finally结构:");
        
        // try-catch示例
        tryCatchExample();
        
        System.out.println("\n3. 异常表结构:");
        System.out.println("   Exception table:");
        System.out.println("     from    to   target type");
        System.out.println("       0     8     11   Class java/lang/Exception");
        System.out.println("       0     8     19   any");
        System.out.println("      11    16     19   any");
        
        System.out.println("\n4. 异常处理过程:");
        System.out.println("   1. 异常发生时，JVM查找异常表");
        System.out.println("   2. 检查PC计数器是否在[from, to)范围内");
        System.out.println("   3. 匹配异常类型");
        System.out.println("   4. 跳转到target处继续执行");
        System.out.println("   5. 如果没找到匹配项，向上传播异常");
        
        System.out.println();
    }
    
    private void tryCatchExample() {
        /*
         * try-catch-finally的字节码结构:
         * 
         * try代码块:
         * 0: // try块代码
         * 
         * catch代码块:
         * 11: astore_1        // 存储异常对象
         * 12: // catch块代码
         * 
         * finally代码块:
         * 19: // finally块代码（可能重复多份）
         * 
         * Exception table:
         *   from  to  target type
         *    0    8    11   Class java/lang/Exception  
         *    0    8    19   any                        // finally
         *   11   16    19   any                        // catch中的finally
         */
        
        try {
            int result = 10 / 2; // 可能抛出异常的代码
            System.out.println("   Try块执行: " + result);
        } catch (ArithmeticException e) {
            System.out.println("   Catch块执行: " + e.getMessage());
        } finally {
            System.out.println("   Finally块执行");
        }
    }
    
    /**
     * 字节码操作工具
     */
    private void bytecodeManipulationTools() {
        System.out.println("=== 字节码操作工具 ===");
        
        System.out.println("1. 字节码查看工具:");
        System.out.println("   • javap - JDK自带反编译工具");
        System.out.println("     javap -c ClassName        # 查看字节码");
        System.out.println("     javap -v ClassName        # 详细信息");
        System.out.println("     javap -p ClassName        # 包含私有成员");
        System.out.println("   • IDEA插件: jclasslib Bytecode viewer");
        System.out.println("   • Eclipse插件: Bytecode Outline");
        
        System.out.println("\n2. 字节码操作框架:");
        System.out.println("   • ASM - 轻量级字节码操作框架");
        System.out.println("     - 直接操作字节码，性能高");
        System.out.println("     - API相对复杂");
        System.out.println("     - 支持访问者模式");
        
        System.out.println("   • Javassist - 高级字节码操作库");
        System.out.println("     - 提供Java源码级别的API");
        System.out.println("     - 使用简单，但性能略低");
        System.out.println("     - 支持运行时修改");
        
        System.out.println("   • ByteBuddy - 现代字节码操作库");
        System.out.println("     - API友好，类型安全");
        System.out.println("     - 支持Java Agent");
        System.out.println("     - 广泛应用于框架开发");
        
        System.out.println("\n3. 应用场景:");
        System.out.println("   • 动态代理实现");
        System.out.println("   • AOP切面编程");
        System.out.println("   • 代码插桩和监控");
        System.out.println("   • 框架和容器开发");
        System.out.println("   • 性能优化和分析");
        System.out.println("   • 代码混淆和加密");
        
        System.out.println("\n4. 字节码分析示例:");
        analyzeCurrentClassBytecode();
        
        System.out.println();
    }
    
    /**
     * 分析当前类的字节码信息
     */
    private void analyzeCurrentClassBytecode() {
        try {
            Class<?> currentClass = this.getClass();
            System.out.println("   当前类: " + currentClass.getName());
            
            // 获取类文件路径
            String className = currentClass.getSimpleName() + ".class";
            String classPath = currentClass.getResource(className).getPath();
            System.out.println("   类文件路径: " + classPath);
            
            // 读取字节码文件头
            try (FileInputStream fis = new FileInputStream(classPath)) {
                byte[] magic = new byte[4];
                fis.read(magic);
                
                // 验证魔数
                if (magic[0] == (byte) 0xCA && magic[1] == (byte) 0xFE && 
                    magic[2] == (byte) 0xBA && magic[3] == (byte) 0xBE) {
                    System.out.println("   魔数验证: ✓ 0xCAFEBABE");
                } else {
                    System.out.println("   魔数验证: ✗ 无效的class文件");
                }
                
                // 读取版本信息
                byte[] version = new byte[4];
                fis.read(version);
                int minorVersion = ((version[0] & 0xFF) << 8) | (version[1] & 0xFF);
                int majorVersion = ((version[2] & 0xFF) << 8) | (version[3] & 0xFF);
                
                System.out.println("   版本信息: " + majorVersion + "." + minorVersion);
                
                // Java版本映射
                String javaVersion = getJavaVersion(majorVersion);
                System.out.println("   编译版本: " + javaVersion);
                
            }
            
            // 分析方法信息
            Method[] methods = currentClass.getDeclaredMethods();
            System.out.println("   方法数量: " + methods.length);
            
            // 显示部分方法信息
            for (int i = 0; i < Math.min(3, methods.length); i++) {
                Method method = methods[i];
                System.out.println("     方法" + (i+1) + ": " + method.getName() + 
                                 " (参数: " + method.getParameterCount() + "个)");
            }
            
        } catch (IOException e) {
            System.out.println("   字节码分析失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据major version获取Java版本
     */
    private String getJavaVersion(int majorVersion) {
        switch (majorVersion) {
            case 45: return "Java 1.1";
            case 46: return "Java 1.2"; 
            case 47: return "Java 1.3";
            case 48: return "Java 1.4";
            case 49: return "Java 5";
            case 50: return "Java 6";
            case 51: return "Java 7";
            case 52: return "Java 8";
            case 53: return "Java 9";
            case 54: return "Java 10";
            case 55: return "Java 11";
            case 56: return "Java 12";
            case 57: return "Java 13";
            case 58: return "Java 14";
            case 59: return "Java 15";
            case 60: return "Java 16";
            case 61: return "Java 17";
            case 62: return "Java 18";
            case 63: return "Java 19";
            case 64: return "Java 20";
            case 65: return "Java 21";
            default: return "未知版本 (" + majorVersion + ")";
        }
    }
}