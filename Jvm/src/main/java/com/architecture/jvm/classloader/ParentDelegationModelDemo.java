package com.architecture.jvm.classloader;

import java.net.*;

/**
 * 双亲委派模型深度演示
 *
 * 双亲委派模型的工作流程：
 * 1. 类加载器收到类加载请求
 * 2. 把请求委派给父加载器完成，一直向上委派到Bootstrap ClassLoader
 * 3. 父加载器无法完成加载时，子加载器才尝试自己加载
 *
 * 类加载器层次：
 * ┌──────────────────────────────┐
 * │ Bootstrap ClassLoader        │ ← C++实现，加载核心库
 * │ (启动类加载器)                │   (rt.jar, JAVA_HOME/lib)
 * └──────────┬───────────────────┘
 *            │ parent
 * ┌──────────▼───────────────────┐
 * │ Extension ClassLoader        │ ← 加载扩展库
 * │ (扩展类加载器)                │   (JAVA_HOME/lib/ext)
 * └──────────┬───────────────────┘
 *            │ parent
 * ┌──────────▼───────────────────┐
 * │ Application ClassLoader      │ ← 加载应用程序类路径
 * │ (应用程序类加载器)            │   (classpath)
 * └──────────┬───────────────────┘
 *            │ parent
 * ┌──────────▼───────────────────┐
 * │ Custom ClassLoader           │ ← 用户自定义加载器
 * │ (自定义类加载器)              │
 * └──────────────────────────────┘
 *
 * @author Architecture
 */
