package com.architecture.example;

import javax.sql.DataSource;
import java.sql.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * 简单连接池示例
 * 演示：数据库连接池的基本原理和实现
 */
public class ConnectionPoolExample {
    
    private static SimpleDataSource dataSource;
    
    static {
        initializeConnectionPool();
    }
    
    /**
     * 初始化连接池
     */
    private static void initializeConnectionPool() {
        dataSource = new SimpleDataSource(
            "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",  // 使用H2内存数据库用于演示
            "sa", 
            "", 
            10  // 最大连接数
        );
    }
    
    /**
     * 获取数据源
     */
    public static DataSource getDataSource() {
        return dataSource;
    }
    
    /**
     * 简单数据源实现
     */
    public static class SimpleDataSource implements DataSource {
        private final String url;
        private final String username;
        private final String password;
        private final int maxConnections;
        
        private final BlockingQueue<Connection> connectionPool;
        private final AtomicInteger currentConnections = new AtomicInteger(0);
        
        public SimpleDataSource(String url, String username, String password, int maxConnections) {
            this.url = url;
            this.username = username;
            this.password = password;
            this.maxConnections = maxConnections;
            this.connectionPool = new ArrayBlockingQueue<>(maxConnections);
            
            // 初始化连接池
            initializePool();
        }
        
        private void initializePool() {
            try {
                // 预创建一些连接
                for (int i = 0; i < Math.min(5, maxConnections); i++) {
                    Connection conn = createNewConnection();
                    if (conn != null) {
                        connectionPool.offer(conn);
                        currentConnections.incrementAndGet();
                    }
                }
                System.out.printf("✅ 连接池初始化完成，预创建 %d 个连接%n", connectionPool.size());
            } catch (SQLException e) {
                System.err.println("❌ 连接池初始化失败: " + e.getMessage());
            }
        }
        
        private Connection createNewConnection() throws SQLException {
            return DriverManager.getConnection(url, username, password);
        }
        
        @Override
        public Connection getConnection() throws SQLException {
            return getConnection(username, password);
        }
        
        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            try {
                // 尝试从池中获取连接
                Connection conn = connectionPool.poll(5, TimeUnit.SECONDS);
                
                if (conn == null) {
                    // 池中没有可用连接，尝试创建新连接
                    if (currentConnections.get() < maxConnections) {
                        conn = createNewConnection();
                        if (conn != null) {
                            currentConnections.incrementAndGet();
                        }
                    } else {
                        throw new SQLException("连接池已满，无法获取连接");
                    }
                }
                
                // 检查连接是否有效
                if (conn != null && conn.isClosed()) {
                    currentConnections.decrementAndGet();
                    return getConnection(username, password); // 递归重试
                }
                
                return new PooledConnection(conn, this);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SQLException("获取连接时被中断", e);
            }
        }
        
        /**
         * 归还连接到池中
         */
        void returnConnection(Connection connection) {
            try {
                if (connection != null && !connection.isClosed()) {
                    // 重置连接状态
                    if (!connection.getAutoCommit()) {
                        connection.rollback();
                        connection.setAutoCommit(true);
                    }
                    
                    // 归还到池中
                    if (!connectionPool.offer(connection)) {
                        // 池已满，关闭连接
                        connection.close();
                        currentConnections.decrementAndGet();
                    }
                } else {
                    currentConnections.decrementAndGet();
                }
            } catch (SQLException e) {
                System.err.println("❌ 归还连接失败: " + e.getMessage());
                currentConnections.decrementAndGet();
            }
        }
        
        public int getActiveConnections() {
            return currentConnections.get();
        }
        
        public int getIdleConnections() {
            return connectionPool.size();
        }
        
