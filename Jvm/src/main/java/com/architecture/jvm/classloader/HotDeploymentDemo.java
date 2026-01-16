package com.architecture.jvm.classloader;

import java.io.*;
import java.nio.file.*;

/**
 * 热部署(Hot Deployment)演示
 *
 * 热部署原理：
 * 1. 使用自定义ClassLoader加载类
 * 2. 当类文件修改时，创建新的ClassLoader实例
 * 3. 用新的ClassLoader加载修改后的类
 * 4. 丢弃旧ClassLoader的引用，让GC回收
 *
 * 关键点：
 * - 每个ClassLoader实例加载的类都是独立的
 * - 同一个类被不同ClassLoader加载，在JVM中被视为不同的类
 * - 通过反射调用，避免类型转换问题
 *
 * @author Architecture
 */
public class HotDeploymentDemo {

    private static final String CLASS_PATH = "target/classes/";
    private static final String CLASS_NAME = "com.architecture.jvm.classloader.DynamicClass";

    public static void main(String[] args) throws Exception {
        System.out.println("=== 热部署演示 ===\n");

        // 1. 热部署原理
        explainHotDeployment();

        // 2. 简单热部署演示
        demonstrateSimpleHotDeployment();

        // 3. 文件监听热部署
        // demonstrateFileWatcherHotDeployment(); // 注释掉，避免无限循环

        // 4. 热部署的应用场景
        discussUseCases();

        // 5. 热部署的注意事项
        discussCautions();
    }

    /**
     * 1. 解释热部署原理
     */
    private static void explainHotDeployment() {
        System.out.println("【1. 热部署原理】\n");

        System.out.println("传统部署 vs 热部署:");
        System.out.println("  传统部署:");
        System.out.println("    1. 修改代码");
        System.out.println("    2. 重新编译");
        System.out.println("    3. 停止应用");
        System.out.println("    4. 替换class文件");
        System.out.println("    5. 重启应用");
        System.out.println("    ✗ 停机时间，影响服务");

        System.out.println("\n  热部署:");
        System.out.println("    1. 修改代码");
        System.out.println("    2. 重新编译");
        System.out.println("    3. 检测到文件变化");
        System.out.println("    4. 创建新ClassLoader");
        System.out.println("    5. 加载新类");
        System.out.println("    6. 替换旧实例");
        System.out.println("    ✓ 零停机，不影响服务");

        System.out.println("\n热部署的核心机制:");
        System.out.println("  ClassLoader隔离:");
        System.out.println("    ┌─────────────────┐");
        System.out.println("    │ ClassLoader V1  │ → Class A (version 1)");
        System.out.println("    └─────────────────┘");
        System.out.println("           ↓ 丢弃引用");
        System.out.println("    ┌─────────────────┐");
        System.out.println("    │ ClassLoader V2  │ → Class A (version 2)");
        System.out.println("    └─────────────────┘");

        System.out.println("\n关键点:");
        System.out.println("  • 每次重新加载创建新的ClassLoader");
        System.out.println("  • 新ClassLoader加载修改后的类");
        System.out.println("  • 旧ClassLoader等待GC回收");
        System.out.println("  • 通过反射调用，避免类型问题");
        System.out.println();
    }

    /**
     * 2. 简单热部署演示
     */
    private static void demonstrateSimpleHotDeployment() throws Exception {
        System.out.println("【2. 简单热部署演示】\n");

        // 第一次加载
        System.out.println("第一次加载:");
        HotSwapClassLoader loader1 = new HotSwapClassLoader();
        Class<?> clazz1 = loader1.loadClass(CLASS_NAME);
        Object instance1 = clazz1.newInstance();

        System.out.println("  类: " + clazz1.getName());
        System.out.println("  ClassLoader: " + clazz1.getClassLoader());
        System.out.println("  实例: " + instance1);

        // 模拟类文件修改
        System.out.println("\n(假设类文件已被修改...)\n");

        // 第二次加载 - 使用新的ClassLoader
        System.out.println("第二次加载 (热部署):");
        HotSwapClassLoader loader2 = new HotSwapClassLoader();
        Class<?> clazz2 = loader2.loadClass(CLASS_NAME);
        Object instance2 = clazz2.newInstance();

        System.out.println("  类: " + clazz2.getName());
        System.out.println("  ClassLoader: " + clazz2.getClassLoader());
        System.out.println("  实例: " + instance2);

        // 比较两次加载的类
        System.out.println("\n比较:");
        System.out.println("  类名相同: " + clazz1.getName().equals(clazz2.getName()));
        System.out.println("  是同一个类: " + (clazz1 == clazz2));
        System.out.println("  ClassLoader相同: " + (loader1 == loader2));

        System.out.println("\n说明:");
        System.out.println("  • 虽然类名相同，但由于ClassLoader不同");
        System.out.println("  • 两个类在JVM中被视为不同的类");
        System.out.println("  • 这就是热部署的基础");

        // 释放旧的ClassLoader
        loader1 = null;
        clazz1 = null;
        instance1 = null;
        System.out.println("\n  已释放旧ClassLoader引用，等待GC回收");
        System.out.println();
    }

