package com.example.springdemo.lifecycle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * Bean生命周期演示服务
 * 
 * 演示Spring Bean的完整生命周期：
 * 1. Bean的实例化
 * 2. 属性注入
 * 3. Aware接口回调
 * 4. BeanPostProcessor前置处理
 * 5. 初始化回调(@PostConstruct, InitializingBean)
 * 6. BeanPostProcessor后置处理
 * 7. Bean使用
 * 8. 销毁回调(@PreDestroy, DisposableBean)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LifecycleDemoService {
    
    private final ApplicationContext applicationContext;
    private final DatabaseConnectionPool connectionPool;
    private final CacheService cacheService;
    
    /**
     * 演示Bean生命周期
     */
    public void demonstrateLifecycle() {
        log.info("🔍 Bean生命周期演示开始...");
        
        // 1. 演示实例化和初始化
        demonstrateBeanCreation();
        
        // 2. 演示Aware接口回调
        demonstrateAwareInterfaces();
        
        // 3. 演示初始化回调
        demonstrateInitializationCallbacks();
        
        // 4. 演示Bean的正常使用
        demonstrateBeanUsage();
        
        // 5. 演示BeanPostProcessor
        demonstrateBeanPostProcessor();
        
        log.info("✅ Bean生命周期演示完成");
    }
    
    /**
     * 演示Bean的创建过程
     */
    private void demonstrateBeanCreation() {
        log.info("\n🏗️ Bean创建演示：");
        
        // 获取一个新的LifecycleBean实例来观察其生命周期
        LifecycleBean lifecycleBean = applicationContext.getBean(LifecycleBean.class);
        log.info("✅ LifecycleBean创建完成，实例: {}", lifecycleBean.getClass().getSimpleName());
        
        // 显示Bean的状态
        lifecycleBean.showStatus();
    }
    
    /**
     * 演示Aware接口回调
     */
    private void demonstrateAwareInterfaces() {
        log.info("\n🧠 Aware接口演示：");
        
        // 获取实现了Aware接口的Bean
        LifecycleBean lifecycleBean = applicationContext.getBean(LifecycleBean.class);
        
        // 验证Aware接口的注入是否成功
        boolean hasApplicationContext = lifecycleBean.hasApplicationContext();
        boolean hasBeanName = lifecycleBean.hasBeanName();
        
        log.info("ApplicationContextAware注入: {}", hasApplicationContext ? "成功" : "失败");
        log.info("BeanNameAware注入: {}", hasBeanName ? "成功" : "失败");
        log.info("Bean名称: {}", lifecycleBean.getBeanName());
    }
    
    /**
     * 演示初始化回调
     */
    private void demonstrateInitializationCallbacks() {
        log.info("\n🚀 初始化回调演示：");
        
        // DatabaseConnectionPool演示@PostConstruct
        log.info("DatabaseConnectionPool连接数: {}", connectionPool.getActiveConnections());
        connectionPool.testConnection();
        
        // CacheService演示InitializingBean
        cacheService.put("test-key", "test-value");
        String value = cacheService.get("test-key");
        log.info("缓存测试 - 存储并获取值: {}", value);
    }
    
    /**
     * 演示Bean的正常使用
     */
    private void demonstrateBeanUsage() {
        log.info("\n💼 Bean使用演示：");
        
        // 使用数据库连接池
        connectionPool.executeQuery("SELECT COUNT(*) FROM users");
        
        // 使用缓存服务
        cacheService.put("user:123", "张三");
        cacheService.put("user:456", "李四");
        
        log.info("缓存统计: {}", cacheService.getStats());
        
        // 清理测试数据
        cacheService.clear();
    }
    
    /**
     * 演示BeanPostProcessor
     */
    private void demonstrateBeanPostProcessor() {
        log.info("\n🔄 BeanPostProcessor演示：");
        
        // 获取被后置处理器处理过的Bean
        String[] postProcessorBeans = applicationContext.getBeanNamesForType(
                org.springframework.beans.factory.config.BeanPostProcessor.class);
        
        log.info("容器中的BeanPostProcessor数量: {}", postProcessorBeans.length);
        for (String beanName : postProcessorBeans) {
            if (beanName.contains("Demo") || beanName.contains("logging") || beanName.contains("performance")) {
                Object processor = applicationContext.getBean(beanName);
                log.info("  - {}: {}", beanName, processor.getClass().getSimpleName());
            }
        }
        
        // 获取一个被处理过的Bean来验证后置处理器的效果
        LifecycleBean bean = applicationContext.getBean(LifecycleBean.class);
        bean.performAction("BeanPostProcessor验证");
    }
}