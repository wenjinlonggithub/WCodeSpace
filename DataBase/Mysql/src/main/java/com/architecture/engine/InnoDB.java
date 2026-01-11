package com.architecture.engine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * InnoDB存储引擎模拟实现
 * 演示InnoDB的核心组件：缓冲池、B+树索引、事务管理、锁机制
 */
public class InnoDB {
    
    // 缓冲池大小（页数）
    private static final int BUFFER_POOL_SIZE = 100;
    
    // 页大小（字节）
    private static final int PAGE_SIZE = 16 * 1024; // 16KB
    
    // 缓冲池 - 模拟内存中的页
    private final Map<PageId, Page> bufferPool = new ConcurrentHashMap<>();
    
    // LRU链表 - 管理页的替换
    private final LinkedHashMap<PageId, Page> lruList = new LinkedHashMap<>(16, 0.75f, true);
    
    // 脏页列表
    private final Set<PageId> dirtyPages = ConcurrentHashMap.newKeySet();
    
    // 读写锁保护缓冲池
    private final ReentrantReadWriteLock bufferPoolLock = new ReentrantReadWriteLock();
    
    // 表空间管理
    private final Map<String, TableSpace> tableSpaces = new ConcurrentHashMap<>();
    
    // 事务管理器
    private final TransactionManager transactionManager = new TransactionManager();
    
    // 锁管理器
    private final LockManager lockManager = new LockManager();
    
    // 日志管理器
    private final LogManager logManager = new LogManager();
    
    /**
     * 页标识符
     */
    public static class PageId {
        private final int spaceId;  // 表空间ID
        private final int pageNo;   // 页号
        
        public PageId(int spaceId, int pageNo) {
            this.spaceId = spaceId;
            this.pageNo = pageNo;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            PageId pageId = (PageId) obj;
            return spaceId == pageId.spaceId && pageNo == pageId.pageNo;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(spaceId, pageNo);
        }
        
        @Override
        public String toString() {
            return String.format("PageId{space=%d, page=%d}", spaceId, pageNo);
        }
        
        public int getSpaceId() { return spaceId; }
        public int getPageNo() { return pageNo; }
    }
    
    /**
     * 页结构模拟
     */
    public static class Page {
        private final PageId pageId;
        private final PageType pageType;
        private final byte[] data;
        private boolean dirty;
        private int pinCount; // 引用计数
        private long lsn; // 日志序列号
        
        public Page(PageId pageId, PageType pageType) {
            this.pageId = pageId;
            this.pageType = pageType;
            this.data = new byte[PAGE_SIZE];
            this.dirty = false;
            this.pinCount = 0;
            this.lsn = 0;
        }
        
        public PageId getPageId() { return pageId; }
        public PageType getPageType() { return pageType; }
        public byte[] getData() { return data; }
        public boolean isDirty() { return dirty; }
        public void setDirty(boolean dirty) { this.dirty = dirty; }
        public int getPinCount() { return pinCount; }
        public void incrementPin() { pinCount++; }
        public void decrementPin() { pinCount = Math.max(0, pinCount - 1); }
        public long getLsn() { return lsn; }
        public void setLsn(long lsn) { this.lsn = lsn; }
    }
    
    /**
     * 页类型枚举
     */
    public enum PageType {
        DATA_PAGE,          // 数据页
        INDEX_PAGE,         // 索引页  
        UNDO_PAGE,          // 回滚页
        SYSTEM_PAGE,        // 系统页
        BLOB_PAGE,          // 大对象页
        EXTENT_DESCRIPTOR   // 区描述页
    }
    
    /**
     * 表空间类
     */
    public static class TableSpace {
        private final int spaceId;
        private final String name;
        private final Map<Integer, Page> pages = new ConcurrentHashMap<>();
        private int nextPageNo = 0;
        
        public TableSpace(int spaceId, String name) {
            this.spaceId = spaceId;
            this.name = name;
        }
        
        public synchronized Page allocatePage(PageType pageType) {
            PageId pageId = new PageId(spaceId, nextPageNo++);
            Page page = new Page(pageId, pageType);
            pages.put(pageId.getPageNo(), page);
            return page;
        }
        
        public Page getPage(int pageNo) {
            return pages.get(pageNo);
        }
        
        public int getSpaceId() { return spaceId; }
        public String getName() { return name; }
        public int getPageCount() { return pages.size(); }
    }
    
