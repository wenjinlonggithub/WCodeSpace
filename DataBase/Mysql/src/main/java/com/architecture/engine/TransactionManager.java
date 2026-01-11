package com.architecture.engine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 事务管理器 - 模拟MySQL InnoDB的事务处理机制
 * 核心功能：
 * 1. ACID特性保证
 * 2. MVCC多版本并发控制
 * 3. 事务隔离级别实现
 * 4. 死锁检测和处理
 * 5. 回滚段管理
 */
public class TransactionManager {
    
    // 全局事务ID生成器
    private final AtomicLong transactionIdGenerator = new AtomicLong(1);
    
    // 活跃事务表
    private final Map<Long, Transaction> activeTransactions = new ConcurrentHashMap<>();
    
    // 事务状态管理锁
    private final ReentrantReadWriteLock transactionLock = new ReentrantReadWriteLock();
    
    // 全局读视图管理
    private final ReadViewManager readViewManager = new ReadViewManager();
    
    // 回滚段
    private final UndoLogManager undoLogManager = new UndoLogManager();
    
    // 死锁检测器
    private final DeadlockDetector deadlockDetector = new DeadlockDetector();
    
    /**
     * 事务状态枚举
     */
    public enum TransactionStatus {
        ACTIVE,     // 活跃
        COMMITTED,  // 已提交
        ABORTED,    // 已回滚
        PREPARED    // 已准备（2PC用）
    }
    
    /**
     * 事务隔离级别
     */
    public enum IsolationLevel {
        READ_UNCOMMITTED(1, "读未提交"),
        READ_COMMITTED(2, "读已提交"), 
        REPEATABLE_READ(3, "可重复读"),
        SERIALIZABLE(4, "串行化");
        
        private final int level;
        private final String description;
        
        IsolationLevel(int level, String description) {
            this.level = level;
            this.description = description;
        }
        
        public int getLevel() { return level; }
        public String getDescription() { return description; }
    }
    
    /**
     * 事务类
     */
    public static class Transaction {
        private final long transactionId;
        private final IsolationLevel isolationLevel;
        private TransactionStatus status;
        private final long startTime;
        private final Set<String> modifiedTables;
        private final List<UndoLogRecord> undoLogs;
        private ReadView readView;
        private final Map<String, Object> lockHeld; // 持有的锁
        
        public Transaction(long transactionId, IsolationLevel isolationLevel) {
            this.transactionId = transactionId;
            this.isolationLevel = isolationLevel;
            this.status = TransactionStatus.ACTIVE;
            this.startTime = System.currentTimeMillis();
            this.modifiedTables = new HashSet<>();
            this.undoLogs = new ArrayList<>();
            this.lockHeld = new ConcurrentHashMap<>();
        }
        
        public long getTransactionId() { return transactionId; }
        public IsolationLevel getIsolationLevel() { return isolationLevel; }
        public TransactionStatus getStatus() { return status; }
        public void setStatus(TransactionStatus status) { this.status = status; }
        public long getStartTime() { return startTime; }
        public Set<String> getModifiedTables() { return modifiedTables; }
        public List<UndoLogRecord> getUndoLogs() { return undoLogs; }
        public ReadView getReadView() { return readView; }
        public void setReadView(ReadView readView) { this.readView = readView; }
        public Map<String, Object> getLockHeld() { return lockHeld; }
        
        public void addModifiedTable(String table) {
            modifiedTables.add(table);
        }
        
        public void addUndoLog(UndoLogRecord record) {
            undoLogs.add(record);
        }
        
        @Override
        public String toString() {
            return String.format("Transaction{id=%d, status=%s, isolation=%s, tables=%s}", 
                transactionId, status, isolationLevel, modifiedTables);
        }
    }
    
    /**
     * 读视图 - MVCC核心组件
     */
    public static class ReadView {
        private final long creatorTransactionId;  // 创建视图的事务ID
        private final List<Long> activeTransactionIds; // 创建视图时的活跃事务ID列表
        private final long minActiveId;  // 最小活跃事务ID
        private final long maxActiveId;  // 最大活跃事务ID
        private final long createTime;
        
