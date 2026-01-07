package com.architecture.designpattern.singleton.spring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ======================= Spring框架中的单例模式案例解析 =======================
 * 
 * Spring框架大量使用了单例模式来管理Bean和核心组件，这是Spring IoC容器的核心特性之一。
 * 本文件通过模拟Spring的实现，深入分析Spring中单例模式的应用和实现原理。
 * 
 * @author Architecture Team
 * @version 1.0
 * @since 2024
 */
public class SpringSingletonAnalysis {

    public static void main(String[] args) {
        System.out.println("================= Spring单例模式深度解析 =================\n");
        
        // 1. Bean作用域单例模式
        demonstrateBeanScopesSingleton();
        
        // 2. 容器组件单例模式
        demonstrateContainerSingleton();
        
        // 3. 配置和环境单例模式
        demonstrateConfigurationSingleton();
        
        // 4. Spring单例注册表机制
        demonstrateSingletonRegistry();
        
        // 5. AOP和数据访问单例模式
        demonstrateAopAndDataAccessSingleton();
    }

    /**
     * 演示Spring Bean作用域中的单例模式
     * Spring中默认所有Bean都是单例的，这是通过Singleton作用域实现的
     */
    private static void demonstrateBeanScopesSingleton() {
        System.out.println("1. 【Bean作用域单例模式】");
        System.out.println("   Spring中@Component、@Service、@Repository、@Controller默认都是单例");
        
        // 模拟Spring容器的Bean单例管理
        MockSpringContainer container = new MockSpringContainer();
        
        // 模拟注册Bean
        container.registerSingleton("userService", new UserService());
        container.registerSingleton("orderService", new OrderService());
        
        // 多次获取相同Bean，验证单例特性
        UserService service1 = (UserService) container.getBean("userService");
        UserService service2 = (UserService) container.getBean("userService");
        OrderService orderService1 = (OrderService) container.getBean("orderService");
        OrderService orderService2 = (OrderService) container.getBean("orderService");
        
        System.out.println("   UserService实例1: " + service1.hashCode());
        System.out.println("   UserService实例2: " + service2.hashCode());
        System.out.println("   是否为同一实例: " + (service1 == service2));
        System.out.println("   OrderService实例1: " + orderService1.hashCode());
        System.out.println("   OrderService实例2: " + orderService2.hashCode());
        System.out.println("   是否为同一实例: " + (orderService1 == orderService2));
        System.out.println();
    }

    /**
     * 演示Spring容器组件的单例模式
     * ApplicationContext、BeanFactory等核心容器组件都是单例的
     */
    private static void demonstrateContainerSingleton() {
        System.out.println("2. 【容器组件单例模式】");
        System.out.println("   ApplicationContext和BeanFactory是Spring的核心单例组件");
        
        // 模拟ApplicationContext单例
        MockApplicationContext context1 = MockApplicationContext.getInstance();
        MockApplicationContext context2 = MockApplicationContext.getInstance();
        
        System.out.println("   ApplicationContext实例1: " + context1.hashCode());
        System.out.println("   ApplicationContext实例2: " + context2.hashCode());
        System.out.println("   是否为同一实例: " + (context1 == context2));
        
        // 模拟BeanFactory单例
        MockBeanFactory factory1 = MockBeanFactory.getInstance();
        MockBeanFactory factory2 = MockBeanFactory.getInstance();
        
        System.out.println("   BeanFactory实例1: " + factory1.hashCode());
        System.out.println("   BeanFactory实例2: " + factory2.hashCode());
        System.out.println("   是否为同一实例: " + (factory1 == factory2));
        System.out.println();
    }

    /**
     * 演示Spring配置和环境组件的单例模式
     * Environment、PropertySource等配置相关组件通常是单例的
     */
    private static void demonstrateConfigurationSingleton() {
        System.out.println("3. 【配置和环境单例模式】");
        System.out.println("   Environment和PropertySource用于管理应用配置，采用单例模式");
        
        // 模拟Environment单例
        MockEnvironment env1 = MockEnvironment.getInstance();
        MockEnvironment env2 = MockEnvironment.getInstance();
        
        env1.setProperty("app.name", "SpringSingletonDemo");
        
        System.out.println("   Environment实例1: " + env1.hashCode());
        System.out.println("   Environment实例2: " + env2.hashCode());
        System.out.println("   是否为同一实例: " + (env1 == env2));
        System.out.println("   共享配置验证 - app.name: " + env2.getProperty("app.name"));
        System.out.println();
    }

