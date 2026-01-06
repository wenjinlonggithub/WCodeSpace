package com.example.springdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 现代Spring Boot演示应用
 * 
 * 🎯 演示目标:
 * 1. Spring Boot 3.x的核心特性
 * 2. 注解驱动的配置方式
 * 3. 自动配置和起步依赖
 * 4. 现代Spring的最佳实践
 * 
 * 🆕 Spring 6.x新特性:
 * - 基于JDK 17的原生支持
 * - AOT(Ahead-of-Time)编译优化
 * - Native Image支持
 * - 改进的反射和代理机制
 * - 更好的观测性支持
 * 
 * 📚 学习价值:
 * - 理解现代Spring开发模式
 * - 掌握Spring Boot的自动配置原理
 * - 体验注解配置的便利性
 * - 学习微服务架构的基础
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class ModernSpringDemoApplication {

    public static void main(String[] args) {
        System.out.println("🚀 === 现代Spring Boot 3.x演示启动 === 🚀");
        System.out.println("🌟 展示Spring 6.x的强大功能和现代化特性\n");
        
        // 启动Spring Boot应用
        ApplicationContext context = SpringApplication.run(ModernSpringDemoApplication.class, args);
        
        // 演示现代Spring特性
        demonstrateModernSpringFeatures(context);
        
        System.out.println("\n🎉 === 现代Spring Boot演示完成 === 🎉");
        System.out.println("💡 现代Spring提供了更简洁、更高效、更智能的开发体验");
        
        // 优雅关闭
        SpringApplication.exit(context, () -> 0);
    }
    
    /**
     * 演示现代Spring的核心特性
     */
    private static void demonstrateModernSpringFeatures(ApplicationContext context) {
        System.out.println("📊 === 现代Spring特性演示 === 📊\n");
        
        // 1. 显示应用信息
        displayApplicationInfo(context);
        
        // 2. Bean管理演示
        demonstrateBeanManagement(context);
        
        // 3. 自动配置演示
        demonstrateAutoConfiguration(context);
        
        // 4. 现代特性对比
        compareWithLegacyApproach();
    }
    
    /**
     * 显示应用基本信息
     */
    private static void displayApplicationInfo(ApplicationContext context) {
        System.out.println("🏗️ === 1. Spring Boot应用信息 === 🏗️");
        
        System.out.println("✅ 应用类型: " + context.getClass().getSimpleName());
        System.out.println("✅ Bean定义数量: " + context.getBeanDefinitionCount());
        System.out.println("✅ 启动时间: " + context.getStartupDate());
        System.out.println("✅ 应用ID: " + context.getId());
        System.out.println("✅ 显示名称: " + context.getDisplayName());
        
        // 显示激活的配置文件
        String[] activeProfiles = context.getEnvironment().getActiveProfiles();
        if (activeProfiles.length > 0) {
            System.out.println("✅ 激活配置: " + java.util.Arrays.toString(activeProfiles));
        } else {
            System.out.println("✅ 默认配置: default profile");
        }
        
        System.out.println();
    }
    
    /**
     * Bean管理演示
     */
    private static void demonstrateBeanManagement(ApplicationContext context) {
        System.out.println("🔧 === 2. 现代Bean管理 === 🔧");
        
        try {
            // 获取自定义Bean(如果存在)
            if (context.containsBean("demoService")) {
                Object demoService = context.getBean("demoService");
                System.out.println("✅ 自定义Bean: " + demoService.getClass().getSimpleName());
            }
            
            // 显示一些核心Bean
            String[] importantBeans = {
                "environment", 
                "applicationEventMulticaster",
                "lifecycleProcessor"
            };
            
            System.out.println("📋 核心Spring Boot Bean:");
            for (String beanName : importantBeans) {
                if (context.containsBean(beanName)) {
                    Object bean = context.getBean(beanName);
                    System.out.println("  🔹 " + beanName + ": " + bean.getClass().getSimpleName());
                }
            }
            
            // 按类型查询Bean
            System.out.println("\n🔍 按类型查询Bean:");
            try {
                String[] stringBeans = context.getBeanNamesForType(String.class);
                System.out.println("  String类型Bean数量: " + stringBeans.length);
            } catch (Exception e) {
                System.out.println("  String类型Bean: 查询时发生异常");
            }
            
        } catch (Exception e) {
            System.out.println("❌ Bean管理演示失败: " + e.getMessage());
        }
        
        System.out.println();
    }
    
    /**
     * 自动配置演示
     */
    private static void demonstrateAutoConfiguration(ApplicationContext context) {
        System.out.println("⚙️ === 3. Spring Boot自动配置 === ⚙️");
        
        System.out.println("🌟 Spring Boot自动配置的优势:");
        System.out.println("  ✅ 零配置启动: 无需XML配置文件");
        System.out.println("  ✅ 智能默认值: 基于类路径自动配置");
        System.out.println("  ✅ 条件化配置: @ConditionalOnClass等注解");
        System.out.println("  ✅ 外部化配置: application.properties/yml");
        System.out.println("  ✅ 生产就绪: 内置健康检查、指标监控");
        
        // 检查一些常见的自动配置
        System.out.println("\n📦 自动配置检查:");
        String[] autoConfigBeans = {
            "dataSourceScriptDatabaseInitializer",
            "jacksonObjectMapper",
            "restTemplateBuilder",
            "taskExecutor"
        };
        
        for (String beanName : autoConfigBeans) {
            boolean exists = context.containsBean(beanName);
            System.out.println("  " + (exists ? "✅" : "❌") + " " + beanName + 
                            (exists ? " (已自动配置)" : " (未配置)"));
        }
        
        System.out.println();
    }
    
    /**
     * 与传统方式对比
     */
    private static void compareWithLegacyApproach() {
        System.out.println("⚖️ === 4. 现代Spring vs 传统方式 === ⚖️");
        
        System.out.println("❌ 传统Spring开发 (已过时):");
        System.out.println("  - 大量XML配置文件");
        System.out.println("  - 手动管理依赖关系");
        System.out.println("  - 复杂的容器初始化");
        System.out.println("  - 使用XmlBeanFactory等底层API");
        System.out.println("  - 繁琐的测试配置");
        
        System.out.println("\n✅ 现代Spring Boot 3.x:");
        System.out.println("  - 注解驱动配置 (@SpringBootApplication)");
        System.out.println("  - 自动依赖管理 (Starter依赖)");
        System.out.println("  - 嵌入式服务器 (Tomcat/Netty)");
        System.out.println("  - 开箱即用功能 (健康检查、指标)");
        System.out.println("  - 简化的测试支持 (@SpringBootTest)");
        System.out.println("  - 云原生支持 (Docker/Kubernetes)");
        System.out.println("  - GraalVM Native Image支持");
        
        System.out.println("\n🚀 迁移建议:");
        System.out.println("  1️⃣ 使用Spring Boot代替传统Spring");
        System.out.println("  2️⃣ 用@Configuration类替代XML配置");
        System.out.println("  3️⃣ 使用ApplicationContext替代BeanFactory");
        System.out.println("  4️⃣ 采用微服务架构和容器化部署");
        System.out.println("  5️⃣ 集成Spring Cloud实现分布式系统");
        
        System.out.println("\n💡 最佳实践:");
        System.out.println("  🎯 开发环境: 使用Spring Boot DevTools热重载");
        System.out.println("  🔧 配置管理: 使用@ConfigurationProperties");
        System.out.println("  🧪 测试策略: 分层测试(@WebMvcTest, @DataJpaTest)");
        System.out.println("  📊 监控观测: 集成Actuator + Micrometer");
        System.out.println("  🔐 安全防护: 使用Spring Security 6.x");
        
        System.out.println();
    }
    
    /**
     * 演示Bean配置
     */
    @Configuration
    static class DemoConfiguration {
        
        @Bean
        public String demoService() {
            return "现代Spring Boot演示服务";
        }
        
        @Bean
        public ModernSpringBean modernSpringBean() {
            return new ModernSpringBean("现代Spring", 2024);
        }
    }
    
    /**
     * 现代Spring Bean示例
     */
    static class ModernSpringBean {
        private final String name;
        private final int year;
        
        public ModernSpringBean(String name, int year) {
            this.name = name;
            this.year = year;
            System.out.println("🌱 ModernSpringBean创建: " + name + " (" + year + ")");
        }
        
        public String getName() { return name; }
        public int getYear() { return year; }
        
        @Override
        public String toString() {
            return "ModernSpringBean{name='" + name + "', year=" + year + "}";
        }
    }
}