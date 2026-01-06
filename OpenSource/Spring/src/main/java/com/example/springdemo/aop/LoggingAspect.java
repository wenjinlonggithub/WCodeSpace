package com.example.springdemo.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * 日志记录切面
 * 
 * 演示Spring AOP的各种通知类型
 */
@Slf4j
@Aspect
@Component
public class LoggingAspect {
    
    // 定义切点表达式
    @Pointcut("execution(* com.example.springdemo.aop.BusinessService.*(..))")
    public void businessServiceMethods() {}
    
    /**
     * 前置通知 - 方法执行前
     */
    @Before("businessServiceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("🟢 [前置通知] 方法开始执行: {}", joinPoint.getSignature().getName());
        log.info("   参数: {}", java.util.Arrays.toString(joinPoint.getArgs()));
    }
    
    /**
     * 后置通知 - 方法执行后（无论是否异常）
     */
    @After("businessServiceMethods()")
    public void logAfter(JoinPoint joinPoint) {
        log.info("🔵 [后置通知] 方法执行结束: {}", joinPoint.getSignature().getName());
    }
    
    /**
     * 返回通知 - 方法正常返回后
     */
    @AfterReturning(pointcut = "businessServiceMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("✅ [返回通知] 方法执行成功: {}，返回值: {}", 
                joinPoint.getSignature().getName(), result);
    }
    
    /**
     * 异常通知 - 方法抛出异常后
     */
    @AfterThrowing(pointcut = "businessServiceMethods()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        log.error("❌ [异常通知] 方法执行异常: {}，异常信息: {}", 
                joinPoint.getSignature().getName(), exception.getMessage());
    }
    
    /**
     * 环绕通知 - 最强大的通知类型
     */
    @Around("execution(* com.example.springdemo.aop.BusinessService.complexCalculation(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        
        log.info("⏰ [环绕通知] 性能监控开始: {}", methodName);
        
        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            long endTime = System.currentTimeMillis();
            log.info("⏰ [环绕通知] 性能监控结束: {}，耗时: {}ms", 
                    methodName, endTime - startTime);
            
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("⏰ [环绕通知] 性能监控异常: {}，耗时: {}ms，异常: {}", 
                    methodName, endTime - startTime, e.getMessage());
            throw e;
        }
    }
}