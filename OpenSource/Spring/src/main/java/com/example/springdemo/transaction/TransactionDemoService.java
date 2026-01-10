package com.example.springdemo.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring事务管理演示服务
 * 
 * 演示Spring事务的核心特性：
 * - 声明式事务管理
 * - 事务传播机制
 * - 事务隔离级别
 * - 编程式事务管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionDemoService {
    
    private final TransactionTemplate transactionTemplate;
    private final AtomicInteger counter = new AtomicInteger(0);
    
    public void demonstrateTransactions() {
        log.info("\n💾 Spring Transaction 事务管理演示");
        
        // 1. 基本声明式事务
        demonstrateBasicTransaction();
        
        // 2. 事务传播机制
        demonstratePropagation();
        
        // 3. 事务隔离级别
        demonstrateIsolation();
        
        // 4. 事务回滚
        demonstrateRollback();
        
        // 5. 编程式事务
        demonstrateProgrammaticTransaction();
    }
    
    private void demonstrateBasicTransaction() {
        log.info("\n--- 基本声明式事务演示 ---");
        
        try {
            String result = performBasicTransaction("用户张三");
            log.info("事务执行成功: {}", result);
        } catch (Exception e) {
            log.error("事务执行失败: {}", e.getMessage());
        }
    }
    
    private void demonstratePropagation() {
        log.info("\n--- 事务传播机制演示 ---");
        
        log.info("1. REQUIRED - 如果当前有事务则加入，没有则创建新事务");
        log.info("2. REQUIRES_NEW - 总是创建新事务，挂起当前事务");
        log.info("3. SUPPORTS - 如果有事务则加入，没有则以非事务方式执行");
        log.info("4. NOT_SUPPORTED - 以非事务方式执行，如果有事务则挂起");
        log.info("5. MANDATORY - 必须在事务中执行，否则抛出异常");
        log.info("6. NEVER - 必须在非事务中执行，否则抛出异常");
        log.info("7. NESTED - 如果有事务则创建嵌套事务，否则创建新事务");
        
        try {
            performRequiredTransaction();
            performRequiresNewTransaction();
        } catch (Exception e) {
            log.error("传播机制演示失败: {}", e.getMessage());
        }
    }
    
    private void demonstrateIsolation() {
        log.info("\n--- 事务隔离级别演示 ---");
        
        log.info("1. DEFAULT - 使用数据库默认隔离级别");
        log.info("2. READ_UNCOMMITTED - 读未提交（最低级别）");
        log.info("3. READ_COMMITTED - 读已提交（避免脏读）");
        log.info("4. REPEATABLE_READ - 可重复读（避免脏读和不可重复读）");
        log.info("5. SERIALIZABLE - 串行化（最高级别，避免所有并发问题）");
        
        try {
            performReadCommittedTransaction();
            performRepeatableReadTransaction();
        } catch (Exception e) {
            log.error("隔离级别演示失败: {}", e.getMessage());
        }
    }
    
    private void demonstrateRollback() {
        log.info("\n--- 事务回滚演示 ---");
        
        try {
            // 演示自动回滚（RuntimeException）
            log.info("尝试执行会抛出异常的事务方法...");
            performTransactionWithException();
        } catch (Exception e) {
            log.info("✅ 事务自动回滚，异常信息: {}", e.getMessage());
        }
        
        try {
            // 演示手动回滚
            performTransactionWithManualRollback();
        } catch (Exception e) {
            log.info("✅ 事务手动回滚，异常信息: {}", e.getMessage());
        }
    }
    
    private void demonstrateProgrammaticTransaction() {
        log.info("\n--- 编程式事务演示 ---");
        
        try {
            String result = transactionTemplate.execute(status -> {
                try {
                    log.info("📝 编程式事务开始执行...");
                    
                    // 模拟业务操作
                    int value = counter.incrementAndGet();
                    log.info("执行业务操作，当前计数: {}", value);
                    
                    if (value % 2 == 0) {
                        log.info("偶数值，手动标记回滚");
                        status.setRollbackOnly();
                        return "事务已标记为回滚";
                    }
                    
                    return "编程式事务执行成功，计数: " + value;
                    
                } catch (Exception e) {
                    log.error("编程式事务执行异常", e);
                    throw new RuntimeException(e);
                }
            });
            
            log.info("编程式事务结果: {}", result);
            
        } catch (Exception e) {
            log.error("编程式事务失败: {}", e.getMessage());
        }
    }
    
    /**
     * 基本事务方法
     */
    @Transactional
    public String performBasicTransaction(String data) {
        log.info("📝 执行基本事务操作: {}", data);
        
        // 模拟数据库操作
        simulateDatabaseOperation("插入用户数据: " + data);
        simulateDatabaseOperation("更新用户状态");
        
        return "基本事务完成: " + data;
    }
    
    /**
     * REQUIRED传播级别（默认）
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void performRequiredTransaction() {
        log.info("📝 REQUIRED传播级别事务");
        simulateDatabaseOperation("REQUIRED事务操作");
        
        // 调用另一个事务方法，会加入当前事务
        performNestedRequiredTransaction();
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void performNestedRequiredTransaction() {
        log.info("   📝 嵌套REQUIRED事务（加入外层事务）");
        simulateDatabaseOperation("嵌套操作");
    }
    
    /**
     * REQUIRES_NEW传播级别
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void performRequiresNewTransaction() {
        log.info("📝 REQUIRES_NEW传播级别事务（创建新事务）");
        simulateDatabaseOperation("REQUIRES_NEW事务操作");
    }
    
    /**
     * READ_COMMITTED隔离级别
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void performReadCommittedTransaction() {
        log.info("📝 READ_COMMITTED隔离级别事务");
        simulateDatabaseOperation("READ_COMMITTED操作");
    }
    
    /**
     * REPEATABLE_READ隔离级别
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void performRepeatableReadTransaction() {
        log.info("📝 REPEATABLE_READ隔离级别事务");
        simulateDatabaseOperation("REPEATABLE_READ操作");
    }
    
    /**
     * 会抛出异常的事务方法
     */
    @Transactional
    public void performTransactionWithException() {
        log.info("📝 执行会抛出异常的事务方法");
        simulateDatabaseOperation("事务操作1");
        simulateDatabaseOperation("事务操作2");
        
        // 抛出RuntimeException，触发自动回滚
        throw new RuntimeException("模拟业务异常，触发事务回滚");
    }
    
    /**
     * 手动回滚的事务方法
     */
    @Transactional
    public void performTransactionWithManualRollback() {
        log.info("📝 执行手动回滚的事务方法");
        simulateDatabaseOperation("事务操作1");
        
        // 检查业务条件，决定是否回滚
        if (System.currentTimeMillis() % 2 == 0) {
            throw new RuntimeException("业务条件不满足，手动触发回滚");
        }
        
        simulateDatabaseOperation("事务操作2");
    }
    
    /**
     * 只读事务
     */
    @Transactional(readOnly = true)
    public String performReadOnlyTransaction() {
        log.info("📝 只读事务执行");
        // 只读事务中只能执行查询操作
        return "只读事务查询结果";
    }
    
    /**
     * 超时事务
     */
    @Transactional(timeout = 5) // 5秒超时
    public void performTimeoutTransaction() {
        log.info("📝 超时事务执行");
        simulateDatabaseOperation("超时事务操作");
    }
    
    /**
     * 模拟数据库操作
     */
    private void simulateDatabaseOperation(String operation) {
        try {
            Thread.sleep(100); // 模拟数据库操作耗时
            log.info("   💾 数据库操作: {}", operation);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("操作被中断", e);
        }
    }
    
    /**
     * 显示事务配置说明
     */
    public void showTransactionConfiguration() {
        log.info("\n--- Spring Transaction 配置说明 ---");
        log.info("1. @EnableTransactionManagement - 启用事务管理");
        log.info("2. PlatformTransactionManager - 事务管理器");
        log.info("3. TransactionTemplate - 编程式事务模板");
        log.info("4. @Transactional - 声明式事务注解");
        
        log.info("\n--- 事务属性配置 ---");
        log.info("propagation - 传播行为");
        log.info("isolation - 隔离级别");
        log.info("timeout - 超时时间");
        log.info("readOnly - 只读标志");
        log.info("rollbackFor - 回滚异常类型");
        log.info("noRollbackFor - 不回滚异常类型");
    }
}