package com.architecture.designpattern.flyweight;

import org.springframework.stereotype.Component;

@Component
public class FlyweightInterviewQuestions {

    /**
     * ====================
     * 享元模式面试问题汇总
     * ====================
     */

    /**
     * Q1: 什么是享元模式？它解决了什么问题？
     * 
     * A: 享元模式定义：
     * 运用共享技术有效地支持大量细粒度对象的复用。
     * 
     * 解决的问题：
     * 1. 内存消耗过大
     *    - 大量相似对象占用过多内存
     *    - 系统性能下降
     * 
     * 2. 对象创建开销
     *    - 频繁创建销毁对象的性能损耗
     *    - 垃圾回收压力
     * 
     * 核心思想：
     * - 将对象状态分为内部状态和外部状态
     * - 内部状态：可共享，不随环境变化
     * - 外部状态：不可共享，需要外部传入
     * 
     * 适用场景：
     * - 文本编辑器中的字符对象
     * - 游戏中的子弹、粒子效果
     * - Web应用中的图标、按钮
     */
    public void whatIsFlyweight() {
        System.out.println("享元模式：通过共享减少对象数量，节约内存");
    }

    /**
     * Q2: 享元模式的核心组件有哪些？
     * 
     * A: 核心组件：
     * 
     * 1. 抽象享元（Flyweight）
     *    - 定义享元对象的接口
     *    - 声明接受外部状态的方法
     * 
     * 2. 具体享元（ConcreteFlyweight）
     *    - 实现抽象享元接口
     *    - 存储内部状态
     *    - 必须是可共享的
     * 
     * 3. 非共享享元（UnsharedConcreteFlyweight）
     *    - 不需要共享的享元子类
     *    - 可以有外部状态
     * 
     * 4. 享元工厂（FlyweightFactory）
     *    - 管理享元对象
     *    - 确保享元对象被适当共享
     * 
     * 5. 客户端（Client）
     *    - 维护外部状态
     *    - 使用享元对象
     */
    public void coreComponents() {
        System.out.println("核心组件：抽象享元、具体享元、享元工厂、客户端");
    }

    /**
     * Q3: 内部状态和外部状态如何区分？
     * 
     * A: 状态分类标准：
     * 
     * 内部状态（Intrinsic State）:
     * 1. 特征：
     *    - 存储在享元对象内部
     *    - 不随环境变化
     *    - 可以被多个对象共享
     *    - 与具体的实例无关
     * 
     * 2. 示例：
     *    - 字符的字体、大小
     *    - 图形的形状、颜色
     *    - 棋子的类型
     * 
     * 外部状态（Extrinsic State）:
     * 1. 特征：
     *    - 由客户端保存
     *    - 随环境变化
     *    - 不能被共享
     *    - 传递给享元对象使用
     * 
     * 2. 示例：
     *    - 字符在文档中的位置
     *    - 图形的坐标
     *    - 棋子在棋盘上的位置
     * 
     * 设计原则：
     * - 尽可能减少外部状态
     * - 将不变的部分作为内部状态
     * - 将变化的部分作为外部状态
     */
    public void intrinsicVsExtrinsicState() {
        System.out.println("状态分离：内部状态可共享不变，外部状态客户端维护");
    }

    /**
     * Q4: 享元模式的优缺点是什么？
     * 
     * A: 优点：
     * 
     * 1. 大幅减少内存使用
     *    - 通过共享减少对象数量
     *    - 降低内存占用
     * 
     * 2. 提高系统性能
     *    - 减少对象创建时间
     *    - 降低垃圾回收频率
     * 
     * 3. 降低系统复杂度
     *    - 集中管理相似对象
     *    - 统一对象创建
     * 
     * 缺点：
     * 
     * 1. 增加程序复杂性
     *    - 需要分离内外部状态
     *    - 理解和维护成本高
     * 
     * 2. 外部状态管理
     *    - 客户端需要维护外部状态
     *    - 可能增加运行时开销
     * 
     * 3. 线程安全问题
     *    - 共享对象的线程安全
     *    - 需要额外同步机制
     * 
     * 4. 系统逻辑复杂化
     *    - 状态分离增加设计复杂度
     *    - 调试困难
     */
    public void advantagesAndDisadvantages() {
        System.out.println("优点：减少内存、提升性能；缺点：增加复杂度、状态管理困难");
    }

    /**
     * Q5: 享元模式适用于哪些场景？
     * 
     * A: 适用条件：
     * 
     * 1. 对象数量庞大
     *    - 系统中存在大量相似对象
     *    - 对象创建成本高
     * 
     * 2. 对象状态可分离
     *    - 对象状态可以分为内部和外部
     *    - 内部状态相对较少
     * 
     * 3. 外部状态可计算
     *    - 外部状态可以从其他数据计算得出
     *    - 或者可以被客户端维护
     * 
     * 具体场景：
     * 
     * 1. 文本编辑器
     *    - 字符对象的字体、大小为内部状态
     *    - 字符位置、选中状态为外部状态
     * 
     * 2. 游戏开发
     *    - 子弹类型为内部状态
     *    - 子弹位置、速度为外部状态
     * 
     * 3. Web应用
     *    - 图标样式为内部状态
     *    - 图标位置、事件为外部状态
     * 
     * 4. 缓存系统
     *    - 缓存键的模式为内部状态
     *    - 具体键值为外部状态
     */
    public void applicableScenarios() {
        System.out.println("适用场景：对象数量大、状态可分离、外部状态可计算");
    }

