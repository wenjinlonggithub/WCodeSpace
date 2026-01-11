package com.architecture.engine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * MySQL MVCC(Multi-Version Concurrency Control)多版本并发控制模拟实现
 * 核心功能：
 * 1. 数据版本链管理
 * 2. Read View实现
 * 3. 版本可见性判断
 * 4. 快照读和当前读
 * 5. 版本回收机制
 * 6. 幻读和不可重复读解决方案
 */
public class MVCCEngine {
    
    // 全局事务ID生成器
    private final AtomicLong transactionIdGenerator = new AtomicLong(1);
    
    // 版本链存储：rowId -> 版本链头节点
    private final Map<String, RowVersion> versionChains = new ConcurrentHashMap<>();
    
    // 活跃事务管理
    private final Map<Long, Transaction> activeTransactions = new ConcurrentHashMap<>();
    
    // Read View管理
    private final Map<Long, ReadView> readViews = new ConcurrentHashMap<>();
    
    // 版本回收管理
    private final VersionGarbageCollector garbageCollector = new VersionGarbageCollector();
    
    // 全局锁
    private final ReentrantReadWriteLock globalLock = new ReentrantReadWriteLock();
    
    /**
     * 事务类（简化版）
     */
    public static class Transaction {
        private final long transactionId;
        private final long startTime;
        private final IsolationLevel isolationLevel;
        private volatile boolean active;
        private ReadView readView;
        
        public Transaction(long transactionId, IsolationLevel isolationLevel) {
            this.transactionId = transactionId;
            this.startTime = System.currentTimeMillis();
            this.isolationLevel = isolationLevel;
            this.active = true;
        }
        
        public long getTransactionId() { return transactionId; }
        public long getStartTime() { return startTime; }
        public IsolationLevel getIsolationLevel() { return isolationLevel; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public ReadView getReadView() { return readView; }
        public void setReadView(ReadView readView) { this.readView = readView; }
        
        @Override
        public String toString() {
            return String.format("Transaction{id=%d, isolation=%s, active=%s}", 
                transactionId, isolationLevel, active);
        }
    }
    
    /**
     * 隔离级别
     */
    public enum IsolationLevel {
        READ_UNCOMMITTED,
        READ_COMMITTED,
        REPEATABLE_READ,
        SERIALIZABLE
    }
    
    /**
     * 行版本数据结构
     */
    public static class RowVersion {
        private final String rowId;
        private final long transactionId;    // 创建该版本的事务ID
        private final long createTime;       // 版本创建时间
        private final Map<String, Object> data;  // 版本数据
        private volatile RowVersion nextVersion; // 下一个版本（版本链）
        private volatile boolean deleted;    // 是否被删除
        private volatile long deleteTransactionId; // 删除该版本的事务ID
        
        public RowVersion(String rowId, long transactionId, Map<String, Object> data) {
            this.rowId = rowId;
            this.transactionId = transactionId;
            this.createTime = System.currentTimeMillis();
            this.data = new HashMap<>(data);
            this.nextVersion = null;
            this.deleted = false;
            this.deleteTransactionId = 0;
        }
        
        public String getRowId() { return rowId; }
        public long getTransactionId() { return transactionId; }
        public long getCreateTime() { return createTime; }
        public Map<String, Object> getData() { return new HashMap<>(data); }
        public RowVersion getNextVersion() { return nextVersion; }
        public void setNextVersion(RowVersion nextVersion) { this.nextVersion = nextVersion; }
        public boolean isDeleted() { return deleted; }
        public void markDeleted(long deleteTransactionId) { 
            this.deleted = true; 
            this.deleteTransactionId = deleteTransactionId;
        }
        public long getDeleteTransactionId() { return deleteTransactionId; }
        
        @Override
        public String toString() {
            return String.format("RowVersion{rowId=%s, txnId=%d, data=%s, deleted=%s}", 
                rowId, transactionId, data, deleted);
        }
    }
    