    /**
     * 3. 文件监听热部署
     */
    @SuppressWarnings("unused")
    private static void demonstrateFileWatcherHotDeployment() throws Exception {
        System.out.println("【3. 文件监听热部署】\n");

        System.out.println("监听类文件变化，自动热部署...");
        System.out.println("(按Ctrl+C停止)\n");

        // 监听目录
        Path path = Paths.get(CLASS_PATH);
        WatchService watchService = FileSystems.getDefault().newWatchService();
        path.register(watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_MODIFY);

        // 当前加载的ClassLoader
        HotSwapClassLoader currentLoader = new HotSwapClassLoader();
        Class<?> currentClass = currentLoader.loadClass(CLASS_NAME);

        System.out.println("初始加载: " + currentClass);

        // 监听循环
        while (true) {
            WatchKey key = watchService.take();

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    String fileName = event.context().toString();

                    if (fileName.endsWith(".class")) {
                        System.out.println("\n检测到文件修改: " + fileName);
                        System.out.println("执行热部署...");

                        // 创建新的ClassLoader
                        currentLoader = new HotSwapClassLoader();
                        currentClass = currentLoader.loadClass(CLASS_NAME);

                        System.out.println("热部署完成: " + currentClass);
                        System.out.println("新ClassLoader: " + currentLoader);
                    }
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

    /**
     * 4. 热部署的应用场景
     */
    private static void discussUseCases() {
        System.out.println("【4. 热部署的应用场景】\n");

        System.out.println("1. 开发阶段:");
        System.out.println("   • IDE的热部署功能 (IntelliJ IDEA, Eclipse)");
        System.out.println("   • Spring Boot DevTools");
        System.out.println("   • JRebel");
        System.out.println("   优势: 提高开发效率，无需频繁重启");

        System.out.println("\n2. Web容器:");
        System.out.println("   • Tomcat的自动部署");
        System.out.println("   • Jetty的热部署");
        System.out.println("   优势: 更新应用不停机");

        System.out.println("\n3. OSGi框架:");
        System.out.println("   • 模块的热插拔");
        System.out.println("   • 动态更新bundle");
        System.out.println("   优势: 模块化管理，灵活更新");

        System.out.println("\n4. 微服务:");
        System.out.println("   • 灰度发布");
        System.out.println("   • 金丝雀部署");
        System.out.println("   优势: 平滑升级，降低风险");

        System.out.println("\n5. 规则引擎:");
        System.out.println("   • 业务规则热更新");
        System.out.println("   • 动态脚本加载");
        System.out.println("   优势: 业务逻辑快速调整");
        System.out.println();
    }

    /**
     * 5. 热部署的注意事项
     */
    private static void discussCautions() {
        System.out.println("【5. 热部署的注意事项】\n");

        System.out.println("限制:");
        System.out.println("  ✗ 无法修改类的结构 (字段、方法签名)");
        System.out.println("  ✗ 无法修改父类");
        System.out.println("  ✗ 无法修改接口");
        System.out.println("  ✓ 可以修改方法实现");

        System.out.println("\n内存问题:");
        System.out.println("  • 旧ClassLoader如果不被释放，会导致内存泄漏");
        System.out.println("  • 每次热部署都会加载新类，增加元空间使用");
        System.out.println("  • 需要配置足够的元空间大小");

        System.out.println("\n类型问题:");
        System.out.println("  • 新旧版本的类不兼容");
        System.out.println("  • 不能直接类型转换");
        System.out.println("  • 建议使用接口 + 反射调用");

        System.out.println("\n线程安全:");
        System.out.println("  • 正在执行的方法不会立即切换");
        System.out.println("  • 需要等待当前请求完成");
        System.out.println("  • 考虑使用版本号或锁机制");

        System.out.println("\n最佳实践:");
        System.out.println("  1. 开发环境使用，生产环境谨慎");
        System.out.println("  2. 配合接口和反射使用");
        System.out.println("  3. 监控内存使用情况");
        System.out.println("  4. 做好异常处理");
        System.out.println("  5. 保留回滚机制");

        System.out.println("\nJDK热部署方案:");
        System.out.println("  • Java Agent (JVMTI)");
        System.out.println("  • Instrumentation API");
        System.out.println("  • Attach API");
        System.out.println("  这些方案可以修改类结构，但有性能开销");
        System.out.println();
    }

    /**
     * 热部署类加载器
     */
    static class HotSwapClassLoader extends ClassLoader {
        private String classPath;

        public HotSwapClassLoader() {
            this.classPath = CLASS_PATH;
        }

        public HotSwapClassLoader(String classPath) {
            this.classPath = classPath;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                // 将类名转换为文件路径
                String fileName = name.replace('.', '/') + ".class";
                String fullPath = classPath + fileName;

                // 读取class文件字节码
                byte[] classData = loadClassData(fullPath);

                if (classData == null) {
                    throw new ClassNotFoundException(name);
                }

                // 定义类
                return defineClass(name, classData, 0, classData.length);

            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }

        /**
         * 加载class文件数据
         */
        private byte[] loadClassData(String filePath) throws IOException {
            File file = new File(filePath);
            if (!file.exists()) {
                return null;
            }

            try (FileInputStream fis = new FileInputStream(file);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) != -1) {
                    baos.write(buffer, 0, length);
                }

                return baos.toByteArray();
            }
        }
    }
}

/**
 * 用于热部署演示的动态类
 * (实际使用时，这个类会被编译并监听修改)
 */
class DynamicClass {
    public String getMessage() {
        return "Version 1.0";
    }

    public void execute() {
        System.out.println("Executing version 1.0");
    }
}
