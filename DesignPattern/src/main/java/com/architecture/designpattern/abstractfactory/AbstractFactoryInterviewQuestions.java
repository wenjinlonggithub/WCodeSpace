package com.architecture.designpattern.abstractfactory;

import org.springframework.stereotype.Component;

@Component
public class AbstractFactoryInterviewQuestions {

    /**
     * ====================
     * 抽象工厂模式面试问题汇总
     * ====================
     */

    /**
     * Q1: 什么是抽象工厂模式？它与工厂方法模式有什么区别？
     * 
     * A: 抽象工厂模式定义：
     * 提供一个创建一系列相关或相互依赖对象的接口，而无需指定它们具体的类。
     * 
     * 与工厂方法模式的区别：
     * 1. 复杂度：
     *    - 工厂方法：创建一种产品
     *    - 抽象工厂：创建一系列相关产品
     * 
     * 2. 结构：
     *    - 工厂方法：一个工厂创建一个产品
     *    - 抽象工厂：一个工厂创建多个相关产品
     * 
     * 3. 使用场景：
     *    - 工厂方法：产品种类较少，扩展简单
     *    - 抽象工厂：产品族较多，需要保证产品一致性
     * 
     * 4. 扩展方式：
     *    - 工厂方法：容易添加新产品
     *    - 抽象工厂：容易添加新产品族，但添加新产品困难
     */
    public void whatIsAbstractFactory() {
        System.out.println("抽象工厂：创建相关产品族，保证产品一致性");
    }

    /**
     * Q2: 抽象工厂模式的核心组件有哪些？
     * 
     * A: 核心组件：
     * 
     * 1. 抽象工厂（AbstractFactory）
     *    - 声明创建抽象产品的方法
     *    - 定义产品族的创建接口
     * 
     * 2. 具体工厂（ConcreteFactory）
     *    - 实现抽象工厂接口
     *    - 创建具体的产品族
     * 
     * 3. 抽象产品（AbstractProduct）
     *    - 声明产品的公共接口
     *    - 定义产品的抽象特征
     * 
     * 4. 具体产品（ConcreteProduct）
     *    - 实现抽象产品接口
     *    - 定义具体的产品特性
     * 
     * 5. 客户端（Client）
     *    - 使用抽象工厂和抽象产品
     *    - 不依赖具体实现
     * 
     * 代码示例：
     * ```java
     * // 抽象工厂
     * interface UIFactory {
     *     Button createButton();
     *     TextField createTextField();
     * }
     * 
     * // 具体工厂
     * class WindowsUIFactory implements UIFactory {
     *     public Button createButton() { return new WindowsButton(); }
     *     public TextField createTextField() { return new WindowsTextField(); }
     * }
     * ```
     */
    public void coreComponents() {
        System.out.println("核心组件：抽象工厂、具体工厂、抽象产品、具体产品、客户端");
    }

    /**
     * Q3: 抽象工厂模式的优缺点是什么？
     * 
     * A: 优点：
     * 
     * 1. 产品族一致性
     *    - 保证同一族产品的一致性
     *    - 避免混用不同族的产品
     * 
     * 2. 解耦客户端代码
     *    - 客户端不依赖具体实现
     *    - 提高代码的可移植性
     * 
     * 3. 支持产品族扩展
     *    - 容易添加新的产品族
     *    - 符合开闭原则
     * 
     * 4. 隐藏创建细节
     *    - 封装对象创建过程
     *    - 简化客户端调用
     * 
     * 缺点：
     * 
     * 1. 扩展产品困难
     *    - 在产品族中添加新产品需要修改抽象工厂
     *    - 违反开闭原则
     * 
     * 2. 增加系统复杂性
     *    - 类的层次结构复杂
     *    - 理解和维护成本高
     * 
     * 3. 抽象程度高
     *    - 对开发者要求较高
     *    - 需要良好的抽象思维
     */
    public void advantagesAndDisadvantages() {
        System.out.println("优点：产品族一致性、解耦、易扩展族；缺点：难扩展产品、复杂度高");
    }

    /**
     * Q4: 抽象工厂模式适用于哪些场景？
     * 
     * A: 适用场景：
     * 
     * 1. 跨平台应用开发
     *    - UI组件的跨平台适配
     *    - 不同操作系统的界面风格
     *    - 示例：Windows风格 vs Mac风格 vs Android风格
     * 
     * 2. 主题切换系统
     *    - 网站主题切换
     *    - 应用皮肤更换
     *    - 示例：浅色主题 vs 深色主题
     * 
     * 3. 数据库访问层
     *    - 不同数据库的DAO实现
     *    - 数据库驱动的切换
     *    - 示例：MySQL vs PostgreSQL vs Oracle
     * 
     * 4. 支付系统集成
     *    - 不同支付渠道的集成
     *    - 支付方式的统一管理
     *    - 示例：支付宝 vs 微信 vs 银联
     * 
     * 5. 消息队列抽象
     *    - 不同MQ产品的适配
     *    - 消息系统的统一接口
     *    - 示例：RabbitMQ vs Kafka vs ActiveMQ
     * 
     * 判断标准：
     * - 需要创建一系列相关的对象
     * - 这些对象需要一起使用
     * - 需要在不同的产品族之间切换
     * - 要保证产品的一致性
     */
    public void applicableScenarios() {
        System.out.println("适用场景：跨平台开发、主题切换、数据库抽象、支付集成、消息队列");
    }

