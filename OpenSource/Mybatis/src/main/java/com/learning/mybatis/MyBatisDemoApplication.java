package com.learning.mybatis;

import com.learning.mybatis.demo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MyBatis学习演示应用主类
 * 
 * 演示MyBatis框架的核心特性：
 * - 基础CRUD操作
 * - 动态SQL
 * - 结果映射
 * - 插件机制
 * - 缓存机制
 * - 高级特性
 */
@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class MyBatisDemoApplication implements CommandLineRunner {
    
    private final BasicCrudDemo basicCrudDemo;
    private final DynamicSqlDemo dynamicSqlDemo;
    private final ResultMapDemo resultMapDemo;
    private final CacheDemo cacheDemo;
    private final AdvancedFeaturesDemo advancedFeaturesDemo;
    private final LogTestDemo logTestDemo;
    
    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("           🗃️  MyBatis框架深度学习演示 🗃️");
        System.out.println("=".repeat(70));
        
        SpringApplication.run(MyBatisDemoApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        try {
            System.out.println("\n🚀 开始MyBatis框架特性演示...\n");
            
            // 0. 日志测试
            runLogTest();
            
            // 1. 基础CRUD操作演示
            runBasicCrudDemo();
            
            // 2. 动态SQL演示
            runDynamicSqlDemo();
            
            // 3. 结果映射演示
            runResultMapDemo();
            
            // 4. 缓存机制演示
            runCacheDemo();
            
            // 5. 高级特性演示
            runAdvancedFeaturesDemo();
            
            System.out.println("\n✅ MyBatis演示完成！");
            
        } catch (Exception e) {
            //log.error("演示运行出错", e);
        }
    }
    
    private void runLogTest() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("         🔍 日志功能测试");
        System.out.println("=".repeat(60));
        logTestDemo.testLogging();
    }
    
    private void runBasicCrudDemo() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("         📝 MyBatis基础CRUD操作演示");
        System.out.println("=".repeat(60));
        basicCrudDemo.demonstrateBasicCrud();
    }
    
    private void runDynamicSqlDemo() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("         🔄 MyBatis动态SQL演示");
        System.out.println("=".repeat(60));
        dynamicSqlDemo.demonstrateDynamicSql();
    }
    
    private void runResultMapDemo() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("         🗺️ MyBatis结果映射演示");
        System.out.println("=".repeat(60));
        resultMapDemo.demonstrateResultMap();
    }
    
    private void runCacheDemo() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("         💾 MyBatis缓存机制演示");
        System.out.println("=".repeat(60));
        cacheDemo.demonstrateCache();
    }
    
    private void runAdvancedFeaturesDemo() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("         🚀 MyBatis高级特性演示");
        System.out.println("=".repeat(60));
        advancedFeaturesDemo.demonstrateAdvancedFeatures();
    }
}