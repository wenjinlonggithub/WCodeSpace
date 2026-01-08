package com.architecture.jvm.classloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 类加载机制演示
 * 
 * 展示双亲委派模型和自定义类加载器
 */
public class ClassLoaderExample {
    
    public static void main(String[] args) {
        ClassLoaderExample example = new ClassLoaderExample();
        
        System.out.println("=== JVM类加载机制演示 ===\n");
        
        // 1. 演示类加载器层次结构
        example.demonstrateClassLoaderHierarchy();
        
        // 2. 演示双亲委派模型
        example.demonstrateParentDelegationModel();
        
        // 3. 演示自定义类加载器
        example.demonstrateCustomClassLoader();
        
        // 4. 演示类的唯一性
        example.demonstrateClassUniqueness();
        
        // 5. 演示类的初始化
        example.demonstrateClassInitialization();
    }
    
    /**
     * 演示类加载器层次结构
     */
    private void demonstrateClassLoaderHierarchy() {
        System.out.println("=== 类加载器层次结构 ===");
        
        // 获取当前类的类加载器
        ClassLoader classLoader = this.getClass().getClassLoader();
        System.out.println("当前类的类加载器: " + classLoader);
        
        // 遍历类加载器层次
        ClassLoader current = classLoader;
        int level = 1;
        
        while (current != null) {
            System.out.println("Level " + level + ": " + current.getClass().getName());
            
            // 显示类加载器的加载路径
            if (current instanceof URLClassLoader) {
                URLClassLoader urlClassLoader = (URLClassLoader) current;
                URL[] urls = urlClassLoader.getURLs();
                System.out.println("  加载路径:");
                for (URL url : urls) {
                    System.out.println("    " + url);
                }
            }
            
            current = current.getParent();
            level++;
        }
        
        System.out.println("Bootstrap ClassLoader (C++实现，Java中显示为null)");
        
        // 演示不同类的类加载器
        System.out.println("\n不同类的类加载器:");
        printClassLoader("java.lang.String", String.class);
        printClassLoader("java.util.List", java.util.List.class);
        printClassLoader("当前类", this.getClass());
        
        System.out.println();
    }
    
    /**
     * 打印类的加载器信息
     */
    private void printClassLoader(String description, Class<?> clazz) {
        ClassLoader loader = clazz.getClassLoader();
        System.out.println(String.format("  %s: %s", description, 
                loader != null ? loader.getClass().getName() : "Bootstrap ClassLoader"));
    }
    