    /**
     * Q5: 如何实现一个简单的抽象工厂模式？
     * 
     * A: 实现步骤：
     * 
     * 1. 定义抽象产品接口
     * ```java
     * interface Button {
     *     void click();
     * }
     * 
     * interface TextField {
     *     void type(String text);
     * }
     * ```
     * 
     * 2. 实现具体产品
     * ```java
     * class WindowsButton implements Button {
     *     public void click() { System.out.println("Windows button clicked"); }
     * }
     * 
     * class MacButton implements Button {
     *     public void click() { System.out.println("Mac button clicked"); }
     * }
     * ```
     * 
     * 3. 定义抽象工厂
     * ```java
     * interface UIFactory {
     *     Button createButton();
     *     TextField createTextField();
     * }
     * ```
     * 
     * 4. 实现具体工厂
     * ```java
     * class WindowsUIFactory implements UIFactory {
     *     public Button createButton() { return new WindowsButton(); }
     *     public TextField createTextField() { return new WindowsTextField(); }
     * }
     * ```
     * 
     * 5. 客户端使用
     * ```java
     * UIFactory factory = new WindowsUIFactory();
     * Button button = factory.createButton();
     * TextField textField = factory.createTextField();
     * ```
     */
    public void howToImplement() {
        System.out.println("实现步骤：定义抽象产品 → 实现具体产品 → 定义抽象工厂 → 实现具体工厂 → 客户端使用");
    }

    /**
     * Q6: 抽象工厂模式如何保证产品族的一致性？
     * 
     * A: 一致性保证机制：
     * 
     * 1. 工厂责任单一
     *    - 每个具体工厂只负责创建一个产品族
     *    - 避免不同族产品的混合
     * 
     * 2. 抽象约束
     *    - 抽象工厂定义创建接口
     *    - 强制具体工厂创建配套产品
     * 
     * 3. 客户端解耦
     *    - 客户端只依赖抽象接口
     *    - 无法直接混合不同族的产品
     * 
     * 4. 编译时检查
     *    - 类型安全保证
     *    - 编译期发现不一致问题
     * 
     * 示例说明：
     * ```java
     * // 保证一致性：同一个工厂创建的产品属于同一族
     * UIFactory windowsFactory = new WindowsUIFactory();
     * Button windowsButton = windowsFactory.createButton();      // Windows风格
     * TextField windowsTextField = windowsFactory.createTextField(); // Windows风格
     * 
     * // 无法混合不同族的产品
     * UIFactory macFactory = new MacUIFactory();
     * Button macButton = macFactory.createButton(); // Mac风格，与上面的Windows组件不会混用
     * ```
     */
    public void productFamilyConsistency() {
        System.out.println("一致性保证：工厂责任单一、抽象约束、客户端解耦、编译时检查");
    }

    /**
     * Q7: 抽象工厂模式与建造者模式有什么区别？
     * 
     * A: 主要区别：
     * 
     * 1. 创建目的：
     *    - 抽象工厂：创建一系列相关对象
     *    - 建造者：创建一个复杂对象
     * 
     * 2. 创建过程：
     *    - 抽象工厂：一次性创建，简单直接
     *    - 建造者：分步骤创建，过程复杂
     * 
     * 3. 产品关系：
     *    - 抽象工厂：多个相关产品，彼此独立
     *    - 建造者：一个产品，内部组件相关
     * 
     * 4. 客户端调用：
     *    - 抽象工厂：调用不同的创建方法
     *    - 建造者：调用构建步骤方法
     * 
     * 5. 扩展方式：
     *    - 抽象工厂：增加新工厂扩展产品族
     *    - 建造者：增加新建造者扩展构建方式
     * 
     * 代码对比：
     * ```java
     * // 抽象工厂：创建多个相关对象
     * UIFactory factory = new WindowsUIFactory();
     * Button button = factory.createButton();
     * TextField textField = factory.createTextField();
     * 
     * // 建造者：创建一个复杂对象
     * Computer computer = new ComputerBuilder()
     *     .setCPU("Intel i7")
     *     .setRAM("16GB")
     *     .setStorage("1TB SSD")
     *     .build();
     * ```
     */
    public void vsBuilderPattern() {
        System.out.println("区别：创建目的、创建过程、产品关系、客户端调用、扩展方式");
    }