        // DataSource接口的其他方法实现
        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return null;
        }
        
        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
        }
        
        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
        }
        
        @Override
        public int getLoginTimeout() throws SQLException {
            return 0;
        }
        
        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }
        
        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new SQLException("不支持unwrap操作");
        }
        
        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }
    }
    
    /**
     * 池化连接包装器
     */
    static class PooledConnection implements Connection {
        private final Connection delegate;
        private final SimpleDataSource dataSource;
        private boolean closed = false;
        
        public PooledConnection(Connection delegate, SimpleDataSource dataSource) {
            this.delegate = delegate;
            this.dataSource = dataSource;
        }
        
        @Override
        public void close() throws SQLException {
            if (!closed) {
                closed = true;
                dataSource.returnConnection(delegate);
            }
        }
        
        @Override
        public boolean isClosed() throws SQLException {
            return closed || delegate.isClosed();
        }
        
        // 委托所有其他方法到实际连接
        @Override
        public Statement createStatement() throws SQLException {
            checkClosed();
            return delegate.createStatement();
        }
        
        @Override
        public PreparedStatement prepareStatement(String sql) throws SQLException {
            checkClosed();
            return delegate.prepareStatement(sql);
        }
        
        @Override
        public CallableStatement prepareCall(String sql) throws SQLException {
            checkClosed();
            return delegate.prepareCall(sql);
        }
        
        @Override
        public String nativeSQL(String sql) throws SQLException {
            checkClosed();
            return delegate.nativeSQL(sql);
        }
        
        @Override
        public void setAutoCommit(boolean autoCommit) throws SQLException {
            checkClosed();
            delegate.setAutoCommit(autoCommit);
        }
        
        @Override
        public boolean getAutoCommit() throws SQLException {
            checkClosed();
            return delegate.getAutoCommit();
        }
        
        @Override
        public void commit() throws SQLException {
            checkClosed();
            delegate.commit();
        }
        
        @Override
        public void rollback() throws SQLException {
            checkClosed();
            delegate.rollback();
        }
        
        @Override
        public DatabaseMetaData getMetaData() throws SQLException {
            checkClosed();
            return delegate.getMetaData();
        }
        
        @Override
        public void setReadOnly(boolean readOnly) throws SQLException {
            checkClosed();
            delegate.setReadOnly(readOnly);
        }
        
        @Override
        public boolean isReadOnly() throws SQLException {
            checkClosed();
            return delegate.isReadOnly();
        }
        
        @Override
        public void setCatalog(String catalog) throws SQLException {
            checkClosed();
            delegate.setCatalog(catalog);
        }
        
        @Override
        public String getCatalog() throws SQLException {
            checkClosed();
            return delegate.getCatalog();
        }
        
        @Override
        public void setTransactionIsolation(int level) throws SQLException {
            checkClosed();
            delegate.setTransactionIsolation(level);
        }
        
        @Override
        public int getTransactionIsolation() throws SQLException {
            checkClosed();
            return delegate.getTransactionIsolation();
        }
        
        @Override
        public SQLWarning getWarnings() throws SQLException {
            checkClosed();
            return delegate.getWarnings();
        }
        
        @Override
        public void clearWarnings() throws SQLException {
            checkClosed();
            delegate.clearWarnings();
        }
        
        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            checkClosed();
            return delegate.createStatement(resultSetType, resultSetConcurrency);
        }
        
        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            checkClosed();
            return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
        }
        
        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            checkClosed();
            return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
        }
        
        @Override
        public java.util.Map<String, Class<?>> getTypeMap() throws SQLException {
            checkClosed();
            return delegate.getTypeMap();
        }
        
        @Override
        public void setTypeMap(java.util.Map<String, Class<?>> map) throws SQLException {
            checkClosed();
            delegate.setTypeMap(map);
        }
        
        @Override
        public void setHoldability(int holdability) throws SQLException {
            checkClosed();
            delegate.setHoldability(holdability);
        }
        
        @Override
        public int getHoldability() throws SQLException {
            checkClosed();
            return delegate.getHoldability();
        }
        
        @Override
        public Savepoint setSavepoint() throws SQLException {
            checkClosed();
            return delegate.setSavepoint();
        }
        
        @Override
        public Savepoint setSavepoint(String name) throws SQLException {
            checkClosed();
            return delegate.setSavepoint(name);
        }
        
        @Override
        public void rollback(Savepoint savepoint) throws SQLException {
            checkClosed();
            delegate.rollback(savepoint);
        }
        
        @Override
        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            checkClosed();
            delegate.releaseSavepoint(savepoint);
        }
        
        @Override
        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            checkClosed();
            return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }
        
        @Override
        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            checkClosed();
            return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }
        
        @Override
        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            checkClosed();
            return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }
        
        @Override
        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            checkClosed();
            return delegate.prepareStatement(sql, autoGeneratedKeys);
        }
        
        @Override
        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            checkClosed();
            return delegate.prepareStatement(sql, columnIndexes);
        }
        
        @Override
        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            checkClosed();
            return delegate.prepareStatement(sql, columnNames);
        }
        
        @Override
        public Clob createClob() throws SQLException {
            checkClosed();
            return delegate.createClob();
        }
        
        @Override
        public Blob createBlob() throws SQLException {
            checkClosed();
            return delegate.createBlob();
        }
        
        @Override
        public NClob createNClob() throws SQLException {
            checkClosed();
            return delegate.createNClob();
        }
        
        @Override
        public SQLXML createSQLXML() throws SQLException {
            checkClosed();
            return delegate.createSQLXML();
        }
        
        @Override
        public boolean isValid(int timeout) throws SQLException {
            if (closed) return false;
            return delegate.isValid(timeout);
        }
        
        @Override
        public void setClientInfo(String name, String value) throws SQLClientInfoException {
            try {
                checkClosed();
                delegate.setClientInfo(name, value);
            } catch (SQLException e) {
                throw new SQLClientInfoException();
            }
        }
        
        @Override
        public void setClientInfo(java.util.Properties properties) throws SQLClientInfoException {
            try {
                checkClosed();
                delegate.setClientInfo(properties);
            } catch (SQLException e) {
                throw new SQLClientInfoException();
            }
        }
        
        @Override
        public String getClientInfo(String name) throws SQLException {
            checkClosed();
            return delegate.getClientInfo(name);
        }
        
        @Override
        public java.util.Properties getClientInfo() throws SQLException {
            checkClosed();
            return delegate.getClientInfo();
        }
        
        @Override
        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            checkClosed();
            return delegate.createArrayOf(typeName, elements);
        }
        
        @Override
        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            checkClosed();
            return delegate.createStruct(typeName, attributes);
        }
        
        @Override
        public void setSchema(String schema) throws SQLException {
            checkClosed();
            delegate.setSchema(schema);
        }
        
        @Override
        public String getSchema() throws SQLException {
            checkClosed();
            return delegate.getSchema();
        }
        
        @Override
        public void abort(Executor executor) throws SQLException {
            checkClosed();
            delegate.abort(executor);
        }
        
        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
            checkClosed();
            delegate.setNetworkTimeout(executor, milliseconds);
        }
        
        @Override
        public int getNetworkTimeout() throws SQLException {
            checkClosed();
            return delegate.getNetworkTimeout();
        }
        
        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            checkClosed();
            return delegate.unwrap(iface);
        }
        
        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            checkClosed();
            return delegate.isWrapperFor(iface);
        }
        
        private void checkClosed() throws SQLException {
            if (closed) {
                throw new SQLException("连接已关闭");
            }
        }
    }
    
    /**
     * 测试连接池功能
     */
    public static void testConnectionPool() {
        System.out.println("🚀 连接池功能测试");
        System.out.println("=".repeat(50));
        
        try {
            // 初始化数据库
            initializeDatabase();
            
            // 测试基本连接功能
            testBasicConnection();
            
            // 测试并发连接
            testConcurrentConnections();
            
            // 显示连接池状态
            showPoolStatus();
            
        } catch (Exception e) {
            System.err.println("❌ 连接池测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void initializeDatabase() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 创建测试表
            stmt.execute("CREATE TABLE IF NOT EXISTS test_users (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(100), " +
                "email VARCHAR(100))");
            
            System.out.println("✅ 数据库初始化完成");
        }
    }
    
    private static void testBasicConnection() throws SQLException {
        System.out.println("\n📋 基本连接测试:");
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO test_users (name, email) VALUES (?, ?)")) {
            
            pstmt.setString(1, "测试用户");
            pstmt.setString(2, "test@example.com");
            int result = pstmt.executeUpdate();
            
            System.out.printf("  插入记录: %d 行%n", result);
        }
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_users")) {
            
            if (rs.next()) {
                System.out.printf("  总记录数: %d%n", rs.getInt(1));
            }
        }
    }
    
    private static void testConcurrentConnections() throws InterruptedException {
        System.out.println("\n🔀 并发连接测试:");
        
        int threadCount = 15;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    testThreadConnection(threadId);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        long endTime = System.currentTimeMillis();
        
        System.out.printf("  %d 个线程并发测试完成，耗时: %d ms%n", threadCount, endTime - startTime);
        
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
    }
    
    private static void testThreadConnection(int threadId) {
        try (Connection conn = dataSource.getConnection()) {
            // 模拟数据库操作
            Thread.sleep(100);
            
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO test_users (name, email) VALUES (?, ?)")) {
                pstmt.setString(1, "用户" + threadId);
                pstmt.setString(2, "user" + threadId + "@example.com");
                pstmt.executeUpdate();
            }
            
            System.out.printf("    线程 %d: 操作完成%n", threadId);
            
        } catch (Exception e) {
            System.err.printf("    线程 %d: 操作失败 - %s%n", threadId, e.getMessage());
        }
    }
    
    private static void showPoolStatus() {
        if (dataSource instanceof SimpleDataSource) {
            SimpleDataSource sds = (SimpleDataSource) dataSource;
            System.out.println("\n📊 连接池状态:");
            System.out.printf("  活跃连接: %d%n", sds.getActiveConnections());
            System.out.printf("  空闲连接: %d%n", sds.getIdleConnections());
        }
    }
    
    /**
     * 主测试方法
     */
    public static void main(String[] args) {
        testConnectionPool();
    }
}