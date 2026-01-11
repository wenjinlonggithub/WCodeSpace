package com.architecture.example;

import java.sql.*;

/**
 * 基础JDBC连接示例
 * 演示：连接建立、基本CRUD操作、资源管理
 */
public class BasicJdbcExample {
    
    private static final String URL = "jdbc:mysql://localhost:3306/test_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "password";
    
    /**
     * 测试数据库连接
     */
    public static void testConnection() {
        Connection connection = null;
        try {
            // 1. 加载驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // 2. 建立连接
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("✅ 数据库连接成功!");
            
            // 3. 创建测试表
            createTestTable(connection);
            
            // 4. 插入数据
            insertTestData(connection);
            
            // 5. 查询数据
            queryTestData(connection);
            
            // 6. 更新数据
            updateTestData(connection);
            
            // 7. 删除数据
            deleteTestData(connection);
            
        } catch (ClassNotFoundException e) {
            System.err.println("❌ 驱动加载失败: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ 数据库操作失败: " + e.getMessage());
        } finally {
            // 8. 关闭连接
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("✅ 数据库连接已关闭");
                } catch (SQLException e) {
                    System.err.println("❌ 关闭连接失败: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 创建测试表
     */
    private static void createTestTable(Connection connection) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(50) NOT NULL,
                email VARCHAR(100) UNIQUE,
                age INT,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_name (name),
                INDEX idx_age (age)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ 测试表创建成功");
        }
    }
    
    /**
     * 插入测试数据 - 演示PreparedStatement防止SQL注入
     */
    private static void insertTestData(Connection connection) throws SQLException {
        String sql = "INSERT INTO users (name, email, age) VALUES (?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // 插入多条数据
            String[][] testData = {
                {"张三", "zhangsan@example.com", "25"},
                {"李四", "lisi@example.com", "30"},
                {"王五", "wangwu@example.com", "28"}
            };
            
            for (String[] data : testData) {
                pstmt.setString(1, data[0]);
                pstmt.setString(2, data[1]);
                pstmt.setInt(3, Integer.parseInt(data[2]));
                pstmt.addBatch();
            }
            
            int[] results = pstmt.executeBatch();
            System.out.println("✅ 插入 " + results.length + " 条记录");
        }
    }
    
    /**
     * 查询数据 - 演示结果集处理
     */
    private static void queryTestData(Connection connection) throws SQLException {
        String sql = "SELECT id, name, email, age, create_time FROM users WHERE age >= ? ORDER BY id";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, 25);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("📋 查询结果:");
                System.out.println("ID\t姓名\t邮箱\t\t\t年龄\t创建时间");
                System.out.println("─".repeat(70));
                
                while (rs.next()) {
                    System.out.printf("%d\t%s\t%s\t%d\t%s%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getInt("age"),
                        rs.getTimestamp("create_time")
                    );
                }
            }
        }
    }
    
    /**
     * 更新数据
     */
    private static void updateTestData(Connection connection) throws SQLException {
        String sql = "UPDATE users SET age = age + 1 WHERE name = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "张三");
            int affected = pstmt.executeUpdate();
            System.out.println("✅ 更新了 " + affected + " 条记录");
        }
    }
    
    /**
     * 删除数据
     */
    private static void deleteTestData(Connection connection) throws SQLException {
        String sql = "DELETE FROM users WHERE email = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "wangwu@example.com");
            int affected = pstmt.executeUpdate();
            System.out.println("✅ 删除了 " + affected + " 条记录");
        }
    }
    
    /**
     * 演示数据库元数据获取
     */
    public static void demonstrateMetadata() {
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            System.out.println("📊 数据库信息:");
            System.out.println("数据库产品名: " + metaData.getDatabaseProductName());
            System.out.println("数据库版本: " + metaData.getDatabaseProductVersion());
            System.out.println("驱动名称: " + metaData.getDriverName());
            System.out.println("驱动版本: " + metaData.getDriverVersion());
            System.out.println("最大连接数: " + metaData.getMaxConnections());
            System.out.println("是否支持事务: " + metaData.supportsTransactions());
            
        } catch (SQLException e) {
            System.err.println("❌ 获取元数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示异常处理和资源管理最佳实践
     */
    public static void demonstrateBestPractices() {
        // 使用try-with-resources自动关闭资源
        String sql = "SELECT COUNT(*) as count FROM users";
        
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                System.out.println("用户总数: " + rs.getInt("count"));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ 查询失败: " + e.getMessage());
            // 生产环境中应该记录日志
        }
        // 资源会自动关闭，无需手动close
    }
}