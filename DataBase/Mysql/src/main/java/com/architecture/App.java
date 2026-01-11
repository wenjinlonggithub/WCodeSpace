package com.architecture;

import com.architecture.example.*;

/**
 * MySQL 示例演示入口
 */
public class App {
    public static void main(String[] args) {
        System.out.println("====== MySQL 学习示例 ======");
        
        try {
            // 1. 基础JDBC连接示例
            System.out.println("\n1. 基础JDBC连接测试:");
            BasicJdbcExample.testConnection();
            
            // 2. 连接池示例
            System.out.println("\n2. 连接池示例:");
            ConnectionPoolExample.testConnectionPool();
            
            // 3. 事务示例
            System.out.println("\n3. 事务处理示例:");
            TransactionExample.testTransaction();
            
            // 4. 索引优化示例
            System.out.println("\n4. 索引优化示例:");
            IndexOptimizationExample.demonstrateIndexUsage();
            
            // 5. 批量操作示例
            System.out.println("\n5. 批量操作示例:");
            BatchOperationExample.testBatchOperations();
            
            // 6. MySQL核心概念演示
            System.out.println("\n6. MySQL核心概念演示:");
            MySQLConceptsDemo.demonstrateAllConcepts();
            
            System.out.println("\n====== 所有示例执行完成 ======");
            
        } catch (Exception e) {
            System.err.println("示例执行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
