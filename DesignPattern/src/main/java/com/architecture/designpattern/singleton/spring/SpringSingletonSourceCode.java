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
        System.out.println("4. 【循环依赖解决机制 - 三级缓存详解】");
        System.out.println("   Spring通过三级缓存巧妙解决单例Bean之间的循环依赖问题\n");
        
        /*
         * =================== 循环依赖背景和问题 ===================
         * 
         * 1. 什么是循环依赖？
         *    - ServiceA依赖ServiceB，ServiceB又依赖ServiceA
         *    - 在创建ServiceA时需要注入ServiceB，但ServiceB还没创建
         *    - 在创建ServiceB时需要注入ServiceA，但ServiceA还没初始化完成
         *    
         * 2. 为什么会出现循环依赖？
         *    - 复杂业务场景中，服务间相互调用很常见
         *    - 用户服务需要订单服务，订单服务也需要用户服务
         *    - 如果不妥善处理，会导致无限循环，最终栈溢出
         *    
         * =================== 无限循环和栈溢出原理详解 ===================
         * 
         * 3. 栈溢出原理分析：
         *    
         *    假设没有三级缓存机制，Spring容器创建Bean的简化流程：
         *    
         *    createBean(ServiceA) {
         *        1. 实例化 ServiceA
         *        2. 发现依赖 ServiceB，调用 getBean(ServiceB)
         *           -> createBean(ServiceB) {
         *                1. 实例化 ServiceB  
         *                2. 发现依赖 ServiceA，调用 getBean(ServiceA)
         *                   -> createBean(ServiceA) {  // 递归调用！
         *                        1. 实例化 ServiceA
         *                        2. 发现依赖 ServiceB，调用 getBean(ServiceB)
         *                           -> createBean(ServiceB) {  // 又是递归调用！
         *                                ...无限循环...
         *                           }
         *                   }
         *           }
         *    }
         *    
         *    调用栈会无限增长：
         *    createBean(ServiceA)
         *      -> getBean(ServiceB) 
         *        -> createBean(ServiceB)
         *          -> getBean(ServiceA)
         *            -> createBean(ServiceA)  // 重复！
         *              -> getBean(ServiceB)
         *                -> createBean(ServiceB) // 重复！
         *                  -> ...直到栈溢出
         *    
         * 4. 栈溢出的技术细节：
         *    - 每次方法调用都会在栈帧中分配内存
         *    - Java虚拟机栈的大小有限（默认1MB左右）
         *    - 递归调用深度超过栈容量时抛出StackOverflowError
         *    - 典型错误信息：java.lang.StackOverflowError
         *    
         * 5. 实际影响：
         *    - 应用启动失败
         *    - 容器初始化异常
         *    - 资源泄漏和内存问题
         *    - 难以调试和定位问题
         *    
         * 3. 传统解决方案的问题：
         *    - 懒加载：延迟到使用时再注入，但可能造成NPE
         *    - 接口隔离：增加代码复杂度，破坏设计
         *    - setter注入：破坏不可变性原则
         *
         * =================== Spring三级缓存原理深度解析 ===================
         * 
         * Spring通过"分层缓存 + 早期暴露"的巧妙设计解决循环依赖：
         * 
         * 【一级缓存 - singletonObjects】- 成品仓库
         *    职责：存储完全初始化的单例Bean实例
         *    特点：
         *    - Bean生命周期完整：实例化 → 依赖注入 → 初始化回调 → 完成
         *    - 线程安全：使用ConcurrentHashMap保证并发访问安全
         *    - 最终状态：这是用户最终使用的Bean对象
         *    - 性能最优：直接返回，无需任何额外处理
         *    工作时机：Bean完全创建完成后放入
         *    存在必要性：
         *    ① 提供完整功能的Bean给业务使用
         *    ② 避免重复创建已完成的Bean
         *    ③ 保证单例模式的语义正确性
         *    
         * 【二级缓存 - earlySingletonObjects】- 半成品仓库  
         *    职责：存储早期暴露的Bean引用（可能是代理对象）
         *    特点：
         *    - Bean状态：实例已创建，但依赖注入可能未完成
         *    - 引用稳定：确保循环依赖中使用的是同一个对象引用
         *    - 代理支持：如果Bean需要AOP，这里存储的是代理对象
         *    - 性能优化：避免重复调用三级缓存的工厂方法
         *    工作时机：首次从三级缓存获取Bean后放入
         *    存在必要性：
         *    ① 缓存从三级缓存生成的Bean引用，提高性能
         *    ② 确保多次获取返回相同对象，维护对象身份一致性
         *    ③ 支持AOP代理在循环依赖中的正确使用
         *    
         * 【三级缓存 - singletonFactories】- 生产车间
         *    职责：存储Bean的工厂函数(ObjectFactory)
         *    特点：
         *    - 延迟执行：只在需要时才调用工厂方法
         *    - AOP集成：工厂方法可以创建代理对象
         *    - 灵活性：支持复杂的Bean创建逻辑
         *    - 时机关键：在Bean实例化后立即放入（依赖注入前）
         *    工作时机：Bean实例化完成后立即放入，首次被依赖时调用
         *    存在必要性：
         *    ① 解决循环依赖的核心机制，提供早期Bean引用
         *    ② 支持AOP代理的延迟创建，保持代理逻辑的完整性
         *    ③ 延迟执行策略，避免不必要的代理对象创建
         *    
         * =================== 三级缓存协作机制 ===================
         * 
         * 为什么需要三级缓存而不是两级？
         * 
         * 【如果只有一级缓存】：
         * - 问题：无法处理循环依赖，会导致无限递归
         * - 原因：Bean必须完全创建才能放入缓存，但循环依赖时无法完成创建
         * 
         * 【如果只有两级缓存】：
         * - 一级：完成Bean，二级：未完成Bean
         * - 问题：AOP代理无法正确处理
         * - 原因：不知道何时创建代理，可能导致注入原始对象而非代理对象
         * - 示例：ServiceA需要ServiceB的代理，但ServiceB在循环依赖时可能注入原始对象
         * 
         * 【三级缓存的优势】：
         * - 职责分离：一级存完成品，二级存半成品，三级存生产方法
         * - AOP兼容：三级缓存的工厂可以决定返回原始对象还是代理对象
         * - 性能优化：二级缓存避免重复调用工厂方法
         * - 时机控制：三级缓存提供了精确的代理创建时机
         * 
         * =================== Bean生命周期与缓存关系 ===================
         * 
         * Bean创建的完整流程：
         * 1. 实例化 (Instantiation)
         *    └── 对象已创建，但属性未注入
         * 2. 放入三级缓存
         *    └── 提供工厂方法，支持早期引用和AOP代理
         * 3. 属性注入 (Population) 
         *    └── 依赖注入，可能触发循环依赖解决
         * 4. 初始化 (Initialization)
         *    └── 执行初始化回调方法
         * 5. 放入一级缓存，清除二三级缓存
         *    └── Bean完全可用，清理临时缓存
         * 
         * =================== 核心算法分析 ===================
         */
        
        /*
         * Spring循环依赖解决核心算法：
         * 
         * protected Object getSingleton(String beanName, boolean allowEarlyReference) {
         *     // 第一步：从一级缓存获取完全初始化的Bean
         *     Object singletonObject = this.singletonObjects.get(beanName);
         *     
         *     // 关键判断：Bean为空且正在创建中
         *     if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
         *         synchronized (this.singletonObjects) {
         *             // 第二步：从二级缓存获取早期暴露的Bean
         *             singletonObject = this.earlySingletonObjects.get(beanName);
         *             
         *             // 二级缓存也没有，且允许早期引用
         *             if (singletonObject == null && allowEarlyReference) {
         *                 // 第三步：从三级缓存获取Bean工厂
         *                 ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
         *                 if (singletonFactory != null) {
         *                     // 通过工厂创建Bean（可能是代理对象）
         *                     singletonObject = singletonFactory.getObject();
         *                     // 升级：将Bean从三级缓存移动到二级缓存
         *                     this.earlySingletonObjects.put(beanName, singletonObject);
         *                     this.singletonFactories.remove(beanName);
         *                 }
         *             }
         *         }
         *     }
         *     return singletonObject;
         * }
         * 
         * =================== 缓存升级机制 ===================
         * 
         * 三级缓存的设计体现了"按需升级"的思想：
         * 1. 三级缓存 -> 二级缓存：当首次被依赖时
         * 2. 二级缓存 -> 一级缓存：当完全初始化后
         * 3. 每个级别都有特定的职责和时机
         * 
         * =================== AOP与循环依赖 ===================
         * 
         * 三级缓存还解决了AOP代理与循环依赖的复杂情况：
         * 1. 如果Bean需要AOP代理，三级缓存中的ObjectFactory会创建代理对象
         * 2. 确保循环依赖注入的是代理对象，而不是原始对象
         * 3. 保持AOP功能的完整性
         */
        
        System.out.println("   =================== 栈溢出演示 ===================");
        
        // 首先演示没有三级缓存时的栈溢出问题
        System.out.println("   【1. 演示传统方式的栈溢出问题】");
        NaiveCircularDependencyResolver naiveResolver = new NaiveCircularDependencyResolver();
        try {
            naiveResolver.createBeanWithoutCache("serviceA");
        } catch (StackOverflowError e) {
            System.out.println("   ❌ 发生栈溢出: " + e.getClass().getSimpleName());
            System.out.println("   ❌ 调用深度过大，超出JVM栈限制");
        } catch (RuntimeException e) {
            System.out.println("   ❌ 检测到无限递归: " + e.getMessage());
        }
        
        System.out.println("\n   =================== 循环依赖解决过程详解 ===================");
        
        System.out.println("   【2. Spring三级缓存解决方案】");
        EnhancedCircularDependencyResolver resolver = new EnhancedCircularDependencyResolver();
        
        System.out.println("   模拟复杂循环依赖场景: ServiceA -> ServiceB -> ServiceC -> ServiceA");
        
        // 开始创建ServiceA，这会触发完整的循环依赖解决机制
        Object serviceA = resolver.createBeanWithCircularDependency("serviceA");
        
        System.out.println("   ✅ ServiceA创建完成: " + serviceA.hashCode());
        System.out.println("\n   三级缓存最终状态:");
        resolver.printDetailedCacheStatus();
        
        System.out.println("\n   =================== 关键时间点分析 ===================");
        resolver.printCreationTimeline();
        
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
 * 朴素的循环依赖解决器 - 演示没有缓存机制时的栈溢出问题
 */
