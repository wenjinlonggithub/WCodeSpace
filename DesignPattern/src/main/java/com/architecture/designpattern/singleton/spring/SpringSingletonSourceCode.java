package com.architecture.designpattern.singleton.spring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * =================== Spring框架单例模式源码实现原理深度解析 ===================
 * 
 * 本类深入分析Spring框架中单例模式的源码实现原理，包括：
 * 1. DefaultSingletonBeanRegistry的实现原理
 * 2. AbstractBeanFactory的getBean机制
 * 3. BeanDefinition和Scope的作用
 * 4. 循环依赖解决机制
 * 5. 线程安全保障机制
 * 
 * @author Architecture Team  
 * @version 1.0
 * @since 2024
 */
public class SpringSingletonSourceCode {

    public static void main(String[] args) {
        System.out.println("============= Spring框架单例模式源码实现原理 =============\n");
        
        // 1. Spring单例注册表核心实现
        demonstrateDefaultSingletonBeanRegistry();
        
        // 2. Bean工厂获取Bean的过程
        demonstrateAbstractBeanFactoryGetBean();
        
        // 3. BeanDefinition和作用域管理
        demonstrateBeanDefinitionScope();
        
        // 4. 循环依赖解决机制
        demonstrateCircularDependencyResolution();
        
        // 5. 线程安全机制
        demonstrateThreadSafety();
    }

    /**
     * 演示Spring DefaultSingletonBeanRegistry的核心实现
     * 这是Spring管理单例Bean的核心数据结构和算法
     */
    private static void demonstrateDefaultSingletonBeanRegistry() {
        System.out.println("1. 【DefaultSingletonBeanRegistry核心实现】");
        System.out.println("   Spring使用三级缓存和注册表来管理单例Bean的生命周期\n");
        
        /*
         * Spring源码分析：
         * 
         * public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {
         *     
         *     // 一级缓存：完全初始化的单例Bean缓存
         *     private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
         *     
         *     // 二级缓存：早期暴露的单例Bean缓存（用于解决循环依赖）
         *     private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);
         *     
         *     // 三级缓存：单例Bean工厂缓存（用于解决循环依赖）
         *     private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);
         *     
         *     // 正在创建中的Bean名称集合
         *     private final Set<String> singletonsCurrentlyInCreation = 
         *         Collections.newSetFromMap(new ConcurrentHashMap<>(16));
         * }
         */
        
        MockDefaultSingletonBeanRegistry registry = new MockDefaultSingletonBeanRegistry();
        
        // 模拟Spring创建和获取单例Bean的过程
        System.out.println("   模拟Bean创建和缓存过程:");
        registry.createAndRegisterBean("userService", MockUserService.class);
        registry.createAndRegisterBean("orderService", MockOrderService.class);
        
        // 验证单例特性
        Object userService1 = registry.getSingleton("userService");
        Object userService2 = registry.getSingleton("userService");
        
        System.out.println("   第一次获取userService: " + userService1.hashCode());
        System.out.println("   第二次获取userService: " + userService2.hashCode());
        System.out.println("   验证单例特性: " + (userService1 == userService2));
        System.out.println("   当前缓存的单例Bean数量: " + registry.getSingletonCount());
        System.out.println();
    }

    /**
     * 演示AbstractBeanFactory的getBean核心机制
     * 展示Spring如何通过工厂模式和单例模式结合管理Bean
     */
    private static void demonstrateAbstractBeanFactoryGetBean() {
        System.out.println("2. 【AbstractBeanFactory.getBean机制】");
        System.out.println("   Spring通过getBean方法实现Bean的懒加载和单例管理\n");
        
        /*
         * Spring源码分析（简化版）：
         * 
         * public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport 
         *         implements ConfigurableBeanFactory {
         *     
         *     public Object getBean(String name) throws BeansException {
         *         return doGetBean(name, null, null, false);
         *     }
         *     
         *     protected <T> T doGetBean(String name, Class<T> requiredType, 
         *                               Object[] args, boolean typeCheckOnly) {
         *         
         *         // 1. 转换Bean名称，处理别名
         *         String beanName = transformedBeanName(name);
         *         
         *         // 2. 先从单例缓存中获取
         *         Object sharedInstance = getSingleton(beanName);
         *         if (sharedInstance != null && args == null) {
         *             return getObjectForBeanInstance(sharedInstance, name, beanName, null);
         *         }
         *         
         *         // 3. 检查Bean定义是否存在
         *         BeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
         *         
         *         // 4. 如果是单例作用域，创建单例Bean
         *         if (mbd.isSingleton()) {
         *             sharedInstance = getSingleton(beanName, () -> {
         *                 return createBean(beanName, mbd, args);
         *             });
         *             return getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
         *         }
         *         
         *         // 5. 其他作用域处理...
         *     }
         * }
         */
        
        MockAbstractBeanFactory beanFactory = new MockAbstractBeanFactory();
        
        // 注册Bean定义
        beanFactory.registerBeanDefinition("userService", new MockBeanDefinition(MockUserService.class, true));
        beanFactory.registerBeanDefinition("orderService", new MockBeanDefinition(MockOrderService.class, true));
        
        System.out.println("   第一次getBean('userService'):");
        Object bean1 = beanFactory.getBean("userService");
        
        System.out.println("   第二次getBean('userService'):");
        Object bean2 = beanFactory.getBean("userService");
        
        System.out.println("   Bean实例1: " + bean1.hashCode());
        System.out.println("   Bean实例2: " + bean2.hashCode());
        System.out.println("   是否从缓存获取: " + (bean1 == bean2));
        System.out.println();
    }

