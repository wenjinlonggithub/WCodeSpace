package com.architecture.example;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 索引优化示例
 * 演示：索引设计、查询优化、EXPLAIN分析、覆盖索引等
 */
public class IndexOptimizationExample {
    
    private static final DataSource dataSource = ConnectionPoolExample.getDataSource();
    private static final Random random = new Random();
    
    /**
     * 演示索引使用
     */
    public static void demonstrateIndexUsage() {
        try {
            // 1. 创建测试表和数据
            setupTestData();
            
            // 2. 无索引查询性能测试
            testQueryWithoutIndex();
            
            // 3. 创建索引并测试性能
            createIndexesAndTest();
            
            // 4. 复合索引演示
            demonstrateCompositeIndex();
            
            // 5. 覆盖索引演示
            demonstrateCoveringIndex();
            
            // 6. 索引失效场景演示
            demonstrateIndexFailure();
            
            // 7. 前缀索引演示
            demonstratePrefixIndex();
            
            // 8. 函数索引演示（MySQL 8.0+）
            demonstrateFunctionalIndex();
            
        } catch (Exception e) {
            System.err.println("❌ 索引优化测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建测试表和数据
     */
    private static void setupTestData() throws SQLException {
        System.out.println("📊 创建测试表和数据...");
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 删除已存在的表
            stmt.execute("DROP TABLE IF EXISTS products");
            stmt.execute("DROP TABLE IF EXISTS orders");
            stmt.execute("DROP TABLE IF EXISTS customers");
            
            // 创建客户表
            String createCustomersSql = """
                CREATE TABLE customers (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) UNIQUE,
                    phone VARCHAR(20),
                    city VARCHAR(50),
                    age INT,
                    registration_date DATE,
                    status ENUM('active', 'inactive') DEFAULT 'active',
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;
            
            // 创建商品表
            String createProductsSql = """
                CREATE TABLE products (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(200) NOT NULL,
                    category VARCHAR(50),
                    price DECIMAL(10,2),
                    stock_quantity INT DEFAULT 0,
                    description TEXT,
                    brand VARCHAR(100),
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;
            
            // 创建订单表
            String createOrdersSql = """
                CREATE TABLE orders (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    customer_id INT NOT NULL,
                    product_id INT NOT NULL,
                    quantity INT DEFAULT 1,
                    unit_price DECIMAL(10,2),
                    total_amount DECIMAL(12,2),
                    order_date DATE,
                    status VARCHAR(20) DEFAULT 'pending',
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;
            
            stmt.execute(createCustomersSql);
            stmt.execute(createProductsSql);
            stmt.execute(createOrdersSql);
            
            System.out.println("✅ 测试表创建完成");
            
            // 插入测试数据
            insertTestData(conn);
        }
    }
    
    /**
     * 插入测试数据
     */
    private static void insertTestData(Connection conn) throws SQLException {
        System.out.println("💾 插入测试数据...");
        
        conn.setAutoCommit(false);
        
        try {
            // 插入客户数据
            insertCustomers(conn, 10000);
            
            // 插入商品数据
            insertProducts(conn, 1000);
            
            // 插入订单数据
            insertOrders(conn, 50000);
            
            conn.commit();
            System.out.println("✅ 测试数据插入完成");
            
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
    
    private static void insertCustomers(Connection conn, int count) throws SQLException {
        String sql = "INSERT INTO customers (name, email, phone, city, age, registration_date) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String[] cities = {"北京", "上海", "广州", "深圳", "杭州", "南京", "武汉", "成都", "重庆", "西安"};
            
            for (int i = 1; i <= count; i++) {
                pstmt.setString(1, "客户" + i);
                pstmt.setString(2, "customer" + i + "@example.com");
                pstmt.setString(3, "1" + String.format("%010d", i));
                pstmt.setString(4, cities[random.nextInt(cities.length)]);
                pstmt.setInt(5, 18 + random.nextInt(50));
                pstmt.setDate(6, new Date(System.currentTimeMillis() - random.nextInt(365 * 24 * 3600 * 1000L)));
                
                pstmt.addBatch();
                
                if (i % 1000 == 0) {
                    pstmt.executeBatch();
                    pstmt.clearBatch();
                }
            }
            
            pstmt.executeBatch();
        }
        
        System.out.println("插入客户数据: " + count + " 条");
    }
    
    private static void insertProducts(Connection conn, int count) throws SQLException {
        String sql = "INSERT INTO products (name, category, price, stock_quantity, brand) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String[] categories = {"电子产品", "服装", "食品", "图书", "家具", "运动", "美妆", "玩具"};
            String[] brands = {"Apple", "Samsung", "华为", "小米", "OPPO", "VIVO", "联想", "戴尔"};
            
            for (int i = 1; i <= count; i++) {
                pstmt.setString(1, "商品" + i);
                pstmt.setString(2, categories[random.nextInt(categories.length)]);
                pstmt.setBigDecimal(3, java.math.BigDecimal.valueOf(10 + random.nextInt(1000) + random.nextDouble()));
                pstmt.setInt(4, random.nextInt(1000));
                pstmt.setString(5, brands[random.nextInt(brands.length)]);
                
                pstmt.addBatch();
                
                if (i % 500 == 0) {
                    pstmt.executeBatch();
                    pstmt.clearBatch();
                }
            }
            
            pstmt.executeBatch();
        }
        
        System.out.println("插入商品数据: " + count + " 条");
    }
    
    private static void insertOrders(Connection conn, int count) throws SQLException {
        String sql = "INSERT INTO orders (customer_id, product_id, quantity, unit_price, total_amount, order_date, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String[] statuses = {"pending", "paid", "shipped", "delivered", "cancelled"};
            
            for (int i = 1; i <= count; i++) {
                int customerId = 1 + random.nextInt(10000);
                int productId = 1 + random.nextInt(1000);
                int quantity = 1 + random.nextInt(5);
                java.math.BigDecimal unitPrice = java.math.BigDecimal.valueOf(10 + random.nextInt(500) + random.nextDouble());
                java.math.BigDecimal totalAmount = unitPrice.multiply(java.math.BigDecimal.valueOf(quantity));
                
                pstmt.setInt(1, customerId);
                pstmt.setInt(2, productId);
                pstmt.setInt(3, quantity);
                pstmt.setBigDecimal(4, unitPrice);
                pstmt.setBigDecimal(5, totalAmount);
                pstmt.setDate(6, new Date(System.currentTimeMillis() - random.nextInt(90 * 24 * 3600 * 1000L)));
                pstmt.setString(7, statuses[random.nextInt(statuses.length)]);
                
                pstmt.addBatch();
                
                if (i % 2000 == 0) {
                    pstmt.executeBatch();
                    pstmt.clearBatch();
                }
            }
            
            pstmt.executeBatch();
        }
        
        System.out.println("插入订单数据: " + count + " 条");
    }
    
    /**
     * 无索引查询性能测试
     */
    private static void testQueryWithoutIndex() throws SQLException {
        System.out.println("\n🐌 无索引查询性能测试");
        
        String[] queries = {
            "SELECT * FROM customers WHERE city = '北京' AND age > 25",
            "SELECT * FROM products WHERE category = '电子产品' AND price BETWEEN 100 AND 500",
            "SELECT c.name, COUNT(*) as order_count FROM customers c JOIN orders o ON c.id = o.customer_id WHERE o.status = 'delivered' GROUP BY c.id"
        };
        
        for (String query : queries) {
            executeQueryWithTiming(query, "无索引");
        }
    }
    
    /**
     * 创建索引并测试性能
     */
    private static void createIndexesAndTest() throws SQLException {
        System.out.println("\n🚀 创建索引并测试性能");
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 创建单列索引
            stmt.execute("CREATE INDEX idx_customers_city ON customers(city)");
            stmt.execute("CREATE INDEX idx_customers_age ON customers(age)");
            stmt.execute("CREATE INDEX idx_products_category ON products(category)");
            stmt.execute("CREATE INDEX idx_products_price ON products(price)");
            stmt.execute("CREATE INDEX idx_orders_customer_id ON orders(customer_id)");
            stmt.execute("CREATE INDEX idx_orders_status ON orders(status)");
            
            System.out.println("✅ 单列索引创建完成");
        }
        
        // 测试相同查询的性能
        String[] queries = {
            "SELECT * FROM customers WHERE city = '北京'",
            "SELECT * FROM customers WHERE age > 30",
            "SELECT * FROM products WHERE category = '电子产品'",
            "SELECT COUNT(*) FROM orders WHERE status = 'delivered'"
        };
        
        for (String query : queries) {
            executeQueryWithTiming(query, "有索引");
            analyzeQueryPlan(query);
        }
    }
    
    /**
     * 复合索引演示
     */
    private static void demonstrateCompositeIndex() throws SQLException {
        System.out.println("\n🔗 复合索引演示");
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 创建复合索引
            stmt.execute("CREATE INDEX idx_customers_city_age ON customers(city, age)");
            stmt.execute("CREATE INDEX idx_orders_customer_status_date ON orders(customer_id, status, order_date)");
            
            System.out.println("✅ 复合索引创建完成");
        }
        
        // 测试复合索引的使用
        String[] queries = {
            // 可以使用 idx_customers_city_age 索引
            "SELECT * FROM customers WHERE city = '上海' AND age = 25",
            "SELECT * FROM customers WHERE city = '上海' AND age > 30",
            "SELECT * FROM customers WHERE city = '上海'", // 最左前缀
            
            // 无法使用 idx_customers_city_age 索引
            "SELECT * FROM customers WHERE age = 25", // 不符合最左前缀
            
            // 可以使用 idx_orders_customer_status_date 索引
            "SELECT * FROM orders WHERE customer_id = 100 AND status = 'paid'",
            "SELECT * FROM orders WHERE customer_id = 100 AND status = 'paid' AND order_date > '2024-01-01'"
        };
        
        System.out.println("\n📋 复合索引使用测试:");
        for (String query : queries) {
            System.out.println("\n查询: " + query);
            executeQueryWithTiming(query, "复合索引");
            analyzeQueryPlan(query);
        }
    }
    
    /**
     * 覆盖索引演示
     */
    private static void demonstrateCoveringIndex() throws SQLException {
        System.out.println("\n🎯 覆盖索引演示");
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 创建覆盖索引
            stmt.execute("CREATE INDEX idx_customers_city_name_age ON customers(city, name, age)");
            stmt.execute("CREATE INDEX idx_orders_customer_total ON orders(customer_id, total_amount)");
            
            System.out.println("✅ 覆盖索引创建完成");
        }
        
        // 覆盖索引查询（不需要回表）
        String[] coveringQueries = {
            "SELECT name, age FROM customers WHERE city = '深圳'",
            "SELECT customer_id, SUM(total_amount) FROM orders WHERE customer_id BETWEEN 100 AND 200 GROUP BY customer_id"
        };
        
        // 非覆盖索引查询（需要回表）
        String[] nonCoveringQueries = {
            "SELECT * FROM customers WHERE city = '深圳'",
            "SELECT * FROM orders WHERE customer_id = 100"
        };
        
        System.out.println("\n✅ 覆盖索引查询（无回表）:");
        for (String query : coveringQueries) {
            System.out.println("\n查询: " + query);
            executeQueryWithTiming(query, "覆盖索引");
            analyzeQueryPlan(query);
        }
        
        System.out.println("\n❌ 非覆盖索引查询（需回表）:");
        for (String query : nonCoveringQueries) {
            System.out.println("\n查询: " + query);
            executeQueryWithTiming(query, "需要回表");
            analyzeQueryPlan(query);
        }
    }
    
    /**
     * 索引失效场景演示
     */
    private static void demonstrateIndexFailure() throws SQLException {
        System.out.println("\n⚠️ 索引失效场景演示");
        
        String[] ineffectiveQueries = {
            // 1. 使用函数
            "SELECT * FROM customers WHERE YEAR(registration_date) = 2024",
            
            // 2. 隐式类型转换
            "SELECT * FROM customers WHERE phone = 13800138001", // phone是VARCHAR
            
            // 3. 模糊查询前缀通配符
            "SELECT * FROM customers WHERE name LIKE '%张%'",
            
            // 4. 不等于操作
            "SELECT * FROM customers WHERE city != '北京'",
            
            // 5. OR条件（其中一个字段无索引）
            "SELECT * FROM customers WHERE city = '北京' OR email LIKE '%@gmail.com'",
            
            // 6. IS NULL / IS NOT NULL（取决于数据分布）
            "SELECT * FROM customers WHERE phone IS NULL"
        };
        
        String[] effectiveQueries = {
            // 对应的有效查询
            "SELECT * FROM customers WHERE registration_date >= '2024-01-01' AND registration_date < '2025-01-01'",
            "SELECT * FROM customers WHERE phone = '13800138001'",
            "SELECT * FROM customers WHERE name LIKE '张%'",
            "SELECT * FROM customers WHERE city = '上海'",
            "SELECT * FROM customers WHERE city = '北京' UNION SELECT * FROM customers WHERE email LIKE '%@gmail.com'",
            "SELECT * FROM customers WHERE phone = '13800138001'"
        };
        
        System.out.println("\n❌ 索引失效的查询:");
        for (int i = 0; i < ineffectiveQueries.length; i++) {
            System.out.println("\n失效查询 " + (i + 1) + ": " + ineffectiveQueries[i]);
            analyzeQueryPlan(ineffectiveQueries[i]);
        }
        
        System.out.println("\n✅ 优化后的查询:");
        for (int i = 0; i < effectiveQueries.length; i++) {
            System.out.println("\n优化查询 " + (i + 1) + ": " + effectiveQueries[i]);
            analyzeQueryPlan(effectiveQueries[i]);
        }
    }
    
    /**
     * 前缀索引演示
     */
    private static void demonstratePrefixIndex() throws SQLException {
        System.out.println("\n✂️ 前缀索引演示");
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 分析前缀选择性
            analyzePrefixSelectivity();
            
            // 创建前缀索引
            stmt.execute("CREATE INDEX idx_customers_email_prefix ON customers(email(10))");
            stmt.execute("CREATE INDEX idx_products_name_prefix ON products(name(15))");
            
            System.out.println("✅ 前缀索引创建完成");
        }
        
        // 测试前缀索引
        String[] prefixQueries = {
            "SELECT * FROM customers WHERE email = 'customer123@example.com'",
            "SELECT * FROM products WHERE name = '商品123'"
        };
        
        for (String query : prefixQueries) {
            System.out.println("\n查询: " + query);
            executeQueryWithTiming(query, "前缀索引");
            analyzeQueryPlan(query);
        }
    }
    
    /**
     * 分析前缀选择性
     */
    private static void analyzePrefixSelectivity() throws SQLException {
        System.out.println("🔍 分析前缀选择性...");
        
        String[] prefixLengths = {"5", "10", "15", "20"};
        
        for (String length : prefixLengths) {
            String sql = "SELECT COUNT(DISTINCT LEFT(email, " + length + ")) / COUNT(*) as selectivity FROM customers";
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                if (rs.next()) {
                    double selectivity = rs.getDouble("selectivity");
                    System.out.printf("邮箱前%s位选择性: %.4f%n", length, selectivity);
                }
            }
        }
    }
    
    /**
     * 函数索引演示（MySQL 8.0+）
     */
    private static void demonstrateFunctionalIndex() {
        System.out.println("\n🧮 函数索引演示（MySQL 8.0+）");
        
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String version = metaData.getDatabaseProductVersion();
            System.out.println("当前MySQL版本: " + version);
            
            // 检查是否支持函数索引（MySQL 8.0+）
            if (version.compareTo("8.0") >= 0) {
                try (Statement stmt = conn.createStatement()) {
                    // 创建函数索引
                    stmt.execute("CREATE INDEX idx_customers_year_reg ON customers((YEAR(registration_date)))");
                    stmt.execute("CREATE INDEX idx_customers_upper_name ON customers((UPPER(name)))");
                    
                    System.out.println("✅ 函数索引创建完成");
                    
                    // 测试函数索引
                    String[] functionalQueries = {
                        "SELECT * FROM customers WHERE YEAR(registration_date) = 2024",
                        "SELECT * FROM customers WHERE UPPER(name) = 'CUSTOMER123'"
                    };
                    
                    for (String query : functionalQueries) {
                        System.out.println("\n查询: " + query);
                        analyzeQueryPlan(query);
                    }
                }
            } else {
                System.out.println("⚠️ 当前MySQL版本不支持函数索引，需要8.0+版本");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ 函数索引演示失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行查询并计时
     */
    private static void executeQueryWithTiming(String sql, String description) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            long startTime = System.currentTimeMillis();
            
            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next() && count < 5) { // 只处理前5条记录
                    count++;
                }
                
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                System.out.printf("%s - 查询耗时: %d ms%n", description, duration);
                
                if (duration > 1000) {
                    System.out.println("⚠️ 查询耗时较长，建议优化");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ 查询执行失败: " + e.getMessage());
        }
    }
    
    /**
     * 分析查询执行计划
     */
    private static void analyzeQueryPlan(String sql) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("EXPLAIN " + sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            System.out.println("📊 执行计划分析:");
            System.out.println("Type\tKey\t\tRows\tExtra");
            System.out.println("─".repeat(50));
            
            while (rs.next()) {
                String type = rs.getString("type");
                String key = rs.getString("key");
                long rows = rs.getLong("rows");
                String extra = rs.getString("Extra");
                
                System.out.printf("%s\t%s\t%d\t%s%n", 
                    type != null ? type : "NULL",
                    key != null ? key : "NULL",
                    rows,
                    extra != null ? extra : ""
                );
                
                // 性能建议
                if ("ALL".equals(type)) {
                    System.out.println("⚠️ 全表扫描，建议添加索引");
                } else if ("index".equals(type)) {
                    System.out.println("⚠️ 全索引扫描，考虑优化查询条件");
                } else if ("range".equals(type) || "ref".equals(type) || "eq_ref".equals(type)) {
                    System.out.println("✅ 使用了索引，性能良好");
                }
                
                if (extra != null) {
                    if (extra.contains("Using filesort")) {
                        System.out.println("⚠️ 使用文件排序，考虑添加排序字段索引");
                    }
                    if (extra.contains("Using temporary")) {
                        System.out.println("⚠️ 使用临时表，考虑优化GROUP BY或ORDER BY");
                    }
                    if (extra.contains("Using index")) {
                        System.out.println("✅ 使用覆盖索引，性能优秀");
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ 执行计划分析失败: " + e.getMessage());
        }
    }
    
    /**
     * 索引使用建议
     */
    public static void printIndexOptimizationTips() {
        System.out.println("\n💡 索引优化建议:");
        
        System.out.println("\n1. 索引设计原则:");
        System.out.println("   • 在WHERE、ORDER BY、GROUP BY列上建索引");
        System.out.println("   • 选择性高的列建索引（重复值少的列）");
        System.out.println("   • 复合索引遵循最左前缀原则");
        System.out.println("   • 考虑使用覆盖索引减少回表");
        
        System.out.println("\n2. 避免索引失效:");
        System.out.println("   • 避免在索引列上使用函数");
        System.out.println("   • 避免隐式类型转换");
        System.out.println("   • 模糊查询避免前缀通配符");
        System.out.println("   • 慎用NOT、!=、IS NULL");
        
        System.out.println("\n3. 索引维护:");
        System.out.println("   • 定期分析表和索引统计信息");
        System.out.println("   • 删除不使用的索引");
        System.out.println("   • 监控索引的使用情况");
        System.out.println("   • 考虑索引的维护成本");
        
        System.out.println("\n4. 性能监控:");
        System.out.println("   • 使用EXPLAIN分析查询计划");
        System.out.println("   • 监控慢查询日志");
        System.out.println("   • 使用Performance Schema");
        System.out.println("   • 定期进行性能测试");
    }
}
            testQueryWithoutIndex();
            
            // 3. 创建索引并测试性能
            createIndexesAndTest();
            
            // 4. 复合索引演示
            demonstrateCompositeIndex();
            
            // 5. 覆盖索引演示
            demonstrateCoveringIndex();
            
            // 6. 索引失效场景演示
            demonstrateIndexFailure();
            
            // 7. 前缀索引演示
            demonstratePrefixIndex();
            
            // 8. 函数索引演示（MySQL 8.0+）
            demonstrateFunctionalIndex();
            
        } catch (Exception e) {
            System.err.println("❌ 索引优化测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建测试表和数据
     */
    private static void setupTestData() throws SQLException {
        System.out.println("📊 创建测试表和数据...");
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 删除已存在的表
            stmt.execute("DROP TABLE IF EXISTS products");
            stmt.execute("DROP TABLE IF EXISTS orders");
            stmt.execute("DROP TABLE IF EXISTS customers");
            
            // 创建客户表
            String createCustomersSql = """\n                CREATE TABLE customers (\n                    id INT PRIMARY KEY AUTO_INCREMENT,\n                    name VARCHAR(100) NOT NULL,\n                    email VARCHAR(100) UNIQUE,\n                    phone VARCHAR(20),\n                    city VARCHAR(50),\n                    age INT,\n                    registration_date DATE,\n                    status ENUM('active', 'inactive') DEFAULT 'active',\n                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4\n                \"\"\";\n            \n            // 创建商品表\n            String createProductsSql = \"\"\"\n                CREATE TABLE products (\n                    id INT PRIMARY KEY AUTO_INCREMENT,\n                    name VARCHAR(200) NOT NULL,\n                    category VARCHAR(50),\n                    price DECIMAL(10,2),\n                    stock_quantity INT DEFAULT 0,\n                    description TEXT,\n                    brand VARCHAR(100),\n                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4\n                \"\"\";\n            \n            // 创建订单表\n            String createOrdersSql = \"\"\"\n                CREATE TABLE orders (\n                    id BIGINT PRIMARY KEY AUTO_INCREMENT,\n                    customer_id INT NOT NULL,\n                    product_id INT NOT NULL,\n                    quantity INT DEFAULT 1,\n                    unit_price DECIMAL(10,2),\n                    total_amount DECIMAL(12,2),\n                    order_date DATE,\n                    status VARCHAR(20) DEFAULT 'pending',\n                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP\n                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4\n                \"\"\";\n            \n            stmt.execute(createCustomersSql);\n            stmt.execute(createProductsSql);\n            stmt.execute(createOrdersSql);\n            \n            System.out.println(\"✅ 测试表创建完成\");\n            \n            // 插入测试数据\n            insertTestData(conn);\n        }\n    }\n    \n    /**\n     * 插入测试数据\n     */\n    private static void insertTestData(Connection conn) throws SQLException {\n        System.out.println(\"💾 插入测试数据...\");\n        \n        conn.setAutoCommit(false);\n        \n        try {\n            // 插入客户数据\n            insertCustomers(conn, 10000);\n            \n            // 插入商品数据\n            insertProducts(conn, 1000);\n            \n            // 插入订单数据\n            insertOrders(conn, 50000);\n            \n            conn.commit();\n            System.out.println(\"✅ 测试数据插入完成\");\n            \n        } catch (SQLException e) {\n            conn.rollback();\n            throw e;\n        } finally {\n            conn.setAutoCommit(true);\n        }\n    }\n    \n    private static void insertCustomers(Connection conn, int count) throws SQLException {\n        String sql = \"INSERT INTO customers (name, email, phone, city, age, registration_date) VALUES (?, ?, ?, ?, ?, ?)\";\n        \n        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {\n            String[] cities = {\"北京\", \"上海\", \"广州\", \"深圳\", \"杭州\", \"南京\", \"武汉\", \"成都\", \"重庆\", \"西安\"};\n            \n            for (int i = 1; i <= count; i++) {\n                pstmt.setString(1, \"客户\" + i);\n                pstmt.setString(2, \"customer\" + i + \"@example.com\");\n                pstmt.setString(3, \"1\" + String.format(\"%010d\", i));\n                pstmt.setString(4, cities[random.nextInt(cities.length)]);\n                pstmt.setInt(5, 18 + random.nextInt(50));\n                pstmt.setDate(6, new Date(System.currentTimeMillis() - random.nextInt(365 * 24 * 3600 * 1000L)));\n                \n                pstmt.addBatch();\n                \n                if (i % 1000 == 0) {\n                    pstmt.executeBatch();\n                    pstmt.clearBatch();\n                }\n            }\n            \n            pstmt.executeBatch();\n        }\n        \n        System.out.println(\"插入客户数据: \" + count + \" 条\");\n    }\n    \n    private static void insertProducts(Connection conn, int count) throws SQLException {\n        String sql = \"INSERT INTO products (name, category, price, stock_quantity, brand) VALUES (?, ?, ?, ?, ?)\";\n        \n        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {\n            String[] categories = {\"电子产品\", \"服装\", \"食品\", \"图书\", \"家具\", \"运动\", \"美妆\", \"玩具\"};\n            String[] brands = {\"Apple\", \"Samsung\", \"华为\", \"小米\", \"OPPO\", \"VIVO\", \"联想\", \"戴尔\"};\n            \n            for (int i = 1; i <= count; i++) {\n                pstmt.setString(1, \"商品\" + i);\n                pstmt.setString(2, categories[random.nextInt(categories.length)]);\n                pstmt.setBigDecimal(3, java.math.BigDecimal.valueOf(10 + random.nextInt(1000) + random.nextDouble()));\n                pstmt.setInt(4, random.nextInt(1000));\n                pstmt.setString(5, brands[random.nextInt(brands.length)]);\n                \n                pstmt.addBatch();\n                \n                if (i % 500 == 0) {\n                    pstmt.executeBatch();\n                    pstmt.clearBatch();\n                }\n            }\n            \n            pstmt.executeBatch();\n        }\n        \n        System.out.println(\"插入商品数据: \" + count + \" 条\");\n    }\n    \n    private static void insertOrders(Connection conn, int count) throws SQLException {\n        String sql = \"INSERT INTO orders (customer_id, product_id, quantity, unit_price, total_amount, order_date, status) VALUES (?, ?, ?, ?, ?, ?, ?)\";\n        \n        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {\n            String[] statuses = {\"pending\", \"paid\", \"shipped\", \"delivered\", \"cancelled\"};\n            \n            for (int i = 1; i <= count; i++) {\n                int customerId = 1 + random.nextInt(10000);\n                int productId = 1 + random.nextInt(1000);\n                int quantity = 1 + random.nextInt(5);\n                java.math.BigDecimal unitPrice = java.math.BigDecimal.valueOf(10 + random.nextInt(500) + random.nextDouble());\n                java.math.BigDecimal totalAmount = unitPrice.multiply(java.math.BigDecimal.valueOf(quantity));\n                \n                pstmt.setInt(1, customerId);\n                pstmt.setInt(2, productId);\n                pstmt.setInt(3, quantity);\n                pstmt.setBigDecimal(4, unitPrice);\n                pstmt.setBigDecimal(5, totalAmount);\n                pstmt.setDate(6, new Date(System.currentTimeMillis() - random.nextInt(90 * 24 * 3600 * 1000L)));\n                pstmt.setString(7, statuses[random.nextInt(statuses.length)]);\n                \n                pstmt.addBatch();\n                \n                if (i % 2000 == 0) {\n                    pstmt.executeBatch();\n                    pstmt.clearBatch();\n                }\n            }\n            \n            pstmt.executeBatch();\n        }\n        \n        System.out.println(\"插入订单数据: \" + count + \" 条\");\n    }\n    \n    /**\n     * 无索引查询性能测试\n     */\n    private static void testQueryWithoutIndex() throws SQLException {\n        System.out.println(\"\\n🐌 无索引查询性能测试\");\n        \n        String[] queries = {\n            \"SELECT * FROM customers WHERE city = '北京' AND age > 25\",\n            \"SELECT * FROM products WHERE category = '电子产品' AND price BETWEEN 100 AND 500\",\n            \"SELECT c.name, COUNT(*) as order_count FROM customers c JOIN orders o ON c.id = o.customer_id WHERE o.status = 'delivered' GROUP BY c.id\"\n        };\n        \n        for (String query : queries) {\n            executeQueryWithTiming(query, \"无索引\");\n        }\n    }\n    \n    /**\n     * 创建索引并测试性能\n     */\n    private static void createIndexesAndTest() throws SQLException {\n        System.out.println(\"\\n🚀 创建索引并测试性能\");\n        \n        try (Connection conn = dataSource.getConnection();\n             Statement stmt = conn.createStatement()) {\n            \n            // 创建单列索引\n            stmt.execute(\"CREATE INDEX idx_customers_city ON customers(city)\");\n            stmt.execute(\"CREATE INDEX idx_customers_age ON customers(age)\");\n            stmt.execute(\"CREATE INDEX idx_products_category ON products(category)\");\n            stmt.execute(\"CREATE INDEX idx_products_price ON products(price)\");\n            stmt.execute(\"CREATE INDEX idx_orders_customer_id ON orders(customer_id)\");\n            stmt.execute(\"CREATE INDEX idx_orders_status ON orders(status)\");\n            \n            System.out.println(\"✅ 单列索引创建完成\");\n        }\n        \n        // 测试相同查询的性能\n        String[] queries = {\n            \"SELECT * FROM customers WHERE city = '北京'\",\n            \"SELECT * FROM customers WHERE age > 30\",\n            \"SELECT * FROM products WHERE category = '电子产品'\",\n            \"SELECT COUNT(*) FROM orders WHERE status = 'delivered'\"\n        };\n        \n        for (String query : queries) {\n            executeQueryWithTiming(query, \"有索引\");\n            analyzeQueryPlan(query);\n        }\n    }\n    \n    /**\n     * 复合索引演示\n     */\n    private static void demonstrateCompositeIndex() throws SQLException {\n        System.out.println(\"\\n🔗 复合索引演示\");\n        \n        try (Connection conn = dataSource.getConnection();\n             Statement stmt = conn.createStatement()) {\n            \n            // 创建复合索引\n            stmt.execute(\"CREATE INDEX idx_customers_city_age ON customers(city, age)\");\n            stmt.execute(\"CREATE INDEX idx_orders_customer_status_date ON orders(customer_id, status, order_date)\");\n            \n            System.out.println(\"✅ 复合索引创建完成\");\n        }\n        \n        // 测试复合索引的使用\n        String[] queries = {\n            // 可以使用 idx_customers_city_age 索引\n            \"SELECT * FROM customers WHERE city = '上海' AND age = 25\",\n            \"SELECT * FROM customers WHERE city = '上海' AND age > 30\",\n            \"SELECT * FROM customers WHERE city = '上海'\", // 最左前缀\n            \n            // 无法使用 idx_customers_city_age 索引\n            \"SELECT * FROM customers WHERE age = 25\", // 不符合最左前缀\n            \n            // 可以使用 idx_orders_customer_status_date 索引\n            \"SELECT * FROM orders WHERE customer_id = 100 AND status = 'paid'\",\n            \"SELECT * FROM orders WHERE customer_id = 100 AND status = 'paid' AND order_date > '2024-01-01'\"\n        };\n        \n        System.out.println(\"\\n📋 复合索引使用测试:\");\n        for (String query : queries) {\n            System.out.println(\"\\n查询: \" + query);\n            executeQueryWithTiming(query, \"复合索引\");\n            analyzeQueryPlan(query);\n        }\n    }\n    \n    /**\n     * 覆盖索引演示\n     */\n    private static void demonstrateCoveringIndex() throws SQLException {\n        System.out.println(\"\\n🎯 覆盖索引演示\");\n        \n        try (Connection conn = dataSource.getConnection();\n             Statement stmt = conn.createStatement()) {\n            \n            // 创建覆盖索引\n            stmt.execute(\"CREATE INDEX idx_customers_city_name_age ON customers(city, name, age)\");\n            stmt.execute(\"CREATE INDEX idx_orders_customer_total ON orders(customer_id, total_amount)\");\n            \n            System.out.println(\"✅ 覆盖索引创建完成\");\n        }\n        \n        // 覆盖索引查询（不需要回表）\n        String[] coveringQueries = {\n            \"SELECT name, age FROM customers WHERE city = '深圳'\",\n            \"SELECT customer_id, SUM(total_amount) FROM orders WHERE customer_id BETWEEN 100 AND 200 GROUP BY customer_id\"\n        };\n        \n        // 非覆盖索引查询（需要回表）\n        String[] nonCoveringQueries = {\n            \"SELECT * FROM customers WHERE city = '深圳'\",\n            \"SELECT * FROM orders WHERE customer_id = 100\"\n        };\n        \n        System.out.println(\"\\n✅ 覆盖索引查询（无回表）:\");\n        for (String query : coveringQueries) {\n            System.out.println(\"\\n查询: \" + query);\n            executeQueryWithTiming(query, \"覆盖索引\");\n            analyzeQueryPlan(query);\n        }\n        \n        System.out.println(\"\\n❌ 非覆盖索引查询（需回表）:\");\n        for (String query : nonCoveringQueries) {\n            System.out.println(\"\\n查询: \" + query);\n            executeQueryWithTiming(query, \"需要回表\");\n            analyzeQueryPlan(query);\n        }\n    }\n    \n    /**\n     * 索引失效场景演示\n     */\n    private static void demonstrateIndexFailure() throws SQLException {\n        System.out.println(\"\\n⚠️ 索引失效场景演示\");\n        \n        String[] ineffectiveQueries = {\n            // 1. 使用函数\n            \"SELECT * FROM customers WHERE YEAR(registration_date) = 2024\",\n            \n            // 2. 隐式类型转换\n            \"SELECT * FROM customers WHERE phone = 13800138001\", // phone是VARCHAR\n            \n            // 3. 模糊查询前缀通配符\n            \"SELECT * FROM customers WHERE name LIKE '%张%'\",\n            \n            // 4. 不等于操作\n            \"SELECT * FROM customers WHERE city != '北京'\",\n            \n            // 5. OR条件（其中一个字段无索引）\n            \"SELECT * FROM customers WHERE city = '北京' OR email LIKE '%@gmail.com'\",\n            \n            // 6. IS NULL / IS NOT NULL（取决于数据分布）\n            \"SELECT * FROM customers WHERE phone IS NULL\"\n        };\n        \n        String[] effectiveQueries = {\n            // 对应的有效查询\n            \"SELECT * FROM customers WHERE registration_date >= '2024-01-01' AND registration_date < '2025-01-01'\",\n            \"SELECT * FROM customers WHERE phone = '13800138001'\",\n            \"SELECT * FROM customers WHERE name LIKE '张%'\",\n            \"SELECT * FROM customers WHERE city = '上海'\",\n            \"SELECT * FROM customers WHERE city = '北京' UNION SELECT * FROM customers WHERE email LIKE '%@gmail.com'\",\n            \"SELECT * FROM customers WHERE phone = '13800138001'\"\n        };\n        \n        System.out.println(\"\\n❌ 索引失效的查询:\");\n        for (int i = 0; i < ineffectiveQueries.length; i++) {\n            System.out.println(\"\\n失效查询 \" + (i + 1) + \": \" + ineffectiveQueries[i]);\n            analyzeQueryPlan(ineffectiveQueries[i]);\n        }\n        \n        System.out.println(\"\\n✅ 优化后的查询:\");\n        for (int i = 0; i < effectiveQueries.length; i++) {\n            System.out.println(\"\\n优化查询 \" + (i + 1) + \": \" + effectiveQueries[i]);\n            analyzeQueryPlan(effectiveQueries[i]);\n        }\n    }\n    \n    /**\n     * 前缀索引演示\n     */\n    private static void demonstratePrefixIndex() throws SQLException {\n        System.out.println(\"\\n✂️ 前缀索引演示\");\n        \n        try (Connection conn = dataSource.getConnection();\n             Statement stmt = conn.createStatement()) {\n            \n            // 分析前缀选择性\n            analyzePrefixSelectivity();\n            \n            // 创建前缀索引\n            stmt.execute(\"CREATE INDEX idx_customers_email_prefix ON customers(email(10))\");\n            stmt.execute(\"CREATE INDEX idx_products_name_prefix ON products(name(15))\");\n            \n            System.out.println(\"✅ 前缀索引创建完成\");\n        }\n        \n        // 测试前缀索引\n        String[] prefixQueries = {\n            \"SELECT * FROM customers WHERE email = 'customer123@example.com'\",\n            \"SELECT * FROM products WHERE name = '商品123'\"\n        };\n        \n        for (String query : prefixQueries) {\n            System.out.println(\"\\n查询: \" + query);\n            executeQueryWithTiming(query, \"前缀索引\");\n            analyzeQueryPlan(query);\n        }\n    }\n    \n    /**\n     * 分析前缀选择性\n     */\n    private static void analyzePrefixSelectivity() throws SQLException {\n        System.out.println(\"🔍 分析前缀选择性...\");\n        \n        String[] prefixLengths = {\"5\", \"10\", \"15\", \"20\"};\n        \n        for (String length : prefixLengths) {\n            String sql = \"SELECT COUNT(DISTINCT LEFT(email, \" + length + \")) / COUNT(*) as selectivity FROM customers\";\n            \n            try (Connection conn = dataSource.getConnection();\n                 PreparedStatement pstmt = conn.prepareStatement(sql);\n                 ResultSet rs = pstmt.executeQuery()) {\n                \n                if (rs.next()) {\n                    double selectivity = rs.getDouble(\"selectivity\");\n                    System.out.printf(\"邮箱前%s位选择性: %.4f%n\", length, selectivity);\n                }\n            }\n        }\n    }\n    \n    /**\n     * 函数索引演示（MySQL 8.0+）\n     */\n    private static void demonstrateFunctionalIndex() {\n        System.out.println(\"\\n🧮 函数索引演示（MySQL 8.0+）\");\n        \n        try (Connection conn = dataSource.getConnection()) {\n            DatabaseMetaData metaData = conn.getMetaData();\n            String version = metaData.getDatabaseProductVersion();\n            System.out.println(\"当前MySQL版本: \" + version);\n            \n            // 检查是否支持函数索引（MySQL 8.0+）\n            if (version.compareTo(\"8.0\") >= 0) {\n                try (Statement stmt = conn.createStatement()) {\n                    // 创建函数索引\n                    stmt.execute(\"CREATE INDEX idx_customers_year_reg ON customers((YEAR(registration_date)))\");\n                    stmt.execute(\"CREATE INDEX idx_customers_upper_name ON customers((UPPER(name)))\");\n                    \n                    System.out.println(\"✅ 函数索引创建完成\");\n                    \n                    // 测试函数索引\n                    String[] functionalQueries = {\n                        \"SELECT * FROM customers WHERE YEAR(registration_date) = 2024\",\n                        \"SELECT * FROM customers WHERE UPPER(name) = 'CUSTOMER123'\"\n                    };\n                    \n                    for (String query : functionalQueries) {\n                        System.out.println(\"\\n查询: \" + query);\n                        analyzeQueryPlan(query);\n                    }\n                }\n            } else {\n                System.out.println(\"⚠️ 当前MySQL版本不支持函数索引，需要8.0+版本\");\n            }\n            \n        } catch (SQLException e) {\n            System.err.println(\"❌ 函数索引演示失败: \" + e.getMessage());\n        }\n    }\n    \n    /**\n     * 执行查询并计时\n     */\n    private static void executeQueryWithTiming(String sql, String description) {\n        try (Connection conn = dataSource.getConnection();\n             PreparedStatement pstmt = conn.prepareStatement(sql)) {\n            \n            long startTime = System.currentTimeMillis();\n            \n            try (ResultSet rs = pstmt.executeQuery()) {\n                int count = 0;\n                while (rs.next() && count < 5) { // 只处理前5条记录\n                    count++;\n                }\n                \n                long endTime = System.currentTimeMillis();\n                long duration = endTime - startTime;\n                \n                System.out.printf(\"%s - 查询耗时: %d ms%n\", description, duration);\n                \n                if (duration > 1000) {\n                    System.out.println(\"⚠️ 查询耗时较长，建议优化\");\n                }\n            }\n            \n        } catch (SQLException e) {\n            System.err.println(\"❌ 查询执行失败: \" + e.getMessage());\n        }\n    }\n    \n    /**\n     * 分析查询执行计划\n     */\n    private static void analyzeQueryPlan(String sql) {\n        try (Connection conn = dataSource.getConnection();\n             PreparedStatement pstmt = conn.prepareStatement(\"EXPLAIN \" + sql);\n             ResultSet rs = pstmt.executeQuery()) {\n            \n            System.out.println(\"📊 执行计划分析:\");\n            System.out.println(\"Type\\tKey\\t\\tRows\\tExtra\");\n            System.out.println(\"─\".repeat(50));\n            \n            while (rs.next()) {\n                String type = rs.getString(\"type\");\n                String key = rs.getString(\"key\");\n                long rows = rs.getLong(\"rows\");\n                String extra = rs.getString(\"Extra\");\n                \n                System.out.printf(\"%s\\t%s\\t%d\\t%s%n\", \n                    type != null ? type : \"NULL\",\n                    key != null ? key : \"NULL\",\n                    rows,\n                    extra != null ? extra : \"\"\n                );\n                \n                // 性能建议\n                if (\"ALL\".equals(type)) {\n                    System.out.println(\"⚠️ 全表扫描，建议添加索引\");\n                } else if (\"index\".equals(type)) {\n                    System.out.println(\"⚠️ 全索引扫描，考虑优化查询条件\");\n                } else if (\"range\".equals(type) || \"ref\".equals(type) || \"eq_ref\".equals(type)) {\n                    System.out.println(\"✅ 使用了索引，性能良好\");\n                }\n                \n                if (extra != null) {\n                    if (extra.contains(\"Using filesort\")) {\n                        System.out.println(\"⚠️ 使用文件排序，考虑添加排序字段索引\");\n                    }\n                    if (extra.contains(\"Using temporary\")) {\n                        System.out.println(\"⚠️ 使用临时表，考虑优化GROUP BY或ORDER BY\");\n                    }\n                    if (extra.contains(\"Using index\")) {\n                        System.out.println(\"✅ 使用覆盖索引，性能优秀\");\n                    }\n                }\n            }\n            \n        } catch (SQLException e) {\n            System.err.println(\"❌ 执行计划分析失败: \" + e.getMessage());\n        }\n    }\n    \n    /**\n     * 索引使用建议\n     */\n    public static void printIndexOptimizationTips() {\n        System.out.println(\"\\n💡 索引优化建议:\");\n        \n        System.out.println(\"\\n1. 索引设计原则:\");\n        System.out.println(\"   • 在WHERE、ORDER BY、GROUP BY列上建索引\");\n        System.out.println(\"   • 选择性高的列建索引（重复值少的列）\");\n        System.out.println(\"   • 复合索引遵循最左前缀原则\");\n        System.out.println(\"   • 考虑使用覆盖索引减少回表\");\n        \n        System.out.println(\"\\n2. 避免索引失效:\");\n        System.out.println(\"   • 避免在索引列上使用函数\");\n        System.out.println(\"   • 避免隐式类型转换\");\n        System.out.println(\"   • 模糊查询避免前缀通配符\");\n        System.out.println(\"   • 慎用NOT、!=、IS NULL\");\n        \n        System.out.println(\"\\n3. 索引维护:\");\n        System.out.println(\"   • 定期分析表和索引统计信息\");\n        System.out.println(\"   • 删除不使用的索引\");\n        System.out.println(\"   • 监控索引的使用情况\");\n        System.out.println(\"   • 考虑索引的维护成本\");\n        \n        System.out.println(\"\\n4. 性能监控:\");\n        System.out.println(\"   • 使用EXPLAIN分析查询计划\");\n        System.out.println(\"   • 监控慢查询日志\");\n        System.out.println(\"   • 使用Performance Schema\");\n        System.out.println(\"   • 定期进行性能测试\");\n    }\n}