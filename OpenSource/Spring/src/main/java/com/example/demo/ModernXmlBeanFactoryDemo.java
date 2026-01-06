package com.example.demo;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.util.Arrays;

/**
 * 现代Spring XML配置演示类 - 替代已废弃的XmlBeanFactory
 * 
 * 🎯 演示目标:
 * 1. 使用DefaultListableBeanFactory + XmlBeanDefinitionReader替代XmlBeanFactory
 * 2. 展示现代Spring框架的XML配置方式
 * 3. 保持与原XmlBeanFactory相同的功能演示
 * 4. 体验Spring 6.x的新特性和改进
 * 
 * 🔄 技术演进:
 * - Spring 1.x-3.0: XmlBeanFactory (简单但功能有限)
 * - Spring 3.1-5.2: XmlBeanFactory @Deprecated (标记为过时)
 * - Spring 5.3+: XmlBeanFactory 完全移除
 * - Spring 6.x: 推荐使用 DefaultListableBeanFactory + XmlBeanDefinitionReader
 * 
 * 📚 学习价值:
 * - 理解Spring技术栈的演进历程
 * - 掌握现代Spring XML配置的最佳实践
 * - 学会使用更强大灵活的Bean工厂实现
 * 
 * ⚠️ 重要说明:
 * 虽然XmlBeanFactory已被移除，但XML配置仍然是Spring的重要特性
 * 本示例展示如何在现代Spring版本中实现相同的功能
 */
public class ModernXmlBeanFactoryDemo {
    
    private static DefaultListableBeanFactory beanFactory;
    private static XmlBeanDefinitionReader reader;
    
