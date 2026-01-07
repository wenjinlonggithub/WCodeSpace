package com.architecture.designpattern.singleton;

import org.springframework.stereotype.Component;

@Component
public class SingletonSourceCodeAnalysis {

    /**
     * ====================
     * 单例模式源码分析
     * ====================
     */

    /**
     * 1. 饿汉式单例（Eager Singleton）
     * 
     * 特点：
     * - 类加载时就创建实例
     * - 线程安全，无需额外同步
     * - 简单易理解
     * 
     * 优点：
     * - 实现简单
     * - 天然线程安全
     * - 没有性能损耗
     * 
     * 缺点：
     * - 可能造成资源浪费（如果实例从未被使用）
     * - 不支持延迟加载
     * 
     * 源码实现：
     * ```java
     * public class EagerSingleton {
     *     // 在类加载时创建实例
     *     private static final EagerSingleton INSTANCE = new EagerSingleton();
     *     
     *     private EagerSingleton() {} // 私有构造器
     *     
     *     public static EagerSingleton getInstance() {
     *         return INSTANCE;
     *     }
     * }
     * ```
     */
    public void analyzeEagerSingleton() {
        System.out.println("饿汉式：类加载时创建，线程安全，无延迟加载");
    }

    /**
     * 2. 懒汉式单例（Lazy Singleton）
     * 
     * 特点：
     * - 需要时才创建实例
     * - 需要处理线程安全问题
     * - 支持延迟加载
     * 
     * 2.1 简单懒汉式（线程不安全）：
     * ```java
     * public class SimpleLazySingleton {
     *     private static SimpleLazySingleton instance;
     *     
     *     private SimpleLazySingleton() {}
     *     
     *     public static SimpleLazySingleton getInstance() {
     *         if (instance == null) {
     *             instance = new SimpleLazySingleton(); // 线程不安全
     *         }
     *         return instance;
     *     }
     * }
     * ```
     * 
     * 2.2 同步方法（线程安全但性能差）：
     * ```java
     * public static synchronized SimpleLazySingleton getInstance() {
     *     if (instance == null) {
     *         instance = new SimpleLazySingleton();
     *     }
     *     return instance;
     * }
     * ```
     * 
     * 2.3 双重检查锁定（推荐）：
     * ```java
     * public class LazySingleton {
     *     private static volatile LazySingleton instance; // volatile很重要
     *     
     *     private LazySingleton() {}
     *     
     *     public static LazySingleton getInstance() {
     *         if (instance == null) { // 第一次检查
     *             synchronized (LazySingleton.class) {
     *                 if (instance == null) { // 第二次检查
     *                     instance = new LazySingleton();
     *                 }
     *             }
     *         }
     *         return instance;
     *     }
     * }
     * ```
     * 
     * volatile关键字作用：
     * - 防止指令重排序
     * - 确保内存可见性
     * - 避免获取未完全初始化的实例
     */
    public void analyzeLazySingleton() {
        System.out.println("懒汉式：延迟加载，双重检查锁定，volatile防止指令重排");
    }

    /**
     * 3. 静态内部类单例（推荐）
     * 
     * 特点：
     * - 利用类加载机制保证线程安全
     * - 支持延迟加载
     * - 没有同步开销
     * 
     * 原理：
     * - 外部类加载时，内部类不会立即加载
     * - 只有调用getInstance()时才加载内部类
     * - 类加载过程由JVM保证线程安全
     * 
     * 源码实现：
     * ```java
     * public class InnerClassSingleton {
     *     private InnerClassSingleton() {}
     *     
     *     // 静态内部类，只有调用getInstance时才加载
     *     private static class SingletonHolder {
     *         private static final InnerClassSingleton INSTANCE = new InnerClassSingleton();
     *     }
     *     
     *     public static InnerClassSingleton getInstance() {
     *         return SingletonHolder.INSTANCE;
     *     }
     * }
     * ```
     * 
     * 优点：
     * - 延迟加载
     * - 线程安全
     * - 高性能（无同步开销）
     * - 实现简单
     */
    public void analyzeInnerClassSingleton() {
        System.out.println("静态内部类：利用类加载机制，延迟加载，线程安全，高性能");
    }

    /**
     * 4. 枚举单例（最安全）
     * 
     * 特点：
     * - JVM保证枚举类型的线程安全
     * - 天然防止反序列化攻击
     * - 防止反射攻击
     * - 实现简洁
     * 
     * 源码实现：
     * ```java
     * public enum EnumSingleton {
     *     INSTANCE;
     *     
     *     public void doSomething() {
     *         // 业务方法
     *     }
     * }
     * ```
     * 
     * JVM层面保证：
     * - 枚举实例的创建是线程安全的
     * - 枚举实例在序列化和反序列化时保持单例
     * - 无法通过反射创建枚举实例
     * 
     * 《Effective Java》作者Joshua Bloch强烈推荐此方式
     */
    public void analyzeEnumSingleton() {
        System.out.println("枚举单例：JVM保证线程安全，防序列化和反射攻击");
    }