    /**
     * 从缓冲池获取页
     */
    public Page getPage(PageId pageId) {
        bufferPoolLock.readLock().lock();
        try {
            // 1. 首先检查缓冲池
            Page page = bufferPool.get(pageId);
            if (page != null) {
                page.incrementPin();
                updateLRU(pageId, page);
                System.out.printf("📖 缓冲池命中: %s (pin=%d)%n", pageId, page.getPinCount());
                return page;
            }
        } finally {
            bufferPoolLock.readLock().unlock();
        }
        
        // 2. 缓冲池未命中，需要从磁盘加载
        return loadPageFromDisk(pageId);
    }
    
    /**
     * 从磁盘加载页到缓冲池
     */
    private Page loadPageFromDisk(PageId pageId) {
        bufferPoolLock.writeLock().lock();
        try {
            // 再次检查（双重检查锁定）
            Page page = bufferPool.get(pageId);
            if (page != null) {
                page.incrementPin();
                updateLRU(pageId, page);
                return page;
            }
            
            // 检查缓冲池是否已满
            if (bufferPool.size() >= BUFFER_POOL_SIZE) {
                evictPage();
            }
            
            // 模拟从磁盘加载页
            page = loadPageFromTableSpace(pageId);
            if (page != null) {
                page.incrementPin();
                bufferPool.put(pageId, page);
                updateLRU(pageId, page);
                System.out.printf("💾 从磁盘加载页: %s%n", pageId);
            }
            
            return page;
        } finally {
            bufferPoolLock.writeLock().unlock();
        }
    }
    
    /**
     * 从表空间加载页
     */
    private Page loadPageFromTableSpace(PageId pageId) {
        TableSpace tableSpace = getTableSpace(pageId.getSpaceId());
        if (tableSpace != null) {
            return tableSpace.getPage(pageId.getPageNo());
        }
        
        // 如果页不存在，创建一个新页
        if (tableSpace != null) {
            return new Page(pageId, PageType.DATA_PAGE);
        }
        
        return null;
    }
    
    /**
     * 页置换算法（LRU）
     */
    private void evictPage() {
        // 找到最少使用且未被pin的页
        PageId victimPageId = null;
        for (Map.Entry<PageId, Page> entry : lruList.entrySet()) {
            Page page = entry.getValue();
            if (page.getPinCount() == 0) {
                victimPageId = entry.getKey();
                break;
            }
        }
        
        if (victimPageId != null) {
            Page victimPage = bufferPool.remove(victimPageId);
            lruList.remove(victimPageId);
            
            // 如果是脏页，需要写回磁盘
            if (victimPage.isDirty()) {
                flushPageToDisk(victimPage);
                dirtyPages.remove(victimPageId);
            }
            
            System.out.printf("🔄 页置换: %s (dirty=%s)%n", victimPageId, victimPage.isDirty());
        }
    }
    
    /**
     * 刷新脏页到磁盘
     */
    private void flushPageToDisk(Page page) {
        // 模拟写磁盘操作
        System.out.printf("💽 刷新脏页到磁盘: %s (LSN=%d)%n", page.getPageId(), page.getLsn());
        page.setDirty(false);
    }
    
    /**
     * 释放页引用
     */
    public void unpinPage(PageId pageId, boolean dirty) {
        bufferPoolLock.readLock().lock();
        try {
            Page page = bufferPool.get(pageId);
            if (page != null) {
                page.decrementPin();
                if (dirty) {
                    page.setDirty(true);
                    dirtyPages.add(pageId);
                    page.setLsn(logManager.getCurrentLSN());
                }
                System.out.printf("📌 释放页引用: %s (pin=%d, dirty=%s)%n", 
                    pageId, page.getPinCount(), page.isDirty());
            }
        } finally {
            bufferPoolLock.readLock().unlock();
        }
    }
    
    /**
     * 更新LRU链表
     */
    private void updateLRU(PageId pageId, Page page) {
        synchronized (lruList) {
            lruList.remove(pageId);
            lruList.put(pageId, page);
        }
    }
    
    /**
     * 创建表空间
     */
    public TableSpace createTableSpace(String name) {
        int spaceId = tableSpaces.size();
        TableSpace tableSpace = new TableSpace(spaceId, name);
        tableSpaces.put(name, tableSpace);
        System.out.printf("📁 创建表空间: %s (ID=%d)%n", name, spaceId);
        return tableSpace;
    }
    
    /**
     * 获取表空间
     */
    public TableSpace getTableSpace(String name) {
        return tableSpaces.get(name);
    }
    