    /**
     * Read View - 读视图
     */
    public static class ReadView {
        private final long creatorTransactionId;
        private final List<Long> activeTransactionIds;
        private final long minActiveTransactionId;
        private final long maxActiveTransactionId;
        private final long createTime;
        
        public ReadView(long creatorTransactionId, List<Long> activeTransactionIds) {
            this.creatorTransactionId = creatorTransactionId;
            this.activeTransactionIds = new ArrayList<>(activeTransactionIds);
            this.createTime = System.currentTimeMillis();
            
            if (activeTransactionIds.isEmpty()) {
                this.minActiveTransactionId = creatorTransactionId;
                this.maxActiveTransactionId = creatorTransactionId;
            } else {
                this.minActiveTransactionId = Collections.min(activeTransactionIds);
                this.maxActiveTransactionId = Collections.max(activeTransactionIds);
            }
        }
        
        /**
         * 判断指定事务的版本是否对当前事务可见
         */
        public boolean isVisible(long transactionId) {
            // 1. 如果是当前事务创建的版本，可见
            if (transactionId == creatorTransactionId) {
                return true;
            }
            
            // 2. 如果版本的事务ID小于最小活跃事务ID，说明已提交，可见
            if (transactionId < minActiveTransactionId) {
                return true;
            }
            
            // 3. 如果版本的事务ID大于最大活跃事务ID，说明在当前事务开始后创建，不可见
            if (transactionId > maxActiveTransactionId) {
                return false;
            }
            
            // 4. 如果在活跃事务列表中，不可见
            return !activeTransactionIds.contains(transactionId);
        }
        
        /**
         * 判断删除版本的可见性
         */
        public boolean isDeleteVisible(long deleteTransactionId) {
            if (deleteTransactionId == 0) {
                return false; // 未被删除
            }
            
            // 如果删除事务对当前事务不可见，说明删除操作不可见，数据仍然可见
            return isVisible(deleteTransactionId);
        }
        
        public long getCreatorTransactionId() { return creatorTransactionId; }
        public List<Long> getActiveTransactionIds() { return new ArrayList<>(activeTransactionIds); }
        public long getCreateTime() { return createTime; }
        
        @Override
        public String toString() {
            return String.format("ReadView{creator=%d, active=%s, range=[%d,%d]}", 
                creatorTransactionId, activeTransactionIds, minActiveTransactionId, maxActiveTransactionId);
        }
    }
    
    /**
     * 版本回收器
     */
    public static class VersionGarbageCollector {
        private final Set<RowVersion> garbageVersions = ConcurrentHashMap.newKeySet();
        
        /**
         * 标记版本为垃圾
         */
        public void markGarbage(RowVersion version) {
            garbageVersions.add(version);
        }
        
        /**
         * 清理垃圾版本
         */
        public int cleanupGarbage(long minActiveTransactionId) {
            List<RowVersion> toRemove = new ArrayList<>();
            
            for (RowVersion version : garbageVersions) {
                // 如果版本的事务ID小于最小活跃事务ID，且版本已被删除或被覆盖，可以回收
                if (version.getTransactionId() < minActiveTransactionId && 
                    (version.isDeleted() || version.getNextVersion() != null)) {
                    toRemove.add(version);
                }
            }
            
            for (RowVersion version : toRemove) {
                garbageVersions.remove(version);
            }
            
            if (!toRemove.isEmpty()) {
                System.out.printf("🧹 回收了 %d 个垃圾版本%n", toRemove.size());
            }
            
            return toRemove.size();
        }
        
        public int getGarbageCount() {
            return garbageVersions.size();
        }
    }
    