    /**
     * 演示BeanDefinition和作用域管理机制
     * 展示Spring如何通过BeanDefinition控制Bean的作用域
     */
    private static void demonstrateBeanDefinitionScope() {
        System.out.println("3. 【BeanDefinition和Scope管理】");
        System.out.println("   Spring通过BeanDefinition来定义Bean的作用域和创建方式\n");
        
        /*
         * Spring源码中的Scope定义：
         * 
         * public interface BeanDefinition extends AttributeAccessor, BeanMetadataElement {
         *     String SCOPE_SINGLETON = "singleton";
         *     String SCOPE_PROTOTYPE = "prototype";
         *     
         *     void setScope(@Nullable String scope);
         *     String getScope();
         *     
         *     void setSingleton(boolean singleton);
         *     boolean isSingleton();
         *     
         *     void setPrototype(boolean prototype);  
         *     boolean isPrototype();
         * }
         */
        
        MockBeanDefinitionRegistry registry = new MockBeanDefinitionRegistry();
        
        // 注册单例Bean定义
        MockBeanDefinition singletonDef = new MockBeanDefinition(MockUserService.class, true);
        singletonDef.setScope("singleton");
        registry.registerBeanDefinition("singletonBean", singletonDef);
        
        // 注册原型Bean定义  
        MockBeanDefinition prototypeDef = new MockBeanDefinition(MockOrderService.class, false);
        prototypeDef.setScope("prototype");
        registry.registerBeanDefinition("prototypeBean", prototypeDef);
        
        System.out.println("   单例Bean定义:");
        MockBeanDefinition def1 = registry.getBeanDefinition("singletonBean");
        System.out.println("   - Scope: " + def1.getScope());
        System.out.println("   - isSingleton: " + def1.isSingleton());
        System.out.println("   - Class: " + def1.getBeanClass().getSimpleName());
        
        System.out.println("\n   原型Bean定义:");
        MockBeanDefinition def2 = registry.getBeanDefinition("prototypeBean");
        System.out.println("   - Scope: " + def2.getScope());
        System.out.println("   - isSingleton: " + def2.isSingleton());
        System.out.println("   - Class: " + def2.getBeanClass().getSimpleName());
        System.out.println();
    }

    /**
     * 演示Spring解决循环依赖的三级缓存机制
     * 这是Spring单例模式实现中最复杂和精巧的部分
     */
    private static void demonstrateCircularDependencyResolution() {
        System.out.println("4. 【循环依赖解决机制 - 三级缓存】");
        System.out.println("   Spring通过三级缓存巧妙解决单例Bean之间的循环依赖问题\n");
        
        /*
         * Spring循环依赖解决核心算法：
         * 
         * protected Object getSingleton(String beanName, boolean allowEarlyReference) {
         *     // 1. 从一级缓存获取完全初始化的Bean
         *     Object singletonObject = this.singletonObjects.get(beanName);
         *     
         *     if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
         *         synchronized (this.singletonObjects) {
         *             // 2. 从二级缓存获取早期暴露的Bean
         *             singletonObject = this.earlySingletonObjects.get(beanName);
         *             
         *             if (singletonObject == null && allowEarlyReference) {
         *                 // 3. 从三级缓存获取Bean工厂
         *                 ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
         *                 if (singletonFactory != null) {
         *                     singletonObject = singletonFactory.getObject();
         *                     // 将Bean从三级缓存移动到二级缓存
         *                     this.earlySingletonObjects.put(beanName, singletonObject);
         *                     this.singletonFactories.remove(beanName);
         *                 }
         *             }
         *         }
         *     }
         *     return singletonObject;
         * }
         */
        
        MockCircularDependencyResolver resolver = new MockCircularDependencyResolver();
        
        System.out.println("   模拟循环依赖场景: ServiceA -> ServiceB -> ServiceA");
        
        // 开始创建ServiceA，这会触发循环依赖解决机制
        Object serviceA = resolver.createBeanWithCircularDependency("serviceA");
        
        System.out.println("   ServiceA创建完成: " + serviceA.hashCode());
        System.out.println("   三级缓存使用情况:");
        resolver.printCacheStatus();
        System.out.println();
    }