    /**
     * 根据ID获取表空间
     */
    public TableSpace getTableSpace(int spaceId) {
        return tableSpaces.values().stream()
            .filter(ts -> ts.getSpaceId() == spaceId)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * 检查点操作 - 刷新所有脏页
     */
    public void checkpoint() {
        System.out.println("🔄 开始检查点操作...");
        
        bufferPoolLock.writeLock().lock();
        try {
            List<PageId> dirtyPageList = new ArrayList<>(dirtyPages);
            
            for (PageId pageId : dirtyPageList) {
                Page page = bufferPool.get(pageId);
                if (page != null && page.isDirty()) {
                    flushPageToDisk(page);
                    dirtyPages.remove(pageId);
                }
            }
            
            System.out.printf("✅ 检查点完成，刷新了 %d 个脏页%n", dirtyPageList.size());
        } finally {
            bufferPoolLock.writeLock().unlock();
        }
    }
    
    /**
     * 获取缓冲池统计信息
     */
    public BufferPoolStats getBufferPoolStats() {
        bufferPoolLock.readLock().lock();
        try {
            int totalPages = bufferPool.size();
            int dirtyPageCount = dirtyPages.size();
            int pinnedPages = (int) bufferPool.values().stream()
                .mapToInt(Page::getPinCount)
                .filter(pin -> pin > 0)
                .count();
                
            return new BufferPoolStats(totalPages, dirtyPageCount, pinnedPages);
        } finally {
            bufferPoolLock.readLock().unlock();
        }
    }
    
    /**
     * 缓冲池统计信息
     */
    public static class BufferPoolStats {
        private final int totalPages;
        private final int dirtyPages;
        private final int pinnedPages;
        
        public BufferPoolStats(int totalPages, int dirtyPages, int pinnedPages) {
            this.totalPages = totalPages;
            this.dirtyPages = dirtyPages;
            this.pinnedPages = pinnedPages;
        }
        
        @Override
        public String toString() {
            return String.format("BufferPool[总页数=%d, 脏页=%d, 被pin页=%d, 利用率=%.1f%%]",
                totalPages, dirtyPages, pinnedPages, 
                (totalPages * 100.0) / BUFFER_POOL_SIZE);
        }
        
        public int getTotalPages() { return totalPages; }
        public int getDirtyPages() { return dirtyPages; }
        public int getPinnedPages() { return pinnedPages; }
    }
    
    /**
     * 事务管理器
     */
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }
    
    /**
     * 锁管理器
     */
    public LockManager getLockManager() {
        return lockManager;
    }
    
    /**
     * 日志管理器
     */
    public LogManager getLogManager() {
        return logManager;
    }
    
    /**
     * 日志管理器实现
     */
    public static class LogManager {
        private final AtomicLong lsnGenerator = new AtomicLong(1);
        private final List<LogRecord> redoLog = new ArrayList<>();
        private final ReentrantReadWriteLock logLock = new ReentrantReadWriteLock();
        private long lastCheckpointLSN = 0;
        
        /**
         * 日志记录类型
         */
        public enum LogType {
            INSERT, UPDATE, DELETE, BEGIN, COMMIT, ROLLBACK, CHECKPOINT
        }
        
        /**
         * 日志记录
         */
        public static class LogRecord {
            private final long lsn;
            private final long transactionId;
            private final LogType logType;
            private final String tableName;
            private final Map<String, Object> oldValues;
            private final Map<String, Object> newValues;
            private final long timestamp;
            
            public LogRecord(long lsn, long transactionId, LogType logType, String tableName,
                           Map<String, Object> oldValues, Map<String, Object> newValues) {
                this.lsn = lsn;
                this.transactionId = transactionId;
                this.logType = logType;
                this.tableName = tableName;
                this.oldValues = oldValues != null ? new HashMap<>(oldValues) : new HashMap<>();
                this.newValues = newValues != null ? new HashMap<>(newValues) : new HashMap<>();
                this.timestamp = System.currentTimeMillis();
            }
            
            public long getLsn() { return lsn; }
            public long getTransactionId() { return transactionId; }
            public LogType getLogType() { return logType; }
            public String getTableName() { return tableName; }
            public Map<String, Object> getOldValues() { return oldValues; }
            public Map<String, Object> getNewValues() { return newValues; }
            public long getTimestamp() { return timestamp; }
            
            @Override
            public String toString() {
                return String.format("LogRecord{LSN=%d, txn=%d, type=%s, table=%s}", 
                    lsn, transactionId, logType, tableName);
            }
        }
        
        /**
         * 获取当前LSN
         */
        public long getCurrentLSN() {
            return lsnGenerator.get();
        }
        
