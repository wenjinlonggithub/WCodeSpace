package com.concurrency.aqs.scenarios;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 基于AQS实现的读写分离缓存
 * 演示读写锁在缓存场景中的应用
 *
 * 特点：
 * - 多个线程可以同时读取
 * - 写操作独占访问
 * - 写操作优先级高于读操作
 */
public class CacheWithReadWriteLock<K, V> {

    private final Map<K, CacheEntry<V>> cache;
    private final ReadWriteLock lock;
    private final long ttlMillis;

    public CacheWithReadWriteLock(long ttlMillis) {
        this.cache = new HashMap<>();
        this.lock = new ReadWriteLock();
        this.ttlMillis = ttlMillis;
    }

    /**
     * 缓存条目
     */
    private static class CacheEntry<V> {
        final V value;
        final long expireTime;

        CacheEntry(V value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    /**
     * 自定义读写锁实现
     */
    private static class ReadWriteLock {
        private final Sync sync = new Sync();

        static final class Sync extends AbstractQueuedSynchronizer {
            // state的高16位表示读锁计数，低16位表示写锁计数
            static final int SHARED_SHIFT = 16;
            static final int SHARED_UNIT = (1 << SHARED_SHIFT);
            static final int MAX_COUNT = (1 << SHARED_SHIFT) - 1;
            static final int EXCLUSIVE_MASK = (1 << SHARED_SHIFT) - 1;

            static int sharedCount(int c) {
                return c >>> SHARED_SHIFT;
            }

            static int exclusiveCount(int c) {
                return c & EXCLUSIVE_MASK;
            }

            // 尝试获取读锁
            @Override
            protected int tryAcquireShared(int unused) {
                for (;;) {
                    int c = getState();
                    // 如果有写锁，且不是当前线程持有，则失败
                    if (exclusiveCount(c) != 0 && getExclusiveOwnerThread() != Thread.currentThread()) {
                        return -1;
                    }
                    int r = sharedCount(c);
                    if (r == MAX_COUNT) {
                        throw new Error("Maximum lock count exceeded");
                    }
                    if (compareAndSetState(c, c + SHARED_UNIT)) {
                        return 1;
                    }
                }
            }

            // 尝试释放读锁
            @Override
            protected boolean tryReleaseShared(int unused) {
                for (;;) {
                    int c = getState();
                    int nextc = c - SHARED_UNIT;
                    if (compareAndSetState(c, nextc)) {
                        return nextc == 0;
                    }
                }
            }

            // 尝试获取写锁
            @Override
            protected boolean tryAcquire(int acquires) {
                int c = getState();
                int w = exclusiveCount(c);
                if (c != 0) {
                    // 如果有读锁或其他线程持有写锁，则失败
                    if (w == 0 || getExclusiveOwnerThread() != Thread.currentThread()) {
                        return false;
                    }
                    if (w + acquires > MAX_COUNT) {
                        throw new Error("Maximum lock count exceeded");
                    }
                }
                if (compareAndSetState(c, c + acquires)) {
                    setExclusiveOwnerThread(Thread.currentThread());
                    return true;
                }
                return false;
            }

            // 尝试释放写锁
            @Override
            protected boolean tryRelease(int releases) {
                if (getExclusiveOwnerThread() != Thread.currentThread()) {
                    throw new IllegalMonitorStateException();
                }
                int nextc = getState() - releases;
                boolean free = exclusiveCount(nextc) == 0;
                if (free) {
                    setExclusiveOwnerThread(null);
                }
                setState(nextc);
                return free;
            }
        }

        void readLock() {
            sync.acquireShared(1);
        }

        void readUnlock() {
            sync.releaseShared(1);
        }

        void writeLock() {
            sync.acquire(1);
        }

        void writeUnlock() {
            sync.release(1);
        }
    }

    /**
     * 获取缓存值（读操作）
     */
    public V get(K key) {
        lock.readLock();
        try {
            CacheEntry<V> entry = cache.get(key);
            if (entry == null || entry.isExpired()) {
                return null;
            }
            return entry.value;
        } finally {
            lock.readUnlock();
        }
    }

    /**
     * 放入缓存（写操作）
     */
    public void put(K key, V value) {
        lock.writeLock();
        try {
            long expireTime = System.currentTimeMillis() + ttlMillis;
            cache.put(key, new CacheEntry<>(value, expireTime));
        } finally {
            lock.writeUnlock();
        }
    }

    /**
     * 删除缓存（写操作）
     */
    public void remove(K key) {
        lock.writeLock();
        try {
            cache.remove(key);
        } finally {
            lock.writeUnlock();
        }
    }

    /**
     * 清理过期缓存（写操作）
     */
    public int evictExpired() {
        lock.writeLock();
        try {
            int count = 0;
            cache.entrySet().removeIf(entry -> {
                if (entry.getValue().isExpired()) {
                    count++;
                    return true;
                }
                return false;
            });
            return count;
        } finally {
            lock.writeUnlock();
        }
    }

    /**
     * 获取缓存大小（读操作）
     */
    public int size() {
        lock.readLock();
        try {
            return cache.size();
        } finally {
            lock.readUnlock();
        }
    }

    /**
     * 测试示例
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 读写分离缓存测试 ===\n");

        CacheWithReadWriteLock<String, String> cache = new CacheWithReadWriteLock<>(5000);

        // 测试1：基本读写操作
        System.out.println("1. 基本读写操作");
        cache.put("user:1", "Alice");
        cache.put("user:2", "Bob");
        System.out.println("   缓存大小: " + cache.size());
        System.out.println("   user:1 = " + cache.get("user:1"));
        System.out.println("   user:2 = " + cache.get("user:2"));

        // 测试2：并发读操作
        System.out.println("\n2. 并发读操作（10个线程同时读）");
        Thread[] readers = new Thread[10];
        long readStart = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            final int id = i;
            readers[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) {
                    String value = cache.get("user:1");
                    if (j == 0) {
                        System.out.println("   读线程" + id + " 读取: " + value);
                    }
                }
            }, "Reader-" + i);
            readers[i].start();
        }
        for (Thread reader : readers) {
            reader.join();
        }
        long readElapsed = System.currentTimeMillis() - readStart;
        System.out.println("   并发读完成，耗时: " + readElapsed + "ms");

        // 测试3：读写混合
        System.out.println("\n3. 读写混合操作");
        Thread writer = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                cache.put("counter", String.valueOf(i));
                System.out.println("   写线程更新 counter = " + i);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "Writer");

        Thread[] mixedReaders = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int id = i;
            mixedReaders[i] = new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    String value = cache.get("counter");
                    System.out.println("   读线程" + id + " 读取 counter = " + value);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "MixedReader-" + i);
            mixedReaders[i].start();
        }

        writer.start();
        writer.join();
        for (Thread reader : mixedReaders) {
            reader.join();
        }

        // 测试4：TTL过期
        System.out.println("\n4. TTL过期测试");
        CacheWithReadWriteLock<String, String> shortCache = new CacheWithReadWriteLock<>(1000);
        shortCache.put("temp", "temporary value");
        System.out.println("   放入临时值: " + shortCache.get("temp"));
        System.out.println("   等待2秒...");
        Thread.sleep(2000);
        System.out.println("   过期后读取: " + shortCache.get("temp"));
        System.out.println("   清理过期项: " + shortCache.evictExpired() + " 个");

        System.out.println("\n测试完成！");
    }
}
