package com.architecture.designpattern.singleton;

import org.springframework.stereotype.Component;

@Component
public class SingletonInterviewQuestions {

    /**
     * ====================
     * 单例模式面试问题汇总
     * ====================
     */

    /**
     * Q1: 什么是单例模式？为什么要使用单例模式？
     * 
     * A: 单例模式定义：
     * 确保一个类只有一个实例，并提供全局访问点。
     * 
     * 使用原因：
     * 1. 资源控制：避免频繁创建/销毁对象，节约系统资源
     * 2. 全局访问：提供全局唯一的访问点
     * 3. 状态管理：保持全局状态的一致性
     * 4. 功能协调：避免多个实例间的冲突
     * 
     * 适用场景：
     * - 线程池、数据库连接池
     * - 配置文件管理器
     * - 日志管理器
     * - 缓存管理器
     * - 应用程序设置
     */
    public void whatIsSingleton() {
        System.out.println("单例模式：一个类只有一个实例 + 全局访问点");
    }

    /**
     * Q2: 单例模式有哪几种实现方式？各有什么优缺点？
     * 
     * A: 主要实现方式：
     * 
     * 1. 饿汉式
     *    优点：实现简单，线程安全，性能高
     *    缺点：可能造成内存浪费，不支持延迟加载
     * 
     * 2. 懒汉式（简单版）
     *    优点：延迟加载，节约内存
     *    缺点：线程不安全
     * 
     * 3. 懒汉式（同步方法）
     *    优点：线程安全，延迟加载
     *    缺点：性能差，每次调用都同步
     * 
     * 4. 懒汉式（双重检查锁）
     *    优点：线程安全，延迟加载，性能好
     *    缺点：实现复杂，需要volatile
     * 
     * 5. 静态内部类
     *    优点：线程安全，延迟加载，性能高，实现简洁
     *    缺点：无明显缺点
     * 
     * 6. 枚举
     *    优点：线程安全，防止序列化和反射攻击
     *    缺点：可读性稍差
     */
    public void singletonImplementations() {
        System.out.println("实现方式：饿汉式、懒汉式、静态内部类、枚举");
    }

    /**
     * Q3: 为什么双重检查锁定需要volatile关键字？
     * 
     * A: volatile的作用：
     * 
     * 1. 防止指令重排序
     *    对象创建过程：
     *    - 分配内存空间
     *    - 初始化对象
     *    - 将引用指向内存地址
     *    
     *    没有volatile时，可能发生指令重排序：
     *    - 分配内存空间
     *    - 将引用指向内存地址（未初始化）
     *    - 初始化对象
     * 
     * 2. 保证内存可见性
     *    确保一个线程对变量的修改对其他线程立即可见
     * 
     * 3. 避免半初始化对象
     *    防止其他线程获取到未完全初始化的对象
     * 
     * 代码示例：
     * ```java
     * private static volatile Singleton instance; // volatile必须
     * 
     * public static Singleton getInstance() {
     *     if (instance == null) {
     *         synchronized (Singleton.class) {
     *             if (instance == null) {
     *                 instance = new Singleton(); // 可能指令重排序
     *             }
     *         }
     *     }
     *     return instance;
     * }
     * ```
     */
    public void whyVolatileNeeded() {
        System.out.println("volatile作用：防指令重排序 + 保证内存可见性");
    }

    /**
     * Q4: 如何防止单例模式被反射、序列化、克隆破坏？
     * 
     * A: 防护措施：
     * 
     * 1. 防反射攻击
     * ```java
     * private Singleton() {
     *     if (instance != null) {
     *         throw new RuntimeException("禁止通过反射创建实例");
     *     }
     * }
     * ```
     * 
     * 2. 防序列化攻击
     * ```java
     * private Object readResolve() {
     *     return instance;
     * }
     * ```
     * 
     * 3. 防克隆攻击
     * ```java
     * @Override
     * protected Object clone() throws CloneNotSupportedException {
     *     throw new CloneNotSupportedException("禁止克隆单例对象");
     * }
     * ```
     * 
     * 4. 最安全的方式：枚举单例
     * ```java
     * public enum Singleton {
     *     INSTANCE;
     *     // JVM天然防止反序列化和反射攻击
     * }
     * ```
     */
    public void preventSingletonDestruction() {
        System.out.println("防破坏：构造器检查 + readResolve + 禁止克隆 + 枚举");
    }