public class ParentDelegationModelDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== 双亲委派模型深度演示 ===\n");

        // 1. 类加载器层次结构
        demonstrateClassLoaderHierarchy();

        // 2. 双亲委派流程
        demonstrateParentDelegation();

        // 3. 为什么需要双亲委派
        explainWhyParentDelegation();

        // 4. 打破双亲委派
        demonstrateBreakParentDelegation();

        // 5. 类的唯一性
        demonstrateClassUniqueness();

        // 6. 双亲委派的优缺点
        discussAdvantagesAndDisadvantages();
    }

    /**
     * 1. 展示类加载器层次结构
     */
    private static void demonstrateClassLoaderHierarchy() {
        System.out.println("【1. 类加载器层次结构】\n");

        // 获取各级类加载器
        ClassLoader appClassLoader = ParentDelegationModelDemo.class.getClassLoader();
        ClassLoader extClassLoader = appClassLoader.getParent();
        ClassLoader bootstrapClassLoader = extClassLoader.getParent();

        System.out.println("当前类的类加载器:");
        System.out.println("  " + appClassLoader);
        System.out.println("  └─ 类型: " + appClassLoader.getClass().getName());

        System.out.println("\n父类加载器:");
        System.out.println("  " + extClassLoader);
        if (extClassLoader != null) {
            System.out.println("  └─ 类型: " + extClassLoader.getClass().getName());
        }

        System.out.println("\n父父类加载器:");
        System.out.println("  " + bootstrapClassLoader);
        System.out.println("  └─ 说明: null表示Bootstrap ClassLoader (C++实现)");

        // 查看不同类的类加载器
        System.out.println("\n不同类的类加载器:");

        // JDK核心类 - Bootstrap ClassLoader加载
        System.out.println("  String类: " + String.class.getClassLoader());
        System.out.println("    └─ 由Bootstrap ClassLoader加载");

        // 扩展库类 - Extension ClassLoader加载
        try {
            Class<?> zipClass = Class.forName("com.sun.crypto.provider.SunJCE");
            System.out.println("  SunJCE类: " + zipClass.getClassLoader());
            System.out.println("    └─ 由Extension ClassLoader加载");
        } catch (ClassNotFoundException e) {
            System.out.println("  (扩展类示例未找到)");
        }

        // 应用程序类 - Application ClassLoader加载
        System.out.println("  本类: " + ParentDelegationModelDemo.class.getClassLoader());
        System.out.println("    └─ 由Application ClassLoader加载");

        System.out.println("\n类加载器的加载路径:");
        System.out.println("  Bootstrap ClassLoader:");
        System.out.println("    " + System.getProperty("sun.boot.class.path"));

        System.out.println("\n  Application ClassLoader:");
        System.out.println("    " + System.getProperty("java.class.path"));
        System.out.println();
    }

    /**
     * 2. 演示双亲委派流程
     */
    private static void demonstrateParentDelegation() {
        System.out.println("【2. 双亲委派流程演示】\n");

        System.out.println("双亲委派的加载流程:");
        System.out.println("  1. 自定义ClassLoader收到加载请求");
        System.out.println("     ↓ 委派给父加载器");
        System.out.println("  2. Application ClassLoader");
        System.out.println("     ↓ 委派给父加载器");
        System.out.println("  3. Extension ClassLoader");
        System.out.println("     ↓ 委派给父加载器");
        System.out.println("  4. Bootstrap ClassLoader");
        System.out.println("     ├─ 找到类 → 加载并返回");
        System.out.println("     └─ 未找到 ↓");
        System.out.println("  5. 返回Extension ClassLoader");
        System.out.println("     ├─ 找到类 → 加载并返回");
        System.out.println("     └─ 未找到 ↓");
        System.out.println("  6. 返回Application ClassLoader");
        System.out.println("     ├─ 找到类 → 加载并返回");
        System.out.println("     └─ 未找到 ↓");
        System.out.println("  7. 返回自定义ClassLoader");
        System.out.println("     ├─ 找到类 → 加载并返回");
        System.out.println("     └─ 未找到 → ClassNotFoundException");

        // 创建自定义类加载器演示
        System.out.println("\n创建自定义类加载器:");
        CustomClassLoaderWithLog customLoader = new CustomClassLoaderWithLog();

        try {
            System.out.println("\n尝试加载 java.lang.String:");
            Class<?> stringClass = customLoader.loadClass("java.lang.String");
            System.out.println("加载成功: " + stringClass.getName());
            System.out.println("实际加载器: " + stringClass.getClassLoader());
            System.out.println("→ 虽然使用自定义加载器，但委派给Bootstrap加载");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println();
    }

    /**
     * 3. 解释为什么需要双亲委派
     */
    private static void explainWhyParentDelegation() {
        System.out.println("【3. 为什么需要双亲委派模型？】\n");

        System.out.println("原因1: 防止类的重复加载");
        System.out.println("  • 父加载器已加载的类，子加载器不会重新加载");
        System.out.println("  • 保证类在JVM中的唯一性");

        System.out.println("\n原因2: 保护核心API");
        System.out.println("  • 防止核心类库被篡改");
        System.out.println("  • 即使自定义java.lang.String，也会被Bootstrap加载器拒绝");

        System.out.println("\n原因3: 保证类加载的一致性");
        System.out.println("  • 同一个类由同一个加载器加载");
        System.out.println("  • 避免类型转换异常");

        // 演示：尝试自定义java.lang.String
        System.out.println("\n演示：尝试自定义核心类");
        System.out.println("如果我们定义一个java.lang.String类:");
        System.out.println("  package java.lang;");
        System.out.println("  public class String { ... }");
        System.out.println("\n结果:");
        System.out.println("  • 双亲委派会将加载请求委派给Bootstrap ClassLoader");
        System.out.println("  • Bootstrap会加载JDK自带的String");
        System.out.println("  • 自定义的String永远不会被加载");
        System.out.println("  • 这保护了核心类库不被篡改");

        System.out.println("\n安全性示例:");
        System.out.println("  假设没有双亲委派:");
        System.out.println("    恶意代码: package java.lang;");
        System.out.println("              public class String {");
        System.out.println("                  // 恶意代码");
        System.out.println("              }");
        System.out.println("  后果: 整个JVM的String类被替换，系统崩溃!");
        System.out.println();
    }

    /**
     * 4. 演示如何打破双亲委派
     */
    private static void demonstrateBreakParentDelegation() {
        System.out.println("【4. 打破双亲委派模型】\n");

        System.out.println("打破双亲委派的场景:");
        System.out.println("  1. Tomcat的类加载器");
        System.out.println("     • 每个Web应用使用独立的ClassLoader");
        System.out.println("     • 优先加载WEB-INF/classes和WEB-INF/lib");
        System.out.println("     • 实现应用间的类隔离");

        System.out.println("\n  2. OSGi模块化框架");
        System.out.println("     • 模块间的类加载可以相互委派");
        System.out.println("     • 实现模块的热插拔");

        System.out.println("\n  3. SPI (Service Provider Interface)");
        System.out.println("     • 核心库需要加载第三方实现");
        System.out.println("     • 使用线程上下文类加载器");

        System.out.println("\n  4. 热部署场景");
        System.out.println("     • 需要卸载旧类，加载新类");
        System.out.println("     • 自定义类加载器不委派给父加载器");

        // 创建打破双亲委派的类加载器
        System.out.println("\n演示：打破双亲委派的类加载器");
        System.out.println("(重写loadClass方法，不调用父加载器)");

        BreakParentDelegationClassLoader breakLoader = new BreakParentDelegationClassLoader();
        System.out.println("创建了打破双亲委派的类加载器");
        System.out.println("此加载器会优先自己加载类，而不是委派给父加载器");
        System.out.println();
    }

    /**
     * 5. 演示类的唯一性
     */
    private static void demonstrateClassUniqueness() {
        System.out.println("【5. 类的唯一性】\n");

        System.out.println("类的唯一性由两个因素决定:");
        System.out.println("  1. 类的全限定名");
        System.out.println("  2. 加载这个类的ClassLoader");

        System.out.println("\n即: 同一个类 + 不同的ClassLoader = 不同的类");

        try {
            // 使用不同的类加载器加载同一个类
            CustomClassLoaderWithLog loader1 = new CustomClassLoaderWithLog();
            CustomClassLoaderWithLog loader2 = new CustomClassLoaderWithLog();

            // 加载应用类
            Class<?> class1 = loader1.loadClass("com.architecture.jvm.classloader.ParentDelegationModelDemo");
            Class<?> class2 = loader2.loadClass("com.architecture.jvm.classloader.ParentDelegationModelDemo");

            System.out.println("\n演示:");
            System.out.println("  类1加载器: " + class1.getClassLoader());
            System.out.println("  类2加载器: " + class2.getClassLoader());
            System.out.println("  class1 == class2: " + (class1 == class2));
            System.out.println("\n说明:");
            System.out.println("  • 虽然使用不同的自定义加载器");
            System.out.println("  • 但由于双亲委派，都委派给Application ClassLoader");
            System.out.println("  • 所以加载的是同一个类");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println();
    }

    /**
     * 6. 讨论双亲委派的优缺点
     */
    private static void discussAdvantagesAndDisadvantages() {
        System.out.println("【6. 双亲委派模型的优缺点】\n");

        System.out.println("优点:");
        System.out.println("  ✓ 避免类的重复加载");
        System.out.println("  ✓ 保护核心类库");
        System.out.println("  ✓ 保证类加载的一致性");
        System.out.println("  ✓ 提供了类的作用域隔离");

        System.out.println("\n缺点:");
        System.out.println("  ✗ 父加载器无法访问子加载器加载的类");
        System.out.println("  ✗ 不适合某些复杂场景 (如Tomcat、OSGi)");
        System.out.println("  ✗ SPI机制需要打破双亲委派");

        System.out.println("\n解决方案:");
        System.out.println("  1. 线程上下文类加载器 (Thread Context ClassLoader)");
        System.out.println("     • JDBC驱动加载");
        System.out.println("     • JNDI服务");

        System.out.println("\n  2. 自定义类加载器");
        System.out.println("     • 重写loadClass方法");
        System.out.println("     • 实现特定的加载逻辑");

        System.out.println("\n  3. 模块化系统 (Java 9+)");
        System.out.println("     • 使用Module System");
        System.out.println("     • 更灵活的类可见性控制");
        System.out.println();
    }

    /**
     * 自定义类加载器 - 带日志
     */
    static class CustomClassLoaderWithLog extends ClassLoader {
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            System.out.println("  → CustomClassLoader尝试加载: " + name);
            System.out.println("    ↓ 委派给父加载器");
            Class<?> clazz = super.loadClass(name);
            System.out.println("    ✓ 加载完成，实际加载器: " + clazz.getClassLoader());
            return clazz;
        }
    }

    /**
     * 打破双亲委派的类加载器
     */
    static class BreakParentDelegationClassLoader extends ClassLoader {
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            // 不委派给父加载器，优先自己加载
            if (name.startsWith("com.myapp.")) {
                // 自定义加载逻辑
                return findClass(name);
            }
            // 系统类还是委派给父加载器
            return super.loadClass(name);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            // 这里实现自定义的类加载逻辑
            // 例如从特定路径、网络、数据库等加载字节码
            return super.findClass(name);
        }
    }
}
