package com.architecture.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 连接池示例
 * 演示：HikariCP连接池配置、多线程并发访问、连接池监控
 */
public class ConnectionPoolExample {
    
    private static HikariDataSource dataSource;
    
    static {
        initializeConnectionPool();
    }
    
    /**
     * 初始化连接池
     */
    private static void initializeConnectionPool() {
        HikariConfig config = new HikariConfig();
        
        // 基本配置
        config.setJdbcUrl("jdbc:mysql://localhost:3306/test_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8");
        config.setUsername("root");
        config.setPassword("password");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        // 连接池配置
        config.setMaximumPoolSize(20);              // 最大连接数
        config.setMinimumIdle(5);                   // 最小空闲连接数
        config.setConnectionTimeout(30000);         // 连接超时时间(毫秒)
        config.setIdleTimeout(600000);              // 空闲连接超时时间(毫秒)
        config.setMaxLifetime(1800000);             // 连接最大生存时间(毫秒)
        config.setLeakDetectionThreshold(60000);    // 连接泄漏检测阈值(毫秒)
        
        // 连接测试
        config.setConnectionTestQuery("SELECT 1");
        
        // 连接池名称
        config.setPoolName("MySQL-Pool");
        
        // 性能优化
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        dataSource = new HikariDataSource(config);
        System.out.println("✅ HikariCP连接池初始化完成");
    }
    
    /**
     * 获取数据源
     */
    public static DataSource getDataSource() {
        return dataSource;
    }
    