    /**
     * Q5: 静态内部类实现单例的原理是什么？
     * 
     * A: 原理分析：
     * 
     * 1. 类加载机制
     *    - 外部类加载时，静态内部类不会被加载
     *    - 只有访问静态内部类时才会触发加载
     *    - JVM保证类加载过程的线程安全
     * 
     * 2. 延迟加载
     *    - 只有调用getInstance()时才会加载SingletonHolder
     *    - 加载SingletonHolder时才会创建INSTANCE
     * 
     * 3. 线程安全
     *    - 利用JVM的类加载机制保证线程安全
     *    - 无需额外的同步措施
     * 
     * 4. 高性能
     *    - 没有同步开销
     *    - 访问速度接近饿汉式
     * 
     * 代码示例：
     * ```java
     * public class Singleton {
     *     private Singleton() {}
     *     
     *     private static class SingletonHolder {
     *         private static final Singleton INSTANCE = new Singleton();
     *     }
     *     
     *     public static Singleton getInstance() {
     *         return SingletonHolder.INSTANCE;
     *     }
     * }
     * ```
     */
    public void innerClassPrinciple() {
        System.out.println("静态内部类：利用类加载机制，延迟加载，天然线程安全");
    }

    /**
     * Q6: 单例模式在Spring框架中是如何实现的？
     * 
     * A: Spring中的单例：
     * 
     * 1. 默认作用域
     *    Spring Bean默认是singleton作用域
     * 
     * 2. 容器级单例
     *    不是JVM级别的单例，而是Spring容器级别的单例
     * 
     * 3. 线程安全
     *    Spring容器创建Bean时保证线程安全
     *    但Bean本身的线程安全需要开发者保证
     * 
     * 4. 实现原理
     * ```java
     * // ConcurrentHashMap存储单例Bean
     * private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
     * 
     * public Object getSingleton(String beanName) {
     *     Object singletonObject = this.singletonObjects.get(beanName);
     *     if (singletonObject == null) {
     *         synchronized (this.singletonObjects) {
     *             singletonObject = this.singletonObjects.get(beanName);
     *             if (singletonObject == null) {
     *                 singletonObject = createBean(beanName);
     *                 this.singletonObjects.put(beanName, singletonObject);
     *             }
     *         }
     *     }
     *     return singletonObject;
     * }
     * ```
     * 
     * 5. 配置方式
     * ```java
     * @Component  // 默认singleton
     * public class MyService {}
     * 
     * @Bean
     * @Scope("singleton")  // 显式指定
     * public SomeService someService() {
     *     return new SomeService();
     * }
     * ```
     */
    public void springSingleton() {
        System.out.println("Spring单例：容器级别 + ConcurrentHashMap + 双重检查锁");
    }

    /**
     * Q7: 单例模式的优缺点是什么？
     * 
     * A: 优点：
     * 
     * 1. 内存优化
     *    - 避免频繁创建对象，节约内存
     *    - 减少垃圾回收压力
     * 
     * 2. 全局访问
     *    - 提供全局唯一访问点
     *    - 便于全局状态管理
     * 
     * 3. 资源控制
     *    - 控制资源的访问
     *    - 避免资源冲突
     * 
     * 缺点：
     * 
     * 1. 违反单一职责原则
     *    - 既负责业务逻辑，又负责实例管理
     * 
     * 2. 难以扩展
     *    - 没有接口，难以继承
     *    - 扩展性差
     * 
     * 3. 隐藏依赖关系
     *    - 代码间的依赖不明确
     *    - 增加代码耦合度
     * 
     * 4. 测试困难
     *    - 单例对象难以Mock
     *    - 测试间可能相互影响
     * 
     * 5. 多线程问题
     *    - 需要考虑线程安全
     *    - 实现复杂度增加
     */
    public void advantagesAndDisadvantages() {
        System.out.println("优点：内存优化、全局访问、资源控制；缺点：扩展困难、测试困难");
    }