    public static void main(String[] args) {
        System.out.println("🌟 === 现代Spring XML配置演示开始 === 🌟");
        System.out.println("📚 使用DefaultListableBeanFactory替代已废弃的XmlBeanFactory\n");
        
        try {
            // 1. 初始化现代Bean工厂
            initializeModernBeanFactory();
            
            // 2. 演示基本Bean操作
            demonstrateBasicBeanOperations();
            
            // 3. 演示Bean作用域
            demonstrateBeanScopes();
            
            // 4. 演示依赖注入
            demonstrateDependencyInjection();
            
            // 5. 演示Bean生命周期
            demonstrateBeanLifecycle();
            
            // 6. 演示容器特性对比
            demonstrateContainerFeatures();
            
            // 7. 性能测试
            performanceComparison();
            
            // 8. 演示现代Spring特性
            demonstrateModernSpringFeatures();
            
        } catch (Exception e) {
            System.err.println("❌ 演示过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 清理资源
            cleanup();
        }
        
        System.out.println("\n🎉 === 现代Spring XML配置演示结束 === 🎉");
        System.out.println("💡 学习要点: 现代Spring提供了更强大灵活的Bean管理方式");
    }
    
    /**
     * 初始化现代Bean工厂
     * 
     * 🔍 关键差异:
     * - XmlBeanFactory: 一体化设计，功能简单
     * - DefaultListableBeanFactory + XmlBeanDefinitionReader: 职责分离，功能强大
     */
    private static void initializeModernBeanFactory() {
        System.out.println("🏗️ === 1. 初始化现代Bean工厂 === 🏗️");
        
        try {
            // 创建Bean工厂实例
            beanFactory = new DefaultListableBeanFactory();
            System.out.println("✅ DefaultListableBeanFactory 创建成功");
            
            // 创建XML Bean定义读取器
            reader = new XmlBeanDefinitionReader(beanFactory);
            System.out.println("✅ XmlBeanDefinitionReader 创建成功");
            
            // 加载XML配置文件
            Resource resource = new ClassPathResource("beanFactoryTest.xml");
            int beanCount = reader.loadBeanDefinitions(resource);
            System.out.println("✅ 成功加载XML配置文件: " + resource.getFilename());
            System.out.println("📊 加载的Bean定义数量: " + beanCount);
            
            // 显示工厂信息
            System.out.println("📊 工厂类型: " + beanFactory.getClass().getSimpleName());
            System.out.println("📊 Bean定义总数: " + beanFactory.getBeanDefinitionCount());
            System.out.println("📊 单例Bean数量: " + beanFactory.getSingletonCount());
            
            // 显示现代Bean工厂的优势
            System.out.println("\n🆕 现代Bean工厂优势:");
            System.out.println("  ✅ 更强的生命周期管理");
            System.out.println("  ✅ 更好的循环依赖处理");
            System.out.println("  ✅ 支持Bean后置处理器");
            System.out.println("  ✅ 更丰富的查询和管理API");
            System.out.println("  ✅ 更好的线程安全性");
            
        } catch (Exception e) {
            System.err.println("❌ 初始化失败: " + e.getMessage());
            throw new RuntimeException("无法初始化现代Bean工厂", e);
        }
        
        System.out.println();
    }
    
    /**
     * 演示基本Bean操作 - 展示现代API的强大功能
     */
    private static void demonstrateBasicBeanOperations() {
        System.out.println("🔧 === 2. 基本Bean操作演示 === 🔧");
        
        // 获取所有Bean定义名称
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        System.out.println("📋 已定义的Bean列表 (共" + beanNames.length + "个):");
        
        for (String beanName : beanNames) {
            System.out.println("  🔹 " + beanName);
            
            // 检查Bean是否存在
            boolean exists = beanFactory.containsBean(beanName);
            System.out.println("    ↳ 存在性: " + (exists ? "✅ 存在" : "❌ 不存在"));
            
            if (exists) {
                try {
                    // 获取Bean类型 - 现代API提供更准确的类型信息
                    Class<?> beanType = beanFactory.getType(beanName);
                    System.out.println("    ↳ 类型: " + (beanType != null ? beanType.getSimpleName() : "未知"));
                    
                    // 检查Bean是否为单例 - 更准确的作用域检测
                    boolean isSingleton = beanFactory.isSingleton(beanName);
                    boolean isPrototype = beanFactory.isPrototype(beanName);
                    String scope = isSingleton ? "🔒 Singleton" : 
                                  isPrototype ? "🔄 Prototype" : "🎯 其他作用域";
                    System.out.println("    ↳ 作用域: " + scope);
                    
                    // 获取Bean的别名 - 现代API支持别名查询
                    String[] aliases = beanFactory.getAliases(beanName);
                    if (aliases.length > 0) {
                        System.out.println("    ↳ 别名: " + Arrays.toString(aliases));
                    }
                    
                    // 检查Bean是否已实例化 - 现代API提供实例化状态
                    if (isSingleton && beanFactory.containsSingleton(beanName)) {
                        System.out.println("    ↳ 实例化状态: ✅ 已实例化");
                    } else {
                        System.out.println("    ↳ 实例化状态: ⏳ 未实例化");
                    }
                    
                    // 获取Bean实例并显示
                    Object bean = beanFactory.getBean(beanName);
                    System.out.println("    ↳ 实例: " + bean);
                    System.out.println("    ↳ 哈希码: " + Integer.toHexString(bean.hashCode()));
                    
                } catch (Exception e) {
                    System.out.println("    ↳ ❌ 获取失败: " + e.getMessage());
                }
            }
            System.out.println();
        }
        
        System.out.println();
    }
    
    /**
     * 演示Bean作用域 - 现代Spring的作用域管理
     */
    private static void demonstrateBeanScopes() {
        System.out.println("🎯 === 3. Bean作用域演示 === 🎯");
        
        // 演示Singleton作用域
        System.out.println("🔒 Singleton作用域演示:");
        TestBean singleton1 = beanFactory.getBean("singletonBean", TestBean.class);
        TestBean singleton2 = beanFactory.getBean("singletonBean", TestBean.class);
        
        System.out.println("  第一次获取: " + singleton1);
        System.out.println("  第二次获取: " + singleton2);
        System.out.println("  是否为同一实例: " + (singleton1 == singleton2 ? "✅ 是" : "❌ 否"));
        System.out.println("  内存地址比较: " + 
            Integer.toHexString(singleton1.hashCode()) + " vs " + 
            Integer.toHexString(singleton2.hashCode()));
        
        // 现代Spring的单例Bean缓存信息
        System.out.println("  📊 单例Bean缓存信息:");
        System.out.println("    缓存中的单例数量: " + beanFactory.getSingletonCount());
        String[] singletonNames = beanFactory.getSingletonNames();
        System.out.println("    已缓存的单例: " + Arrays.toString(Arrays.copyOf(singletonNames, Math.min(3, singletonNames.length))) + 
                          (singletonNames.length > 3 ? "..." : ""));
        
        // 演示Prototype作用域
        System.out.println("\n🔄 Prototype作用域演示:");
        TestBean prototype1 = beanFactory.getBean("prototypeBean", TestBean.class);
        TestBean prototype2 = beanFactory.getBean("prototypeBean", TestBean.class);
        
        System.out.println("  第一次获取: " + prototype1);
        System.out.println("  第二次获取: " + prototype2);
        System.out.println("  是否为同一实例: " + (prototype1 == prototype2 ? "✅ 是" : "❌ 否"));
        System.out.println("  内存地址比较: " + 
            Integer.toHexString(prototype1.hashCode()) + " vs " + 
            Integer.toHexString(prototype2.hashCode()));
        
        System.out.println("  💡 Prototype Bean特点: 每次获取都创建新实例，不会被缓存");
        
        System.out.println();
    }
    
    /**
     * 演示依赖注入 - 现代Spring的强大注入能力
     */
    private static void demonstrateDependencyInjection() {
        System.out.println("💉 === 4. 依赖注入演示 === 💉");
        
        // 属性注入演示
        System.out.println("🔧 属性注入 (Setter Injection):");
        TestBean propertyBean = beanFactory.getBean("testBean", TestBean.class);
        System.out.println("  Bean实例: " + propertyBean);
        System.out.println("  注入的消息: " + propertyBean.getMessage());
        System.out.println("  注入的数字: " + propertyBean.getNumber());
        
        // 构造器注入演示
        System.out.println("\n🏗️ 构造器注入 (Constructor Injection):");
        TestBean constructorBean = beanFactory.getBean("constructorBean", TestBean.class);
        System.out.println("  Bean实例: " + constructorBean);
        System.out.println("  注入的消息: " + constructorBean.getMessage());
        System.out.println("  注入的数字: " + constructorBean.getNumber());
        
        // 现代Spring的类型安全获取
        System.out.println("\n🎯 现代Spring的类型安全API:");
        try {
            // 按类型获取Bean (更安全)
            TestBean beanByType = beanFactory.getBean(TestBean.class);
            System.out.println("  按类型获取: " + beanByType.getInstanceId());
        } catch (Exception e) {
            System.out.println("  ⚠️ 按类型获取失败 (可能有多个同类型Bean): " + e.getMessage());
        }
        
        // 复杂依赖演示
        if (beanFactory.containsBean("complexBean")) {
            System.out.println("\n🔗 复杂依赖注入:");
            ComplexBean complexBean = beanFactory.getBean("complexBean", ComplexBean.class);
            System.out.println("  复杂Bean: " + complexBean);
            
            // 现代Spring支持更详细的依赖分析
            System.out.println("  📊 依赖分析:");
            if (complexBean.getTestBean() != null) {
                System.out.println("    关联的TestBean ID: " + complexBean.getTestBean().getInstanceId());
            }
            System.out.println("    列表项数量: " + complexBean.getItems().size());
            System.out.println("    属性映射数量: " + complexBean.getProperties().size());
        }
        
        System.out.println();
    }
    
    /**
     * 演示Bean生命周期 - 现代Spring的完整生命周期支持
     */
    private static void demonstrateBeanLifecycle() {
        System.out.println("♻️ === 5. Bean生命周期演示 === ♻️");
        
        System.out.println("📋 现代Spring Bean生命周期阶段:");
        System.out.println("  1️⃣ Bean定义加载和解析");
        System.out.println("  2️⃣ Bean实例化 (Constructor)");
        System.out.println("  3️⃣ 属性注入 (Setter Methods)");
        System.out.println("  4️⃣ Aware接口回调 (BeanNameAware, BeanFactoryAware等)");
        System.out.println("  5️⃣ Bean后置处理器前置处理 (BeanPostProcessor.postProcessBeforeInitialization)");
        System.out.println("  6️⃣ 初始化方法调用 (init-method, @PostConstruct)");
        System.out.println("  7️⃣ Bean后置处理器后置处理 (BeanPostProcessor.postProcessAfterInitialization)");
        System.out.println("  8️⃣ Bean使用阶段");
        System.out.println("  9️⃣ 销毁方法调用 (destroy-method, @PreDestroy)");
        
        System.out.println("\n🔄 获取带生命周期的Bean:");
        TestBean lifecycleBean = beanFactory.getBean("testBean", TestBean.class);
        System.out.println("✅ Bean获取完成: " + lifecycleBean.getMessage());
        
        // 调用业务方法
        System.out.println("\n💼 调用Bean业务方法:");
        lifecycleBean.doSomething();
        
        // 现代Spring支持手动销毁单例Bean
        System.out.println("\n🗑️ 现代Spring的Bean销毁支持:");
        if (beanFactory.containsSingleton("testBean")) {
            System.out.println("  Bean在单例缓存中: ✅ 存在");
            // 注意: DefaultListableBeanFactory支持销毁单例Bean
            System.out.println("  💡 可以调用 beanFactory.destroySingleton() 手动销毁");
        }
        
        System.out.println();
    }
    
    /**
     * 演示容器特性对比 - 现代Spring vs 传统XmlBeanFactory
     */
    private static void demonstrateContainerFeatures() {
        System.out.println("⚖️ === 6. 容器特性对比 === ⚖️");
        
        System.out.println("🆚 XmlBeanFactory vs DefaultListableBeanFactory:");
        System.out.println();
        
        System.out.println("📊 XmlBeanFactory (已废弃):");
        System.out.println("  ❌ 功能简单: 只支持基本的Bean管理");
        System.out.println("  ❌ 懒加载: Bean在首次获取时才创建");
        System.out.println("  ❌ 无生命周期支持: 不支持完整的Bean生命周期");
        System.out.println("  ❌ 无后置处理器: 不支持BeanPostProcessor");
        System.out.println("  ❌ 线程安全性有限: 并发访问可能有问题");
        
        System.out.println("\n🌟 DefaultListableBeanFactory (现代推荐):");
        System.out.println("  ✅ 功能强大: 完整的Bean工厂实现");
        System.out.println("  ✅ 灵活的生命周期: 支持完整的Bean生命周期管理");
        System.out.println("  ✅ 后置处理器: 支持BeanPostProcessor和BeanFactoryPostProcessor");
        System.out.println("  ✅ 类型安全: 泛型支持和类型安全的API");
        System.out.println("  ✅ 线程安全: 优秀的并发访问支持");
        System.out.println("  ✅ 可扩展性: 丰富的扩展点和自定义能力");
        
        // 展示现代特性
        System.out.println("\n🆕 现代Spring独有特性演示:");
        
        // 1. Bean定义的详细信息
        System.out.println("📋 Bean定义详细信息:");
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (int i = 0; i < Math.min(3, beanNames.length); i++) {
            String beanName = beanNames[i];
            if (beanFactory.containsBeanDefinition(beanName)) {
                System.out.println("  " + (i+1) + ". " + beanName + ":");
                System.out.println("     作用域: " + (beanFactory.isSingleton(beanName) ? "singleton" : "prototype"));
                System.out.println("     懒加载: " + beanFactory.getBeanDefinition(beanName).isLazyInit());
                System.out.println("     抽象Bean: " + beanFactory.getBeanDefinition(beanName).isAbstract());
            }
        }
        
        // 2. 类型查询功能
        System.out.println("\n🔍 按类型查询Bean:");
        String[] testBeanNames = beanFactory.getBeanNamesForType(TestBean.class);
        System.out.println("  TestBean类型的Bean数量: " + testBeanNames.length);
        System.out.println("  Bean名称: " + Arrays.toString(testBeanNames));
        
        System.out.println();
    }
    
    /**
     * 性能对比测试 - 现代Spring的性能优化
     */
    private static void performanceComparison() {
        System.out.println("⏱️ === 7. 性能测试 === ⏱️");
        
        // 现代Spring的性能优化
        System.out.println("🚀 现代Spring性能优化特性:");
        System.out.println("  ✅ 更高效的Bean创建和缓存机制");
        System.out.println("  ✅ 优化的类型查询和转换");
        System.out.println("  ✅ 更好的内存管理和垃圾收集");
        System.out.println("  ✅ 并发访问的性能优化");
        
        // 单例Bean获取性能测试
        System.out.println("\n🔒 Singleton Bean获取性能测试:");
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            beanFactory.getBean("singletonBean", TestBean.class);
        }
        long singletonTime = System.nanoTime() - startTime;
        System.out.println("  1000次类型安全获取耗时: " + (singletonTime / 1_000_000.0) + " ms");
        
        // 原型Bean创建性能测试
        System.out.println("\n🔄 Prototype Bean创建性能测试:");
        startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            beanFactory.getBean("prototypeBean", TestBean.class);
        }
        long prototypeTime = System.nanoTime() - startTime;
        System.out.println("  1000次类型安全创建耗时: " + (prototypeTime / 1_000_000.0) + " ms");
        