        public ReadView(long creatorTransactionId, List<Long> activeTransactionIds) {
            this.creatorTransactionId = creatorTransactionId;
            this.activeTransactionIds = new ArrayList<>(activeTransactionIds);
            this.createTime = System.currentTimeMillis();
            
            if (activeTransactionIds.isEmpty()) {
                this.minActiveId = creatorTransactionId;
                this.maxActiveId = creatorTransactionId;
            } else {
                this.minActiveId = Collections.min(activeTransactionIds);
                this.maxActiveId = Collections.max(activeTransactionIds);
            }
        }
        
        /**
         * 判断某个事务的数据版本对当前事务是否可见
         */
        public boolean isVisible(long transactionId) {
            // 1. 如果是当前事务修改的，则可见
            if (transactionId == creatorTransactionId) {
                return true;
            }
            
            // 2. 如果事务ID小于最小活跃事务ID，说明已提交，可见
            if (transactionId < minActiveId) {
                return true;
            }
            
            // 3. 如果事务ID大于最大活跃事务ID，说明在当前事务开始后才启动，不可见
            if (transactionId > maxActiveId) {
                return false;
            }
            
            // 4. 如果事务ID在活跃列表中，不可见
            return !activeTransactionIds.contains(transactionId);
        }
        
        public long getCreatorTransactionId() { return creatorTransactionId; }
        public List<Long> getActiveTransactionIds() { return activeTransactionIds; }
        public long getCreateTime() { return createTime; }
        
        @Override
        public String toString() {
            return String.format("ReadView{creator=%d, active=%s, range=[%d,%d]}", 
                creatorTransactionId, activeTransactionIds, minActiveId, maxActiveId);
        }
    }
    
    /**
     * 回滚日志记录
     */
    public static class UndoLogRecord {
        private final long transactionId;
        private final String tableName;
        private final String operation; // INSERT, UPDATE, DELETE
        private final Map<String, Object> oldValues;
        private final Map<String, Object> newValues;
        private final long timestamp;
        
        public UndoLogRecord(long transactionId, String tableName, String operation,
                           Map<String, Object> oldValues, Map<String, Object> newValues) {
            this.transactionId = transactionId;
            this.tableName = tableName;
            this.operation = operation;
            this.oldValues = new HashMap<>(oldValues != null ? oldValues : Collections.emptyMap());
            this.newValues = new HashMap<>(newValues != null ? newValues : Collections.emptyMap());
            this.timestamp = System.currentTimeMillis();
        }
        
