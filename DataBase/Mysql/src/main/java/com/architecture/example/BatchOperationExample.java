package com.architecture.example;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.math.BigDecimal;

/**
 * 批量操作示例
 * 演示：批量插入、批量更新、分页处理、并发批量操作
 * 
 * 主要功能：
 * 1. 不同批量插入方法的性能对比
 * 2. 批量更新和删除操作
 * 3. 大数据量处理策略
 * 4. 并发批量操作
 * 5. 批量操作错误处理和恢复
 * 6. 性能优化技巧
 */
public class BatchOperationExample {
    
    private static final DataSource dataSource = ConnectionPoolExample.getDataSource();
    private static final int BATCH_SIZE = 1000;
    
    /**
     * 测试批量操作
     */
    public static void testBatchOperations() {
        System.out.println("🚀 MySQL批量操作性能优化演示");
        System.out.println("=".repeat(60));
        
        try {
            // 1. 创建测试表
            setupBatchTestTable();
            
            // 2. 批量插入对比
            compareBatchInsertMethods();
            
            // 3. 批量更新演示
            demonstrateBatchUpdate();
            
            // 4. 批量删除演示
            demonstrateBatchDelete();
            
            // 5. 大数据量处理
            demonstrateLargeDataProcessing();
            
            // 6. 并发批量操作
            demonstrateConcurrentBatchOperation();
            
            // 7. 错误处理和事务回滚
            demonstrateBatchErrorHandling();
            
            // 8. 输出优化建议
            printBatchOptimizationTips();
            
        } catch (Exception e) {
            System.err.println("❌ 批量操作测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 创建批量测试表
     */
    private static void setupBatchTestTable() throws SQLException {
        System.out.println("\n📋 创建批量测试表...");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // 删除已存在的表
            stmt.execute("DROP TABLE IF EXISTS batch_test");
            stmt.execute("DROP TABLE IF EXISTS user_scores");

            // 创建批量测试表
            String createBatchTestSql = """
                CREATE TABLE batch_test (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(100),
                    score DECIMAL(5,2) DEFAULT 0,
                    status TINYINT DEFAULT 1,
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_user_id (user_id),
                    INDEX idx_email (email),
                    INDEX idx_score (score)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;

            // 创建用户成绩表
            String createUserScoresSql = """
                CREATE TABLE user_scores (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    subject VARCHAR(50) NOT NULL,
                    score DECIMAL(5,2) NOT NULL,
                    exam_date DATE NOT NULL,
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_user_subject_date (user_id, subject, exam_date),
                    INDEX idx_user_id (user_id),
                    INDEX idx_subject (subject),
                    INDEX idx_exam_date (exam_date)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;

            stmt.execute(createBatchTestSql);
            stmt.execute(createUserScoresSql);

            System.out.println("✅ 批量测试表创建完成");
        }
    }

    /**
     * 比较不同批量插入方法的性能
     */
    private static void compareBatchInsertMethods() throws SQLException {
        System.out.println("\n⚡ 批量插入方法性能对比");
        System.out.println("=".repeat(50));

        int recordCount = 10000;

        // 方法1：逐条插入
        testSingleInsert(recordCount);

        // 方法2：批量插入
        testBatchInsert(recordCount);

        // 方法3：批量插入 + 事务
        testBatchInsertWithTransaction(recordCount);

        // 方法4：多值插入
        testMultiValueInsert(recordCount);
    }

    /**
     * 逐条插入测试
     */
    private static void testSingleInsert(int count) throws SQLException {
        System.out.println("\n🐌 方法1：逐条插入测试");

        // 清空表
        clearTable("batch_test");

        long startTime = System.currentTimeMillis();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO batch_test (user_id, name, email, score) VALUES (?, ?, ?, ?)")) {

            for (int i = 1; i <= count; i++) {
                pstmt.setInt(1, i);
                pstmt.setString(2, "用户" + i);
                pstmt.setString(3, "user" + i + "@example.com");
                pstmt.setBigDecimal(4, BigDecimal.valueOf(60 + Math.random() * 40));

                pstmt.executeUpdate();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("  插入 %d 条记录，耗时: %d ms%n", count, endTime - startTime);
    }

    /**
     * 批量插入测试
     */
    private static void testBatchInsert(int count) throws SQLException {
        System.out.println("\n🚀 方法2：批量插入测试");

        // 清空表
        clearTable("batch_test");

        long startTime = System.currentTimeMillis();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO batch_test (user_id, name, email, score) VALUES (?, ?, ?, ?)")) {

            for (int i = 1; i <= count; i++) {
                pstmt.setInt(1, i);
                pstmt.setString(2, "用户" + i);
                pstmt.setString(3, "user" + i + "@example.com");
                pstmt.setBigDecimal(4, BigDecimal.valueOf(60 + Math.random() * 40));

                pstmt.addBatch();

                // 每1000条执行一次批量插入
                if (i % BATCH_SIZE == 0) {
                    pstmt.executeBatch();
                    pstmt.clearBatch();
                }
            }

            // 处理剩余的记录
            pstmt.executeBatch();
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("  插入 %d 条记录，耗时: %d ms%n", count, endTime - startTime);
    }

    /**
     * 批量插入 + 事务测试
     */
    private static void testBatchInsertWithTransaction(int count) throws SQLException {
        System.out.println("\n💪 方法3：批量插入 + 事务测试");

        // 清空表
        clearTable("batch_test");

        long startTime = System.currentTimeMillis();

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO batch_test (user_id, name, email, score) VALUES (?, ?, ?, ?)")) {

                for (int i = 1; i <= count; i++) {
                    pstmt.setInt(1, i);
                    pstmt.setString(2, "用户" + i);
                    pstmt.setString(3, "user" + i + "@example.com");
                    pstmt.setBigDecimal(4, BigDecimal.valueOf(60 + Math.random() * 40));

                    pstmt.addBatch();

                    // 每1000条执行一次批量插入
                    if (i % BATCH_SIZE == 0) {
                        pstmt.executeBatch();
                        pstmt.clearBatch();
                        conn.commit(); // 分批提交事务
                    }
                }

                // 处理剩余的记录
                pstmt.executeBatch();
                conn.commit();
            }

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("  插入 %d 条记录，耗时: %d ms%n", count, endTime - startTime);
    }

    /**
     * 多值插入测试
     */
    private static void testMultiValueInsert(int count) throws SQLException {
        System.out.println("\n🌟 方法4：多值插入测试");

        // 清空表
        clearTable("batch_test");

        long startTime = System.currentTimeMillis();

        try (Connection conn = dataSource.getConnection()) {

            int batchCount = 0;
            StringBuilder sql = new StringBuilder("INSERT INTO batch_test (user_id, name, email, score) VALUES ");

            for (int i = 1; i <= count; i++) {
                if (batchCount > 0) {
                    sql.append(", ");
                }

                sql.append(String.format("(%d, '用户%d', 'user%d@example.com', %.2f)", 
                    i, i, i, 60 + Math.random() * 40));

                batchCount++;

                // 每500条执行一次插入（避免SQL过长）
                if (batchCount == 500 || i == count) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate(sql.toString());
                    }

                    // 重置StringBuilder
                    sql.setLength(0);
                    sql.append("INSERT INTO batch_test (user_id, name, email, score) VALUES ");
                    batchCount = 0;
                }
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("  插入 %d 条记录，耗时: %d ms%n", count, endTime - startTime);
        
        // 显示性能对比总结
        System.out.println("\n📊 性能对比总结:");
        System.out.println("  🐌 逐条插入: 最慢，适合少量数据");
        System.out.println("  🚀 批量插入: 较快，推荐使用");
        System.out.println("  💪 批量+事务: 最快，大批量首选");
        System.out.println("  🌟 多值插入: 快速，但SQL长度有限制");
    }

    /**
     * 批量更新演示
     */
    private static void demonstrateBatchUpdate() throws SQLException {
        System.out.println("\n🔄 批量更新演示");
        System.out.println("=".repeat(40));

        // 准备测试数据
        if (getTableRowCount("batch_test") == 0) {
            testBatchInsert(5000);
        }

        long startTime = System.currentTimeMillis();

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // 批量更新成绩
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE batch_test SET score = score + ?, update_time = NOW() WHERE user_id = ?")) {

                int updateCount = 0;

                for (int userId = 1; userId <= 3000; userId++) {
                    double bonus = Math.random() * 10; // 随机加分

                    pstmt.setBigDecimal(1, BigDecimal.valueOf(bonus));
                    pstmt.setInt(2, userId);
                    pstmt.addBatch();

                    updateCount++;

                    if (updateCount % BATCH_SIZE == 0) {
                        int[] results = pstmt.executeBatch();
                        pstmt.clearBatch();

                        System.out.printf("  已更新 %d 条记录%n", updateCount);
                    }
                }

                // 处理剩余的更新
                int[] results = pstmt.executeBatch();
                conn.commit();

                System.out.printf("✅ 批量更新完成，总共更新 %d 条记录%n", updateCount);
            }

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("⏱️ 批量更新耗时: %d ms%n", endTime - startTime);
    }

    /**
     * 批量删除演示
     */
    private static void demonstrateBatchDelete() throws SQLException {
        System.out.println("\n🗑️ 批量删除演示");
        System.out.println("=".repeat(40));

        long startTime = System.currentTimeMillis();

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // 分批删除成绩低于70的记录
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "DELETE FROM batch_test WHERE score < ? LIMIT ?")) {

                int totalDeleted = 0;
                int batchSize = 500;

                while (true) {
                    pstmt.setBigDecimal(1, BigDecimal.valueOf(70));
                    pstmt.setInt(2, batchSize);

                    int deleted = pstmt.executeUpdate();
                    totalDeleted += deleted;

                    if (deleted < batchSize) {
                        // 没有更多记录需要删除
                        break;
                    }

                    System.out.printf("  已删除 %d 条记录，累计删除: %d%n", deleted, totalDeleted);

                    // 提交当前批次
                    conn.commit();

                    // 避免长时间锁定
                    Thread.sleep(10);
                }

                conn.commit();
                System.out.printf("✅ 批量删除完成，总共删除 %d 条记录%n", totalDeleted);
            }

        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }
            throw new SQLException(e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("⏱️ 批量删除耗时: %d ms%n", endTime - startTime);
    }

