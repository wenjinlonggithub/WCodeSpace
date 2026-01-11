package com.architecture.engine;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 锁管理器 - 模拟MySQL InnoDB的锁机制
 * 核心功能：
 * 1. 行锁（Record Lock）
 * 2. 间隙锁（Gap Lock）
 * 3. Next-Key锁（Record Lock + Gap Lock）
 * 4. 表锁（Table Lock）
 * 5. 意向锁（Intention Lock）
 * 6. 死锁检测和处理
 */
public class LockManager {
    
    // 锁表 - 记录所有资源的锁信息
    private final Map<String, ResourceLockInfo> resourceLocks = new ConcurrentHashMap<>();
    
    // 等待队列 - 记录等待锁的事务
    private final Map<String, Queue<LockWaiter>> waitQueues = new ConcurrentHashMap<>();
    
    // 死锁检测器
    private final DeadlockDetector deadlockDetector = new DeadlockDetector();
    
    // 锁管理器的全局锁
    private final ReentrantLock managerLock = new ReentrantLock();
    
    // 锁等待超时时间（毫秒）
    private static final long LOCK_WAIT_TIMEOUT = 5000;
    
    /**
     * 锁类型枚举
     */
    public enum LockType {
        // 行锁
        SHARED("S", "共享锁"),
        EXCLUSIVE("X", "排他锁"),
        
        // 间隙锁
        GAP_SHARED("Gap-S", "间隙共享锁"),
        GAP_EXCLUSIVE("Gap-X", "间隙排他锁"),
        
        // Next-Key锁
        NEXT_KEY_SHARED("NK-S", "Next-Key共享锁"),
        NEXT_KEY_EXCLUSIVE("NK-X", "Next-Key排他锁"),
        
        // 表锁
        TABLE_SHARED("TS", "表共享锁"),
        TABLE_EXCLUSIVE("TX", "表排他锁"),
        
        // 意向锁
        INTENTION_SHARED("IS", "意向共享锁"),
        INTENTION_EXCLUSIVE("IX", "意向排他锁");
        
        private final String code;
        private final String description;
        
        LockType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() { return code; }
        public String getDescription() { return description; }
    }
    
    /**
     * 锁模式兼容性矩阵
     */
    private static final boolean[][] COMPATIBILITY_MATRIX = {
        // S   X   GS  GX  NKS NKX TS  TX  IS  IX
        {true, false, true, false, true, false, true, false, true, true},  // S
        {false, false, false, false, false, false, false, false, false, false}, // X
        {true, false, true, false, true, false, true, false, true, true},  // Gap-S
        {false, false, false, false, false, false, false, false, false, false}, // Gap-X
        {true, false, true, false, true, false, true, false, true, true},  // NK-S
        {false, false, false, false, false, false, false, false, false, false}, // NK-X
        {true, false, true, false, true, false, true, false, true, false}, // TS
        {false, false, false, false, false, false, false, false, false, false}, // TX
        {true, false, true, false, true, false, true, false, true, true},  // IS
        {true, false, true, false, true, false, false, false, true, true}  // IX
    };
    
    /**
     * 锁对象
     */
    public static class Lock {
        private final long transactionId;
        private final String resourceId;
        private final LockType lockType;
        private final long acquireTime;
        private final Map<String, Object> metadata;
        
        public Lock(long transactionId, String resourceId, LockType lockType) {
            this.transactionId = transactionId;
            this.resourceId = resourceId;
            this.lockType = lockType;
            this.acquireTime = System.currentTimeMillis();
            this.metadata = new HashMap<>();
        }
        
        public long getTransactionId() { return transactionId; }
        public String getResourceId() { return resourceId; }
        public LockType getLockType() { return lockType; }
        public long getAcquireTime() { return acquireTime; }
        public Map<String, Object> getMetadata() { return metadata; }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Lock lock = (Lock) obj;
            return transactionId == lock.transactionId && 
                   Objects.equals(resourceId, lock.resourceId) &&
                   lockType == lock.lockType;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(transactionId, resourceId, lockType);
        }
        
