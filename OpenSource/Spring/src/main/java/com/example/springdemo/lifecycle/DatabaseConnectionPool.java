package com.example.springdemo.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据库连接池演示类
 * 
 * 演示@PostConstruct和@PreDestroy的使用
 */
@Slf4j
@Component
public class DatabaseConnectionPool {
    
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final int maxConnections = 10;
    private boolean initialized = false;
    
    @PostConstruct
    public void initialize() {
        log.info("🗄️  数据库连接池初始化...");
        
        // 模拟创建初始连接
        for (int i = 0; i < 3; i++) {
            activeConnections.incrementAndGet();
        }
        
        initialized = true;
        log.info("✅ 数据库连接池初始化完成，初始连接数: {}", activeConnections.get());
    }
    
    @PreDestroy
    public void cleanup() {
        log.info("🧹 数据库连接池清理开始...");
        
        // 关闭所有连接
        int connections = activeConnections.getAndSet(0);
        log.info("🚪 关闭 {} 个数据库连接", connections);
        
        initialized = false;
        log.info("✅ 数据库连接池清理完成");
    }
    
    public void testConnection() {
        if (!initialized) {
            throw new IllegalStateException("连接池未初始化");
        }
        
        log.info("🔗 测试数据库连接 - 成功");
    }
    
    public void executeQuery(String sql) {
        if (!initialized) {
            throw new IllegalStateException("连接池未初始化");
        }
        
        log.info("📝 执行SQL查询: {}", sql);
        
        // 模拟查询执行
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.info("✅ SQL查询执行完成");
    }
    
    public int getActiveConnections() {
        return activeConnections.get();
    }
    
    public boolean isInitialized() {
        return initialized;
    }
}