    /**
     * 开始事务
     */
    public Transaction beginTransaction(IsolationLevel isolationLevel) {
        globalLock.writeLock().lock();
        try {
            long txnId = transactionIdGenerator.getAndIncrement();
            Transaction transaction = new Transaction(txnId, isolationLevel);
            activeTransactions.put(txnId, transaction);
            
            // 为REPEATABLE_READ和SERIALIZABLE创建Read View
            if (isolationLevel == IsolationLevel.REPEATABLE_READ || 
                isolationLevel == IsolationLevel.SERIALIZABLE) {
                createReadView(transaction);
            }
            
            System.out.printf("🚀 开始事务: %s%n", transaction);
            return transaction;
        } finally {
            globalLock.writeLock().unlock();
        }
    }
    
    /**
     * 提交事务
     */
    public void commitTransaction(long transactionId) {
        globalLock.writeLock().lock();
        try {
            Transaction transaction = activeTransactions.remove(transactionId);
            if (transaction != null) {
                transaction.setActive(false);
                readViews.remove(transactionId);
                System.out.printf("✅ 提交事务: %d%n", transactionId);
                
                // 触发垃圾回收
                triggerGarbageCollection();
            }
        } finally {
            globalLock.writeLock().unlock();
        }
    }
    
    /**
     * 回滚事务
     */
    public void rollbackTransaction(long transactionId) {
        globalLock.writeLock().lock();
        try {
            Transaction transaction = activeTransactions.remove(transactionId);
            if (transaction != null) {
                transaction.setActive(false);
                readViews.remove(transactionId);
                
                // 标记该事务创建的版本为垃圾
                markTransactionVersionsAsGarbage(transactionId);
                
                System.out.printf("🔄 回滚事务: %d%n", transactionId);
                
                // 触发垃圾回收
                triggerGarbageCollection();
            }
        } finally {
            globalLock.writeLock().unlock();
        }
    }
    
    /**
     * 创建Read View
     */
    private void createReadView(Transaction transaction) {
        List<Long> activeIds = new ArrayList<>(activeTransactions.keySet());
        activeIds.remove(transaction.getTransactionId()); // 排除当前事务
        
        ReadView readView = new ReadView(transaction.getTransactionId(), activeIds);
        transaction.setReadView(readView);
        readViews.put(transaction.getTransactionId(), readView);
        
        System.out.printf("📖 创建读视图: %s%n", readView);
    }
    
    /**
     * 获取或创建Read View
     */
    private ReadView getOrCreateReadView(Transaction transaction) {
        switch (transaction.getIsolationLevel()) {
            case READ_UNCOMMITTED:
                return null; // 读未提交不需要Read View
                
            case READ_COMMITTED:
                // 读已提交：每次读取都创建新的Read View
                List<Long> activeIds = new ArrayList<>(activeTransactions.keySet());
                activeIds.remove(transaction.getTransactionId());
                return new ReadView(transaction.getTransactionId(), activeIds);
                
            case REPEATABLE_READ:
            case SERIALIZABLE:
                // 可重复读/串行化：使用事务开始时的Read View
                return transaction.getReadView();
                
            default:
                return null;
        }
    }
    
    /**
     * 插入数据（创建新版本）
     */
    public void insert(long transactionId, String rowId, Map<String, Object> data) {
        globalLock.readLock().lock();
        try {
            if (!activeTransactions.containsKey(transactionId)) {
                throw new IllegalArgumentException("事务不存在: " + transactionId);
            }
            
            RowVersion newVersion = new RowVersion(rowId, transactionId, data);
            
            // 如果行已存在，将新版本插入到版本链头部
            RowVersion existingVersion = versionChains.get(rowId);
            if (existingVersion != null) {
                newVersion.setNextVersion(existingVersion);
            }
            
            versionChains.put(rowId, newVersion);
            
            System.out.printf("📝 插入数据: rowId=%s, txnId=%d, data=%s%n", 
                rowId, transactionId, data);
        } finally {
            globalLock.readLock().unlock();
        }
    }
    