    /**
     * 演示双亲委派模型
     */
    private void demonstrateParentDelegationModel() {
        System.out.println("=== 双亲委派模型演示 ===");
        
        try {
            // 创建自定义类加载器
            CustomClassLoader customLoader = new CustomClassLoader();
            
            // 尝试加载系统类 - 会委派给父加载器
            System.out.println("加载系统类 java.lang.String:");
            Class<?> stringClass = customLoader.loadClass("java.lang.String");
            System.out.println("  类加载器: " + stringClass.getClassLoader());
            System.out.println("  预期: Bootstrap ClassLoader (null)");
            
            // 尝试加载应用类
            System.out.println("\n加载应用类:");
            Class<?> currentClass = customLoader.loadClass(this.getClass().getName());
            System.out.println("  类加载器: " + currentClass.getClassLoader());
            
            // 演示类加载过程
            System.out.println("\n双亲委派过程演示:");
            System.out.println("1. 自定义类加载器首先检查类是否已加载");
            System.out.println("2. 如未加载，委派给父类加载器");
            System.out.println("3. 父类加载器重复此过程");
            System.out.println("4. 如果所有父加载器都无法加载，自己尝试加载");
            
        } catch (ClassNotFoundException e) {
            System.err.println("类加载失败: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 演示自定义类加载器
     */
    private void demonstrateCustomClassLoader() {
        System.out.println("=== 自定义类加载器演示 ===");
        
        try {
            // 创建文件类加载器
            FileClassLoader fileLoader = new FileClassLoader("./classes");
            
            System.out.println("文件类加载器创建成功");
            System.out.println("加载目录: " + new File("./classes").getAbsolutePath());
            
            // 演示加载过程
            System.out.println("\n自定义类加载器特点:");
            System.out.println("✓ 可以从文件系统加载类");
            System.out.println("✓ 可以实现热部署");
            System.out.println("✓ 可以加密/解密类文件");
            System.out.println("✓ 可以从网络加载类");
            
            // 创建网络类加载器示例
            NetworkClassLoader networkLoader = new NetworkClassLoader("http://example.com/classes/");
            System.out.println("\n网络类加载器示例:");
            System.out.println("可以从URL加载类: " + networkLoader.getBaseUrl());
            
        } catch (Exception e) {
            System.err.println("自定义类加载器演示失败: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 演示类的唯一性
     */
    private void demonstrateClassUniqueness() {
        System.out.println("=== 类的唯一性演示 ===");
        
        try {
            // 使用不同的类加载器加载同一个类
            CustomClassLoader loader1 = new CustomClassLoader("Loader1");
            CustomClassLoader loader2 = new CustomClassLoader("Loader2");
            
            // 加载同一个类
            String className = "java.util.ArrayList";
            Class<?> class1 = loader1.loadClass(className);
            Class<?> class2 = loader2.loadClass(className);
            
            System.out.println("类1: " + class1 + " (加载器: " + class1.getClassLoader() + ")");
            System.out.println("类2: " + class2 + " (加载器: " + class2.getClassLoader() + ")");
            System.out.println("类是否相同: " + (class1 == class2));
            System.out.println("类加载器是否相同: " + (class1.getClassLoader() == class2.getClassLoader()));
            
            System.out.println("\n类唯一性规则:");
            System.out.println("• 类的完整标识 = 类的全限定名 + 加载它的类加载器");
            System.out.println("• 同一个类加载器加载的同名类是同一个类");
            System.out.println("• 不同类加载器加载的同名类是不同的类");
            System.out.println("• 即使类文件完全相同，不同类加载器加载后也是不同的类");
            
        } catch (ClassNotFoundException e) {
            System.err.println("类加载失败: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 演示类的初始化
     */
    private void demonstrateClassInitialization() {
        System.out.println("=== 类初始化演示 ===");
        
        System.out.println("类初始化时机:");
        System.out.println("1. new 实例化对象时");
        System.out.println("2. 访问静态变量时（final常量除外）");
        System.out.println("3. 访问静态方法时");
        System.out.println("4. 反射调用时");
        System.out.println("5. 初始化子类时（先初始化父类）");
        System.out.println("6. JVM启动时的主类");
        
        System.out.println("\n演示类初始化过程:");
        
        try {
            // 这里会触发类的初始化
            System.out.println("创建示例类实例...");
            ExampleClass example = new ExampleClass();
            System.out.println("实例创建完成");
            
            System.out.println("\n访问静态方法...");
            ExampleClass.staticMethod();
            
            System.out.println("\n访问静态变量...");
            System.out.println("静态变量值: " + ExampleClass.staticVar);
            
        } catch (Exception e) {
            System.err.println("类初始化演示失败: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 自定义类加载器
     */
    static class CustomClassLoader extends ClassLoader {
        private String name;
        
        public CustomClassLoader() {
            this("CustomClassLoader");
        }
        
        public CustomClassLoader(String name) {
            super();
            this.name = name;
        }
        
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            System.out.println("[" + this.name + "] 尝试加载类: " + name);
            // 这里简化实现，实际应该从特定位置读取类文件
            throw new ClassNotFoundException("无法加载类: " + name);
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    /**
     * 文件类加载器
     */
    static class FileClassLoader extends ClassLoader {
        private String classPath;
        
        public FileClassLoader(String classPath) {
            this.classPath = classPath;
        }
        
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                // 将类名转换为文件路径
                String fileName = classPath + "/" + name.replace('.', '/') + ".class";
                File file = new File(fileName);
                
                if (!file.exists()) {
                    throw new ClassNotFoundException("类文件不存在: " + fileName);
                }
                
                // 读取类文件
                byte[] classData = loadClassFile(file);
                
                // 定义类
                return defineClass(name, classData, 0, classData.length);
                
            } catch (IOException e) {
                throw new ClassNotFoundException("无法加载类文件", e);
            }
        }
        
        private byte[] loadClassFile(File file) throws IOException {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                return data;
            }
        }
    }
    
    /**
     * 网络类加载器
     */
    static class NetworkClassLoader extends ClassLoader {
        private String baseUrl;
        
        public NetworkClassLoader(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                // 构造类文件URL
                String classUrl = baseUrl + name.replace('.', '/') + ".class";
                
                // 这里简化实现，实际应该从网络下载类文件
                System.out.println("从网络加载类: " + classUrl);
                
                throw new ClassNotFoundException("网络类加载功能未完整实现");
                
            } catch (Exception e) {
                throw new ClassNotFoundException("网络加载类失败: " + name, e);
            }
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
    }
    
    /**
     * 示例类 - 用于演示类初始化
     */
    static class ExampleClass {
        // 静态变量
        public static int staticVar;
        
        // 静态代码块
        static {
            System.out.println("  ExampleClass 静态代码块执行");
            staticVar = 42;
        }
        
        // 实例代码块
        {
            System.out.println("  ExampleClass 实例代码块执行");
        }
        
        // 构造方法
        public ExampleClass() {
            System.out.println("  ExampleClass 构造方法执行");
        }
        
        // 静态方法
        public static void staticMethod() {
            System.out.println("  ExampleClass 静态方法执行");
        }
    }
}