    /**
     * Q8: 单例模式和静态类有什么区别？
     * 
     * A: 主要区别：
     * 
     * 1. 内存管理
     *    - 单例：延迟创建，可控制生命周期
     *    - 静态类：类加载时初始化，无法控制
     * 
     * 2. 继承性
     *    - 单例：可以继承类和实现接口
     *    - 静态类：无法继承和实现接口
     * 
     * 3. 多态性
     *    - 单例：支持多态
     *    - 静态类：不支持多态
     * 
     * 4. 接口实现
     *    - 单例：可以实现接口，便于依赖注入
     *    - 静态类：无法实现接口
     * 
     * 5. 状态管理
     *    - 单例：可以维护状态
     *    - 静态类：状态存储在静态字段中
     * 
     * 6. 线程安全
     *    - 单例：需要考虑创建过程的线程安全
     *    - 静态类：静态初始化天然线程安全
     * 
     * 选择建议：
     * - 需要状态管理、继承、多态：选择单例
     * - 纯工具类、无状态：选择静态类
     */
    public void singletonVsStaticClass() {
        System.out.println("区别：继承性、多态性、接口实现、状态管理、生命周期");
    }

    /**
     * Q9: 在多线程环境下如何安全地实现单例模式？
     * 
     * A: 线程安全实现方式：
     * 
     * 1. 饿汉式（推荐）
     * ```java
     * public class Singleton {
     *     private static final Singleton INSTANCE = new Singleton();
     *     private Singleton() {}
     *     public static Singleton getInstance() {
     *         return INSTANCE;
     *     }
     * }
     * ```
     * 
     * 2. 双重检查锁定
     * ```java
     * public class Singleton {
     *     private static volatile Singleton instance;
     *     
     *     public static Singleton getInstance() {
     *         if (instance == null) {
     *             synchronized (Singleton.class) {
     *                 if (instance == null) {
     *                     instance = new Singleton();
     *                 }
     *             }
     *         }
     *         return instance;
     *     }
     * }
     * ```
     * 
     * 3. 静态内部类（推荐）
     * ```java
     * public class Singleton {
     *     private static class SingletonHolder {
     *         private static final Singleton INSTANCE = new Singleton();
     *     }
     *     
     *     public static Singleton getInstance() {
     *         return SingletonHolder.INSTANCE;
     *     }
     * }
     * ```
     * 
     * 4. 枚举（最安全）
     * ```java
     * public enum Singleton {
     *     INSTANCE;
     * }
     * ```
     * 
     * 关键点：
     * - volatile防止指令重排序
     * - synchronized确保同步
     * - 双重检查减少同步开销
     * - 类加载机制保证线程安全
     */
    public void threadSafeSingleton() {
        System.out.println("线程安全：饿汉式、双重检查锁、静态内部类、枚举");
    }

    /**
     * Q10: 如何测试单例模式？
     * 
     * A: 测试策略：
     * 
     * 1. 单例性测试
     * ```java
     * @Test
     * public void testSingleton() {
     *     Singleton instance1 = Singleton.getInstance();
     *     Singleton instance2 = Singleton.getInstance();
     *     assertSame(instance1, instance2);
     * }
     * ```
     * 
     * 2. 线程安全测试
     * ```java
     * @Test
     * public void testThreadSafety() throws InterruptedException {
     *     Set<Singleton> instances = ConcurrentHashMap.newKeySet();
     *     CountDownLatch latch = new CountDownLatch(100);
     *     
     *     for (int i = 0; i < 100; i++) {
     *         new Thread(() -> {
     *             instances.add(Singleton.getInstance());
     *             latch.countDown();
     *         }).start();
     *     }
     *     
     *     latch.await();
     *     assertEquals(1, instances.size());
     * }
     * ```
     * 
     * 3. 反射测试
     * ```java
     * @Test
     * public void testReflection() {
     *     assertThrows(Exception.class, () -> {
     *         Constructor<Singleton> constructor = Singleton.class.getDeclaredConstructor();
     *         constructor.setAccessible(true);
     *         constructor.newInstance();
     *     });
     * }
     * ```
     * 
     * 4. 序列化测试
     * ```java
     * @Test
     * public void testSerialization() throws Exception {
     *     Singleton instance1 = Singleton.getInstance();
     *     
     *     // 序列化
     *     ByteArrayOutputStream bos = new ByteArrayOutputStream();
     *     ObjectOutputStream oos = new ObjectOutputStream(bos);
     *     oos.writeObject(instance1);
     *     
     *     // 反序列化
     *     ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
     *     ObjectInputStream ois = new ObjectInputStream(bis);
     *     Singleton instance2 = (Singleton) ois.readObject();
     *     
     *     assertSame(instance1, instance2);
     * }
     * ```
     */
    public void testSingleton() {
        System.out.println("测试策略：单例性、线程安全、反射防护、序列化防护");
    }
}