        public long getTransactionId() { return transactionId; }
        public String getTableName() { return tableName; }
        public String getOperation() { return operation; }
        public Map<String, Object> getOldValues() { return oldValues; }
        public Map<String, Object> getNewValues() { return newValues; }
        public long getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("UndoLog{txn=%d, table=%s, op=%s, old=%s}", 
                transactionId, tableName, operation, oldValues);
        }
    }
    
    /**
     * 读视图管理器
     */
    public static class ReadViewManager {
        private final Map<Long, ReadView> readViews = new ConcurrentHashMap<>();
        
        public ReadView createReadView(long transactionId, List<Long> activeTransactionIds) {
            ReadView readView = new ReadView(transactionId, activeTransactionIds);
            readViews.put(transactionId, readView);
            return readView;
        }
        
        public ReadView getReadView(long transactionId) {
            return readViews.get(transactionId);
        }
        
        public void removeReadView(long transactionId) {
            readViews.remove(transactionId);
        }
        
        public int getActiveReadViewCount() {
            return readViews.size();
        }
    }
    
    /**
     * 回滚日志管理器
     */
    public static class UndoLogManager {
        private final Map<Long, List<UndoLogRecord>> undoLogs = new ConcurrentHashMap<>();
        
        public void addUndoLog(long transactionId, UndoLogRecord record) {
            undoLogs.computeIfAbsent(transactionId, k -> new ArrayList<>()).add(record);
        }
        
        public List<UndoLogRecord> getUndoLogs(long transactionId) {
            return undoLogs.getOrDefault(transactionId, Collections.emptyList());
        }
        
        public void removeUndoLogs(long transactionId) {
            undoLogs.remove(transactionId);
        }
        
        public void rollback(long transactionId) {
            List<UndoLogRecord> logs = getUndoLogs(transactionId);
            
            // 逆序执行回滚操作
            for (int i = logs.size() - 1; i >= 0; i--) {
                UndoLogRecord log = logs.get(i);
                System.out.printf("🔄 回滚操作: %s%n", log);
                
                // 这里应该执行实际的回滚操作
                // 例如：恢复旧值、删除插入的记录等
            }
        }
        
        public int getTotalUndoLogCount() {
            return undoLogs.values().stream()
                .mapToInt(List::size)
                .sum();
        }
    }
    
    /**
     * 死锁检测器
     */
    public static class DeadlockDetector {
        
        /**
         * 检测死锁（简化版等待图算法）
         */
        public List<Long> detectDeadlock(Map<Long, Transaction> activeTransactions) {
            // 构建等待图
            Map<Long, Set<Long>> waitGraph = buildWaitGraph(activeTransactions);
            
            // 检测环
            for (Long txnId : waitGraph.keySet()) {
                Set<Long> visited = new HashSet<>();
                Set<Long> recursionStack = new HashSet<>();
                
                List<Long> cycle = detectCycle(waitGraph, txnId, visited, recursionStack, new ArrayList<>());
                if (!cycle.isEmpty()) {
                    System.out.printf("💀 检测到死锁环: %s%n", cycle);
                    return cycle;
                }
            }
            
            return Collections.emptyList();
        }
        
        /**
         * 构建等待图（简化实现）
         */
        private Map<Long, Set<Long>> buildWaitGraph(Map<Long, Transaction> activeTransactions) {
            Map<Long, Set<Long>> waitGraph = new HashMap<>();
            
            // 这里应该根据锁等待关系构建等待图
            // 简化实现：随机模拟一些等待关系
            for (Long txnId : activeTransactions.keySet()) {
                waitGraph.put(txnId, new HashSet<>());
            }
            
            return waitGraph;
        }
        
        /**
         * 深度优先搜索检测环
         */
        private List<Long> detectCycle(Map<Long, Set<Long>> waitGraph, Long current,
                                     Set<Long> visited, Set<Long> recursionStack, List<Long> path) {
            if (recursionStack.contains(current)) {
                // 找到环
                List<Long> cycle = new ArrayList<>();
                boolean inCycle = false;
                for (Long node : path) {
                    if (node.equals(current)) {
                        inCycle = true;
                    }
                    if (inCycle) {
                        cycle.add(node);
                    }
                }
                cycle.add(current);
                return cycle;
            }
            
            if (visited.contains(current)) {
                return Collections.emptyList();
            }
            
            visited.add(current);
            recursionStack.add(current);
            path.add(current);
            
            Set<Long> neighbors = waitGraph.getOrDefault(current, Collections.emptySet());
            for (Long neighbor : neighbors) {
                List<Long> cycle = detectCycle(waitGraph, neighbor, visited, recursionStack, new ArrayList<>(path));
                if (!cycle.isEmpty()) {
                    return cycle;
                }
            }
            
            recursionStack.remove(current);
            return Collections.emptyList();
        }
    }
    
    /**
     * 开始事务
     */
    public Transaction beginTransaction(IsolationLevel isolationLevel) {
        transactionLock.writeLock().lock();
        try {
            long txnId = transactionIdGenerator.getAndIncrement();
            Transaction transaction = new Transaction(txnId, isolationLevel);
            
            activeTransactions.put(txnId, transaction);
            
            // 为REPEATABLE_READ和SERIALIZABLE创建读视图
            if (isolationLevel == IsolationLevel.REPEATABLE_READ || 
                isolationLevel == IsolationLevel.SERIALIZABLE) {
                createReadViewForTransaction(transaction);
            }
            
            System.out.printf("🚀 开始事务: %s%n", transaction);
            return transaction;
        } finally {
            transactionLock.writeLock().unlock();
        }
    }
    
    /**
     * 为事务创建读视图
     */
    private void createReadViewForTransaction(Transaction transaction) {
        List<Long> activeIds = new ArrayList<>(activeTransactions.keySet());
        activeIds.remove(transaction.getTransactionId()); // 排除自己
        
        ReadView readView = readViewManager.createReadView(
            transaction.getTransactionId(), activeIds);
        transaction.setReadView(readView);
        
        System.out.printf("📖 创建读视图: %s%n", readView);
    }
    
    /**
     * 提交事务
     */
    public boolean commitTransaction(long transactionId) {
        transactionLock.writeLock().lock();
        try {
            Transaction transaction = activeTransactions.get(transactionId);
            if (transaction == null) {
                System.out.printf("❌ 事务 %d 不存在%n", transactionId);
                return false;
            }
            
            if (transaction.getStatus() != TransactionStatus.ACTIVE) {
                System.out.printf("❌ 事务 %d 状态不正确: %s%n", 
                    transactionId, transaction.getStatus());
                return false;
            }
            
            // 1. 检查死锁
            List<Long> deadlockedTransactions = deadlockDetector.detectDeadlock(activeTransactions);
            if (deadlockedTransactions.contains(transactionId)) {
                System.out.printf("💀 事务 %d 发生死锁，自动回滚%n", transactionId);
                return rollbackTransaction(transactionId);
            }
            
            // 2. 写提交日志
            System.out.printf("📝 写提交日志: 事务 %d%n", transactionId);
            
            // 3. 释放锁
            releaseLocks(transaction);
            
            // 4. 更新事务状态
            transaction.setStatus(TransactionStatus.COMMITTED);
            
            // 5. 清理资源
            cleanupTransaction(transaction);
            
            activeTransactions.remove(transactionId);
            
            System.out.printf("✅ 事务 %d 提交成功%n", transactionId);
            return true;
        } finally {
            transactionLock.writeLock().unlock();
        }
    }
    
    /**
     * 回滚事务
     */
    public boolean rollbackTransaction(long transactionId) {
        transactionLock.writeLock().lock();
        try {
            Transaction transaction = activeTransactions.get(transactionId);
            if (transaction == null) {
                System.out.printf("❌ 事务 %d 不存在%n", transactionId);
                return false;
            }
            
            System.out.printf("🔄 开始回滚事务: %d%n", transactionId);
            
            // 1. 执行undo日志回滚
            undoLogManager.rollback(transactionId);
            
            // 2. 释放锁
            releaseLocks(transaction);
            
            // 3. 更新事务状态
            transaction.setStatus(TransactionStatus.ABORTED);
            
            // 4. 清理资源
            cleanupTransaction(transaction);
            
            activeTransactions.remove(transactionId);
            
            System.out.printf("✅ 事务 %d 回滚成功%n", transactionId);
            return true;
        } finally {
            transactionLock.writeLock().unlock();
        }
    }
    
    /**
     * 释放事务持有的锁
     */
    private void releaseLocks(Transaction transaction) {
        System.out.printf("🔓 释放事务 %d 的所有锁%n", transaction.getTransactionId());
        transaction.getLockHeld().clear();
    }
    
    /**
     * 清理事务资源
     */
    private void cleanupTransaction(Transaction transaction) {
        long transactionId = transaction.getTransactionId();
        
        // 清理读视图
        readViewManager.removeReadView(transactionId);
        
        // 清理undo日志
        undoLogManager.removeUndoLogs(transactionId);
        
        System.out.printf("🧹 清理事务 %d 的资源%n", transactionId);
    }
    
    /**
     * 记录数据修改操作（生成undo日志）
     */
    public void recordOperation(long transactionId, String tableName, String operation,
                              Map<String, Object> oldValues, Map<String, Object> newValues) {
        Transaction transaction = activeTransactions.get(transactionId);
        if (transaction == null) {
            throw new IllegalArgumentException("事务不存在: " + transactionId);
        }
        
        UndoLogRecord undoLog = new UndoLogRecord(transactionId, tableName, operation, oldValues, newValues);
        transaction.addUndoLog(undoLog);
        transaction.addModifiedTable(tableName);
        undoLogManager.addUndoLog(transactionId, undoLog);
        
        System.out.printf("📝 记录操作: %s%n", undoLog);
    }
    
    /**
     * 根据隔离级别创建读视图
     */
    public ReadView getOrCreateReadView(long transactionId) {
        Transaction transaction = activeTransactions.get(transactionId);
        if (transaction == null) {
            return null;
        }
        
        IsolationLevel isolationLevel = transaction.getIsolationLevel();
        
        switch (isolationLevel) {
            case READ_UNCOMMITTED:
                // 读未提交：不需要读视图
                return null;
                
            case READ_COMMITTED:
                // 读已提交：每次SELECT都创建新读视图
                List<Long> activeIds = new ArrayList<>(activeTransactions.keySet());
                activeIds.remove(transactionId);
                return readViewManager.createReadView(transactionId, activeIds);
                
            case REPEATABLE_READ:
            case SERIALIZABLE:
                // 可重复读/串行化：使用事务开始时的读视图
                return transaction.getReadView();
                
            default:
                return null;
        }
    }
    
    /**
     * 获取事务管理器统计信息
     */
    public TransactionManagerStats getStats() {
        transactionLock.readLock().lock();
        try {
            int activeCount = activeTransactions.size();
            int readViewCount = readViewManager.getActiveReadViewCount();
            int undoLogCount = undoLogManager.getTotalUndoLogCount();
            
            return new TransactionManagerStats(activeCount, readViewCount, undoLogCount);
        } finally {
            transactionLock.readLock().unlock();
        }
    }
    
    /**
     * 事务管理器统计信息
     */
    public static class TransactionManagerStats {
        private final int activeTransactions;
        private final int activeReadViews;
        private final int totalUndoLogs;
        
        public TransactionManagerStats(int activeTransactions, int activeReadViews, int totalUndoLogs) {
            this.activeTransactions = activeTransactions;
            this.activeReadViews = activeReadViews;
            this.totalUndoLogs = totalUndoLogs;
        }
        
        @Override
        public String toString() {
            return String.format("TransactionManager[活跃事务=%d, 读视图=%d, 回滚日志=%d]",
                activeTransactions, activeReadViews, totalUndoLogs);
        }
        
        public int getActiveTransactions() { return activeTransactions; }
        public int getActiveReadViews() { return activeReadViews; }
        public int getTotalUndoLogs() { return totalUndoLogs; }
    }
    
    /**
     * 获取活跃事务列表
     */
    public List<Transaction> getActiveTransactions() {
        transactionLock.readLock().lock();
        try {
            return new ArrayList<>(activeTransactions.values());
        } finally {
            transactionLock.readLock().unlock();
        }
    }
    
    /**
     * 演示事务管理器工作原理
     */
    public static void demonstrateTransactionManager() {
        System.out.println("⚙️ 事务管理器原理演示");
        System.out.println("=".repeat(50));
        
        TransactionManager tm = new TransactionManager();
        
        // 1. 开始不同隔离级别的事务
        System.out.println("\n🚀 开始事务演示:");
        Transaction txn1 = tm.beginTransaction(IsolationLevel.READ_COMMITTED);
        Transaction txn2 = tm.beginTransaction(IsolationLevel.REPEATABLE_READ);
        Transaction txn3 = tm.beginTransaction(IsolationLevel.SERIALIZABLE);
        
        // 2. 模拟数据修改操作
        System.out.println("\n📝 数据修改操作演示:");
        Map<String, Object> oldValues = Map.of("id", 1, "name", "张三", "age", 25);
        Map<String, Object> newValues = Map.of("id", 1, "name", "张三", "age", 26);
        
        tm.recordOperation(txn1.getTransactionId(), "users", "UPDATE", oldValues, newValues);
        tm.recordOperation(txn2.getTransactionId(), "orders", "INSERT", Collections.emptyMap(), 
            Map.of("id", 101, "user_id", 1, "amount", 100.0));
        
        // 3. 读视图演示
        System.out.println("\n📖 读视图演示:");
        ReadView readView1 = tm.getOrCreateReadView(txn1.getTransactionId());
        ReadView readView2 = tm.getOrCreateReadView(txn2.getTransactionId());
        
        if (readView1 != null) {
            System.out.println("事务1读视图: " + readView1);
        }
        if (readView2 != null) {
            System.out.println("事务2读视图: " + readView2);
        }
        
        // 4. MVCC可见性检查
        System.out.println("\n👁️ MVCC可见性检查:");
        if (readView2 != null) {
            System.out.printf("事务2能否看到事务1的修改: %s%n", 
                readView2.isVisible(txn1.getTransactionId()));
            System.out.printf("事务2能否看到事务3的修改: %s%n", 
                readView2.isVisible(txn3.getTransactionId()));
        }
        
        // 5. 显示统计信息
        System.out.println("\n📊 事务管理器状态:");
        System.out.println(tm.getStats());
        
        // 6. 提交和回滚演示
        System.out.println("\n✅ 事务提交回滚演示:");
        tm.commitTransaction(txn1.getTransactionId());
        tm.rollbackTransaction(txn2.getTransactionId());
        tm.commitTransaction(txn3.getTransactionId());
        
        // 7. 最终状态
        System.out.println("\n📊 最终状态:");
        System.out.println(tm.getStats());
        
        System.out.println("\n✅ 事务管理器演示完成");
    }
    
    public static void main(String[] args) {
        demonstrateTransactionManager();
    }
}