    /**
     * 大数据量处理演示
     */
    private static void demonstrateLargeDataProcessing() throws SQLException {
        System.out.println("\n📈 大数据量处理演示");
        System.out.println("=".repeat(40));

        // 模拟处理100万条记录
        int totalRecords = 100000; // 减少到10万以便演示
        int batchSize = 10000;

        System.out.printf("开始处理 %d 条记录，批次大小: %d%n", totalRecords, batchSize);

        long startTime = System.currentTimeMillis();

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            // 清空表
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("TRUNCATE TABLE user_scores");
            }

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO user_scores (user_id, subject, score, exam_date) VALUES (?, ?, ?, ?)")) {

                String[] subjects = {"数学", "英语", "语文", "物理", "化学"};
                java.sql.Date examDate = new java.sql.Date(System.currentTimeMillis());

                for (int i = 1; i <= totalRecords; i++) {
                    int userId = (i - 1) % 10000 + 1; // 假设有1万个用户
                    String subject = subjects[(i - 1) % subjects.length];
                    double score = 60 + Math.random() * 40;

                    pstmt.setInt(1, userId);
                    pstmt.setString(2, subject);
                    pstmt.setBigDecimal(3, BigDecimal.valueOf(score));
                    pstmt.setDate(4, examDate);

                    pstmt.addBatch();

                    if (i % batchSize == 0) {
                        pstmt.executeBatch();
                        pstmt.clearBatch();
                        conn.commit();

                        long currentTime = System.currentTimeMillis();
                        double progress = (double) i / totalRecords * 100;
                        long elapsedTime = currentTime - startTime;
                        long estimatedTotal = (long) (elapsedTime / (i / (double) totalRecords));
                        long remainingTime = estimatedTotal - elapsedTime;

                        System.out.printf("  进度: %.1f%% (%d/%d), 已用时: %d ms, 预计剩余: %d ms%n", 
                            progress, i, totalRecords, elapsedTime, remainingTime);
                    }
                }