        @Override
        public String toString() {
            return String.format("Lock{txn=%d, resource='%s', type=%s}", 
                transactionId, resourceId, lockType.getCode());
        }
    }
    
    /**
     * 资源锁信息
     */
    public static class ResourceLockInfo {
        private final String resourceId;
        private final Set<Lock> grantedLocks;
        private final ReentrantLock resourceLock;
        
        public ResourceLockInfo(String resourceId) {
            this.resourceId = resourceId;
            this.grantedLocks = new HashSet<>();
            this.resourceLock = new ReentrantLock();
        }
        
        public String getResourceId() { return resourceId; }
        public Set<Lock> getGrantedLocks() { return new HashSet<>(grantedLocks); }
        
        public synchronized void addLock(Lock lock) {
            grantedLocks.add(lock);
        }
        
        public synchronized void removeLock(Lock lock) {
            grantedLocks.remove(lock);
        }
        
        public synchronized boolean hasLock(long transactionId, LockType lockType) {
            return grantedLocks.stream().anyMatch(lock -> 
                lock.getTransactionId() == transactionId && lock.getLockType() == lockType);
        }
        
        public synchronized List<Lock> getLocksForTransaction(long transactionId) {
            return grantedLocks.stream()
                .filter(lock -> lock.getTransactionId() == transactionId)
                .collect(ArrayList::new, (list, lock) -> list.add(lock), List::addAll);
        }
        
        @Override
        public String toString() {
            return String.format("ResourceLockInfo{resource='%s', locks=%d}", 
                resourceId, grantedLocks.size());
        }
    }
    
    /**
     * 锁等待者
     */
    public static class LockWaiter {
        private final long transactionId;
        private final LockType requestedLockType;
        private final long requestTime;
        private final CountDownLatch latch;
        private volatile boolean granted;
        private volatile boolean timeout;
        
        public LockWaiter(long transactionId, LockType requestedLockType) {
            this.transactionId = transactionId;
            this.requestedLockType = requestedLockType;
            this.requestTime = System.currentTimeMillis();
            this.latch = new CountDownLatch(1);
            this.granted = false;
            this.timeout = false;
        }
        
        public long getTransactionId() { return transactionId; }
        public LockType getRequestedLockType() { return requestedLockType; }
        public long getRequestTime() { return requestTime; }
        
        public void grantLock() {
            granted = true;
            latch.countDown();
        }
        
        public void timeout() {
            timeout = true;
            latch.countDown();
        }
        
        public boolean waitForLock(long timeoutMillis) throws InterruptedException {
            return latch.await(timeoutMillis, TimeUnit.MILLISECONDS) && granted;
        }
        
        public boolean isGranted() { return granted; }
        public boolean isTimeout() { return timeout; }
        
        @Override
        public String toString() {
            return String.format("LockWaiter{txn=%d, type=%s, waiting=%dms}", 
                transactionId, requestedLockType.getCode(), 
                System.currentTimeMillis() - requestTime);
        }
    }
    
    /**
     * 死锁检测器
     */
    public static class DeadlockDetector {
        
        /**
         * 检测死锁
         */
        public List<Long> detectDeadlock(Map<String, ResourceLockInfo> resourceLocks,
                                       Map<String, Queue<LockWaiter>> waitQueues) {
            // 构建等待图
            Map<Long, Set<Long>> waitGraph = buildWaitGraph(resourceLocks, waitQueues);
            
            // DFS检测环
            for (Long txnId : waitGraph.keySet()) {
                Set<Long> visited = new HashSet<>();
                Set<Long> recursionStack = new HashSet<>();
                
                List<Long> cycle = findCycle(waitGraph, txnId, visited, recursionStack);
                if (!cycle.isEmpty()) {
                    return cycle;
                }
            }
            
            return Collections.emptyList();
        }
        
        /**
         * 构建等待图
         */
        private Map<Long, Set<Long>> buildWaitGraph(Map<String, ResourceLockInfo> resourceLocks,
                                                  Map<String, Queue<LockWaiter>> waitQueues) {
            Map<Long, Set<Long>> waitGraph = new HashMap<>();
            
            for (Map.Entry<String, Queue<LockWaiter>> entry : waitQueues.entrySet()) {
                String resourceId = entry.getKey();
                Queue<LockWaiter> waitQueue = entry.getValue();
                ResourceLockInfo lockInfo = resourceLocks.get(resourceId);
                
                if (lockInfo != null && !waitQueue.isEmpty()) {
                    // 等待的事务依赖于持有锁的事务
                    for (LockWaiter waiter : waitQueue) {
                        Long waitingTxn = waiter.getTransactionId();
                        waitGraph.computeIfAbsent(waitingTxn, k -> new HashSet<>());
                        
                        for (Lock grantedLock : lockInfo.getGrantedLocks()) {
                            if (!isCompatible(waiter.getRequestedLockType(), grantedLock.getLockType())) {
                                waitGraph.get(waitingTxn).add(grantedLock.getTransactionId());
                            }
                        }
                    }
                }
            }
            
            return waitGraph;
        }
        
        /**
         * DFS查找环
         */
        private List<Long> findCycle(Map<Long, Set<Long>> waitGraph, Long node,
                                   Set<Long> visited, Set<Long> recursionStack) {
            visited.add(node);
            recursionStack.add(node);
            
            Set<Long> neighbors = waitGraph.getOrDefault(node, Collections.emptySet());
            for (Long neighbor : neighbors) {
                if (recursionStack.contains(neighbor)) {
                    // 找到环
                    return Arrays.asList(node, neighbor);
                }
                
                if (!visited.contains(neighbor)) {
                    List<Long> cycle = findCycle(waitGraph, neighbor, visited, recursionStack);
                    if (!cycle.isEmpty()) {
                        return cycle;
                    }
                }
            }
            
            recursionStack.remove(node);
            return Collections.emptyList();
        }
    }
    
    /**
     * 申请锁
     */
    public boolean acquireLock(long transactionId, String resourceId, LockType lockType) {
        return acquireLock(transactionId, resourceId, lockType, LOCK_WAIT_TIMEOUT);
    }
    
    /**
     * 申请锁（带超时）
     */
    public boolean acquireLock(long transactionId, String resourceId, LockType lockType, long timeoutMillis) {
        System.out.printf("🔒 事务 %d 申请锁: %s 类型: %s%n", 
            transactionId, resourceId, lockType.getDescription());
        
        managerLock.lock();
        try {
            ResourceLockInfo lockInfo = resourceLocks.computeIfAbsent(resourceId, 
                k -> new ResourceLockInfo(resourceId));
            
            // 检查是否已经持有兼容的锁
            if (lockInfo.hasLock(transactionId, lockType)) {
                System.out.printf("✅ 事务 %d 已持有锁: %s%n", transactionId, lockType.getCode());
                return true;
            }
            
            // 检查锁兼容性
            if (canGrantLock(lockInfo, lockType)) {
                // 直接授予锁
                Lock lock = new Lock(transactionId, resourceId, lockType);
                lockInfo.addLock(lock);
                System.out.printf("✅ 直接授予锁: %s%n", lock);
                return true;
            } else {
                // 需要等待，加入等待队列
                return waitForLock(transactionId, resourceId, lockType, timeoutMillis);
            }
        } finally {
            managerLock.unlock();
        }
    }
    
    /**
     * 检查是否可以直接授予锁
     */
    private boolean canGrantLock(ResourceLockInfo lockInfo, LockType requestedType) {
        for (Lock grantedLock : lockInfo.getGrantedLocks()) {
            if (!isCompatible(requestedType, grantedLock.getLockType())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 检查两种锁类型是否兼容
     */
    private static boolean isCompatible(LockType type1, LockType type2) {
        int index1 = type1.ordinal();
        int index2 = type2.ordinal();
        return COMPATIBILITY_MATRIX[index1][index2];
    }
    
    /**
     * 等待锁
     */
    private boolean waitForLock(long transactionId, String resourceId, LockType lockType, long timeoutMillis) {
        Queue<LockWaiter> waitQueue = waitQueues.computeIfAbsent(resourceId, 
            k -> new ConcurrentLinkedQueue<>());
        
        LockWaiter waiter = new LockWaiter(transactionId, lockType);
        waitQueue.offer(waiter);
        
        System.out.printf("⏳ 事务 %d 等待锁: %s%n", transactionId, lockType.getDescription());
        
        // 释放管理器锁，允许其他操作
        managerLock.unlock();
        
        try {
            // 检查死锁
            List<Long> deadlockedTransactions = deadlockDetector.detectDeadlock(resourceLocks, waitQueues);
            if (deadlockedTransactions.contains(transactionId)) {
                System.out.printf("💀 检测到死锁，事务 %d 被选为牺牲者%n", transactionId);
                waitQueue.remove(waiter);
                return false;
            }
            
            // 等待锁
            boolean granted = waiter.waitForLock(timeoutMillis);
            
            if (granted) {
                System.out.printf("✅ 锁等待完成: 事务 %d 获得 %s%n", 
                    transactionId, lockType.getDescription());
            } else {
                System.out.printf("⏰ 锁等待超时: 事务 %d%n", transactionId);
                waitQueue.remove(waiter);
            }
            
            return granted;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            waitQueue.remove(waiter);
            return false;
        } finally {
            // 重新获取管理器锁
            managerLock.lock();
        }
    }
    
    /**
     * 释放锁
     */
    public void releaseLock(long transactionId, String resourceId, LockType lockType) {
        managerLock.lock();
        try {
            ResourceLockInfo lockInfo = resourceLocks.get(resourceId);
            if (lockInfo == null) {
                return;
            }
            
            Lock lockToRelease = new Lock(transactionId, resourceId, lockType);
            lockInfo.removeLock(lockToRelease);
            
            System.out.printf("🔓 释放锁: %s%n", lockToRelease);
            
            // 通知等待队列
            notifyWaiters(resourceId);
            
        } finally {
            managerLock.unlock();
        }
    }
    
    /**
     * 释放事务的所有锁
     */
    public void releaseAllLocks(long transactionId) {
        managerLock.lock();
        try {
            System.out.printf("🔓 释放事务 %d 的所有锁%n", transactionId);
            
            Set<String> resourcesToNotify = new HashSet<>();
            
            for (ResourceLockInfo lockInfo : resourceLocks.values()) {
                List<Lock> locksToRemove = lockInfo.getLocksForTransaction(transactionId);
                for (Lock lock : locksToRemove) {
                    lockInfo.removeLock(lock);
                    resourcesToNotify.add(lock.getResourceId());
                    System.out.printf("  🔓 释放: %s%n", lock);
                }
            }
            
            // 通知所有相关的等待队列
            for (String resourceId : resourcesToNotify) {
                notifyWaiters(resourceId);
            }
            
        } finally {
            managerLock.unlock();
        }
    }
    
    /**
     * 通知等待队列中的事务
     */
    private void notifyWaiters(String resourceId) {
        Queue<LockWaiter> waitQueue = waitQueues.get(resourceId);
        if (waitQueue == null || waitQueue.isEmpty()) {
            return;
        }
        
        ResourceLockInfo lockInfo = resourceLocks.get(resourceId);
        if (lockInfo == null) {
            return;
        }
        
        Iterator<LockWaiter> iterator = waitQueue.iterator();
        while (iterator.hasNext()) {
            LockWaiter waiter = iterator.next();
            
            if (canGrantLock(lockInfo, waiter.getRequestedLockType())) {
                // 可以授予锁
                Lock lock = new Lock(waiter.getTransactionId(), resourceId, waiter.getRequestedLockType());
                lockInfo.addLock(lock);
                waiter.grantLock();
                iterator.remove();
                
                System.out.printf("✅ 通知等待者: %s 获得锁%n", waiter);
            }
        }
    }
    
    /**
     * 获取资源的锁信息
     */
    public ResourceLockInfo getResourceLockInfo(String resourceId) {
        return resourceLocks.get(resourceId);
    }
    
    /**
     * 获取锁管理器统计信息
     */
    public LockManagerStats getStats() {
        managerLock.lock();
        try {
            int totalLocks = resourceLocks.values().stream()
                .mapToInt(lockInfo -> lockInfo.getGrantedLocks().size())
                .sum();
                
            int totalWaiters = waitQueues.values().stream()
                .mapToInt(Queue::size)
                .sum();
                
            return new LockManagerStats(resourceLocks.size(), totalLocks, totalWaiters);
        } finally {
            managerLock.unlock();
        }
    }
    
    /**
     * 锁管理器统计信息
     */
    public static class LockManagerStats {
        private final int lockedResources;
        private final int totalLocks;
        private final int waitingTransactions;
        
        public LockManagerStats(int lockedResources, int totalLocks, int waitingTransactions) {
            this.lockedResources = lockedResources;
            this.totalLocks = totalLocks;
            this.waitingTransactions = waitingTransactions;
        }
        
        @Override
        public String toString() {
            return String.format("LockManager[资源=%d, 锁=%d, 等待=%d]",
                lockedResources, totalLocks, waitingTransactions);
        }
        
        public int getLockedResources() { return lockedResources; }
        public int getTotalLocks() { return totalLocks; }
        public int getWaitingTransactions() { return waitingTransactions; }
    }
    
    /**
     * 打印锁表信息
     */
    public void printLockTable() {
        managerLock.lock();
        try {
            System.out.println("\n🔒 锁表信息:");
            System.out.println("-".repeat(80));
            
            for (ResourceLockInfo lockInfo : resourceLocks.values()) {
                if (!lockInfo.getGrantedLocks().isEmpty()) {
                    System.out.printf("资源: %s%n", lockInfo.getResourceId());
                    for (Lock lock : lockInfo.getGrantedLocks()) {
                        System.out.printf("  %s (持有时间: %dms)%n", 
                            lock, System.currentTimeMillis() - lock.getAcquireTime());
                    }
                }
            }
            
            System.out.println("\n⏳ 等待队列:");
            for (Map.Entry<String, Queue<LockWaiter>> entry : waitQueues.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    System.out.printf("资源: %s%n", entry.getKey());
                    for (LockWaiter waiter : entry.getValue()) {
                        System.out.printf("  %s%n", waiter);
                    }
                }
            }
            System.out.println("-".repeat(80));
        } finally {
            managerLock.unlock();
        }
    }
    
    /**
     * 演示锁管理器工作原理
     */
    public static void demonstrateLockManager() {
        System.out.println("🔒 锁管理器原理演示");
        System.out.println("=".repeat(50));
        
        LockManager lockManager = new LockManager();
        
        // 1. 基本锁操作演示
        System.out.println("\n🔒 基本锁操作演示:");
        
        // 事务1获取共享锁
        lockManager.acquireLock(1, "table1:row1", LockType.SHARED);
        
        // 事务2也获取共享锁（兼容）
        lockManager.acquireLock(2, "table1:row1", LockType.SHARED);
        
        lockManager.printLockTable();
        
        // 2. 锁冲突演示
        System.out.println("\n💥 锁冲突演示:");
        
        // 模拟锁等待（在单独线程中）
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            // 事务3尝试获取排他锁（与共享锁冲突）
            return lockManager.acquireLock(3, "table1:row1", LockType.EXCLUSIVE, 2000);
        });
        
        try {
            Thread.sleep(500); // 让事务3开始等待
            lockManager.printLockTable();
            
            // 事务1释放锁
            lockManager.releaseLock(1, "table1:row1", LockType.SHARED);
            
            // 事务2释放锁
            lockManager.releaseLock(2, "table1:row1", LockType.SHARED);
            
            // 检查事务3是否获得锁
            boolean granted = future.get();
            System.out.printf("事务3获得排他锁: %s%n", granted);
            
        } catch (Exception e) {
            System.err.println("演示过程中发生异常: " + e.getMessage());
        }
        
        // 3. 不同类型锁演示
        System.out.println("\n🔒 不同类型锁演示:");
        
        // 意向锁
        lockManager.acquireLock(4, "table1", LockType.INTENTION_SHARED);
        lockManager.acquireLock(4, "table1:row2", LockType.SHARED);
        
        // 间隙锁
        lockManager.acquireLock(5, "table1:gap(10,20)", LockType.GAP_EXCLUSIVE);
        
        // Next-Key锁
        lockManager.acquireLock(6, "table1:row15+gap", LockType.NEXT_KEY_EXCLUSIVE);
        
        lockManager.printLockTable();
        
        // 4. 批量释放锁
        System.out.println("\n🔓 批量释放锁演示:");
        lockManager.releaseAllLocks(4);
        lockManager.releaseAllLocks(5);
        lockManager.releaseAllLocks(6);
        
        // 5. 最终统计
        System.out.println("\n📊 最终统计:");
        System.out.println(lockManager.getStats());
        
        System.out.println("\n✅ 锁管理器演示完成");
    }
    
    public static void main(String[] args) {
        demonstrateLockManager();
    }
}