    /**
     * 更新数据（创建新版本）
     */
    public void update(long transactionId, String rowId, Map<String, Object> newData) {
        globalLock.readLock().lock();
        try {
            if (!activeTransactions.containsKey(transactionId)) {
                throw new IllegalArgumentException("事务不存在: " + transactionId);
            }
            
            RowVersion currentHead = versionChains.get(rowId);
            if (currentHead == null) {
                throw new IllegalArgumentException("行不存在: " + rowId);
            }
            
            // 创建新版本
            RowVersion newVersion = new RowVersion(rowId, transactionId, newData);
            newVersion.setNextVersion(currentHead);
            versionChains.put(rowId, newVersion);
            
            System.out.printf("✏️ 更新数据: rowId=%s, txnId=%d, data=%s%n", 
                rowId, transactionId, newData);
        } finally {
            globalLock.readLock().unlock();
        }
    }
    
    /**
     * 删除数据（标记删除）
     */
    public void delete(long transactionId, String rowId) {
        globalLock.readLock().lock();
        try {
            if (!activeTransactions.containsKey(transactionId)) {
                throw new IllegalArgumentException("事务不存在: " + transactionId);
            }
            
            RowVersion currentHead = versionChains.get(rowId);
            if (currentHead == null) {
                throw new IllegalArgumentException("行不存在: " + rowId);
            }
            
            // 标记当前版本为删除
            currentHead.markDeleted(transactionId);
            
            System.out.printf("🗑️ 删除数据: rowId=%s, txnId=%d%n", rowId, transactionId);
        } finally {
            globalLock.readLock().unlock();
        }
    }
    
    /**
     * 快照读 - 读取对当前事务可见的数据版本
     */
    public Map<String, Object> snapshotRead(long transactionId, String rowId) {
        globalLock.readLock().lock();
        try {
            Transaction transaction = activeTransactions.get(transactionId);
            if (transaction == null) {
                throw new IllegalArgumentException("事务不存在: " + transactionId);
            }
            
            ReadView readView = getOrCreateReadView(transaction);
            RowVersion version = findVisibleVersion(rowId, readView, transactionId, 
                transaction.getIsolationLevel());
            
            if (version != null && (!version.isDeleted() || 
                (readView != null && !readView.isDeleteVisible(version.getDeleteTransactionId())))) {
                System.out.printf("👁️ 快照读: rowId=%s, txnId=%d, 找到版本: %s%n", 
                    rowId, transactionId, version);
                return version.getData();
            } else {
                System.out.printf("👁️ 快照读: rowId=%s, txnId=%d, 未找到可见版本%n", 
                    rowId, transactionId);
                return null;
            }
        } finally {
            globalLock.readLock().unlock();
        }
    }
    
    /**
     * 当前读 - 读取最新版本（需要加锁）
     */
    public Map<String, Object> currentRead(long transactionId, String rowId) {
        globalLock.readLock().lock();
        try {
            if (!activeTransactions.containsKey(transactionId)) {
                throw new IllegalArgumentException("事务不存在: " + transactionId);
            }
            
            RowVersion currentVersion = versionChains.get(rowId);
            if (currentVersion != null && !currentVersion.isDeleted()) {
                System.out.printf("🔒 当前读: rowId=%s, txnId=%d, 最新版本: %s%n", 
                    rowId, transactionId, currentVersion);
                return currentVersion.getData();
            } else {
                System.out.printf("🔒 当前读: rowId=%s, txnId=%d, 无可用版本%n", 
                    rowId, transactionId);
                return null;
            }
        } finally {
            globalLock.readLock().unlock();
        }
    }
    
    /**
     * 查找对指定事务可见的版本
     */
    private RowVersion findVisibleVersion(String rowId, ReadView readView, long transactionId, 
                                        IsolationLevel isolationLevel) {
        RowVersion current = versionChains.get(rowId);
        
        while (current != null) {
            // 对于READ_UNCOMMITTED，所有版本都可见
            if (isolationLevel == IsolationLevel.READ_UNCOMMITTED) {
                return current;
            }
            
            // 对于其他隔离级别，使用Read View判断可见性
            if (readView == null || readView.isVisible(current.getTransactionId())) {
                return current;
            }
            
            current = current.getNextVersion();
        }
        
        return null;
    }
    
