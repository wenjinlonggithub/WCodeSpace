package com.example.springdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Spring Boot 演示应用程序主启动类
 * 
 * 演示现代Spring Boot应用的完整特性：
 * - IoC容器和依赖注入
 * - Bean生命周期管理 
 * - AOP面向切面编程
 * - 事件驱动架构
 * - 异步处理
 */
@SpringBootApplication
@EnableAsync
public class SpringDemoApplication {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("           🌱 现代Spring Boot框架演示 🌱");
        System.out.println("=".repeat(70));
        
        ConfigurableApplicationContext context = SpringApplication.run(SpringDemoApplication.class, args);
        
        // 运行演示程序
        SpringDemoRunner runner = context.getBean(SpringDemoRunner.class);
        runner.runAllDemos();
        
        System.out.println("\n=".repeat(70));
        System.out.println("           ✨ Spring Boot演示完成 ✨");
        System.out.println("=".repeat(70));
    }
}