class NaiveCircularDependencyResolver {
    private int recursionDepth = 0;
    private static final int MAX_SAFE_DEPTH = 100; // 安全递归深度限制
    
    public Object createBeanWithoutCache(String beanName) {
        recursionDepth++;
        
        // 为了演示目的，设置一个递归深度限制来避免真正的栈溢出
        if (recursionDepth > MAX_SAFE_DEPTH) {
            throw new RuntimeException("检测到循环依赖导致的无限递归，已达到安全深度限制: " + MAX_SAFE_DEPTH);
        }
        
        System.out.println("   递归深度: " + recursionDepth + " - 创建Bean: " + beanName);
        
        // 模拟Bean实例化
        Object bean = new Object();
        
        // 模拟依赖注入 - 这里会触发循环依赖
        if ("serviceA".equals(beanName)) {
            System.out.println("   serviceA需要依赖serviceB，开始创建serviceB...");
            Object serviceB = createBeanWithoutCache("serviceB"); // 递归调用
            
        } else if ("serviceB".equals(beanName)) {
            System.out.println("   serviceB需要依赖serviceA，开始创建serviceA...");
            Object serviceA = createBeanWithoutCache("serviceA"); // 递归调用 - 形成循环！
        }
        
        System.out.println("   完成创建Bean: " + beanName + " (递归深度: " + recursionDepth + ")");
        recursionDepth--;
        return bean;
    }
    