    /**
     * 标记事务创建的版本为垃圾
     */
    private void markTransactionVersionsAsGarbage(long transactionId) {
        for (RowVersion head : versionChains.values()) {
            RowVersion current = head;
            while (current != null) {
                if (current.getTransactionId() == transactionId) {
                    garbageCollector.markGarbage(current);
                }
                current = current.getNextVersion();
            }
        }
    }
    
    /**
     * 触发垃圾回收
     */
    private void triggerGarbageCollection() {
        if (activeTransactions.isEmpty()) {
            return;
        }
        
        long minActiveTransactionId = activeTransactions.keySet().stream()
            .mapToLong(Long::longValue)
            .min()
            .orElse(Long.MAX_VALUE);
            
        garbageCollector.cleanupGarbage(minActiveTransactionId);
    }
    
    /**
     * 获取版本链信息（用于调试）
     */
    public void printVersionChain(String rowId) {
        System.out.printf("🔗 版本链 [%s]:%n", rowId);
        RowVersion current = versionChains.get(rowId);
        int depth = 0;
        
        while (current != null && depth < 10) { // 限制显示深度
            String status = current.isDeleted() ? 
                String.format("(已删除,txn=%d)", current.getDeleteTransactionId()) : "";
            System.out.printf("  %d: %s %s%n", depth, current, status);
            current = current.getNextVersion();
            depth++;
        }
        
        if (current != null) {
            System.out.println("  ... (更多版本)");
        }
        
        if (depth == 0) {
            System.out.println("  (空链)");
        }
    }
    
    /**
     * 获取MVCC引擎统计信息
     */
    public MVCCStats getStats() {
        globalLock.readLock().lock();
        try {
            int activeTransactionCount = activeTransactions.size();
            int versionChainCount = versionChains.size();
            int totalVersions = versionChains.values().stream()
                .mapToInt(this::countVersionsInChain)
                .sum();
            int garbageVersions = garbageCollector.getGarbageCount();
            
            return new MVCCStats(activeTransactionCount, versionChainCount, 
                               totalVersions, garbageVersions);
        } finally {
            globalLock.readLock().unlock();
        }
    }
    
    /**
     * 计算版本链中的版本数量
     */
    private int countVersionsInChain(RowVersion head) {
        int count = 0;
        RowVersion current = head;
        while (current != null) {
            count++;
            current = current.getNextVersion();
        }
        return count;
    }
    
    /**
     * MVCC统计信息
     */
    public static class MVCCStats {
        private final int activeTransactions;
        private final int versionChains;
        private final int totalVersions;
        private final int garbageVersions;
        
        public MVCCStats(int activeTransactions, int versionChains, 
                        int totalVersions, int garbageVersions) {
            this.activeTransactions = activeTransactions;
            this.versionChains = versionChains;
            this.totalVersions = totalVersions;
            this.garbageVersions = garbageVersions;
        }
        
        @Override
        public String toString() {
            return String.format("MVCC统计[活跃事务=%d, 版本链=%d, 总版本=%d, 垃圾版本=%d]",
                activeTransactions, versionChains, totalVersions, garbageVersions);
        }
        
        public int getActiveTransactions() { return activeTransactions; }
        public int getVersionChains() { return versionChains; }
        public int getTotalVersions() { return totalVersions; }
        public int getGarbageVersions() { return garbageVersions; }
    }
    