    /**
     * 5. Spring中的单例模式
     * 
     * Spring IoC容器中的Bean默认是单例的：
     * 
     * ```java
     * @Component
     * public class SpringSingleton {
     *     // Spring管理的单例Bean
     * }
     * 
     * @Bean
     * @Scope("singleton") // 默认就是singleton
     * public SomeService someService() {
     *     return new SomeService();
     * }
     * ```
     * 
     * Spring单例特点：
     * - 容器级别的单例（不是JVM级别）
     * - 线程安全由容器保证
     * - 支持依赖注入
     * - 生命周期由容器管理
     */
    public void analyzeSpringtonon() {
        System.out.println("Spring单例：容器级别，依赖注入，生命周期管理");
    }

    /**
     * 6. 单例模式的破坏与防护
     * 
     * 6.1 反射攻击：
     * ```java
     * Constructor<Singleton> constructor = Singleton.class.getDeclaredConstructor();
     * constructor.setAccessible(true);
     * Singleton instance = constructor.newInstance(); // 创建新实例
     * ```
     * 
     * 防护措施：
     * ```java
     * private Singleton() {
     *     if (INSTANCE != null) {
     *         throw new RuntimeException("单例模式禁止反射调用");
     *     }
     * }
     * ```
     * 
     * 6.2 序列化攻击：
     * ```java
     * // 序列化后再反序列化会创建新实例
     * ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("singleton.obj"));
     * oos.writeObject(singleton);
     * 
     * ObjectInputStream ois = new ObjectInputStream(new FileInputStream("singleton.obj"));
     * Singleton deserializedSingleton = (Singleton) ois.readObject();
     * ```
     * 
     * 防护措施：
     * ```java
     * public class Singleton implements Serializable {
     *     // ... 其他代码
     *     
     *     // 反序列化时调用此方法
     *     private Object readResolve() {
     *         return INSTANCE;
     *     }
     * }
     * ```
     * 
     * 6.3 克隆攻击：
     * 防护措施：
     * ```java
     * @Override
     * protected Object clone() throws CloneNotSupportedException {
     *     throw new CloneNotSupportedException("单例模式禁止克隆");
     * }
     * ```
     */
    public void analyzeSingletonSecurity() {
        System.out.println("单例安全：防反射、防序列化、防克隆攻击");
    }

    /**
     * 7. 性能对比分析
     * 
     * 各种实现方式的性能对比：
     * 
     * 1. 饿汉式：★★★★★
     *    - 最高性能，无同步开销
     *    - 类加载时创建，之后直接返回
     * 
     * 2. 懒汉式（同步方法）：★★☆☆☆
     *    - 每次调用都需要同步，性能较差
     *    - 不推荐使用
     * 
     * 3. 懒汉式（双重检查）：★★★★☆
     *    - 只有首次创建时才同步
     *    - 之后调用无同步开销
     * 
     * 4. 静态内部类：★★★★★
     *    - 与饿汉式性能相当
     *    - 支持延迟加载
     * 
     * 5. 枚举单例：★★★★☆
     *    - 性能良好，略逊于饿汉式
     *    - 安全性最高
     * 
     * 性能测试代码示例：
     * ```java
     * long start = System.nanoTime();
     * for (int i = 0; i < 1000000; i++) {
     *     Singleton.getInstance();
     * }
     * long end = System.nanoTime();
     * System.out.println("耗时: " + (end - start) / 1000000 + "ms");
     * ```
     */
    public void performanceAnalysis() {
        System.out.println("性能分析：饿汉式 ≈ 静态内部类 > 双重检查 > 枚举 > 同步方法");
    }

    /**
     * 8. 实际应用场景
     * 
     * 8.1 数据库连接池
     * ```java
     * public class ConnectionPool {
     *     private static volatile ConnectionPool instance;
     *     private final List<Connection> connections;
     *     
     *     private ConnectionPool() {
     *         connections = new ArrayList<>();
     *         // 初始化连接池
     *     }
     *     
     *     public static ConnectionPool getInstance() {
     *         if (instance == null) {
     *             synchronized (ConnectionPool.class) {
     *                 if (instance == null) {
     *                     instance = new ConnectionPool();
     *                 }
     *             }
     *         }
     *         return instance;
     *     }
     * }
     * ```
     * 
     * 8.2 配置管理器
     * ```java
     * public class ConfigManager {
     *     private static final ConfigManager INSTANCE = new ConfigManager();
     *     private final Properties config;
     *     
     *     private ConfigManager() {
     *         config = new Properties();
     *         loadConfig();
     *     }
     *     
     *     public static ConfigManager getInstance() {
     *         return INSTANCE;
     *     }
     * }
     * ```
     * 
     * 8.3 日志记录器
     * ```java
     * public class Logger {
     *     private static class LoggerHolder {
     *         private static final Logger INSTANCE = new Logger();
     *     }
     *     
     *     public static Logger getInstance() {
     *         return LoggerHolder.INSTANCE;
     *     }
     * }
     * ```
     */
    public void realWorldApplications() {
        System.out.println("应用场景：连接池、配置管理、日志记录器、缓存管理器");
    }
}