    /**
     * 测试连接池
     */
    public static void testConnectionPool() {
        try {
            // 1. 测试基本连接获取
            testBasicConnection();
            
            // 2. 测试并发访问
            testConcurrentAccess();
            
            // 3. 监控连接池状态
            monitorConnectionPool();
            
        } catch (Exception e) {
            System.err.println("❌ 连接池测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试基本连接获取
     */
    private static void testBasicConnection() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("✅ 从连接池获取连接成功");
            
            // 执行简单查询测试连接有效性
            try (PreparedStatement pstmt = connection.prepareStatement("SELECT 1 as test");
                 ResultSet rs = pstmt.executeQuery()) {
                
                if (rs.next()) {
                    System.out.println("✅ 连接有效性测试通过: " + rs.getInt("test"));
                }
            }
        }
    }
    
    /**
     * 测试并发访问
     */
    private static void testConcurrentAccess() {
        System.out.println("🔄 开始并发访问测试...");
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        // 提交20个并发任务
        for (int i = 0; i < 20; i++) {
            final int taskId = i + 1;
            executor.submit(() -> {
                try {
                    performDatabaseOperation(taskId);
                } catch (SQLException e) {
                    System.err.println("❌ 任务" + taskId + "执行失败: " + e.getMessage());
                }
            });
        }
        
        executor.shutdown();
        try {
            if (executor.awaitTermination(30, TimeUnit.SECONDS)) {
                System.out.println("✅ 所有并发任务执行完成");
            } else {
                System.out.println("⚠️ 部分任务执行超时");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ 并发测试被中断");
        }
    }
    
    /**
     * 执行数据库操作
     */
    private static void performDatabaseOperation(int taskId) throws SQLException {
        long startTime = System.currentTimeMillis();
        
        try (Connection connection = dataSource.getConnection()) {
            // 模拟数据库操作
            String sql = "SELECT SLEEP(0.1), ? as task_id, CONNECTION_ID() as conn_id";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, taskId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        long endTime = System.currentTimeMillis();
                        System.out.printf("✅ 任务%d完成 - 连接ID: %d, 耗时: %dms%n", 
                            taskId, rs.getLong("conn_id"), endTime - startTime);
                    }
                }
            }
        }
    }
    
    /**
     * 监控连接池状态
     */
    private static void monitorConnectionPool() {
        if (dataSource != null) {
            System.out.println("\n📊 连接池状态监控:");
            System.out.println("活跃连接数: " + dataSource.getHikariPoolMXBean().getActiveConnections());
            System.out.println("空闲连接数: " + dataSource.getHikariPoolMXBean().getIdleConnections());
            System.out.println("总连接数: " + dataSource.getHikariPoolMXBean().getTotalConnections());
            System.out.println("等待连接的线程数: " + dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
            System.out.println("连接池配置:");
            System.out.println("  最大连接数: " + dataSource.getMaximumPoolSize());
            System.out.println("  最小空闲连接数: " + dataSource.getMinimumIdle());
            System.out.println("  连接超时时间: " + dataSource.getConnectionTimeout() + "ms");
            System.out.println("  空闲超时时间: " + dataSource.getIdleTimeout() + "ms");
        }
    }
    
    /**
     * 演示连接池优化策略
     */
    public static void demonstrateOptimization() {
        System.out.println("\n🔧 连接池优化建议:");
        System.out.println("1. 根据应用并发量设置合适的最大连接数");
        System.out.println("   - 经验公式: max_connections = CPU核数 × 2 + 磁盘数");
        System.out.println("   - 考虑数据库服务器的最大连接数限制");
        
        System.out.println("\n2. 设置合适的空闲连接数");
        System.out.println("   - minimum_idle建议设置为max_connections的25%-50%");
        System.out.println("   - 避免频繁的连接创建和销毁");
        
        System.out.println("\n3. 配置连接超时和生存时间");
        System.out.println("   - connection_timeout: 30秒（避免长时间等待）");
        System.out.println("   - idle_timeout: 10分钟（释放长时间空闲连接）");
        System.out.println("   - max_lifetime: 30分钟（防止连接过期）");
        
        System.out.println("\n4. 启用PreparedStatement缓存");
        System.out.println("   - cachePrepStmts=true");
        System.out.println("   - prepStmtCacheSize=250");
        System.out.println("   - prepStmtCacheSqlLimit=2048");
        
        System.out.println("\n5. 监控和调试");
        System.out.println("   - 启用连接泄漏检测：leakDetectionThreshold");
        System.out.println("   - 监控连接池指标：活跃连接、等待线程数等");
        System.out.println("   - 定期检查慢查询和连接异常");
    }
    
    /**
     * 关闭连接池
     */
    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            System.out.println("✅ 连接池已关闭");
        }
    }
    
    /**
     * 演示连接池压力测试
     */
    public static void stressTest() {
        System.out.println("\n🧪 连接池压力测试开始...");
        
        ExecutorService executor = Executors.newFixedThreadPool(50);
        final int totalTasks = 100;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < totalTasks; i++) {
            final int taskId = i + 1;
            executor.submit(() -> {
                try {
                    // 模拟重负载数据库操作
                    heavyDatabaseOperation(taskId);
                } catch (Exception e) {
                    System.err.println("❌ 压力测试任务" + taskId + "失败: " + e.getMessage());
                }
            });
        }
        
        executor.shutdown();
        try {
            if (executor.awaitTermination(60, TimeUnit.SECONDS)) {
                long endTime = System.currentTimeMillis();
                System.out.println("✅ 压力测试完成，总耗时: " + (endTime - startTime) + "ms");
                System.out.println("平均每个任务耗时: " + (endTime - startTime) / totalTasks + "ms");
            } else {
                System.out.println("⚠️ 压力测试超时");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 最终监控状态
        monitorConnectionPool();
    }
    
    /**
     * 重负载数据库操作
     */
    private static void heavyDatabaseOperation(int taskId) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            // 模拟复杂查询
            String sql = """
                SELECT 
                    ? as task_id,
                    CONNECTION_ID() as conn_id,
                    COUNT(*) as count,
                    AVG(LENGTH(?)) as avg_length
                FROM information_schema.columns 
                WHERE table_schema = 'information_schema'
                """;
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, taskId);
                pstmt.setString(2, "test_string_" + taskId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        if (taskId % 10 == 0) { // 每10个任务打印一次进度
                            System.out.printf("🔄 任务%d完成，连接ID: %d%n", 
                                taskId, rs.getLong("conn_id"));
                        }
                    }
                }
            }
        }
    }
}