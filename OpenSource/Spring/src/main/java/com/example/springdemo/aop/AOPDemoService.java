package com.example.springdemo.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * AOP演示服务
 * 
 * 演示Spring AOP的各种通知类型：
 * 1. @Before - 前置通知
 * 2. @After - 后置通知  
 * 3. @AfterReturning - 返回通知
 * 4. @AfterThrowing - 异常通知
 * 5. @Around - 环绕通知
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AOPDemoService {
    
    private final BusinessService businessService;
    
    public void demonstrateAOP() {
        log.info("🔍 AOP演示开始...");
        
        // 1. 演示正常业务方法执行
        demonstrateNormalExecution();
        
        // 2. 演示异常情况处理
        demonstrateExceptionHandling();
        
        // 3. 演示性能监控
        demonstratePerformanceMonitoring();
        
        // 4. 演示参数校验
        demonstrateValidation();
        
        log.info("✅ AOP演示完成");
    }
    
    private void demonstrateNormalExecution() {
        log.info("\n🚀 正常业务执行演示:");
        
        // 调用业务方法 - 将触发各种AOP通知
        String result = businessService.processOrder("ORD-001", 299.99);
        log.info("业务执行结果: {}", result);
    }
    
    private void demonstrateExceptionHandling() {
        log.info("\n⚠️  异常处理演示:");
        
        try {
            // 调用会抛出异常的方法
            businessService.processRefund("ORD-999", -100.0);
        } catch (Exception e) {
            log.info("捕获到异常: {}", e.getMessage());
        }
    }
    
    private void demonstratePerformanceMonitoring() {
        log.info("\n⏱️  性能监控演示:");
        
        // 调用耗时较长的方法
        businessService.complexCalculation(1000);
    }
    
    private void demonstrateValidation() {
        log.info("\n✅ 参数校验演示:");
        
        try {
            // 传入无效参数
            businessService.processOrder(null, 0);
        } catch (Exception e) {
            log.info("校验失败: {}", e.getMessage());
        }
    }
}