    /**
     * Q6: 如何实现线程安全的享元模式？
     * 
     * A: 线程安全策略：
     * 
     * 1. 不可变享元对象
     * ```java
     * public final class ImmutableFlyweight {
     *     private final String intrinsicState;
     *     
     *     public ImmutableFlyweight(String intrinsicState) {
     *         this.intrinsicState = intrinsicState;
     *     }
     *     
     *     public void operation(String extrinsicState) {
     *         // 只读操作，天然线程安全
     *     }
     * }
     * ```
     * 
     * 2. 线程安全的工厂
     * ```java
     * public class ThreadSafeFlyweightFactory {
     *     private final ConcurrentHashMap<String, Flyweight> flyweights = new ConcurrentHashMap<>();
     *     
     *     public Flyweight getFlyweight(String key) {
     *         return flyweights.computeIfAbsent(key, k -> new ConcreteFlyweight(k));
     *     }
     * }
     * ```
     * 
     * 3. 同步访问控制
     * ```java
     * public class SynchronizedFlyweight {
     *     private String intrinsicState;
     *     
     *     public synchronized void operation(String extrinsicState) {
     *         // 同步方法保证线程安全
     *     }
     * }
     * ```
     * 
     * 4. 使用ThreadLocal
     * ```java
     * public class ThreadLocalFlyweight {
     *     private static final ThreadLocal<Map<String, Flyweight>> localCache = 
     *         ThreadLocal.withInitial(HashMap::new);
     *     
     *     public static Flyweight getFlyweight(String key) {
     *         return localCache.get().computeIfAbsent(key, k -> new ConcreteFlyweight(k));
     *     }
     * }
     * ```
     */
    public void threadSafety() {
        System.out.println("线程安全：不可变对象、线程安全工厂、同步控制、ThreadLocal");
    }

    /**
     * Q7: 享元模式与单例模式有什么区别？
     * 
     * A: 主要区别：
     * 
     * 1. 实例数量：
     *    - 单例：一个类只有一个实例
     *    - 享元：一个类可以有多个实例，但实例数量受限
     * 
     * 2. 共享方式：
     *    - 单例：整个应用共享同一个实例
     *    - 享元：相同内部状态的对象共享实例
     * 
     * 3. 状态管理：
     *    - 单例：可以有状态，状态属于实例
     *    - 享元：内部状态不变，外部状态由客户端管理
     * 
     * 4. 使用目的：
     *    - 单例：控制实例数量，全局访问
     *    - 享元：减少内存使用，提高性能
     * 
     * 5. 创建方式：
     *    - 单例：私有构造器，静态方法获取
     *    - 享元：工厂方法管理，按需创建
     * 
     * 联系：
     * - 都涉及对象的共享
     * - 都可以减少对象创建
     * - 工厂中的享元对象可以是单例的
     */
    public void vsSingletonPattern() {
        System.out.println("区别：实例数量、共享方式、状态管理、使用目的");
    }

    /**
     * Q8: 享元模式在Java中的应用有哪些？
     * 
     * A: Java中的应用：
     * 
     * 1. String常量池
     * ```java
     * String s1 = "Hello";  // 存储在常量池
     * String s2 = "Hello";  // 复用常量池中的对象
     * System.out.println(s1 == s2); // true
     * ```
     * 
     * 2. Integer缓存（-128到127）
     * ```java
     * Integer i1 = 100;    // 使用缓存
     * Integer i2 = 100;    // 复用缓存对象
     * System.out.println(i1 == i2); // true
     * 
     * Integer i3 = 200;    // 超出缓存范围
     * Integer i4 = 200;    // 创建新对象
     * System.out.println(i3 == i4); // false
     * ```
     * 
     * 3. Boolean.TRUE和Boolean.FALSE
     * ```java
     * Boolean b1 = Boolean.valueOf(true);
     * Boolean b2 = Boolean.valueOf(true);
     * System.out.println(b1 == b2); // true
     * ```
     * 
     * 4. 枚举常量
     * ```java
     * public enum Color { RED, GREEN, BLUE }
     * Color c1 = Color.RED;
     * Color c2 = Color.RED;
     * System.out.println(c1 == c2); // true
     * ```
     * 
     * 5. Pattern编译缓存
     * ```java
     * Pattern pattern1 = Pattern.compile("\\d+");
     * Pattern pattern2 = Pattern.compile("\\d+");
     * // 内部使用缓存机制减少重复编译
     * ```
     */
    public void javaApplications() {
        System.out.println("Java应用：String常量池、Integer缓存、Boolean常量、枚举");
    }