    /**
     * 演示真正的栈溢出（谨慎使用，会导致JVM崩溃）
     */
    public Object createBeanWithRealStackOverflow(String beanName) {
        System.out.println("正在创建Bean: " + beanName);
        
        // 无限递归 - 会导致真正的StackOverflowError
        if ("serviceA".equals(beanName)) {
            return createBeanWithRealStackOverflow("serviceB");
        } else {
            return createBeanWithRealStackOverflow("serviceA");
        }
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
 * 增强版循环依赖解决器 - 完整模拟Spring的三级缓存机制
 * 
 * 这个类详细演示了Spring三级缓存的工作原理，包括：
 * 1. 每级缓存的具体职责和工作时机
 * 2. 缓存间的数据流转和升级机制
 * 3. AOP代理与循环依赖的结合处理
 * 4. 完整的Bean生命周期管理
 */
class EnhancedCircularDependencyResolver {
    // 一级缓存：存储完全初始化的单例Bean实例 (成品仓库)
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
    
    // 二级缓存：存储早期暴露的Bean引用，可能是代理对象 (半成品仓库)
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(16);
    
    // 三级缓存：存储Bean的工厂函数，用于创建早期引用 (生产车间)
    private final Map<String, MockObjectFactory<?>> singletonFactories = new ConcurrentHashMap<>(16);
    
    // 创建时间线记录，用于分析缓存使用过程
    private final Map<String, String> creationTimeline = new ConcurrentHashMap<>();
    
    // 正在创建中的Bean集合，用于检测循环依赖
    private final java.util.Set<String> singletonsCurrentlyInCreation = ConcurrentHashMap.newKeySet();
    
    // 缓存操作统计
    private final Map<String, Integer> cacheHitStats = new ConcurrentHashMap<>();
    
    private int stepCounter = 0;
    
    public Object createBeanWithCircularDependency(String beanName) {
        logStep("开始创建Bean: " + beanName);
        
        // 标记Bean正在创建中
        singletonsCurrentlyInCreation.add(beanName);
        
        // 1. 实例化Bean（构造函数调用）
        MockServiceBean bean = createBeanInstance(beanName);
        logStep("实例化完成: " + beanName + " (实例ID: " + bean.hashCode() + ")");
        
        // 2. 立即将Bean工厂放入三级缓存（关键步骤）
        // 这是解决循环依赖的核心机制：在依赖注入前就暴露获取Bean的能力
        singletonFactories.put(beanName, () -> {
            logStep("🏭 从三级缓存工厂获取早期引用: " + beanName);
            logStep("   └─ 工厂方法被调用，开始创建早期Bean引用");
            // 这里可以进行AOP代理处理，确保循环依赖中注入的是正确的对象
            return getEarlyBeanReference(beanName, bean);
        });
        logStep("📦 Bean工厂已放入三级缓存: " + beanName);
        logStep("   └─ 时机：实例化完成后立即放入，在依赖注入之前");
        logStep("   └─ 目的：提供早期引用获取能力，支持循环依赖解决");
        
        // 3. 模拟依赖注入过程（可能触发循环依赖）
        populateBean(beanName, bean);
        
        // 4. 完成初始化回调
        initializeBean(beanName, bean);
        
        // 5. 将完全初始化的Bean放入一级缓存
        singletonObjects.put(beanName, bean);
        earlySingletonObjects.remove(beanName);
        singletonFactories.remove(beanName);
        singletonsCurrentlyInCreation.remove(beanName);
        
        logStep("Bean完全初始化并放入一级缓存: " + beanName);
        
        return bean;
    }
    
    private MockServiceBean createBeanInstance(String beanName) {
        return new MockServiceBean(beanName);
    }
    
    private Object getEarlyBeanReference(String beanName, Object bean) {
        // 模拟AOP代理创建逻辑
        if (needsProxy(beanName)) {
            logStep("为Bean创建AOP代理: " + beanName);
            return new MockProxyBean(bean, beanName);
        }
        return bean;
    }
    
    private boolean needsProxy(String beanName) {
        // 模拟某些Bean需要代理
        return beanName.equals("serviceA") || beanName.equals("serviceC");
    }
    
    private void populateBean(String beanName, MockServiceBean bean) {
        logStep("开始依赖注入: " + beanName);
        
        // 模拟复杂的循环依赖关系
        if ("serviceA".equals(beanName)) {
            // serviceA依赖serviceB
            Object serviceB = getSingleton("serviceB", true);
            if (serviceB == null) {
                serviceB = createBeanWithCircularDependency("serviceB");
            }
            bean.setDependency("serviceB", serviceB);
            logStep("serviceA注入依赖serviceB完成");
            
        } else if ("serviceB".equals(beanName)) {
            // serviceB依赖serviceC
            Object serviceC = getSingleton("serviceC", true);
            if (serviceC == null) {
                serviceC = createBeanWithCircularDependency("serviceC");
            }
            bean.setDependency("serviceC", serviceC);
            logStep("serviceB注入依赖serviceC完成");
            
        } else if ("serviceC".equals(beanName)) {
            // serviceC依赖serviceA（形成循环）
            Object serviceA = getSingleton("serviceA", true);
            if (serviceA == null) {
                // 这里不会再次创建，而是从缓存获取
                serviceA = getSingleton("serviceA", true);
            }
            bean.setDependency("serviceA", serviceA);
            logStep("serviceC注入依赖serviceA完成（循环依赖解决）");
        }
    }
    
    private void initializeBean(String beanName, MockServiceBean bean) {
        logStep("执行初始化回调: " + beanName);
        bean.afterPropertiesSet();
    }
    
    /**
     * 核心方法：模拟Spring的getSingleton方法
     * 这是三级缓存机制的核心实现，体现了分层查找和缓存升级的完整逻辑
     */
    protected Object getSingleton(String beanName, boolean allowEarlyReference) {
        logStep("🔍 尝试获取单例Bean: " + beanName);
        recordCacheAccess("总访问次数");
        
        // =================== 第一级缓存查找 ===================
        // 职责：查找完全初始化的Bean实例
        // 特点：性能最优，直接返回可用的Bean
        Object singletonObject = singletonObjects.get(beanName);
        if (singletonObject != null) {
            logStep("✅ 【一级缓存命中】从成品仓库获取完整Bean: " + beanName);
            recordCacheAccess("一级缓存命中");
            logStep("   └─ Bean状态：完全初始化，可直接使用");
            logStep("   └─ 性能：最优，无需额外处理");
            return singletonObject;
        }
        
        logStep("❌ 一级缓存未命中: " + beanName);
        recordCacheAccess("一级缓存未命中");
        
        // =================== 循环依赖检测 ===================
        // 只有在Bean正在创建中时，才需要查找二三级缓存
        // 这是循环依赖的关键判断条件
        if (singletonsCurrentlyInCreation.contains(beanName)) {
            logStep("🔄 检测到循环依赖场景，Bean正在创建中: " + beanName);
            logStep("   └─ 开始查找早期引用以打破循环依赖");
            
            // 使用synchronized确保缓存操作的线程安全
            synchronized (this.singletonObjects) {
                
                // =================== 双重检查锁定 ===================
                // 防止在获取锁的过程中，其他线程已经将Bean放入一级缓存
                singletonObject = this.singletonObjects.get(beanName);
                if (singletonObject != null) {
                    logStep("✅ 【双重检查】其他线程已完成Bean创建: " + beanName);
                    return singletonObject;
                }
                
                // =================== 第二级缓存查找 ===================
                // 职责：查找早期暴露的Bean引用（可能是代理对象）
                // 特点：Bean实例已创建，但可能还在初始化过程中
                singletonObject = this.earlySingletonObjects.get(beanName);
                if (singletonObject != null) {
                    logStep("✅ 【二级缓存命中】从半成品仓库获取早期引用: " + beanName);
                    recordCacheAccess("二级缓存命中");
                    logStep("   └─ Bean状态：实例已创建，可能正在依赖注入");
                    logStep("   └─ 引用类型：" + (singletonObject.getClass().getSimpleName().contains("Proxy") 
                           ? "AOP代理对象" : "原始对象"));
                    logStep("   └─ 性能：良好，避免重复调用工厂方法");
                    return singletonObject;
                }
                
                logStep("❌ 二级缓存未命中: " + beanName);
                recordCacheAccess("二级缓存未命中");
                
                // =================== 第三级缓存查找 ===================
                // 职责：通过工厂方法创建早期Bean引用
                // 特点：延迟执行，支持AOP代理的动态创建
                if (allowEarlyReference) {
                    MockObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                    if (singletonFactory != null) {
                        logStep("✅ 【三级缓存命中】从生产车间获取Bean工厂: " + beanName);
                        recordCacheAccess("三级缓存命中");
                        logStep("   └─ 开始调用工厂方法创建早期引用");
                        
                        // 调用工厂方法，可能创建代理对象
                        singletonObject = singletonFactory.getObject();
                        
                        // =================== 关键的缓存升级操作 ===================
                        // 将Bean从三级缓存升级到二级缓存
                        // 目的：
                        // 1. 提高性能：避免重复调用工厂方法
                        // 2. 保证一致性：确保多次获取返回同一个对象引用
                        // 3. 支持AOP：维护代理对象的单例特性
                        this.earlySingletonObjects.put(beanName, singletonObject);
                        this.singletonFactories.remove(beanName);
                        
                        logStep("🔄 【缓存升级】Bean从三级缓存升级到二级缓存: " + beanName);
                        recordCacheAccess("缓存升级操作");
                        logStep("   └─ 升级原因：提高后续访问性能");
                        logStep("   └─ 升级结果：" + (singletonObject.getClass().getSimpleName().contains("Proxy") 
                               ? "AOP代理对象已缓存" : "原始对象已缓存"));
                        logStep("   └─ 三级缓存清理：工厂方法已移除");
                        
                        return singletonObject;
                    }
                    
                    logStep("❌ 三级缓存未命中: " + beanName);
                    recordCacheAccess("三级缓存未命中");
                } else {
                    logStep("⚠️  不允许早期引用，跳过三级缓存: " + beanName);
                    recordCacheAccess("跳过三级缓存");
                }
            }
        } else {
            logStep("ℹ️  Bean未在创建中，无需查找早期引用: " + beanName);
            recordCacheAccess("无循环依赖场景");
        }
        
        logStep("❌ 所有缓存级别均未命中: " + beanName);
        recordCacheAccess("完全未命中");
        return null;
    }
    
    /**
     * 记录缓存访问统计信息
     */
    private void recordCacheAccess(String operation) {
        cacheHitStats.merge(operation, 1, Integer::sum);
    }
    
    private void logStep(String message) {
        stepCounter++;
        String step = String.format("步骤%02d", stepCounter);
        System.out.println("   " + step + ": " + message);
        creationTimeline.put(step, message);
    }
    
    public void printDetailedCacheStatus() {
        System.out.println("   =================== 三级缓存最终状态分析 ===================");
        
        System.out.println("   【一级缓存 - 成品仓库】(singletonObjects): " + singletonObjects.size() + " 个Bean");
        System.out.println("   ├─ 职责：存储完全初始化的Bean实例");
        System.out.println("   ├─ 特点：生命周期完整，可直接使用");
        singletonObjects.forEach((name, bean) -> {
            String beanType = bean.getClass().getSimpleName();
            boolean isProxy = beanType.contains("Proxy");
            System.out.println("   │   └─ " + name + ": " + beanType + "@" + bean.hashCode() 
                             + (isProxy ? " [AOP代理]" : " [原始对象]"));
        });
        
        System.out.println("\n   【二级缓存 - 半成品仓库】(earlySingletonObjects): " + earlySingletonObjects.size() + " 个Bean");
        System.out.println("   ├─ 职责：存储早期暴露的Bean引用");
        System.out.println("   ├─ 特点：实例已创建，可能在依赖注入中");
        if (earlySingletonObjects.isEmpty()) {
            System.out.println("   │   └─ (空) 所有Bean已完成创建并升级到一级缓存");
        } else {
            earlySingletonObjects.forEach((name, bean) -> {
                String beanType = bean.getClass().getSimpleName();
                boolean isProxy = beanType.contains("Proxy");
                System.out.println("   │   └─ " + name + ": " + beanType + "@" + bean.hashCode()
                                 + (isProxy ? " [AOP代理]" : " [原始对象]"));
            });
        }
        
        System.out.println("\n   【三级缓存 - 生产车间】(singletonFactories): " + singletonFactories.size() + " 个工厂");
        System.out.println("   ├─ 职责：存储Bean工厂函数，支持延迟创建");
        System.out.println("   ├─ 特点：按需执行，支持AOP代理动态创建");
        if (singletonFactories.isEmpty()) {
            System.out.println("   │   └─ (空) 所有工厂已被调用并升级到二级缓存");
        } else {
            singletonFactories.forEach((name, factory) -> {
                System.out.println("   │   └─ " + name + ": " + factory.getClass().getSimpleName());
            });
        }
        
        System.out.println("\n   【创建状态】正在创建中的Bean: " + 
                         (singletonsCurrentlyInCreation.isEmpty() ? "(无)" : singletonsCurrentlyInCreation));
        
        System.out.println("\n   =================== 缓存性能统计 ===================");
        cacheHitStats.forEach((operation, count) -> {
            System.out.println("   " + operation + ": " + count + " 次");
        });
        
        // 计算缓存命中率
        int totalAccess = cacheHitStats.getOrDefault("总访问次数", 0);
        int level1Hits = cacheHitStats.getOrDefault("一级缓存命中", 0);
        int level2Hits = cacheHitStats.getOrDefault("二级缓存命中", 0);
        int level3Hits = cacheHitStats.getOrDefault("三级缓存命中", 0);
        
        if (totalAccess > 0) {
            System.out.printf("   总体命中率: %.1f%% (%d/%d)\n", 
                            (level1Hits + level2Hits + level3Hits) * 100.0 / totalAccess,
                            level1Hits + level2Hits + level3Hits, totalAccess);
            System.out.printf("   一级缓存命中率: %.1f%% (%d/%d)\n", 
                            level1Hits * 100.0 / totalAccess, level1Hits, totalAccess);
        }
    }
    
    public void printCreationTimeline() {
        creationTimeline.forEach((step, message) -> {
            System.out.println("   " + step + ": " + message);
        });
    }
}

/**
 * 模拟业务服务Bean
 */
class MockServiceBean {
    private final String name;
    private final Map<String, Object> dependencies = new ConcurrentHashMap<>();
    private boolean initialized = false;
    
    public MockServiceBean(String name) {
        this.name = name;
    }
    
    public void setDependency(String depName, Object dependency) {
        dependencies.put(depName, dependency);
    }
    
    public void afterPropertiesSet() {
        initialized = true;
    }
    
    public String getName() { return name; }
    public boolean isInitialized() { return initialized; }
    public Map<String, Object> getDependencies() { return dependencies; }
}

/**
 * 模拟AOP代理Bean
 */
class MockProxyBean {
    private final Object target;
    private final String targetName;
    
    public MockProxyBean(Object target, String targetName) {
        this.target = target;
        this.targetName = targetName;
    }
    
    public Object getTarget() { return target; }
    public String getTargetName() { return targetName; }
    
    @Override
    public String toString() {
        return "ProxyFor[" + targetName + "]@" + target.hashCode();
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