package com.example.springdemo.ioc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * IoC容器演示服务
 * 
 * 演示Spring IoC容器的核心功能：
 * 1. Bean的自动注册与管理
 * 2. Bean的作用域控制
 * 3. Bean的获取方式
 * 4. 容器的生命周期管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IoCDemoService {
    
    private final ApplicationContext applicationContext;
    private final UserRepository userRepository;
    private final UserService userService;
    
    /**
     * 演示IoC容器功能
     */
    public void demonstrateIoC() {
        log.info("🔍 IoC容器演示开始...");
        
        // 1. 演示Bean的注册与获取
        demonstrateBeanRegistration();
        
        // 2. 演示Bean的作用域
        demonstrateBeanScopes();
        
        // 3. 演示依赖注入的工作原理
        demonstrateDependencyInjection();
        
        // 4. 演示容器管理的Bean信息
        demonstrateBeanInformation();
        
        log.info("✅ IoC容器演示完成");
    }
    
    /**
     * 演示Bean的注册与获取
     */
    private void demonstrateBeanRegistration() {
        log.info("\n📋 Bean注册与获取演示：");
        
        // 通过类型获取Bean
        UserRepository repo1 = applicationContext.getBean(UserRepository.class);
        log.info("通过类型获取UserRepository: {}", repo1.getClass().getSimpleName());
        
        // 通过名称获取Bean
        Object repo2 = applicationContext.getBean("userRepositoryImpl");
        log.info("通过名称获取Bean: {}", repo2.getClass().getSimpleName());
        
        // 验证单例模式
        boolean isSame = repo1 == repo2;
        log.info("验证单例模式 - 同一个实例: {}", isSame);
        
        // 获取所有UserRepository类型的Bean
        String[] beanNames = applicationContext.getBeanNamesForType(UserRepository.class);
        log.info("所有UserRepository类型的Bean: {}", String.join(", ", beanNames));
    }
    
    /**
     * 演示Bean的作用域
     */
    private void demonstrateBeanScopes() {
        log.info("\n🎯 Bean作用域演示：");
        
        // 单例Bean演示
        UserService service1 = applicationContext.getBean(UserService.class);
        UserService service2 = applicationContext.getBean(UserService.class);
        log.info("单例Bean演示 - 是否为同一实例: {}", service1 == service2);
        
        // 如果有原型Bean，可以这样演示
        // PrototypeBean proto1 = applicationContext.getBean(PrototypeBean.class);
        // PrototypeBean proto2 = applicationContext.getBean(PrototypeBean.class);
        // log.info("原型Bean演示 - 是否为不同实例: {}", proto1 != proto2);
    }
    
    /**
     * 演示依赖注入的工作原理
     */
    private void demonstrateDependencyInjection() {
        log.info("\n💉 依赖注入演示：");
        
        // 演示构造器注入
        log.info("UserService依赖的UserRepository: {}", 
                userService.getRepository().getClass().getSimpleName());
        
        // 演示业务逻辑
        User user = new User("张三", "zhangsan@example.com");
        userService.saveUser(user);
        
        User foundUser = userService.findUserByEmail("zhangsan@example.com");
        log.info("通过IoC容器管理的服务查找用户: {}", foundUser);
    }
    
    /**
     * 演示容器管理的Bean信息
     */
    private void demonstrateBeanInformation() {
        log.info("\n📊 容器Bean信息：");
        
        // 获取容器中所有Bean的数量
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        log.info("容器中总Bean数量: {}", allBeanNames.length);
        
        // 显示我们自定义的Bean
        log.info("我们的自定义Bean:");
        for (String beanName : allBeanNames) {
            if (beanName.contains("Demo") || beanName.contains("user")) {
                Object bean = applicationContext.getBean(beanName);
                log.info("  - {}: {}", beanName, bean.getClass().getSimpleName());
            }
        }
    }
}