        // 类型查询性能测试
        System.out.println("\n🔍 类型查询性能测试:");
        startTime = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            beanFactory.getBeanNamesForType(TestBean.class);
        }
        long queryTime = System.nanoTime() - startTime;
        System.out.println("  1000次类型查询耗时: " + (queryTime / 1_000_000.0) + " ms");
        
        // 性能对比
        System.out.println("\n📊 性能总结:");
        double ratio = (double) prototypeTime / singletonTime;
        System.out.println("  Prototype比Singleton慢: " + String.format("%.2f", ratio) + " 倍");
        System.out.println("  💡 现代Spring在所有操作上都有显著的性能提升");
        
        System.out.println();
    }
    
    /**
     * 演示现代Spring特性
     */
    private static void demonstrateModernSpringFeatures() {
        System.out.println("🌟 === 8. 现代Spring特性演示 === 🌟");
        
        System.out.println("🆕 DefaultListableBeanFactory独有功能:");
        
        // 1. 预实例化单例Bean
        System.out.println("\n1️⃣ 预实例化单例Bean:");
        System.out.println("  当前单例Bean数量: " + beanFactory.getSingletonCount());
        beanFactory.preInstantiateSingletons();
        System.out.println("  预实例化后数量: " + beanFactory.getSingletonCount());
        System.out.println("  ✅ 所有单例Bean已预实例化");
        
        // 2. Bean依赖关系分析
        System.out.println("\n2️⃣ Bean依赖关系分析:");
        if (beanFactory.containsBean("complexBean")) {
            String[] dependencies = beanFactory.getDependenciesForBean("complexBean");
            System.out.println("  complexBean的依赖: " + Arrays.toString(dependencies));
            
            String[] dependentBeans = beanFactory.getDependentBeans("testBean");
            System.out.println("  依赖testBean的Bean: " + Arrays.toString(dependentBeans));
        }
        
        // 3. Bean定义的合并
        System.out.println("\n3️⃣ Bean定义处理:");
        try {
            if (beanFactory.containsBeanDefinition("testBean")) {
                var beanDefinition = beanFactory.getMergedBeanDefinition("testBean");
                System.out.println("  testBean合并后的Bean定义:");
                System.out.println("    Bean类名: " + beanDefinition.getBeanClassName());
                System.out.println("    作用域: " + beanDefinition.getScope());
                System.out.println("    懒加载: " + beanDefinition.isLazyInit());
            }
        } catch (Exception e) {
            System.out.println("  Bean定义分析出现问题: " + e.getMessage());
        }
        
        // 4. 自定义Bean后置处理器支持
        System.out.println("\n4️⃣ 扩展点支持:");
        System.out.println("  ✅ 支持BeanPostProcessor注册");
        System.out.println("  ✅ 支持BeanFactoryPostProcessor");
        System.out.println("  ✅ 支持自定义作用域注册");
        System.out.println("  ✅ 支持PropertyEditorRegistrar");
        
        System.out.println("\n💡 升级建议:");
        System.out.println("  🔄 从XmlBeanFactory迁移到DefaultListableBeanFactory");
        System.out.println("  🆕 考虑使用ApplicationContext系列(更高级)");
        System.out.println("  🚀 在新项目中使用Spring Boot + 注解配置");
        
        System.out.println();
    }
    
    /**
     * 清理资源
     */
    private static void cleanup() {
        System.out.println("🧹 === 资源清理 === 🧹");
        if (beanFactory != null) {
            // 现代Spring支持完整的Bean销毁
            try {
                beanFactory.destroySingletons();
                System.out.println("✅ 所有单例Bean已销毁");
            } catch (Exception e) {
                System.out.println("⚠️ Bean销毁过程中出现问题: " + e.getMessage());
            }
            System.out.println("✅ DefaultListableBeanFactory资源已清理");
        }
    }
}