    /**
     * Q8: 如何测试抽象工厂模式？
     * 
     * A: 测试策略：
     * 
     * 1. 产品族一致性测试
     * ```java
     * @Test
     * public void testProductFamilyConsistency() {
     *     UIFactory windowsFactory = new WindowsUIFactory();
     *     Button button = windowsFactory.createButton();
     *     TextField textField = windowsFactory.createTextField();
     *     
     *     assertTrue(button instanceof WindowsButton);
     *     assertTrue(textField instanceof WindowsTextField);
     * }
     * ```
     * 
     * 2. 工厂切换测试
     * ```java
     * @Test
     * public void testFactorySwitching() {
     *     UIFactory[] factories = {
     *         new WindowsUIFactory(),
     *         new MacUIFactory()
     *     };
     *     
     *     for (UIFactory factory : factories) {
     *         Button button = factory.createButton();
     *         assertNotNull(button);
     *         
     *         TextField textField = factory.createTextField();
     *         assertNotNull(textField);
     *     }
     * }
     * ```
     * 
     * 3. 抽象接口测试
     * ```java
     * @Test
     * public void testAbstractInterface() {
     *     UIFactory factory = new WindowsUIFactory();
     *     
     *     // 确保返回的是抽象接口，而不是具体实现
     *     Object button = factory.createButton();
     *     assertTrue(button instanceof Button);
     * }
     * ```
     * 
     * 4. Mock测试
     * ```java
     * @Test
     * public void testWithMock() {
     *     UIFactory mockFactory = Mockito.mock(UIFactory.class);
     *     Button mockButton = Mockito.mock(Button.class);
     *     
     *     when(mockFactory.createButton()).thenReturn(mockButton);
     *     
     *     Application app = new Application(mockFactory);
     *     app.run();
     *     
     *     verify(mockButton).click();
     * }
     * ```
     */
    public void testingStrategies() {
        System.out.println("测试策略：产品族一致性、工厂切换、抽象接口、Mock测试");
    }

    /**
     * Q9: Spring框架中抽象工厂模式的应用？
     * 
     * A: Spring中的应用：
     * 
     * 1. BeanFactory体系
     * ```java
     * // BeanFactory作为抽象工厂
     * public interface BeanFactory {
     *     Object getBean(String name);
     *     <T> T getBean(Class<T> requiredType);
     * }
     * 
     * // ApplicationContext作为具体工厂
     * public class ClassPathXmlApplicationContext implements ApplicationContext {
     *     // 创建和管理Bean实例
     * }
     * ```
     * 
     * 2. FactoryBean接口
     * ```java
     * public interface FactoryBean<T> {
     *     T getObject() throws Exception;
     *     Class<?> getObjectType();
     *     boolean isSingleton();
     * }
     * ```
     * 
     * 3. 事务管理器工厂
     * ```java
     * // 抽象工厂
     * interface PlatformTransactionManagerFactory {
     *     PlatformTransactionManager createTransactionManager();
     * }
     * 
     * // 具体工厂
     * class DataSourceTransactionManagerFactory implements PlatformTransactionManagerFactory {
     *     public PlatformTransactionManager createTransactionManager() {
     *         return new DataSourceTransactionManager(dataSource);
     *     }
     * }
     * ```
     * 
     * 4. 消息转换器工厂
     *    Spring Boot中根据类路径自动配置不同的消息转换器
     */
    public void springFrameworkUsage() {
        System.out.println("Spring应用：BeanFactory、FactoryBean、事务管理器、消息转换器");
    }

    /**
     * Q10: 如何优化抽象工厂模式的扩展性？
     * 
     * A: 优化策略：
     * 
     * 1. 使用注册机制
     * ```java
     * public class FactoryRegistry {
     *     private static Map<String, UIFactory> factories = new HashMap<>();
     *     
     *     static {
     *         register("windows", new WindowsUIFactory());
     *         register("mac", new MacUIFactory());
     *     }
     *     
     *     public static void register(String type, UIFactory factory) {
     *         factories.put(type, factory);
     *     }
     *     
     *     public static UIFactory getFactory(String type) {
     *         return factories.get(type);
     *     }
     * }
     * ```
     * 
     * 2. 配置驱动
     * ```java
     * @Configuration
     * public class FactoryConfiguration {
     *     @Bean
     *     @ConditionalOnProperty(name = "ui.theme", havingValue = "windows")
     *     public UIFactory windowsUIFactory() {
     *         return new WindowsUIFactory();
     *     }
     *     
     *     @Bean
     *     @ConditionalOnProperty(name = "ui.theme", havingValue = "mac")
     *     public UIFactory macUIFactory() {
     *         return new MacUIFactory();
     *     }
     * }
     * ```
     * 
     * 3. 反射机制
     * ```java
     * public class FactoryCreator {
     *     public static UIFactory createFactory(String className) {
     *         try {
     *             Class<?> clazz = Class.forName(className);
     *             return (UIFactory) clazz.newInstance();
     *         } catch (Exception e) {
     *             throw new RuntimeException("Factory creation failed", e);
     *         }
     *     }
     * }
     * ```
     * 
     * 4. SPI机制
     * ```java
     * // 在META-INF/services/com.example.UIFactory文件中配置实现类
     * ServiceLoader<UIFactory> loader = ServiceLoader.load(UIFactory.class);
     * for (UIFactory factory : loader) {
     *     // 使用工厂
     * }
     * ```
     */
    public void extensibilityOptimization() {
        System.out.println("优化策略：注册机制、配置驱动、反射机制、SPI机制");
    }
}