        /**
         * 写入日志记录
         */
        public long writeLog(long transactionId, LogType logType, String tableName,
                           Map<String, Object> oldValues, Map<String, Object> newValues) {
            logLock.writeLock().lock();
            try {
                long lsn = lsnGenerator.getAndIncrement();
                LogRecord record = new LogRecord(lsn, transactionId, logType, tableName, oldValues, newValues);
                redoLog.add(record);
                
                System.out.printf("📄 写入日志: %s%n", record);
                
                // 强制刷盘（简化实现）
                if (logType == LogType.COMMIT) {
                    System.out.printf("💾 强制刷盘: LSN=%d%n", lsn);
                }
                
                return lsn;
            } finally {
                logLock.writeLock().unlock();
            }
        }
        
        /**
         * 写入事务开始日志
         */
        public long writeBeginLog(long transactionId) {
            return writeLog(transactionId, LogType.BEGIN, null, null, null);
        }
        
        /**
         * 写入事务提交日志
         */
        public long writeCommitLog(long transactionId) {
            return writeLog(transactionId, LogType.COMMIT, null, null, null);
        }
        
        /**
         * 写入事务回滚日志
         */
        public long writeRollbackLog(long transactionId) {
            return writeLog(transactionId, LogType.ROLLBACK, null, null, null);
        }
        
        /**
         * 写入检查点日志
         */
        public long writeCheckpointLog() {
            logLock.writeLock().lock();
            try {
                long checkpointLSN = writeLog(0, LogType.CHECKPOINT, null, null, null);
                lastCheckpointLSN = checkpointLSN;
                System.out.printf("🔄 写入检查点日志: LSN=%d%n", checkpointLSN);
                return checkpointLSN;
            } finally {
                logLock.writeLock().unlock();
            }
        }
        
        /**
         * 获取指定事务的所有日志
         */
        public List<LogRecord> getTransactionLogs(long transactionId) {
            logLock.readLock().lock();
            try {
                return redoLog.stream()
                    .filter(log -> log.getTransactionId() == transactionId)
                    .collect(java.util.stream.Collectors.toList());
            } finally {
                logLock.readLock().unlock();
            }
        }
        
        /**
         * 获取从指定LSN开始的所有日志
         */
        public List<LogRecord> getLogsFromLSN(long fromLSN) {
            logLock.readLock().lock();
            try {
                return redoLog.stream()
                    .filter(log -> log.getLsn() >= fromLSN)
                    .collect(java.util.stream.Collectors.toList());
            } finally {
                logLock.readLock().unlock();
            }
        }
        
        /**
         * 恢复操作 - 重做日志
         */
        public void recovery() {
            logLock.readLock().lock();
            try {
                System.out.println("🔄 开始数据库恢复...");
                
                // 从上次检查点开始重做
                List<LogRecord> logsToRedo = getLogsFromLSN(lastCheckpointLSN);
                
                for (LogRecord log : logsToRedo) {
                    switch (log.getLogType()) {
                        case INSERT:
                        case UPDATE:
                        case DELETE:
                            System.out.printf("🔄 重做操作: %s%n", log);
                            break;
                        case COMMIT:
                            System.out.printf("✅ 提交事务: %d%n", log.getTransactionId());
                            break;
                        case ROLLBACK:
                            System.out.printf("🔄 回滚事务: %d%n", log.getTransactionId());
                            break;
                    }
                }
                
                System.out.printf("✅ 恢复完成，处理了 %d 条日志%n", logsToRedo.size());
            } finally {
                logLock.readLock().unlock();
            }
        }
        
        /**
         * 获取日志统计信息
         */
        public LogStats getStats() {
            logLock.readLock().lock();
            try {
                long totalLogs = redoLog.size();
                long currentLSN = lsnGenerator.get();
                
                Map<LogType, Long> logTypeCounts = redoLog.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                        LogRecord::getLogType, 
                        java.util.stream.Collectors.counting()));
                