                // 处理剩余记录
                pstmt.executeBatch();
                conn.commit();
            }

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("✅ 大数据量处理完成，总耗时: %d ms (%.2f 秒)%n", 
            endTime - startTime, (endTime - startTime) / 1000.0);
    }

    /**
     * 并发批量操作演示
     */
    private static void demonstrateConcurrentBatchOperation() throws Exception {
        System.out.println("\n🔀 并发批量操作演示");
        System.out.println("=".repeat(40));

        // 清空表
        clearTable("batch_test");

        int threadCount = 4;
        int recordsPerThread = 5000;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            final int startId = t * recordsPerThread + 1;
            final int endId = (t + 1) * recordsPerThread;

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    insertRecordsBatch(threadId, startId, endId);
                } catch (SQLException e) {
                    System.err.printf("线程 %d 执行失败: %s%n", threadId, e.getMessage());
                }
            }, executor);

            futures.add(future);
        }

        // 等待所有线程完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long endTime = System.currentTimeMillis();

        // 验证结果
        int totalInserted = getTableRowCount("batch_test");

        System.out.printf("✅ 并发批量插入完成:%n");
        System.out.printf("  线程数: %d%n", threadCount);
        System.out.printf("  每线程记录数: %d%n", recordsPerThread);
        System.out.printf("  总记录数: %d%n", totalInserted);
        System.out.printf("  总耗时: %d ms%n", endTime - startTime);

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
    }

    /**
     * 线程内批量插入记录
     */
    private static void insertRecordsBatch(int threadId, int startId, int endId) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO batch_test (user_id, name, email, score) VALUES (?, ?, ?, ?)")) {

                int batchCount = 0;

                for (int id = startId; id <= endId; id++) {
                    pstmt.setInt(1, id);
                    pstmt.setString(2, "用户" + id + "_T" + threadId);
                    pstmt.setString(3, "user" + id + "_t" + threadId + "@example.com");
                    pstmt.setBigDecimal(4, BigDecimal.valueOf(60 + Math.random() * 40));

                    pstmt.addBatch();
                    batchCount++;

                    if (batchCount % BATCH_SIZE == 0) {
                        pstmt.executeBatch();
                        pstmt.clearBatch();
                        conn.commit();

                        System.out.printf("  线程 %d: 已插入 %d 条记录%n", threadId, batchCount);
                    }
                }

                // 处理剩余记录
                pstmt.executeBatch();
                conn.commit();

                System.out.printf("  线程 %d: 完成插入 %d 条记录%n", threadId, endId - startId + 1);
            }

        } catch (SQLException e) {
            System.err.printf("线程 %d 插入失败: %s%n", threadId, e.getMessage());
            throw e;
        }
    }

    /**
     * 批量操作错误处理演示
     */
    private static void demonstrateBatchErrorHandling() throws SQLException {
        System.out.println("\n⚠️ 批量操作错误处理演示");
        System.out.println("=".repeat(40));

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO batch_test (user_id, name, email, score) VALUES (?, ?, ?, ?)")) {

                // 准备一些正常数据和一些会出错的数据
                for (int i = 1; i <= 100; i++) {
                    pstmt.setInt(1, 90000 + i);
                    pstmt.setString(2, "测试用户" + i);

                    if (i == 50) {
                        // 故意插入超长邮箱引发错误
                        pstmt.setString(3, "very_long_email_" + "x".repeat(200) + "@example.com");
                    } else {
                        pstmt.setString(3, "test" + i + "@example.com");
                    }

                    pstmt.setBigDecimal(4, BigDecimal.valueOf(70 + Math.random() * 30));
                    pstmt.addBatch();
                }

                try {
                    int[] results = pstmt.executeBatch();
                    conn.commit();

                    System.out.println("✅ 批量插入成功，结果: " + java.util.Arrays.toString(results));

                } catch (BatchUpdateException e) {
                    System.err.println("❌ 批量操作中发生错误: " + e.getMessage());
                    System.err.println("  错误代码: " + e.getErrorCode());
                    System.err.println("  SQL状态: " + e.getSQLState());

                    // 获取部分成功的结果
                    int[] updateCounts = e.getUpdateCounts();
                    System.out.println("  部分执行结果: " + java.util.Arrays.toString(updateCounts));

                    // 回滚事务
                    conn.rollback();
                    System.out.println("⚠️ 事务已回滚");

                    // 重新执行，跳过错误记录
                    retryBatchWithErrorSkip(conn, pstmt);
                }
            }

        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * 跳过错误记录重新执行批量操作
     */
    private static void retryBatchWithErrorSkip(Connection conn, PreparedStatement pstmt) throws SQLException {
        System.out.println("🔄 跳过错误记录重新执行...");

        pstmt.clearBatch();

        // 重新添加正确的数据（跳过第50条）
        for (int i = 1; i <= 100; i++) {
            if (i == 50) {
                continue; // 跳过会出错的记录
            }

            pstmt.setInt(1, 90000 + i);
            pstmt.setString(2, "测试用户" + i);
            pstmt.setString(3, "test" + i + "@example.com");
            pstmt.setBigDecimal(4, BigDecimal.valueOf(70 + Math.random() * 30));
            pstmt.addBatch();
        }

        try {
            int[] results = pstmt.executeBatch();
            conn.commit();

            System.out.printf("✅ 重新执行成功，插入 %d 条记录%n", results.length);

        } catch (SQLException e) {
            conn.rollback();
            System.err.println("❌ 重新执行仍然失败: " + e.getMessage());
        }
    }

    // ========== 辅助方法 ==========

    /**
     * 清空表数据
     */
    private static void clearTable(String tableName) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM " + tableName);
        }
    }

    /**
     * 获取表行数
     */
    private static int getTableRowCount(String tableName) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM " + tableName);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    /**
     * 批量操作优化建议
     */
    public static void printBatchOptimizationTips() {
        System.out.println("\n💡 MySQL批量操作优化建议");
        System.out.println("=".repeat(50));

        System.out.println("\n1️⃣ 批量大小选择:");
        System.out.println("   • 一般推荐1000-5000条记录为一批");
        System.out.println("   • 过大可能导致内存问题和锁等待");
        System.out.println("   • 过小无法发挥批量优势");

        System.out.println("\n2️⃣ 事务控制:");
        System.out.println("   • 使用手动事务控制提交时机");
        System.out.println("   • 分批提交避免长事务");
        System.out.println("   • 合理使用Savepoint进行部分回滚");

        System.out.println("\n3️⃣ 性能优化:");
        System.out.println("   • 使用PreparedStatement重用执行计划");
        System.out.println("   • 关闭自动提交提高性能");
        System.out.println("   • 考虑暂时禁用索引（大批量插入时）");
        System.out.println("   • 使用多值INSERT语句");

        System.out.println("\n4️⃣ 错误处理:");
        System.out.println("   • 使用BatchUpdateException处理部分失败");
        System.out.println("   • 实现重试机制");
        System.out.println("   • 记录失败的记录用于后续处理");

        System.out.println("\n5️⃣ 监控和调试:");
        System.out.println("   • 监控批量操作的执行时间");
        System.out.println("   • 记录操作日志便于问题排查");
        System.out.println("   • 定期检查数据一致性");

        System.out.println("\n6️⃣ 高级技巧:");
        System.out.println("   • 使用LOAD DATA INFILE处理超大文件");
        System.out.println("   • 合理使用并发线程");
        System.out.println("   • 考虑分库分表策略");
        System.out.println("   • 使用读写分离减少主库压力");
    }

    /**
     * 主测试方法
     */
    public static void main(String[] args) {
        testBatchOperations();
    }
}