package com.concurrency.aqs.scenarios;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于AQS实现的数据库连接池
 * 演示AQS在资源池管理中的应用
 */
public class ConnectionPool {

    private final LinkedList<Connection> pool;
    private final int maxSize;
    private final ReentrantLock lock;
    private final Condition notEmpty;
    private final Semaphore semaphore;

    public ConnectionPool(int maxSize, String url, String user, String password) {
        this.maxSize = maxSize;
        this.pool = new LinkedList<>();
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
        this.semaphore = new Semaphore(maxSize);

        // 初始化连接池
        try {
            for (int i = 0; i < maxSize; i++) {
                pool.add(createConnection(url, user, password));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize connection pool", e);
        }
    }

    /**
     * 自定义信号量，用于控制并发访问
     */
    private static class Semaphore {
        private final Sync sync;

        Semaphore(int permits) {
            sync = new Sync(permits);
        }

        static final class Sync extends AbstractQueuedSynchronizer {
            Sync(int permits) {
                setState(permits);
            }

            @Override
            protected int tryAcquireShared(int acquires) {
                for (;;) {
                    int available = getState();
                    int remaining = available - acquires;
                    if (remaining < 0 || compareAndSetState(available, remaining)) {
                        return remaining;
                    }
                }
            }

            @Override
            protected boolean tryReleaseShared(int releases) {
                for (;;) {
                    int current = getState();
                    int next = current + releases;
                    if (compareAndSetState(current, next)) {
                        return true;
                    }
                }
            }
        }

        void acquire() throws InterruptedException {
            sync.acquireSharedInterruptibly(1);
        }

        boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
            return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
        }

        void release() {
            sync.releaseShared(1);
        }
    }

    /**
     * 获取连接（阻塞）
     */
    public Connection getConnection() throws InterruptedException {
        semaphore.acquire();
        lock.lock();
        try {
            while (pool.isEmpty()) {
                notEmpty.await();
            }
            return pool.removeFirst();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取连接（带超时）
     */
    public Connection getConnection(long timeout, TimeUnit unit)
            throws InterruptedException {
        if (!semaphore.tryAcquire(timeout, unit)) {
            return null;
        }

        lock.lock();
        try {
            long nanos = unit.toNanos(timeout);
            while (pool.isEmpty()) {
                if (nanos <= 0) {
                    semaphore.release();
                    return null;
                }
                nanos = notEmpty.awaitNanos(nanos);
            }
            return pool.removeFirst();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 归还连接
     */
    public void releaseConnection(Connection conn) {
        if (conn == null) return;

        lock.lock();
        try {
            pool.addLast(conn);
            notEmpty.signal();
        } finally {
            lock.unlock();
            semaphore.release();
        }
    }

    /**
     * 获取可用连接数
     */
    public int getAvailableCount() {
        lock.lock();
        try {
            return pool.size();
        } finally {
            lock.unlock();
        }
    }

    private Connection createConnection(String url, String user, String password)
            throws SQLException {
        // 实际项目中应该创建真实连接
        return new MockConnection();
    }

    /**
     * 模拟连接对象（用于测试）
     */
    static class MockConnection implements Connection {
        private boolean closed = false;

        @Override
        public void close() {
            closed = true;
        }

        public boolean isClosed() {
            return closed;
        }

        // 其他Connection方法省略...
        @Override public java.sql.Statement createStatement() { return null; }
        @Override public java.sql.PreparedStatement prepareStatement(String sql) { return null; }
        @Override public java.sql.CallableStatement prepareCall(String sql) { return null; }
        @Override public String nativeSQL(String sql) { return null; }
        @Override public void setAutoCommit(boolean autoCommit) {}
        @Override public boolean getAutoCommit() { return false; }
        @Override public void commit() {}
        @Override public void rollback() {}
        @Override public java.sql.DatabaseMetaData getMetaData() { return null; }
        @Override public void setReadOnly(boolean readOnly) {}
        @Override public boolean isReadOnly() { return false; }
        @Override public void setCatalog(String catalog) {}
        @Override public String getCatalog() { return null; }
        @Override public void setTransactionIsolation(int level) {}
        @Override public int getTransactionIsolation() { return 0; }
        @Override public java.sql.SQLWarning getWarnings() { return null; }
        @Override public void clearWarnings() {}
        @Override public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency) { return null; }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) { return null; }
        @Override public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) { return null; }
        @Override public java.util.Map<String,Class<?>> getTypeMap() { return null; }
        @Override public void setTypeMap(java.util.Map<String,Class<?>> map) {}
        @Override public void setHoldability(int holdability) {}
        @Override public int getHoldability() { return 0; }
        @Override public java.sql.Savepoint setSavepoint() { return null; }
        @Override public java.sql.Savepoint setSavepoint(String name) { return null; }
        @Override public void rollback(java.sql.Savepoint savepoint) {}
        @Override public void releaseSavepoint(java.sql.Savepoint savepoint) {}
        @Override public java.sql.Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) { return null; }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) { return null; }
        @Override public java.sql.CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) { return null; }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) { return null; }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, int[] columnIndexes) { return null; }
        @Override public java.sql.PreparedStatement prepareStatement(String sql, String[] columnNames) { return null; }
        @Override public java.sql.Clob createClob() { return null; }
        @Override public java.sql.Blob createBlob() { return null; }
        @Override public java.sql.NClob createNClob() { return null; }
        @Override public java.sql.SQLXML createSQLXML() { return null; }
        @Override public boolean isValid(int timeout) { return !closed; }
        @Override public void setClientInfo(String name, String value) {}
        @Override public void setClientInfo(java.util.Properties properties) {}
        @Override public String getClientInfo(String name) { return null; }
        @Override public java.util.Properties getClientInfo() { return null; }
        @Override public java.sql.Array createArrayOf(String typeName, Object[] elements) { return null; }
        @Override public java.sql.Struct createStruct(String typeName, Object[] attributes) { return null; }
        @Override public void setSchema(String schema) {}
        @Override public String getSchema() { return null; }
        @Override public void abort(java.util.concurrent.Executor executor) {}
        @Override public void setNetworkTimeout(java.util.concurrent.Executor executor, int milliseconds) {}
        @Override public int getNetworkTimeout() { return 0; }
        @Override public <T> T unwrap(Class<T> iface) { return null; }
        @Override public boolean isWrapperFor(Class<?> iface) { return false; }
    }

    /**
     * 测试示例
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 连接池测试 ===\n");

        ConnectionPool pool = new ConnectionPool(3, "", "", "");

        System.out.println("初始可用连接数: " + pool.getAvailableCount());

        // 创建5个线程竞争3个连接
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int id = i;
            threads[i] = new Thread(() -> {
                try {
                    System.out.println("线程" + id + " 请求连接...");
                    Connection conn = pool.getConnection(2, TimeUnit.SECONDS);

                    if (conn != null) {
                        System.out.println("线程" + id + " 获取连接成功（剩余: " +
                            pool.getAvailableCount() + "）");
                        Thread.sleep(1000); // 模拟使用连接
                        pool.releaseConnection(conn);
                        System.out.println("线程" + id + " 归还连接");
                    } else {
                        System.out.println("线程" + id + " 获取连接超时");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Thread-" + i);
            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("\n最终可用连接数: " + pool.getAvailableCount());
    }
}