                return new LogStats(totalLogs, currentLSN, lastCheckpointLSN, logTypeCounts);
            } finally {
                logLock.readLock().unlock();
            }
        }
        
        /**
         * 日志统计信息
         */
        public static class LogStats {
            private final long totalLogs;
            private final long currentLSN;
            private final long lastCheckpointLSN;
            private final Map<LogType, Long> logTypeCounts;
            
            public LogStats(long totalLogs, long currentLSN, long lastCheckpointLSN, 
                          Map<LogType, Long> logTypeCounts) {
                this.totalLogs = totalLogs;
                this.currentLSN = currentLSN;
                this.lastCheckpointLSN = lastCheckpointLSN;
                this.logTypeCounts = new HashMap<>(logTypeCounts);
            }
            
            @Override
            public String toString() {
                return String.format("LogStats[总日志=%d, 当前LSN=%d, 检查点LSN=%d, 类型分布=%s]",
                    totalLogs, currentLSN, lastCheckpointLSN, logTypeCounts);
            }
            
            public long getTotalLogs() { return totalLogs; }
            public long getCurrentLSN() { return currentLSN; }
            public long getLastCheckpointLSN() { return lastCheckpointLSN; }
            public Map<LogType, Long> getLogTypeCounts() { return logTypeCounts; }
        }
    }
    
    /**
     * 演示InnoDB存储引擎工作原理
     */
    public static void demonstrateInnoDB() {
        System.out.println("🚀 InnoDB存储引擎原理演示");
        System.out.println("=" .repeat(50));
        
        InnoDB innodb = new InnoDB();
        
        // 1. 创建表空间
        TableSpace userTableSpace = innodb.createTableSpace("user_data");
        TableSpace indexTableSpace = innodb.createTableSpace("user_index");
        
        // 2. 分配页
        Page dataPage1 = userTableSpace.allocatePage(PageType.DATA_PAGE);
        Page dataPage2 = userTableSpace.allocatePage(PageType.DATA_PAGE);
        Page indexPage = indexTableSpace.allocatePage(PageType.INDEX_PAGE);
        
        System.out.println("\n📄 分配的页:");
        System.out.println("  " + dataPage1.getPageId() + " - " + dataPage1.getPageType());
        System.out.println("  " + dataPage2.getPageId() + " - " + dataPage2.getPageType());
        System.out.println("  " + indexPage.getPageId() + " - " + indexPage.getPageType());
        
        // 3. 模拟页访问
        System.out.println("\n🔍 页访问模拟:");
        
        Page page1 = innodb.getPage(dataPage1.getPageId());
        Page page2 = innodb.getPage(dataPage2.getPageId());
        Page page3 = innodb.getPage(indexPage.getPageId());
        
        // 4. 修改页并标记为脏页
        innodb.unpinPage(dataPage1.getPageId(), true);  // 标记为脏页
        innodb.unpinPage(dataPage2.getPageId(), false); // 只读访问
        innodb.unpinPage(indexPage.getPageId(), true);  // 标记为脏页
        
        // 5. 显示缓冲池状态
        System.out.println("\n📊 缓冲池状态:");
        System.out.println("  " + innodb.getBufferPoolStats());
        
        // 6. 模拟大量页访问（触发页置换）
        System.out.println("\n🔄 模拟大量页访问:");
        for (int i = 0; i < 5; i++) {
            Page newPage = userTableSpace.allocatePage(PageType.DATA_PAGE);
            Page loadedPage = innodb.getPage(newPage.getPageId());
            innodb.unpinPage(newPage.getPageId(), i % 2 == 0);
        }
        
        // 7. 日志管理演示
        System.out.println("\n📄 日志管理演示:");
        LogManager logManager = innodb.getLogManager();
        
        // 模拟事务操作日志
        long txn1 = 1001;
        long txn2 = 1002;
        
        logManager.writeBeginLog(txn1);
        logManager.writeLog(txn1, LogManager.LogType.INSERT, "users", 
            Collections.emptyMap(), Map.of("id", 1, "name", "张三"));
        logManager.writeLog(txn1, LogManager.LogType.UPDATE, "users", 
            Map.of("id", 1, "age", 25), Map.of("id", 1, "age", 26));
        logManager.writeCommitLog(txn1);
        
        logManager.writeBeginLog(txn2);
        logManager.writeLog(txn2, LogManager.LogType.DELETE, "users", 
            Map.of("id", 2, "name", "李四"), Collections.emptyMap());
        logManager.writeRollbackLog(txn2);
        
        System.out.println("\n📊 日志统计:");
        System.out.println("  " + logManager.getStats());
        
        // 8. 执行检查点
        innodb.checkpoint();
        logManager.writeCheckpointLog();
        
        // 9. 恢复演示
        System.out.println("\n🔄 数据库恢复演示:");
        logManager.recovery();
        
        // 10. 最终状态
        System.out.println("\n📊 最终状态:");
        System.out.println("  缓冲池: " + innodb.getBufferPoolStats());
        System.out.println("  日志: " + logManager.getStats());
        
        System.out.println("\n✅ InnoDB存储引擎演示完成");
    }
    
    public static void main(String[] args) {
        demonstrateInnoDB();
    }
}