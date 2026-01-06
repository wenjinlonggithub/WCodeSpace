package com.example.demo;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

/**
 * 现代Spring XML配置测试运行器
 * 
 * 🎯 测试目标:
 * 1. 验证DefaultListableBeanFactory + XmlBeanDefinitionReader的功能
 * 2. 对比传统XmlBeanFactory与现代实现的差异
 * 3. 展示现代Spring的强大功能和性能优化
 * 4. 提供完整的迁移指南和最佳实践
 * 
 * 📈 技术演进:
 * XmlBeanFactory (废弃) → DefaultListableBeanFactory + XmlBeanDefinitionReader (现代)
 * 
 * 🏃‍♂️ 运行方式:
 * java -cp target/classes com.example.demo.ModernXmlBeanFactoryTestRunner
 */
public class ModernXmlBeanFactoryTestRunner {
    
    public static void main(String[] args) {
        System.out.println("🚀 === 现代Spring XML配置完整测试开始 === 🚀");
        System.out.println("🎓 验证DefaultListableBeanFactory的强大功能\n");
        
        try {
            // 1. 基本功能测试
            testBasicModernFeatures();
            
            // 2. 高级功能测试
            testAdvancedModernFeatures();
            
            // 3. 循环依赖解决测试
            testCircularDependencyWithModernFactory();
            
            // 4. 性能对比测试
            performModernPerformanceTests();
            
            // 5. 迁移指南演示
            demonstrateMigrationGuide();
            
            System.out.println("🎉 === 现代Spring测试完成 === 🎉");
            System.out.println("💡 总结: 现代Spring提供了更强大、更灵活、更高效的Bean管理能力");
            
        } catch (Exception e) {
            System.err.println("❌ 测试过程中发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试现代Spring的基本功能
     */
    private static void testBasicModernFeatures() {
        System.out.println("🔧 === 1. 现代Spring基本功能测试 === 🔧");
        
        try {
            // 创建现代Bean工厂
            DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
            
            // 加载配置
            int beanCount = reader.loadBeanDefinitions(new ClassPathResource("beanFactoryTest.xml"));
            System.out.println("✅ 成功加载 " + beanCount + " 个Bean定义");
            
            // 基本Bean操作测试
            TestBean testBean = factory.getBean("testBean", TestBean.class);
            System.out.println("✅ 类型安全Bean获取: " + testBean.getMessage());
            
            // 作用域测试
            TestBean singleton1 = factory.getBean("singletonBean", TestBean.class);
            TestBean singleton2 = factory.getBean("singletonBean", TestBean.class);
            System.out.println("✅ Singleton测试: " + (singleton1 == singleton2 ? "通过" : "失败"));
            
            TestBean prototype1 = factory.getBean("prototypeBean", TestBean.class);
            TestBean prototype2 = factory.getBean("prototypeBean", TestBean.class);
            System.out.println("✅ Prototype测试: " + (prototype1 != prototype2 ? "通过" : "失败"));
            
            // 清理
            factory.destroySingletons();
            System.out.println("✅ Bean清理完成");
            
        } catch (Exception e) {
            System.err.println("❌ 基本功能测试失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * 测试现代Spring的高级功能
     */
    private static void testAdvancedModernFeatures() {
        System.out.println("🌟 === 2. 现代Spring高级功能测试 === 🌟");
        
        try {
            DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
            reader.loadBeanDefinitions(new ClassPathResource("beanFactoryTest.xml"));
            
            // 1. 按类型查询所有Bean
            System.out.println("🔍 按类型查询功能:");
            String[] testBeanNames = factory.getBeanNamesForType(TestBean.class);
            System.out.println("  TestBean类型的Bean: " + java.util.Arrays.toString(testBeanNames));
            
            // 2. Bean依赖关系分析
            System.out.println("\n🔗 依赖关系分析:");
            if (factory.containsBean("complexBean")) {
                String[] dependencies = factory.getDependenciesForBean("complexBean");
                System.out.println("  complexBean的依赖: " + java.util.Arrays.toString(dependencies));
            }
            
            // 3. Bean定义详情
            System.out.println("\n📋 Bean定义详情:");
            if (factory.containsBeanDefinition("testBean")) {
                var beanDef = factory.getBeanDefinition("testBean");
                System.out.println("  testBean类名: " + beanDef.getBeanClassName());
                System.out.println("  作用域: " + beanDef.getScope());
                System.out.println("  懒加载: " + beanDef.isLazyInit());
            }
            
            // 4. 预实例化功能
            System.out.println("\n⚡ 预实例化功能:");
            System.out.println("  预实例化前单例数量: " + factory.getSingletonCount());
            factory.preInstantiateSingletons();
            System.out.println("  预实例化后单例数量: " + factory.getSingletonCount());
            
            // 5. 别名支持
            System.out.println("\n🏷️ 别名支持:");
            String[] aliases = factory.getAliases("testBean");
            System.out.println("  testBean的别名: " + java.util.Arrays.toString(aliases));
            
            // 清理
            factory.destroySingletons();
            System.out.println("✅ 高级功能测试通过");
            
        } catch (Exception e) {
            System.err.println("❌ 高级功能测试失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * 测试现代Spring的循环依赖处理
     */
    private static void testCircularDependencyWithModernFactory() {
        System.out.println("🔄 === 3. 现代Spring循环依赖测试 === 🔄");
        
        try {
            DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
            reader.loadBeanDefinitions(new ClassPathResource("circular-dependency-test.xml"));
            
            System.out.println("🧪 测试循环依赖解决:");
            
            // 获取循环依赖的Bean
            CircularDependencyDemo.ServiceA serviceA = 
                factory.getBean("serviceA", CircularDependencyDemo.ServiceA.class);
            CircularDependencyDemo.ServiceB serviceB = 
                factory.getBean("serviceB", CircularDependencyDemo.ServiceB.class);
            
            // 验证循环依赖
            boolean aHasB = serviceA.getServiceB() == serviceB;
            boolean bHasA = serviceB.getServiceA() == serviceA;
            
            System.out.println("✅ 循环依赖解决测试:");
            System.out.println("  ServiceA -> ServiceB: " + (aHasB ? "✅ 正确" : "❌ 错误"));
            System.out.println("  ServiceB -> ServiceA: " + (bHasA ? "✅ 正确" : "❌ 错误"));
            
            if (aHasB && bHasA) {
                System.out.println("🎉 现代Spring成功解决了循环依赖问题!");
                
                // 测试业务逻辑
                System.out.println("\n💼 业务逻辑测试:");
                serviceA.doWork();
                serviceB.process();
            }
            
            // 清理
            factory.destroySingletons();
            
        } catch (Exception e) {
            System.err.println("❌ 循环依赖测试失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println();
    }
    
    /**
     * 现代Spring性能测试
     */
    private static void performModernPerformanceTests() {
        System.out.println("⚡ === 4. 现代Spring性能测试 === ⚡");
        
        try {
            DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
            reader.loadBeanDefinitions(new ClassPathResource("beanFactoryTest.xml"));
            
            // 预热
            for (int i = 0; i < 100; i++) {
                factory.getBean("singletonBean", TestBean.class);
                factory.getBean("prototypeBean", TestBean.class);
            }
            
            System.out.println("🏎️ 性能基准测试:");
            
            // 1. 类型安全获取性能
            long start = System.nanoTime();
            for (int i = 0; i < 10000; i++) {
                factory.getBean("singletonBean", TestBean.class);
            }
            long typeSefeTime = System.nanoTime() - start;
            System.out.printf("  类型安全获取 (10000次): %.2f ms%n", typeSefeTime / 1_000_000.0);
            
            // 2. 按名称获取性能
            start = System.nanoTime();
            for (int i = 0; i < 10000; i++) {
                factory.getBean("singletonBean");
            }
            long nameTime = System.nanoTime() - start;
            System.out.printf("  按名称获取 (10000次): %.2f ms%n", nameTime / 1_000_000.0);
            
            // 3. 类型查询性能
            start = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                factory.getBeanNamesForType(TestBean.class);
            }
            long queryTime = System.nanoTime() - start;
            System.out.printf("  类型查询 (1000次): %.2f ms%n", queryTime / 1_000_000.0);
            
            // 4. Prototype创建性能
            start = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                factory.getBean("prototypeBean", TestBean.class);
            }
            long prototypeTime = System.nanoTime() - start;
            System.out.printf("  Prototype创建 (1000次): %.2f ms%n", prototypeTime / 1_000_000.0);
            
            System.out.println("\n📊 性能总结:");
            System.out.printf("  类型安全 vs 按名称: %.1fx%n", (double) typeSefeTime / nameTime);
            System.out.println("  💡 现代Spring在类型安全性和性能之间取得了良好平衡");
            
            // 清理
            factory.destroySingletons();
            
        } catch (Exception e) {
            System.err.println("❌ 性能测试失败: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 迁移指南演示
     */
    private static void demonstrateMigrationGuide() {
        System.out.println("📖 === 5. XmlBeanFactory 迁移指南 === 📖");
        
        System.out.println("🔄 从XmlBeanFactory迁移到现代Spring:");
        System.out.println();
        
        System.out.println("❌ 旧代码 (XmlBeanFactory - 已废弃):");
        System.out.println("```java");
        System.out.println("// 这段代码在Spring 6.x中无法运行!");
        System.out.println("Resource resource = new ClassPathResource(\"config.xml\");");
        System.out.println("XmlBeanFactory factory = new XmlBeanFactory(resource); // ❌ 类不存在");
        System.out.println("Object bean = factory.getBean(\"beanName\");");
        System.out.println("```");
        
        System.out.println("\n✅ 新代码 (DefaultListableBeanFactory - 推荐):");
        System.out.println("```java");
        System.out.println("// 现代Spring的正确方式");
        System.out.println("DefaultListableBeanFactory factory = new DefaultListableBeanFactory();");
        System.out.println("XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);");
        System.out.println("reader.loadBeanDefinitions(new ClassPathResource(\"config.xml\"));");
        System.out.println("MyBean bean = factory.getBean(\"beanName\", MyBean.class); // 类型安全");
        System.out.println("```");
        
        System.out.println("\n🌟 迁移优势:");
        System.out.println("  ✅ 更强大的功能: 完整的Bean生命周期支持");
        System.out.println("  ✅ 更好的性能: 优化的缓存和查询机制");
        System.out.println("  ✅ 类型安全: 泛型支持，减少ClassCastException");
        System.out.println("  ✅ 更好的扩展性: 支持BeanPostProcessor等扩展点");
        System.out.println("  ✅ 现代Spring兼容: 与Spring 6.x完全兼容");
        
        System.out.println("\n🚀 进一步升级建议:");
        System.out.println("  1️⃣ 考虑使用 ApplicationContext (更高级的容器)");
        System.out.println("  2️⃣ 迁移到 Spring Boot + 注解配置 (推荐)");
        System.out.println("  3️⃣ 使用 Java Configuration (@Configuration类)");
        System.out.println("  4️⃣ 利用 Spring Boot Starter 简化依赖管理");
        
        System.out.println("\n📝 迁移检查清单:");
        System.out.println("  □ 将XmlBeanFactory替换为DefaultListableBeanFactory + XmlBeanDefinitionReader");
        System.out.println("  □ 更新Bean获取代码以使用类型安全的API");
        System.out.println("  □ 添加适当的资源清理代码 (factory.destroySingletons())");
        System.out.println("  □ 测试所有Bean的创建和依赖注入");
        System.out.println("  □ 验证Bean的生命周期方法正常工作");
        
        System.out.println("\n🎯 最佳实践:");
        System.out.println("  💡 在学习阶段: 使用DefaultListableBeanFactory理解核心原理");
        System.out.println("  🏭 在生产环境: 优先使用ApplicationContext或Spring Boot");
        System.out.println("  🔬 在测试环境: 考虑使用Spring Test Context Framework");
        
        System.out.println();
    }
}