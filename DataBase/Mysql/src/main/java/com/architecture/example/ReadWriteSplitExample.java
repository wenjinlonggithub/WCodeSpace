package com.architecture.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 读写分离示例
 * 演示：主从数据库配置、读写分离路由、数据同步、故障处理
 */
public class ReadWriteSplitExample {
    
    // 主库数据源（写操作）
    private static HikariDataSource masterDataSource;
    
    // 从库数据源（读操作）
    private static HikariDataSource[] slaveDataSources;
    
    // 从库数量
    private static final int SLAVE_COUNT = 2;
    
    static {
        initializeDataSources();
    }
    
    /**
     * 初始化数据源
     */
    private static void initializeDataSources() {
        try {
            // 初始化主库连接池
            masterDataSource = createDataSource(
                "jdbc:mysql://localhost:3306/test_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8",
                "root", "password", "Master-Pool");
            
            // 初始化从库连接池
            slaveDataSources = new HikariDataSource[SLAVE_COUNT];
            for (int i = 0; i < SLAVE_COUNT; i++) {
                // 实际环境中这些应该是不同的从库地址
                // 这里为了演示使用相同的数据库
                slaveDataSources[i] = createDataSource(
                    "jdbc:mysql://localhost:3306/test_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8",
                    "root", "password", "Slave-Pool-" + (i + 1));
            }
            
            System.out.println("✅ 读写分离数据源初始化完成");
            
        } catch (Exception e) {
            System.err.println("❌ 数据源初始化失败: " + e.getMessage());
            // 降级到单一数据源
            initializeFallbackDataSource();
        }
    }
    
    /**
     * 创建数据源
     */
    private static HikariDataSource createDataSource(String url, String username, String password, String poolName) {
        HikariConfig config = new HikariConfig();
        
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        
        // 连接池配置
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(900000);
        config.setPoolName(poolName);
        
        // 连接测试
        config.setConnectionTestQuery("SELECT 1");
        
        return new HikariDataSource(config);
    }
    
    /**
     * 降级到单一数据源
     */
    private static void initializeFallbackDataSource() {
        System.out.println("⚠️ 使用单一数据源作为降级方案");
        
        // 使用现有的连接池作为主库
        masterDataSource = (HikariDataSource) ConnectionPoolExample.getDataSource();
        
        // 从库也指向同一个数据源
        slaveDataSources = new HikariDataSource[SLAVE_COUNT];
        for (int i = 0; i < SLAVE_COUNT; i++) {
            slaveDataSources[i] = masterDataSource;
        }
    }
    
    /**
     * 获取写数据源（主库）
     */
    public static DataSource getWriteDataSource() {
        return masterDataSource;
    }
    
    /**
     * 获取读数据源（从库）- 负载均衡
     */
    public static DataSource getReadDataSource() {
        return getReadDataSource(LoadBalanceStrategy.ROUND_ROBIN);
    }
    
    /**
     * 获取读数据源（从库）- 指定负载均衡策略
     */
    public static DataSource getReadDataSource(LoadBalanceStrategy strategy) {
        switch (strategy) {
            case RANDOM:
                return slaveDataSources[ThreadLocalRandom.current().nextInt(SLAVE_COUNT)];
            case ROUND_ROBIN:
                return slaveDataSources[RoundRobinCounter.getNext() % SLAVE_COUNT];
            case WEIGHTED:
                return getWeightedReadDataSource();
            default:
                return slaveDataSources[0];
        }
    }
    
    /**
     * 权重负载均衡
     */
    private static DataSource getWeightedReadDataSource() {
        // 简单的权重实现：第一个从库权重70%，第二个从库权重30%
        int random = ThreadLocalRandom.current().nextInt(100);
        if (random < 70) {
            return slaveDataSources[0];
        } else {
            return slaveDataSources[1];
        }
    }
    
    /**
     * 负载均衡策略枚举
     */
    public enum LoadBalanceStrategy {
        RANDOM,      // 随机
        ROUND_ROBIN, // 轮询
        WEIGHTED     // 权重
    }
    
    /**
     * 轮询计数器
     */
    private static class RoundRobinCounter {
        private static volatile int counter = 0;
        
        public static synchronized int getNext() {
            return counter++;
        }
    }
    