    /**
     * 演示Spring的单例注册表机制
     * Spring内部使用单例注册表来管理所有单例Bean
     */
    private static void demonstrateSingletonRegistry() {
        System.out.println("4. 【Spring单例注册表机制】");
        System.out.println("   Spring内部使用SingletonBeanRegistry来管理单例Bean");
        
        MockSingletonBeanRegistry registry = MockSingletonBeanRegistry.getInstance();
        
        // 注册单例Bean
        registry.registerSingleton("dataSource", new MockDataSource());
        registry.registerSingleton("transactionManager", new MockTransactionManager());
        
        // 验证单例特性
        MockDataSource ds1 = (MockDataSource) registry.getSingleton("dataSource");
        MockDataSource ds2 = (MockDataSource) registry.getSingleton("dataSource");
        
        System.out.println("   注册表中的单例Bean数量: " + registry.getSingletonCount());
        System.out.println("   DataSource实例1: " + ds1.hashCode());
        System.out.println("   DataSource实例2: " + ds2.hashCode());
        System.out.println("   是否为同一实例: " + (ds1 == ds2));
        System.out.println();
    }

    /**
     * 演示AOP和数据访问组件的单例模式
     * ProxyFactory、JdbcTemplate、TransactionManager等都使用单例模式
     */
    private static void demonstrateAopAndDataAccessSingleton() {
        System.out.println("5. 【AOP和数据访问单例模式】");
        System.out.println("   AOP代理工厂和数据访问模板采用单例模式提高性能");
        
        // 模拟AOP代理工厂单例
        MockAopProxyFactory proxyFactory1 = MockAopProxyFactory.getInstance();
        MockAopProxyFactory proxyFactory2 = MockAopProxyFactory.getInstance();
        
        System.out.println("   AOP代理工厂实例1: " + proxyFactory1.hashCode());
        System.out.println("   AOP代理工厂实例2: " + proxyFactory2.hashCode());
        System.out.println("   是否为同一实例: " + (proxyFactory1 == proxyFactory2));
        
        // 模拟JdbcTemplate单例使用
        MockJdbcTemplate template1 = MockJdbcTemplate.getInstance();
        MockJdbcTemplate template2 = MockJdbcTemplate.getInstance();
        
        System.out.println("   JdbcTemplate实例1: " + template1.hashCode());
        System.out.println("   JdbcTemplate实例2: " + template2.hashCode());
        System.out.println("   是否为同一实例: " + (template1 == template2));
        
        System.out.println("\n================= Spring单例模式总结 =================");
        System.out.println("Spring使用单例模式的优势:");
        System.out.println("1. 内存效率: 减少对象创建，节约内存空间");
        System.out.println("2. 性能优化: 避免重复初始化，提高应用启动速度");
        System.out.println("3. 状态共享: 容器级别的组件需要共享状态和配置");
        System.out.println("4. 资源管理: 统一管理连接池、缓存等共享资源");
        System.out.println("5. 线程安全: Spring确保单例Bean的线程安全性");
    }
}

/**
 * 模拟Spring容器的简化实现
 * 展示Spring如何使用单例注册表管理Bean
 */
class MockSpringContainer {
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    
    public void registerSingleton(String beanName, Object singletonObject) {
        singletonObjects.put(beanName, singletonObject);
    }
    
    public Object getBean(String beanName) {
        return singletonObjects.get(beanName);
    }
}

/**
 * 模拟ApplicationContext的单例实现
 * 使用双重检查锁定确保线程安全
 */
class MockApplicationContext {
    private static volatile MockApplicationContext instance;
    private final Map<String, Object> applicationScope = new ConcurrentHashMap<>();
    
    private MockApplicationContext() {
        // 私有构造函数
        System.out.println("   ApplicationContext初始化...");
    }
    
    public static MockApplicationContext getInstance() {
        if (instance == null) {
            synchronized (MockApplicationContext.class) {
                if (instance == null) {
                    instance = new MockApplicationContext();
                }
            }
        }
        return instance;
    }
    
