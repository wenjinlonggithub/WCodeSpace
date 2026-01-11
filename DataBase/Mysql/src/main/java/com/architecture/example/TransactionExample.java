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
        
        // 开启两个连接模拟并发事务
        try (Connection conn1 = dataSource.getConnection();
             Connection conn2 = dataSource.getConnection()) {
            
            conn1.setAutoCommit(false);
            conn2.setAutoCommit(false);
            
            // 设置隔离级别
            conn1.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            
            // 事务1：第一次读取
            BigDecimal balance1 = queryBalanceWithConnection("ACC001", conn1);
            System.out.println("事务1第一次读取 ACC001 余额: " + balance1);
            
            // 事务2：修改数据并提交
            try (PreparedStatement pstmt = conn2.prepareStatement(
                    "UPDATE accounts SET balance = balance + 50 WHERE account_no = ?")) {
                pstmt.setString(1, "ACC001");
                pstmt.executeUpdate();
                conn2.commit();
                System.out.println("事务2：给 ACC001 增加 50 元并提交");
            }
            
            // 事务1：第二次读取（应该看到相同的值）
            BigDecimal balance2 = queryBalanceWithConnection("ACC001", conn1);
            System.out.println("事务1第二次读取 ACC001 余额: " + balance2);
            
            if (balance1.equals(balance2)) {
                System.out.println("✅ REPEATABLE READ 验证成功：两次读取结果一致");
            } else {
                System.out.println("❌ REPEATABLE READ 验证失败：出现不可重复读");
            }
            
            conn1.commit();
            
            // 事务1提交后再次查询
            BigDecimal balance3 = queryBalance("ACC001");
            System.out.println("事务1提交后 ACC001 余额: " + balance3);
        }
    }
    
    /**
     * 演示READ COMMITTED隔离级别
     */
    private static void demonstrateReadCommitted() throws SQLException {
        System.out.println("\n📝 READ COMMITTED 隔离级别演示");
        
        try (Connection conn1 = dataSource.getConnection();
             Connection conn2 = dataSource.getConnection()) {
            
            conn1.setAutoCommit(false);
            conn2.setAutoCommit(false);
            
            // 设置隔离级别
            conn1.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            
            // 事务1：第一次读取
            BigDecimal balance1 = queryBalanceWithConnection("ACC002", conn1);
            System.out.println("事务1第一次读取 ACC002 余额: " + balance1);
            
            // 事务2：修改数据并提交
            try (PreparedStatement pstmt = conn2.prepareStatement(
                    "UPDATE accounts SET balance = balance + 30 WHERE account_no = ?")) {
                pstmt.setString(1, "ACC002");
                pstmt.executeUpdate();
                conn2.commit();
                System.out.println("事务2：给 ACC002 增加 30 元并提交");
            }
            
            // 事务1：第二次读取（会看到最新提交的值）
            BigDecimal balance2 = queryBalanceWithConnection("ACC002", conn1);
            System.out.println("事务1第二次读取 ACC002 余额: " + balance2);
            
            if (!balance1.equals(balance2)) {
                System.out.println("✅ READ COMMITTED 验证成功：读取到已提交的最新数据");
            } else {
                System.out.println("❌ READ COMMITTED 验证失败：未读取到最新数据");
            }
            
            conn1.commit();
        }
    }
    
    /**
     * 转账事务演示（经典案例）
     */
    private static void demonstrateTransferTransaction() throws SQLException {
        System.out.println("\n💸 转账事务演示（经典案例）");
        
        String fromAccount = "ACC001";
        String toAccount = "ACC003";
        BigDecimal amount = new BigDecimal("150.00");
        
        System.out.printf("转账操作: %s -> %s, 金额: %s%n", fromAccount, toAccount, amount);
        
        // 查询转账前余额
        BigDecimal fromBalanceBefore = queryBalance(fromAccount);
        BigDecimal toBalanceBefore = queryBalance(toAccount);
        System.out.printf("转账前余额 - %s: %s, %s: %s%n", 
            fromAccount, fromBalanceBefore, toAccount, toBalanceBefore);
        
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            
            // 检查余额是否足够
            if (fromBalanceBefore.compareTo(amount) < 0) {
                throw new SQLException("余额不足，无法完成转账");
            }
            
            // 1. 从转出账户扣款
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance - ?, version = version + 1 " +
                    "WHERE account_no = ? AND version = ?")) {
                
                pstmt.setBigDecimal(1, amount);
                pstmt.setString(2, fromAccount);
                pstmt.setInt(3, getCurrentVersion(fromAccount));
                
                int affected = pstmt.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("转出账户更新失败，可能被其他事务修改");
                }
            }
            
            // 记录转账日志
            logTransaction(conn, fromAccount, toAccount, amount, "TRANSFER", "PROCESSING");
            
            // 模拟网络延迟或其他处理时间
            Thread.sleep(100);
            
            // 2. 向转入账户加款
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance + ?, version = version + 1 " +
                    "WHERE account_no = ?")) {
                
                pstmt.setBigDecimal(1, amount);
                pstmt.setString(2, toAccount);
                
                int affected = pstmt.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("转入账户更新失败");
                }
            }
            
            // 更新交易状态为成功
            updateTransactionStatus(conn, fromAccount, toAccount, "SUCCESS");
            
            // 提交事务
            conn.commit();
            System.out.println("✅ 转账成功！");
            
            // 查询转账后余额
            BigDecimal fromBalanceAfter = queryBalance(fromAccount);
            BigDecimal toBalanceAfter = queryBalance(toAccount);
            System.out.printf("转账后余额 - %s: %s, %s: %s%n", 
                fromAccount, fromBalanceAfter, toAccount, toBalanceAfter);
            
            // 验证转账金额
            BigDecimal expectedFromBalance = fromBalanceBefore.subtract(amount);
            BigDecimal expectedToBalance = toBalanceBefore.add(amount);
            
            if (fromBalanceAfter.equals(expectedFromBalance) && 
                toBalanceAfter.equals(expectedToBalance)) {
                System.out.println("✅ 转账金额验证正确");
            } else {
                System.out.println("❌ 转账金额验证失败");
            }
            
        } catch (Exception e) {
            System.err.println("❌ 转账失败: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    // 更新交易状态为失败
                    updateTransactionStatus(conn, fromAccount, toAccount, "FAILED");
                    conn.commit();
                    System.out.println("⚠️ 事务已回滚");
                } catch (SQLException rollbackEx) {
                    System.err.println("❌ 回滚失败: " + rollbackEx.getMessage());
                }
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
     * 死锁演示
     */
    private static void demonstrateDeadlock() {
        System.out.println("\n💀 死锁演示");
        
        Thread thread1 = new Thread(() -> {
            try {
                transferWithDelay("ACC001", "ACC002", new BigDecimal("10.00"), 1000);
            } catch (Exception e) {
                System.err.println("线程1异常: " + e.getMessage());
            }
        }, "Thread-1");
        
        Thread thread2 = new Thread(() -> {
            try {
                transferWithDelay("ACC002", "ACC001", new BigDecimal("5.00"), 1000);
            } catch (Exception e) {
                System.err.println("线程2异常: " + e.getMessage());
            }
        }, "Thread-2");
        
        thread1.start();
        thread2.start();
        
        try {
            thread1.join(5000);
            thread2.join(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("死锁演示完成");
    }
    
    /**
     * 带延迟的转账操作（用于死锁演示）
     */
    private static void transferWithDelay(String fromAccount, String toAccount, 
                                        BigDecimal amount, long delayMs) throws SQLException {
        
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            System.out.println(Thread.currentThread().getName() + 
                " 开始转账: " + fromAccount + " -> " + toAccount);
            
            // 锁定第一个账户
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_no = ? FOR UPDATE")) {
                pstmt.setString(1, fromAccount);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    System.out.println(Thread.currentThread().getName() + 
                        " 锁定账户: " + fromAccount);
                }
            }
            
            // 模拟业务处理延迟
            Thread.sleep(delayMs);
            
            // 尝试锁定第二个账户（可能导致死锁）
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT balance FROM accounts WHERE account_no = ? FOR UPDATE")) {
                pstmt.setString(1, toAccount);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    System.out.println(Thread.currentThread().getName() + 
                        " 锁定账户: " + toAccount);
                }
            }
            
            // 执行转账
            try (PreparedStatement pstmt1 = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance - ? WHERE account_no = ?");
                 PreparedStatement pstmt2 = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance + ? WHERE account_no = ?")) {
                
                pstmt1.setBigDecimal(1, amount);
                pstmt1.setString(2, fromAccount);
                pstmt1.executeUpdate();
                
                pstmt2.setBigDecimal(1, amount);
                pstmt2.setString(2, toAccount);
                pstmt2.executeUpdate();
            }
            
            conn.commit();
            System.out.println(Thread.currentThread().getName() + 
                " 转账完成: " + fromAccount + " -> " + toAccount);
            
        } catch (SQLException e) {
            if (e.getErrorCode() == 1213) { // MySQL死锁错误码
                System.err.println(Thread.currentThread().getName() + 
                    " 检测到死锁: " + e.getMessage());
            } else {
                System.err.println(Thread.currentThread().getName() + 
                    " SQL异常: " + e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 批量事务处理演示
     */
    private static void demonstrateBatchTransaction() throws SQLException {
        System.out.println("\n📦 批量事务处理演示");
        
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            
            // 批量转账操作
            String[] fromAccounts = {"ACC001", "ACC002", "ACC003"};
            String[] toAccounts = {"ACC002", "ACC003", "ACC001"};
            BigDecimal[] amounts = {
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                new BigDecimal("15.00")
            };
            
            try (PreparedStatement debitStmt = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance - ? WHERE account_no = ?");
                 PreparedStatement creditStmt = conn.prepareStatement(
                    "UPDATE accounts SET balance = balance + ? WHERE account_no = ?")) {
                
                for (int i = 0; i < fromAccounts.length; i++) {
                    // 借方
                    debitStmt.setBigDecimal(1, amounts[i]);
                    debitStmt.setString(2, fromAccounts[i]);
                    debitStmt.addBatch();
                    
                    // 贷方
                    creditStmt.setBigDecimal(1, amounts[i]);
                    creditStmt.setString(2, toAccounts[i]);
                    creditStmt.addBatch();
                }
                
                // 执行批量操作
                int[] debitResults = debitStmt.executeBatch();
                int[] creditResults = creditStmt.executeBatch();
                
                System.out.println("批量借方操作影响行数: " + java.util.Arrays.toString(debitResults));
                System.out.println("批量贷方操作影响行数: " + java.util.Arrays.toString(creditResults));
                
                // 验证批量操作结果
                boolean success = true;
                for (int result : debitResults) {
                    if (result <= 0) {
                        success = false;
                        break;
                    }
                }
                for (int result : creditResults) {
                    if (result <= 0) {
                        success = false;
                        break;
                    }
                }
                
                if (success) {
                    conn.commit();
                    System.out.println("✅ 批量事务提交成功");
                } else {
                    conn.rollback();
                    System.out.println("⚠️ 批量操作失败，事务已回滚");
                }
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
    
    // 辅助方法
    
    private static BigDecimal queryBalance(String accountNo) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return queryBalanceWithConnection(accountNo, conn);
        }
    }
    
    private static BigDecimal queryBalanceWithConnection(String accountNo, Connection conn) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT balance FROM accounts WHERE account_no = ?")) {
            pstmt.setString(1, accountNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("balance");
                }
                throw new SQLException("账户不存在: " + accountNo);
            }
        }
    }
    
    private static int getCurrentVersion(String accountNo) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT version FROM accounts WHERE account_no = ?")) {
            pstmt.setString(1, accountNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("version");
                }
                throw new SQLException("账户不存在: " + accountNo);
            }
        }
    }
    
    private static void logTransaction(Connection conn, String fromAccount, String toAccount, 
                                     BigDecimal amount, String type, String status) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO transaction_logs (from_account, to_account, amount, transaction_type, status) " +
                "VALUES (?, ?, ?, ?, ?)")) {
            pstmt.setString(1, fromAccount);
            pstmt.setString(2, toAccount);
            pstmt.setBigDecimal(3, amount);
            pstmt.setString(4, type);
            pstmt.setString(5, status);
            pstmt.executeUpdate();
        }
    }
    
    private static void updateTransactionStatus(Connection conn, String fromAccount, 
                                              String toAccount, String status) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "UPDATE transaction_logs SET status = ? " +
                "WHERE from_account = ? AND to_account = ? " +
                "ORDER BY create_time DESC LIMIT 1")) {
            pstmt.setString(1, status);
            pstmt.setString(2, fromAccount);
            pstmt.setString(3, toAccount);
            pstmt.executeUpdate();
        }
    }
    
    private static String getIsolationLevelName(int level) {
        return switch (level) {
            case Connection.TRANSACTION_READ_UNCOMMITTED -> "READ_UNCOMMITTED";
            case Connection.TRANSACTION_READ_COMMITTED -> "READ_COMMITTED";
            case Connection.TRANSACTION_REPEATABLE_READ -> "REPEATABLE_READ";
            case Connection.TRANSACTION_SERIALIZABLE -> "SERIALIZABLE";
            default -> "UNKNOWN(" + level + ")";
        };
    }
    
    /**
     * 主方法用于测试
     */
    public static void main(String[] args) {
        testTransaction();
    }
}