    /**
     * 演示MVCC工作原理
     */
    public static void demonstrateMVCC() {
        System.out.println("🔄 MySQL MVCC多版本并发控制演示");
        System.out.println("=".repeat(50));
        
        MVCCEngine mvcc = new MVCCEngine();
        
        // 1. 开始多个不同隔离级别的事务
        System.out.println("\n🚀 开始事务演示:");
        Transaction txn1 = mvcc.beginTransaction(IsolationLevel.READ_COMMITTED);
        Transaction txn2 = mvcc.beginTransaction(IsolationLevel.REPEATABLE_READ);
        Transaction txn3 = mvcc.beginTransaction(IsolationLevel.SERIALIZABLE);
        
        // 2. 事务1插入初始数据
        System.out.println("\n📝 插入初始数据:");
        mvcc.insert(txn1.getTransactionId(), "user:1", 
            Map.of("id", 1, "name", "张三", "age", 25, "salary", 5000));
        mvcc.commitTransaction(txn1.getTransactionId());
        
        // 3. 重新开始事务进行演示
        System.out.println("\n🔄 重新开始事务进行MVCC演示:");
        txn1 = mvcc.beginTransaction(IsolationLevel.READ_COMMITTED);
        txn2 = mvcc.beginTransaction(IsolationLevel.REPEATABLE_READ);
        
        // 4. 事务1更新数据但未提交
        System.out.println("\n✏️ 事务1更新数据（未提交）:");
        mvcc.update(txn1.getTransactionId(), "user:1", 
            Map.of("id", 1, "name", "张三", "age", 26, "salary", 5500));
        
        mvcc.printVersionChain("user:1");
        
        // 5. 事务2读取数据（应该看到旧版本）
        System.out.println("\n👁️ 事务2快照读（应该看到更新前的数据）:");
        Map<String, Object> data2 = mvcc.snapshotRead(txn2.getTransactionId(), "user:1");
        System.out.println("事务2读到的数据: " + data2);
        
        // 6. 事务1提交
        System.out.println("\n✅ 事务1提交:");
        mvcc.commitTransaction(txn1.getTransactionId());
        
        // 7. 新开始的事务能看到更新后的数据
        System.out.println("\n🚀 新事务读取数据:");
        Transaction txn4 = mvcc.beginTransaction(IsolationLevel.READ_COMMITTED);
        Map<String, Object> data4 = mvcc.snapshotRead(txn4.getTransactionId(), "user:1");
        System.out.println("新事务读到的数据: " + data4);
        
        // 8. REPEATABLE_READ事务仍然看到旧数据（演示可重复读）
        System.out.println("\n🔒 可重复读事务再次读取（应该看到一致的数据）:");
        Map<String, Object> data2Again = mvcc.snapshotRead(txn2.getTransactionId(), "user:1");
        System.out.println("可重复读事务读到的数据: " + data2Again);
        
        // 9. 删除演示
        System.out.println("\n🗑️ 删除演示:");
        mvcc.delete(txn4.getTransactionId(), "user:1");
        mvcc.printVersionChain("user:1");
        
        // 10. 不同事务的删除可见性
        System.out.println("\n👁️ 删除可见性测试:");
        Map<String, Object> dataAfterDelete = mvcc.snapshotRead(txn2.getTransactionId(), "user:1");
        System.out.println("可重复读事务看到删除后的数据: " + dataAfterDelete);
        
        Map<String, Object> dataCurrentRead = mvcc.currentRead(txn4.getTransactionId(), "user:1");
        System.out.println("删除事务当前读: " + dataCurrentRead);
        
        // 11. 提交所有事务
        System.out.println("\n✅ 提交所有事务:");
        mvcc.commitTransaction(txn2.getTransactionId());
        mvcc.commitTransaction(txn4.getTransactionId());
        
        // 12. 最终统计信息
        System.out.println("\n📊 最终MVCC统计:");
        System.out.println(mvcc.getStats());
        
        // 13. 显示最终版本链
        System.out.println("\n🔗 最终版本链状态:");
        mvcc.printVersionChain("user:1");
        
        System.out.println("\n✅ MVCC演示完成");
    }
    
    public static void main(String[] args) {
        demonstrateMVCC();
    }
}