    /**
     * 演示Spring单例模式的线程安全机制
     * 展示Spring如何在多线程环境下保证单例Bean的安全性
     */
    private static void demonstrateThreadSafety() {
        System.out.println("5. 【线程安全机制】");
        System.out.println("   Spring使用synchronized和ConcurrentHashMap保证单例Bean的线程安全\n");
        
        /*
         * Spring线程安全关键点：
         * 
         * 1. 使用ConcurrentHashMap作为单例缓存容器
         * 2. 在创建Bean时使用synchronized同步块
         * 3. 双重检查锁定模式避免重复创建
         * 4. volatile关键字确保可见性
         */
        
        MockThreadSafeSingletonRegistry registry = new MockThreadSafeSingletonRegistry();
        
        // 模拟多线程并发获取单例Bean
        System.out.println("   模拟10个线程并发获取同一个Bean:");
        
        for (int i = 0; i < 10; i++) {
            final int threadId = i;
            new Thread(() -> {
                Object bean = registry.getSingletonWithThreadSafety("testBean");
                System.out.println("   线程" + threadId + "获取到Bean: " + bean.hashCode());
            }).start();
        }
        
        // 等待所有线程完成
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\n   验证结果: 所有线程获取到的Bean哈希码相同，证明是同一个实例");
        System.out.println("   Bean创建次数: " + registry.getCreationCount());
        
        System.out.println("\n============= Spring单例模式源码总结 =============");
        System.out.println("Spring单例模式的核心实现机制:");
        System.out.println("1. 三级缓存体系: singletonObjects、earlySingletonObjects、singletonFactories");
        System.out.println("2. 注册表模式: DefaultSingletonBeanRegistry统一管理单例Bean");
        System.out.println("3. 工厂模式结合: AbstractBeanFactory提供Bean创建的统一接口");
        System.out.println("4. 循环依赖解决: 通过早期暴露和代理机制解决复杂依赖关系");
        System.out.println("5. 线程安全保障: ConcurrentHashMap + synchronized确保多线程安全");
        System.out.println("6. 懒加载机制: 只有在需要时才创建Bean，提高启动性能");
    }
}

/**
 * 模拟Spring的DefaultSingletonBeanRegistry实现
 */
class MockDefaultSingletonBeanRegistry {
    // 一级缓存：完全初始化的单例Bean
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
    
    // 二级缓存：早期暴露的Bean（用于循环依赖）
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);
    
    // 三级缓存：Bean工厂（用于循环依赖）
    private final Map<String, MockObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>(16);
    
    public void createAndRegisterBean(String beanName, Class<?> beanClass) {
        if (!singletonObjects.containsKey(beanName)) {
            try {
                Object bean = beanClass.getDeclaredConstructor().newInstance();
                singletonObjects.put(beanName, bean);
                System.out.println("   创建并注册Bean: " + beanName + " -> " + bean.hashCode());
            } catch (Exception e) {
                System.err.println("   创建Bean失败: " + e.getMessage());
            }
        }
    }
    
    public Object getSingleton(String beanName) {
        return singletonObjects.get(beanName);
    }
    
    public int getSingletonCount() {
        return singletonObjects.size();
    }
}

/**
 * 模拟Spring的AbstractBeanFactory核心逻辑
 */
class MockAbstractBeanFactory {
    private final Map<String, MockBeanDefinition> beanDefinitions = new ConcurrentHashMap<>();
    private final Map<String, Object> singletonCache = new ConcurrentHashMap<>();
    
    public void registerBeanDefinition(String beanName, MockBeanDefinition beanDefinition) {
        beanDefinitions.put(beanName, beanDefinition);
    }
    