    public void setAttribute(String key, Object value) {
        applicationScope.put(key, value);
    }
    
    public Object getAttribute(String key) {
        return applicationScope.get(key);
    }
}

/**
 * 模拟BeanFactory的单例实现
 * Spring的核心Bean工厂
 */
class MockBeanFactory {
    private static volatile MockBeanFactory instance;
    private final Map<String, Object> beans = new ConcurrentHashMap<>();
    
    private MockBeanFactory() {
        System.out.println("   BeanFactory初始化...");
    }
    
    public static MockBeanFactory getInstance() {
        if (instance == null) {
            synchronized (MockBeanFactory.class) {
                if (instance == null) {
                    instance = new MockBeanFactory();
                }
            }
        }
        return instance;
    }
    
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
    
    public void registerBean(String beanName, Object bean) {
        beans.put(beanName, bean);
    }
}

/**
 * 模拟Environment的单例实现
 * 管理应用环境配置
 */
class MockEnvironment {
    private static volatile MockEnvironment instance;
    private final Map<String, String> properties = new ConcurrentHashMap<>();
    
    private MockEnvironment() {
        System.out.println("   Environment初始化...");
        // 预设一些默认配置
        properties.put("spring.profiles.active", "dev");
    }
    
    public static MockEnvironment getInstance() {
        if (instance == null) {
            synchronized (MockEnvironment.class) {
                if (instance == null) {
                    instance = new MockEnvironment();
                }
            }
        }
        return instance;
    }
    
    public void setProperty(String key, String value) {
        properties.put(key, value);
    }
    
    public String getProperty(String key) {
        return properties.get(key);
    }
}

/**
 * 模拟Spring的单例Bean注册表
 * 这是Spring IoC容器管理单例Bean的核心机制
 */
class MockSingletonBeanRegistry {
    private static volatile MockSingletonBeanRegistry instance;
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    
    private MockSingletonBeanRegistry() {
        System.out.println("   SingletonBeanRegistry初始化...");
    }
    
    public static MockSingletonBeanRegistry getInstance() {
        if (instance == null) {
            synchronized (MockSingletonBeanRegistry.class) {
                if (instance == null) {
                    instance = new MockSingletonBeanRegistry();
                }
            }
        }
        return instance;
    }
    
    public void registerSingleton(String beanName, Object singletonObject) {
        singletonObjects.put(beanName, singletonObject);
    }
    
    public Object getSingleton(String beanName) {
        return singletonObjects.get(beanName);
    }
    
    public int getSingletonCount() {
        return singletonObjects.size();
    }
}

/**
 * 模拟AOP代理工厂的单例实现
 */
class MockAopProxyFactory {
    private static volatile MockAopProxyFactory instance;
    
    private MockAopProxyFactory() {
        System.out.println("   AOP代理工厂初始化...");
    }
    
    public static MockAopProxyFactory getInstance() {
        if (instance == null) {
            synchronized (MockAopProxyFactory.class) {
                if (instance == null) {
                    instance = new MockAopProxyFactory();
                }
            }
        }
        return instance;
    }
    
    public Object createProxy(Object target) {
        return target; // 简化实现
    }
}

/**
 * 模拟JdbcTemplate的单例实现
 */
class MockJdbcTemplate {
    private static volatile MockJdbcTemplate instance;
    
    private MockJdbcTemplate() {
        System.out.println("   JdbcTemplate初始化...");
    }
    
    public static MockJdbcTemplate getInstance() {
        if (instance == null) {
            synchronized (MockJdbcTemplate.class) {
                if (instance == null) {
                    instance = new MockJdbcTemplate();
                }
            }
        }
        return instance;
    }
    
    public void execute(String sql) {
        System.out.println("   执行SQL: " + sql);
    }
}

// 业务Bean示例
class UserService {
    public void createUser(String username) {
        System.out.println("创建用户: " + username);
    }
}

class OrderService {
    public void createOrder(String orderId) {
        System.out.println("创建订单: " + orderId);
    }
}

// 数据访问组件示例
class MockDataSource {
    public MockDataSource() {
        System.out.println("   DataSource初始化...");
    }
}

class MockTransactionManager {
    public MockTransactionManager() {
        System.out.println("   TransactionManager初始化...");
    }
}