    /**
     * 演示读写分离
     */
    public static void demonstrateReadWriteSplit() {
        try {
            // 1. 创建测试表
            setupTestTable();
            
            // 2. 演示写操作（主库）
            demonstrateWriteOperations();
            
            // 3. 演示读操作（从库）
            demonstrateReadOperations();
            
            // 4. 演示负载均衡
            demonstrateLoadBalancing();
            
            // 5. 演示事务处理
            demonstrateTransactionHandling();
            
            // 6. 演示故障处理
            demonstrateFailoverHandling();
            
        } catch (Exception e) {
            System.err.println("❌ 读写分离演示失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建测试表
     */
    private static void setupTestTable() throws SQLException {
        System.out.println("📋 创建读写分离测试表...");
        
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS rw_split_test (
                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                user_id INT NOT NULL,
                action VARCHAR(100) NOT NULL,
                data_source VARCHAR(50) NOT NULL,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_user_id (user_id),
                INDEX idx_action (action),
                INDEX idx_create_time (create_time)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        
        // 在主库创建表
        try (Connection conn = getWriteDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("DROP TABLE IF EXISTS rw_split_test");
            stmt.execute(createTableSql);
            System.out.println("✅ 主库表创建完成");
        }
        
        // 等待主从同步（实际环境中需要检查同步状态）
        System.out.println("⏳ 等待主从同步...");
        Thread.sleep(1000);
    }
    
    /**
     * 演示写操作（主库）
     */
    private static void demonstrateWriteOperations() throws SQLException {
        System.out.println("\n📝 演示写操作（主库）");
        
        try (Connection conn = getWriteDataSource().getConnection()) {
            
            // 插入测试数据
            String insertSql = "INSERT INTO rw_split_test (user_id, action, data_source) VALUES (?, ?, ?)";
            
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                
                for (int i = 1; i <= 100; i++) {
                    pstmt.setInt(1, i);
                    pstmt.setString(2, "用户操作" + i);
                    pstmt.setString(3, "MASTER");
                    pstmt.addBatch();
                    
                    if (i % 20 == 0) {
                        pstmt.executeBatch();
                        pstmt.clearBatch();
                    }
                }
                
                pstmt.executeBatch();
            }
            
            System.out.println("✅ 主库写入100条记录完成");
            
            // 查询主库数据确认
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM rw_split_test WHERE data_source = 'MASTER'");
                 ResultSet rs = pstmt.executeQuery()) {
                
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.printf("主库确认：共 %d 条记录%n", count);
                }
            }
        }
    }
    
    /**
     * 演示读操作（从库）
     */
    private static void demonstrateReadOperations() throws SQLException, InterruptedException {
        System.out.println("\n📖 演示读操作（从库）");
        
        // 等待主从同步
        Thread.sleep(500);
        
        try (Connection conn = getReadDataSource().getConnection()) {
            
            // 查询总数
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT COUNT(*) as total FROM rw_split_test");
                 ResultSet rs = pstmt.executeQuery()) {
                
                if (rs.next()) {
                    int total = rs.getInt("total");
                    System.out.printf("从库查询：总记录数 %d%n", total);
                }
            }
            
            // 查询最近的记录
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT user_id, action, create_time FROM rw_split_test ORDER BY id DESC LIMIT 5");
                 ResultSet rs = pstmt.executeQuery()) {
                
                System.out.println("从库查询：最新5条记录");
                while (rs.next()) {
                    System.out.printf("  用户ID: %d, 操作: %s, 时间: %s%n",
                        rs.getInt("user_id"),
                        rs.getString("action"),
                        rs.getTimestamp("create_time")
                    );
                }
            }
        }
    }
    
    /**
     * 演示负载均衡
     */
    private static void demonstrateLoadBalancing() throws Exception {
        System.out.println("\n⚖️ 演示负载均衡策略");
        
        // 测试不同的负载均衡策略
        LoadBalanceStrategy[] strategies = {
            LoadBalanceStrategy.RANDOM,
            LoadBalanceStrategy.ROUND_ROBIN,
            LoadBalanceStrategy.WEIGHTED
        };
        
        for (LoadBalanceStrategy strategy : strategies) {
            System.out.printf("%n📊 测试 %s 策略:%n", strategy.name());
            
            int[] slaveHits = new int[SLAVE_COUNT];
            int testCount = 100;
            
            for (int i = 0; i < testCount; i++) {
                DataSource ds = getReadDataSource(strategy);
                
                // 识别是哪个从库（简单方法：通过连接池名称）
                if (ds instanceof HikariDataSource) {
                    HikariDataSource hds = (HikariDataSource) ds;
                    String poolName = hds.getPoolName();
                    
                    if (poolName.contains("Slave-Pool-1")) {
                        slaveHits[0]++;
                    } else if (poolName.contains("Slave-Pool-2")) {
                        slaveHits[1]++;
                    }
                }
            }
            
            for (int i = 0; i < SLAVE_COUNT; i++) {
                double percentage = (double) slaveHits[i] / testCount * 100;
                System.out.printf("  从库%d: %d 次 (%.1f%%)%n", i + 1, slaveHits[i], percentage);
            }
        }
    }
    
    /**
     * 演示事务处理
     */
    private static void demonstrateTransactionHandling() throws SQLException, InterruptedException {
        System.out.println("\n💼 演示事务处理");
        
        Connection writeConn = null;
        try {
            writeConn = getWriteDataSource().getConnection();
            writeConn.setAutoCommit(false);
            
            // 在事务中执行多个写操作
            try (PreparedStatement pstmt = writeConn.prepareStatement(
                    "INSERT INTO rw_split_test (user_id, action, data_source) VALUES (?, ?, ?)")) {
                
                // 插入多条关联数据
                for (int i = 1001; i <= 1010; i++) {
                    pstmt.setInt(1, i);
                    pstmt.setString(2, "事务操作" + i);
                    pstmt.setString(3, "MASTER_TXN");
                    pstmt.addBatch();
                }
                
                pstmt.executeBatch();
                
                // 模拟业务逻辑处理
                Thread.sleep(100);
                
                // 提交事务
                writeConn.commit();
                System.out.println("✅ 事务提交成功");
                
            } catch (Exception e) {
                writeConn.rollback();
                System.err.println("❌ 事务回滚: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("❌ 事务处理失败: " + e.getMessage());
        } finally {
            if (writeConn != null) {
                writeConn.setAutoCommit(true);
                writeConn.close();
            }
        }
        
        // 等待同步后从从库读取
        Thread.sleep(500);
        
        try (Connection readConn = getReadDataSource().getConnection();
             PreparedStatement pstmt = readConn.prepareStatement(
                "SELECT COUNT(*) FROM rw_split_test WHERE data_source = 'MASTER_TXN'");
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.printf("从库确认事务数据：%d 条记录%n", count);
            }
        }
    }
    
    /**
     * 演示故障处理
     */
    private static void demonstrateFailoverHandling() {
        System.out.println("\n🚨 演示故障处理");
        
        // 模拟从库故障检测
        for (int i = 0; i < SLAVE_COUNT; i++) {
            boolean isHealthy = checkSlaveHealth(slaveDataSources[i]);
            System.out.printf("从库%d健康检查: %s%n", i + 1, isHealthy ? "✅ 正常" : "❌ 异常");
        }
        
        // 演示智能路由（排除故障节点）
        DataSource healthySlave = getHealthyReadDataSource();
        if (healthySlave != null) {
            System.out.println("✅ 已选择健康的从库进行读取");
        } else {
            System.out.println("⚠️ 所有从库不可用，将使用主库进行读取");
        }
    }
    
    /**
     * 检查从库健康状态
     */
    private static boolean checkSlaveHealth(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT 1");
             ResultSet rs = pstmt.executeQuery()) {
            
            return rs.next();
            
        } catch (SQLException e) {
            System.err.println("从库健康检查失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取健康的读数据源
     */
    private static DataSource getHealthyReadDataSource() {
        // 检查所有从库，返回第一个健康的
        for (HikariDataSource slave : slaveDataSources) {
            if (checkSlaveHealth(slave)) {
                return slave;
            }
        }
        
        // 如果所有从库都不健康，返回主库
        return masterDataSource;
    }
    
    /**
     * 操作类型枚举
     */
    public enum OperationType {
        WRITE,       // 写操作
        READ,        // 读操作
        READ_MASTER  // 强制读主库
    }
    
    /**
     * 数据一致性级别
     */
    public enum ConsistencyLevel {
        STRONG,   // 强一致性
        EVENTUAL  // 最终一致性
    }
    
    /**
     * 智能路由器示例
     */
    public static class SmartRouter {
        
        /**
         * 根据操作类型路由数据源
         */
        public static DataSource route(OperationType operation) {
            switch (operation) {
                case WRITE:
                    return getWriteDataSource();
                case READ:
                    return getHealthyReadDataSource();
                case READ_MASTER: // 强制读主库（读取最新数据）
                    return getWriteDataSource();
                default:
                    return getWriteDataSource();
            }
        }
        
        /**
         * 根据数据一致性要求路由
         */
        public static DataSource route(ConsistencyLevel level) {
            switch (level) {
                case STRONG: // 强一致性，读主库
                    return getWriteDataSource();
                case EVENTUAL: // 最终一致性，读从库
                    return getHealthyReadDataSource();
                default:
                    return getWriteDataSource();
            }
        }
    }
    
    /**
     * 关闭数据源
     */
    public static void shutdown() {
        if (masterDataSource != null) {
            masterDataSource.close();
        }
        
        if (slaveDataSources != null) {
            for (HikariDataSource slave : slaveDataSources) {
                if (slave != null && slave != masterDataSource) {
                    slave.close();
                }
            }
        }
        
        System.out.println("✅ 读写分离数据源已关闭");
    }
}