    /**
     * Q9: 如何设计一个高效的享元工厂？
     * 
     * A: 设计要点：
     * 
     * 1. 选择合适的存储结构
     * ```java
     * // 简单键值：HashMap
     * Map<String, Flyweight> simpleCache = new HashMap<>();
     * 
     * // 复合键：自定义Key类
     * public class FlyweightKey {
     *     private final String type;
     *     private final String style;
     *     
     *     // 重写equals和hashCode
     * }
     * 
     * // 大量数据：ConcurrentHashMap
     * ConcurrentHashMap<FlyweightKey, Flyweight> cache = new ConcurrentHashMap<>();
     * ```
     * 
     * 2. 内存管理策略
     * ```java
     * public class ManagedFlyweightFactory {
     *     private final Map<String, Flyweight> cache = new LinkedHashMap<String, Flyweight>(16, 0.75f, true) {
     *         @Override
     *         protected boolean removeEldestEntry(Map.Entry<String, Flyweight> eldest) {
     *             return size() > MAX_CACHE_SIZE; // LRU淘汰
     *         }
     *     };
     *     
     *     private static final int MAX_CACHE_SIZE = 1000;
     * }
     * ```
     * 
     * 3. 弱引用缓存
     * ```java
     * private final Map<String, WeakReference<Flyweight>> cache = 
     *     new ConcurrentHashMap<>();
     *     
     * public Flyweight getFlyweight(String key) {
     *     WeakReference<Flyweight> ref = cache.get(key);
     *     Flyweight flyweight = ref != null ? ref.get() : null;
     *     
     *     if (flyweight == null) {
     *         flyweight = new ConcreteFlyweight(key);
     *         cache.put(key, new WeakReference<>(flyweight));
     *     }
     *     
     *     return flyweight;
     * }
     * ```
     * 
     * 4. 预加载策略
     * ```java
     * @PostConstruct
     * public void preloadCommonFlyweights() {
     *     String[] commonKeys = {"bold", "italic", "underline"};
     *     for (String key : commonKeys) {
     *         cache.put(key, new ConcreteFlyweight(key));
     *     }
     * }
     * ```
     */
    public void efficientFactory() {
        System.out.println("高效工厂：合适存储结构、内存管理、弱引用、预加载");
    }

    /**
     * Q10: 如何测试享元模式的有效性？
     * 
     * A: 测试策略：
     * 
     * 1. 对象共享测试
     * ```java
     * @Test
     * public void testObjectSharing() {
     *     FlyweightFactory factory = new FlyweightFactory();
     *     
     *     Flyweight f1 = factory.getFlyweight("type1");
     *     Flyweight f2 = factory.getFlyweight("type1");
     *     
     *     assertSame("相同key应返回同一对象", f1, f2);
     * }
     * ```
     * 
     * 2. 内存使用测试
     * ```java
     * @Test
     * public void testMemoryUsage() {
     *     Runtime runtime = Runtime.getRuntime();
     *     
     *     long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
     *     
     *     // 创建大量享元对象
     *     for (int i = 0; i < 10000; i++) {
     *         factory.getFlyweight("type" + (i % 10)); // 只有10种类型
     *     }
     *     
     *     long afterMemory = runtime.totalMemory() - runtime.freeMemory();
     *     long memoryUsed = afterMemory - beforeMemory;
     *     
     *     assertTrue("内存使用应该很少", memoryUsed < EXPECTED_MAX_MEMORY);
     * }
     * ```
     * 
     * 3. 性能基准测试
     * ```java
     * @Test
     * public void benchmarkTest() {
     *     long startTime = System.nanoTime();
     *     
     *     for (int i = 0; i < 100000; i++) {
     *         Flyweight f = factory.getFlyweight("test");
     *         f.operation("external" + i);
     *     }
     *     
     *     long endTime = System.nanoTime();
     *     long duration = endTime - startTime;
     *     
     *     assertTrue("性能应该满足要求", duration < MAX_DURATION);
     * }
     * ```
     * 
     * 4. 并发安全测试
     * ```java
     * @Test
     * public void testConcurrency() throws InterruptedException {
     *     Set<Flyweight> results = ConcurrentHashMap.newKeySet();
     *     CountDownLatch latch = new CountDownLatch(100);
     *     
     *     for (int i = 0; i < 100; i++) {
     *         new Thread(() -> {
     *             results.add(factory.getFlyweight("concurrent"));
     *             latch.countDown();
     *         }).start();
     *     }
     *     
     *     latch.await();
     *     assertEquals("所有线程应获得同一对象", 1, results.size());
     * }
     * ```
     */
    public void testingStrategies() {
        System.out.println("测试策略：对象共享、内存使用、性能基准、并发安全");
    }
}