package com.architecture.example;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;

/**
 * 事务处理示例
 * 演示：事务的ACID特性、隔离级别、死锁处理、分布式事务
 */
public class TransactionExample {
    
    private static final DataSource dataSource = ConnectionPoolExample.getDataSource();
    
    /**
     * 测试事务处理
     */
    public static void testTransaction() {
        try {
            // 1. 创建测试表
            setupTestTables();
            
            // 2. 基本事务操作
            demonstrateBasicTransaction();
            
            // 3. 事务回滚
            demonstrateTransactionRollback();
            
            // 4. 事务隔离级别
            demonstrateIsolationLevels();
            
            // 5. 转账场景（经典事务案例）
            demonstrateTransferTransaction();
            
            // 6. 死锁演示
            demonstrateDeadlock();
            
            // 7. 批量事务处理
            demonstrateBatchTransaction();
            
        } catch (Exception e) {
            System.err.println("❌ 事务测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建测试表
     */
    private static void setupTestTables() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 创建账户表
            String createAccountSql = """
                CREATE TABLE IF NOT EXISTS accounts (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    account_no VARCHAR(20) UNIQUE NOT NULL,
                    balance DECIMAL(15,2) NOT NULL DEFAULT 0,
                    status TINYINT DEFAULT 1,
                    version INT DEFAULT 0,
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;
            
            // 创建交易记录表
            String createTransactionLogSql = """
                CREATE TABLE IF NOT EXISTS transaction_logs (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    from_account VARCHAR(20),
                    to_account VARCHAR(20),
                    amount DECIMAL(15,2),
                    transaction_type VARCHAR(20),
                    status VARCHAR(20),
                    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """;
            
            stmt.execute(createAccountSql);
            stmt.execute(createTransactionLogSql);
            
            // 清空测试数据
            stmt.execute("DELETE FROM accounts");
            stmt.execute("DELETE FROM transaction_logs");
            
            // 插入测试账户
            stmt.execute("INSERT INTO accounts (account_no, balance) VALUES ('ACC001', 1000.00)");
            stmt.execute("INSERT INTO accounts (account_no, balance) VALUES ('ACC002', 500.00)");
            stmt.execute("INSERT INTO accounts (account_no, balance) VALUES ('ACC003', 800.00)");
            
            System.out.println("✅ 测试表创建完成");
        }
    }
    
    /**
     * 基本事务操作演示
     */
    private static void demonstrateBasicTransaction() throws SQLException {
        System.out.println("\n💰 基本事务操作演示");
        
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            
            // 关闭自动提交
            conn.setAutoCommit(false);
            
            System.out.println("当前自动提交状态: " + conn.getAutoCommit());
            
            // 执行一些操作
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance + ? WHERE account_no = ?")) {
                
                // 给账户ACC001增加100元
                pstmt.setBigDecimal(1, new BigDecimal("100.00"));
                pstmt.setString(2, "ACC001");
                int affected = pstmt.executeUpdate();
                
                System.out.println("更新了 " + affected + " 条记录");
                
                // 手动提交事务
                conn.commit();
                System.out.println("✅ 事务提交成功");
                
                // 查询更新后的余额
                queryBalance("ACC001");
            }
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("⚠️ 事务已回滚");
                } catch (SQLException rollbackEx) {
                    System.err.println("❌ 回滚失败: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // 恢复自动提交
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("❌ 关闭连接失败: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 事务回滚演示
     */
    private static void demonstrateTransactionRollback() throws SQLException {
        System.out.println("\n🔄 事务回滚演示");
        
        Connection conn = null;
        Savepoint savepoint = null;
        
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            
            // 查询初始余额
            BigDecimal initialBalance = queryBalance("ACC002");
            System.out.println("初始余额: " + initialBalance);
            
            // 创建保存点
            savepoint = conn.setSavepoint("before_update");
            System.out.println("✅ 创建保存点: before_update");
            
            // 执行更新操作
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance - ? WHERE account_no = ?")) {
                
                pstmt.setBigDecimal(1, new BigDecimal("200.00"));
                pstmt.setString(2, "ACC002");
                pstmt.executeUpdate();
                
                BigDecimal newBalance = queryBalance("ACC002");
                System.out.println("更新后余额: " + newBalance);
                
                // 模拟业务异常
                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                    throw new SQLException("余额不足，触发回滚");
                }
                
                conn.commit();
                System.out.println("✅ 事务提交成功");
                
            } catch (SQLException e) {
                System.err.println("❌ 发生异常: " + e.getMessage());
                
                if (savepoint != null) {
                    conn.rollback(savepoint);
                    System.out.println("⚠️ 回滚到保存点");
                } else {
                    conn.rollback();
                    System.out.println("⚠️ 完全回滚事务");
                }
                
                // 验证回滚结果
                BigDecimal finalBalance = queryBalance("ACC002");
                System.out.println("回滚后余额: " + finalBalance);
            }
            
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("❌ 关闭连接失败: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 演示事务隔离级别
     */
    private static void demonstrateIsolationLevels() throws SQLException {
        System.out.println("\n🔒 事务隔离级别演示");
        
        // 获取当前隔离级别
        try (Connection conn = dataSource.getConnection()) {
            int isolationLevel = conn.getTransactionIsolation();
            System.out.println("当前隔离级别: " + getIsolationLevelName(isolationLevel));
        }
        
        // 演示可重复读
        demonstrateRepeatableRead();
        
        // 演示读已提交
        demonstrateReadCommitted();
    }
    
    /**
     * 演示REPEATABLE READ隔离级别
     */
    private static void demonstrateRepeatableRead() throws SQLException {
        System.out.println("\n📖 REPEATABLE READ 隔离级别演示");
        
        // 开启两个连接模拟并发事务\n        try (Connection conn1 = dataSource.getConnection();\n             Connection conn2 = dataSource.getConnection()) {\n            \n            conn1.setAutoCommit(false);\n            conn2.setAutoCommit(false);\n            \n            // 设置隔离级别\n            conn1.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);\n            \n            // 事务1：第一次读取\n            BigDecimal balance1 = queryBalanceWithConnection(\"ACC001\", conn1);\n            System.out.println(\"事务1第一次读取 ACC001 余额: \" + balance1);\n            \n            // 事务2：修改数据并提交\n            try (PreparedStatement pstmt = conn2.prepareStatement(\n                    \"UPDATE accounts SET balance = balance + 50 WHERE account_no = ?\")) {\n                pstmt.setString(1, \"ACC001\");\n                pstmt.executeUpdate();\n                conn2.commit();\n                System.out.println(\"事务2：给 ACC001 增加 50 元并提交\");\n            }\n            \n            // 事务1：第二次读取（应该看到相同的值）\n            BigDecimal balance2 = queryBalanceWithConnection(\"ACC001\", conn1);\n            System.out.println(\"事务1第二次读取 ACC001 余额: \" + balance2);\n            \n            if (balance1.equals(balance2)) {\n                System.out.println(\"✅ REPEATABLE READ 验证成功：两次读取结果一致\");\n            } else {\n                System.out.println(\"❌ REPEATABLE READ 验证失败：出现不可重复读\");\n            }\n            \n            conn1.commit();\n            \n            // 事务1提交后再次查询\n            BigDecimal balance3 = queryBalance(\"ACC001\");\n            System.out.println(\"事务1提交后 ACC001 余额: \" + balance3);\n        }\n    }\n    \n    /**\n     * 演示READ COMMITTED隔离级别\n     */\n    private static void demonstrateReadCommitted() throws SQLException {\n        System.out.println(\"\\n📝 READ COMMITTED 隔离级别演示\");\n        \n        try (Connection conn1 = dataSource.getConnection();\n             Connection conn2 = dataSource.getConnection()) {\n            \n            conn1.setAutoCommit(false);\n            conn2.setAutoCommit(false);\n            \n            // 设置隔离级别\n            conn1.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);\n            \n            // 事务1：第一次读取\n            BigDecimal balance1 = queryBalanceWithConnection(\"ACC002\", conn1);\n            System.out.println(\"事务1第一次读取 ACC002 余额: \" + balance1);\n            \n            // 事务2：修改数据并提交\n            try (PreparedStatement pstmt = conn2.prepareStatement(\n                    \"UPDATE accounts SET balance = balance + 30 WHERE account_no = ?\")) {\n                pstmt.setString(1, \"ACC002\");\n                pstmt.executeUpdate();\n                conn2.commit();\n                System.out.println(\"事务2：给 ACC002 增加 30 元并提交\");\n            }\n            \n            // 事务1：第二次读取（会看到最新提交的值）\n            BigDecimal balance2 = queryBalanceWithConnection(\"ACC002\", conn1);\n            System.out.println(\"事务1第二次读取 ACC002 余额: \" + balance2);\n            \n            if (!balance1.equals(balance2)) {\n                System.out.println(\"✅ READ COMMITTED 验证成功：读取到已提交的最新数据\");\n            } else {\n                System.out.println(\"❌ read COMMITTED 验证失败：未读取到最新数据\");\n            }\n            \n            conn1.commit();\n        }\n    }\n    \n    /**\n     * 转账事务演示（经典案例）\n     */\n    private static void demonstrateTransferTransaction() throws SQLException {\n        System.out.println(\"\\n💸 转账事务演示（经典案例）\");\n        \n        String fromAccount = \"ACC001\";\n        String toAccount = \"ACC003\";\n        BigDecimal amount = new BigDecimal(\"150.00\");\n        \n        System.out.printf(\"转账操作: %s -> %s, 金额: %s%n\", fromAccount, toAccount, amount);\n        \n        // 查询转账前余额\n        BigDecimal fromBalanceBefore = queryBalance(fromAccount);\n        BigDecimal toBalanceBefore = queryBalance(toAccount);\n        System.out.printf(\"转账前余额 - %s: %s, %s: %s%n\", \n            fromAccount, fromBalanceBefore, toAccount, toBalanceBefore);\n        \n        Connection conn = null;\n        try {\n            conn = dataSource.getConnection();\n            conn.setAutoCommit(false);\n            \n            // 检查余额是否足够\n            if (fromBalanceBefore.compareTo(amount) < 0) {\n                throw new SQLException(\"余额不足，无法完成转账\");\n            }\n            \n            // 1. 从转出账户扣款\n            try (PreparedStatement pstmt = conn.prepareStatement(\n                    \"UPDATE accounts SET balance = balance - ?, version = version + 1 \" +\n                    \"WHERE account_no = ? AND version = ?\")) {\n                \n                pstmt.setBigDecimal(1, amount);\n                pstmt.setString(2, fromAccount);\n                pstmt.setInt(3, getCurrentVersion(fromAccount));\n                \n                int affected = pstmt.executeUpdate();\n                if (affected == 0) {\n                    throw new SQLException(\"转出账户更新失败，可能被其他事务修改\");\n                }\n            }\n            \n            // 记录转账日志\n            logTransaction(conn, fromAccount, toAccount, amount, \"TRANSFER\", \"PROCESSING\");\n            \n            // 模拟网络延迟或其他处理时间\n            Thread.sleep(100);\n            \n            // 2. 向转入账户加款\n            try (PreparedStatement pstmt = conn.prepareStatement(\n                    \"UPDATE accounts SET balance = balance + ?, version = version + 1 \" +\n                    \"WHERE account_no = ?\")) {\n                \n                pstmt.setBigDecimal(1, amount);\n                pstmt.setString(2, toAccount);\n                \n                int affected = pstmt.executeUpdate();\n                if (affected == 0) {\n                    throw new SQLException(\"转入账户更新失败\");\n                }\n            }\n            \n            // 更新交易状态为成功\n            updateTransactionStatus(conn, fromAccount, toAccount, \"SUCCESS\");\n            \n            // 提交事务\n            conn.commit();\n            System.out.println(\"✅ 转账成功！\");\n            \n            // 查询转账后余额\n            BigDecimal fromBalanceAfter = queryBalance(fromAccount);\n            BigDecimal toBalanceAfter = queryBalance(toAccount);\n            System.out.printf(\"转账后余额 - %s: %s, %s: %s%n\", \n                fromAccount, fromBalanceAfter, toAccount, toBalanceAfter);\n            \n            // 验证转账金额\n            BigDecimal expectedFromBalance = fromBalanceBefore.subtract(amount);\n            BigDecimal expectedToBalance = toBalanceBefore.add(amount);\n            \n            if (fromBalanceAfter.equals(expectedFromBalance) && \n                toBalanceAfter.equals(expectedToBalance)) {\n                System.out.println(\"✅ 转账金额验证正确\");\n            } else {\n                System.out.println(\"❌ 转账金额验证失败\");\n            }\n            \n        } catch (Exception e) {\n            System.err.println(\"❌ 转账失败: \" + e.getMessage());\n            if (conn != null) {\n                try {\n                    conn.rollback();\n                    // 更新交易状态为失败\n                    updateTransactionStatus(conn, fromAccount, toAccount, \"FAILED\");\n                    conn.commit();\n                    System.out.println(\"⚠️ 事务已回滚\");\n                } catch (SQLException rollbackEx) {\n                    System.err.println(\"❌ 回滚失败: \" + rollbackEx.getMessage());\n                }\n            }\n        } finally {\n            if (conn != null) {\n                try {\n                    conn.setAutoCommit(true);\n                    conn.close();\n                } catch (SQLException e) {\n                    System.err.println(\"❌ 关闭连接失败: \" + e.getMessage());\n                }\n            }\n        }\n    }\n    \n    /**\n     * 死锁演示\n     */\n    private static void demonstrateDeadlock() {\n        System.out.println(\"\\n💀 死锁演示\");\n        \n        Thread thread1 = new Thread(() -> {\n            try {\n                transferWithDelay(\"ACC001\", \"ACC002\", new BigDecimal(\"10.00\"), 1000);\n            } catch (Exception e) {\n                System.err.println(\"线程1异常: \" + e.getMessage());\n            }\n        }, \"Thread-1\");\n        \n        Thread thread2 = new Thread(() -> {\n            try {\n                transferWithDelay(\"ACC002\", \"ACC001\", new BigDecimal(\"5.00\"), 1000);\n            } catch (Exception e) {\n                System.err.println(\"线程2异常: \" + e.getMessage());\n            }\n        }, \"Thread-2\");\n        \n        thread1.start();\n        thread2.start();\n        \n        try {\n            thread1.join(5000);\n            thread2.join(5000);\n        } catch (InterruptedException e) {\n            Thread.currentThread().interrupt();\n        }\n        \n        System.out.println(\"死锁演示完成\");\n    }\n    \n    /**\n     * 带延迟的转账操作（用于死锁演示）\n     */\n    private static void transferWithDelay(String fromAccount, String toAccount, \n                                        BigDecimal amount, long delayMs) throws SQLException {\n        \n        try (Connection conn = dataSource.getConnection()) {\n            conn.setAutoCommit(false);\n            \n            System.out.println(Thread.currentThread().getName() + \n                \" 开始转账: \" + fromAccount + \" -> \" + toAccount);\n            \n            // 锁定第一个账户\n            try (PreparedStatement pstmt = conn.prepareStatement(\n                    \"SELECT balance FROM accounts WHERE account_no = ? FOR UPDATE\")) {\n                pstmt.setString(1, fromAccount);\n                ResultSet rs = pstmt.executeQuery();\n                if (rs.next()) {\n                    System.out.println(Thread.currentThread().getName() + \n                        \" 锁定账户: \" + fromAccount);\n                }\n            }\n            \n            // 模拟业务处理延迟\n            Thread.sleep(delayMs);\n            \n            // 尝试锁定第二个账户（可能导致死锁）\n            try (PreparedStatement pstmt = conn.prepareStatement(\n                    \"SELECT balance FROM accounts WHERE account_no = ? FOR UPDATE\")) {\n                pstmt.setString(1, toAccount);\n                ResultSet rs = pstmt.executeQuery();\n                if (rs.next()) {\n                    System.out.println(Thread.currentThread().getName() + \n                        \" 锁定账户: \" + toAccount);\n                }\n            }\n            \n            // 执行转账\n            try (PreparedStatement pstmt1 = conn.prepareStatement(\n                    \"UPDATE accounts SET balance = balance - ? WHERE account_no = ?\");\n                 PreparedStatement pstmt2 = conn.prepareStatement(\n                    \"UPDATE accounts SET balance = balance + ? WHERE account_no = ?\")) {\n                \n                pstmt1.setBigDecimal(1, amount);\n                pstmt1.setString(2, fromAccount);\n                pstmt1.executeUpdate();\n                \n                pstmt2.setBigDecimal(1, amount);\n                pstmt2.setString(2, toAccount);\n                pstmt2.executeUpdate();\n            }\n            \n            conn.commit();\n            System.out.println(Thread.currentThread().getName() + \n                \" 转账完成: \" + fromAccount + \" -> \" + toAccount);\n            \n        } catch (SQLException e) {\n            if (e.getErrorCode() == 1213) { // MySQL死锁错误码\n                System.err.println(Thread.currentThread().getName() + \n                    \" 检测到死锁: \" + e.getMessage());\n            } else {\n                System.err.println(Thread.currentThread().getName() + \n                    \" SQL异常: \" + e.getMessage());\n            }\n        } catch (InterruptedException e) {\n            Thread.currentThread().interrupt();\n        }\n    }\n    \n    /**\n     * 批量事务处理演示\n     */\n    private static void demonstrateBatchTransaction() throws SQLException {\n        System.out.println(\"\\n📦 批量事务处理演示\");\n        \n        Connection conn = null;\n        try {\n            conn = dataSource.getConnection();\n            conn.setAutoCommit(false);\n            \n            // 批量转账操作\n            String[] fromAccounts = {\"ACC001\", \"ACC002\", \"ACC003\"};\n            String[] toAccounts = {\"ACC002\", \"ACC003\", \"ACC001\"};\n            BigDecimal[] amounts = {\n                new BigDecimal(\"10.00\"),\n                new BigDecimal(\"20.00\"),\n                new BigDecimal(\"15.00\")\n            };\n            \n            try (PreparedStatement debitStmt = conn.prepareStatement(\n                    \"UPDATE accounts SET balance = balance - ? WHERE account_no = ?\");\n                 PreparedStatement creditStmt = conn.prepareStatement(\n                    \"UPDATE accounts SET balance = balance + ? WHERE account_no = ?\")) {\n                \n                for (int i = 0; i < fromAccounts.length; i++) {\n                    // 借方\n                    debitStmt.setBigDecimal(1, amounts[i]);\n                    debitStmt.setString(2, fromAccounts[i]);\n                    debitStmt.addBatch();\n                    \n                    // 贷方\n                    creditStmt.setBigDecimal(1, amounts[i]);\n                    creditStmt.setString(2, toAccounts[i]);\n                    creditStmt.addBatch();\n                }\n                \n                // 执行批量操作\n                int[] debitResults = debitStmt.executeBatch();\n                int[] creditResults = creditStmt.executeBatch();\n                \n                System.out.println(\"批量借方操作影响行数: \" + java.util.Arrays.toString(debitResults));\n                System.out.println(\"批量贷方操作影响行数: \" + java.util.Arrays.toString(creditResults));\n                \n                // 验证批量操作结果\n                boolean success = true;\n                for (int result : debitResults) {\n                    if (result <= 0) {\n                        success = false;\n                        break;\n                    }\n                }\n                for (int result : creditResults) {\n                    if (result <= 0) {\n                        success = false;\n                        break;\n                    }\n                }\n                \n                if (success) {\n                    conn.commit();\n                    System.out.println(\"✅ 批量事务提交成功\");\n                } else {\n                    conn.rollback();\n                    System.out.println(\"⚠️ 批量操作失败，事务已回滚\");\n                }\n            }\n            \n        } finally {\n            if (conn != null) {\n                try {\n                    conn.setAutoCommit(true);\n                    conn.close();\n                } catch (SQLException e) {\n                    System.err.println(\"❌ 关闭连接失败: \" + e.getMessage());\n                }\n            }\n        }\n    }\n    \n    // 辅助方法\n    \n    private static BigDecimal queryBalance(String accountNo) throws SQLException {\n        try (Connection conn = dataSource.getConnection()) {\n            return queryBalanceWithConnection(accountNo, conn);\n        }\n    }\n    \n    private static BigDecimal queryBalanceWithConnection(String accountNo, Connection conn) throws SQLException {\n        try (PreparedStatement pstmt = conn.prepareStatement(\n                \"SELECT balance FROM accounts WHERE account_no = ?\")) {\n            pstmt.setString(1, accountNo);\n            try (ResultSet rs = pstmt.executeQuery()) {\n                if (rs.next()) {\n                    return rs.getBigDecimal(\"balance\");\n                }\n                throw new SQLException(\"账户不存在: \" + accountNo);\n            }\n        }\n    }\n    \n    private static int getCurrentVersion(String accountNo) throws SQLException {\n        try (Connection conn = dataSource.getConnection();\n             PreparedStatement pstmt = conn.prepareStatement(\n                \"SELECT version FROM accounts WHERE account_no = ?\")) {\n            pstmt.setString(1, accountNo);\n            try (ResultSet rs = pstmt.executeQuery()) {\n                if (rs.next()) {\n                    return rs.getInt(\"version\");\n                }\n                throw new SQLException(\"账户不存在: \" + accountNo);\n            }\n        }\n    }\n    \n    private static void logTransaction(Connection conn, String fromAccount, String toAccount, \n                                     BigDecimal amount, String type, String status) throws SQLException {\n        try (PreparedStatement pstmt = conn.prepareStatement(\n                \"INSERT INTO transaction_logs (from_account, to_account, amount, transaction_type, status) \" +\n                \"VALUES (?, ?, ?, ?, ?)\")) {\n            pstmt.setString(1, fromAccount);\n            pstmt.setString(2, toAccount);\n            pstmt.setBigDecimal(3, amount);\n            pstmt.setString(4, type);\n            pstmt.setString(5, status);\n            pstmt.executeUpdate();\n        }\n    }\n    \n    private static void updateTransactionStatus(Connection conn, String fromAccount, \n                                              String toAccount, String status) throws SQLException {\n        try (PreparedStatement pstmt = conn.prepareStatement(\n                \"UPDATE transaction_logs SET status = ? \" +\n                \"WHERE from_account = ? AND to_account = ? \" +\n                \"ORDER BY create_time DESC LIMIT 1\")) {\n            pstmt.setString(1, status);\n            pstmt.setString(2, fromAccount);\n            pstmt.setString(3, toAccount);\n            pstmt.executeUpdate();\n        }\n    }\n    \n    private static String getIsolationLevelName(int level) {\n        return switch (level) {\n            case Connection.TRANSACTION_READ_UNCOMMITTED -> \"READ_UNCOMMITTED\";\n            case Connection.TRANSACTION_READ_COMMITTED -> \"READ_COMMITTED\";\n            case Connection.TRANSACTION_REPEATABLE_READ -> \"REPEATABLE_READ\";\n            case Connection.TRANSACTION_SERIALIZABLE -> \"SERIALIZABLE\";\n            default -> \"UNKNOWN(\" + level + \")\";\n        };\n    }\n}