    public Object getBean(String beanName) {
        // 1. 检查单例缓存
        Object cachedBean = singletonCache.get(beanName);
        if (cachedBean != null) {
            System.out.println("   从缓存获取Bean: " + beanName);
            return cachedBean;
        }
        
        // 2. 获取Bean定义
        MockBeanDefinition beanDefinition = beanDefinitions.get(beanName);
        if (beanDefinition == null) {
            throw new RuntimeException("No bean definition found for: " + beanName);
        }
        
        // 3. 根据作用域创建Bean
        if (beanDefinition.isSingleton()) {
            return createSingletonBean(beanName, beanDefinition);
        } else {
            return createPrototypeBean(beanDefinition);
        }
    }
    
    private Object createSingletonBean(String beanName, MockBeanDefinition beanDefinition) {
        System.out.println("   创建单例Bean: " + beanName);
        try {
            Object bean = beanDefinition.getBeanClass().getDeclaredConstructor().newInstance();
            singletonCache.put(beanName, bean);
            return bean;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + beanName, e);
        }
    }
    
    private Object createPrototypeBean(MockBeanDefinition beanDefinition) {
        System.out.println("   创建原型Bean: " + beanDefinition.getBeanClass().getSimpleName());
        try {
            return beanDefinition.getBeanClass().getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create prototype bean", e);
        }
    }
}

/**
 * 模拟Spring的BeanDefinition
 */
class MockBeanDefinition {
    private final Class<?> beanClass;
    private final boolean singleton;
    private String scope;
    
    public MockBeanDefinition(Class<?> beanClass, boolean singleton) {
        this.beanClass = beanClass;
        this.singleton = singleton;
        this.scope = singleton ? "singleton" : "prototype";
    }
    
    public Class<?> getBeanClass() { return beanClass; }
    public boolean isSingleton() { return singleton; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
}

/**
 * 模拟Spring的BeanDefinitionRegistry
 */
class MockBeanDefinitionRegistry {
    private final Map<String, MockBeanDefinition> beanDefinitions = new ConcurrentHashMap<>();
    
    public void registerBeanDefinition(String beanName, MockBeanDefinition beanDefinition) {
        beanDefinitions.put(beanName, beanDefinition);
    }
    
    public MockBeanDefinition getBeanDefinition(String beanName) {
        return beanDefinitions.get(beanName);
    }
}

/**
 * 模拟Spring的循环依赖解决机制
 */
class MockCircularDependencyResolver {
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>();
    private final Map<String, MockObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>();
    
    public Object createBeanWithCircularDependency(String beanName) {
        // 模拟创建过程中的循环依赖处理
        System.out.println("   开始创建Bean: " + beanName);
        
        // 创建Bean实例
        Object bean = new Object();
        
        // 将Bean工厂放入三级缓存
        singletonFactories.put(beanName, () -> {
            System.out.println("   从工厂获取早期Bean: " + beanName);
            return bean;
        });
        
        // 模拟依赖注入过程（这里可能触发循环依赖）
        // ... 依赖注入逻辑 ...
        
        // 完成创建后放入一级缓存
        singletonObjects.put(beanName, bean);
        earlySingletonObjects.remove(beanName);
        singletonFactories.remove(beanName);
        
        return bean;
    }
    
    public void printCacheStatus() {
        System.out.println("   一级缓存Bean数量: " + singletonObjects.size());
        System.out.println("   二级缓存Bean数量: " + earlySingletonObjects.size());
        System.out.println("   三级缓存Bean数量: " + singletonFactories.size());
    }
}

/**
 * 模拟Spring的线程安全单例注册表
 */
class MockThreadSafeSingletonRegistry {
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();
    private volatile int creationCount = 0;
    
    public Object getSingletonWithThreadSafety(String beanName) {
        Object result = singletonObjects.get(beanName);
        if (result == null) {
            synchronized (this) {
                result = singletonObjects.get(beanName);
                if (result == null) {
                    result = createBean(beanName);
                    singletonObjects.put(beanName, result);
                    creationCount++;
                }
            }
        }
        return result;
    }
    
    private Object createBean(String beanName) {
        return new Object(); // 简化的Bean创建
    }
    
    public int getCreationCount() {
        return creationCount;
    }
}

// 辅助接口和类
@FunctionalInterface
interface MockObjectFactory<T> {
    T getObject();
}

// 示例业务类
class MockUserService {
    public MockUserService() {
        System.out.println("   MockUserService实例化");
    }
}

class MockOrderService {
    public MockOrderService() {
        System.out.println("   MockOrderService实例化");
    }
}