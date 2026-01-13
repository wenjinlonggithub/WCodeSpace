package com.architecture.distributedlock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 基于Zookeeper的分布式锁
 *
 * 实现原理：
 * 1. 客户端在/locks目录下创建临时顺序节点：/locks/lock_0000000001
 * 2. 获取/locks下所有子节点，判断自己是否是最小节点
 * 3. 如果是最小节点，获取锁成功
 * 4. 如果不是，监听前一个节点的删除事件，等待唤醒
 * 5. 前一个节点删除后，重新判断是否是最小节点
 *
 * 优势：
 * 1. 自动删除：临时节点在客户端断开连接后自动删除，避免死锁
 * 2. 公平锁：天然支持FIFO（按照节点序号排序）
 * 3. 高可用：Zookeeper集群保证可用性
 * 4. 强一致性：基于ZAB协议，保证强一致性
 *
 * 劣势：
 * 1. 性能较低：创建/删除节点有性能开销
 * 2. 复杂度高：需要维护Zookeeper集群
 * 3. 羊群效应：某个节点删除时，会唤醒所有等待的客户端（优化：只监听前一个节点）
 *
 * Maven依赖：
 * <dependency>
 *     <groupId>org.apache.curator</groupId>
 *     <artifactId>curator-recipes</artifactId>
 *     <version>5.3.0</version>
 * </dependency>
 */
public class ZookeeperDistributedLock {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperDistributedLock.class);

    /**
     * 使用Apache Curator的InterProcessMutex实现（推荐）
     *
     * Curator是Netflix开源的Zookeeper客户端，提供了高级封装
     */
    public static void curatorExample() {
        /*
        // 1. 创建Zookeeper客户端
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(3000)
                .retryPolicy(retryPolicy)
                .build();

        client.start();

        // 2. 创建分布式锁
        InterProcessMutex lock = new InterProcessMutex(client, "/locks/myLock");

        try {
            // 3. 获取锁（阻塞等待）
            lock.acquire();

            // 或者尝试获取锁（带超时）
            // boolean locked = lock.acquire(10, TimeUnit.SECONDS);

            logger.info("获取锁成功，开始执行业务逻辑");

            // 执行业务逻辑
            Thread.sleep(5000);

            logger.info("业务逻辑执行完成");

        } catch (Exception e) {
            logger.error("业务执行异常", e);
        } finally {
            try {
                // 4. 释放锁
                lock.release();
                logger.info("释放锁成功");
            } catch (Exception e) {
                logger.error("释放锁异常", e);
            }
        }

        // 5. 关闭客户端
        client.close();
        */
    }

    /**
     * 原生Zookeeper实现（理解原理）
     *
     * 实现步骤：
     * 1. 创建持久节点/locks（锁的根目录）
     * 2. 创建临时顺序节点/locks/lock_0000000001
     * 3. 获取/locks下所有子节点，排序
     * 4. 判断自己是否是最小节点
     *    - 是：获取锁成功
     *    - 否：监听前一个节点的删除事件
     * 5. 前一个节点删除后，重新判断
     * 6. 释放锁：删除自己的节点
     */
    public static class SimpleZkLock {

        private static final String LOCK_ROOT = "/locks";
        private static final String LOCK_PREFIX = "lock_";

        // private ZooKeeper zookeeper;
        private String lockPath;
        private CountDownLatch latch;

        /*
        public SimpleZkLock(ZooKeeper zookeeper) {
            this.zookeeper = zookeeper;
        }

        public boolean tryLock(long timeout, TimeUnit unit) throws Exception {
            // 1. 创建锁根目录（持久节点）
            createRootIfNotExists();

            // 2. 创建临时顺序节点
            lockPath = zookeeper.create(
                LOCK_ROOT + "/" + LOCK_PREFIX,
                new byte[0],
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL
            );

            logger.info("创建锁节点: {}", lockPath);

            // 3. 获取所有子节点，判断是否是最小节点
            return checkAndWait(timeout, unit);
        }

        private boolean checkAndWait(long timeout, TimeUnit unit) throws Exception {
            // 获取所有子节点
            List<String> children = zookeeper.getChildren(LOCK_ROOT, false);
            Collections.sort(children);

            // 当前节点名称
            String currentNode = lockPath.substring(LOCK_ROOT.length() + 1);
            int currentIndex = children.indexOf(currentNode);

            if (currentIndex == 0) {
                // 是最小节点，获取锁成功
                logger.info("获取锁成功: {}", lockPath);
                return true;
            }

            // 不是最小节点，监听前一个节点
            String prevNode = children.get(currentIndex - 1);
            String prevPath = LOCK_ROOT + "/" + prevNode;

            latch = new CountDownLatch(1);

            // 监听前一个节点的删除事件
            Stat stat = zookeeper.exists(prevPath, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeDeleted) {
                        logger.info("前一个节点已删除: {}", prevPath);
                        latch.countDown();
                    }
                }
            });

            if (stat == null) {
                // 前一个节点已经不存在，重新检查
                return checkAndWait(timeout, unit);
            }

            // 等待前一个节点删除
            logger.info("等待前一个节点删除: {}", prevPath);
            boolean acquired = latch.await(timeout, unit);

            if (acquired) {
                // 被唤醒，重新检查是否是最小节点
                return checkAndWait(timeout, unit);
            } else {
                // 超时
                logger.warn("获取锁超时");
                unlock();
                return false;
            }
        }

        public void unlock() {
            try {
                if (lockPath != null) {
                    zookeeper.delete(lockPath, -1);
                    logger.info("释放锁: {}", lockPath);
                }
            } catch (Exception e) {
                logger.error("释放锁异常", e);
            }
        }

        private void createRootIfNotExists() throws Exception {
            Stat stat = zookeeper.exists(LOCK_ROOT, false);
            if (stat == null) {
                zookeeper.create(
                    LOCK_ROOT,
                    new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT
                );
                logger.info("创建锁根目录: {}", LOCK_ROOT);
            }
        }
        */
    }

    /**
     * Curator读写锁示例
     */
    public static void readWriteLockExample() {
        /*
        CuratorFramework client = CuratorFrameworkFactory.newClient(
            "127.0.0.1:2181",
            new ExponentialBackoffRetry(1000, 3)
        );
        client.start();

        // 创建读写锁
        InterProcessReadWriteLock rwLock = new InterProcessReadWriteLock(client, "/locks/rwLock");

        // 读锁（共享锁）
        InterProcessMutex readLock = rwLock.readLock();

        // 写锁（排他锁）
        InterProcessMutex writeLock = rwLock.writeLock();

        // 使用方式与InterProcessMutex相同
        try {
            readLock.acquire();
            logger.info("获取读锁成功");
        } catch (Exception e) {
            logger.error("获取读锁失败", e);
        } finally {
            try {
                readLock.release();
            } catch (Exception e) {
                logger.error("释放读锁失败", e);
            }
        }

        client.close();
        */
    }

    /**
     * Curator信号量示例（限流）
     *
     * 使用场景：
     * 限制同时访问某资源的线程数量（如数据库连接池）
     */
    public static void semaphoreExample() {
        /*
        CuratorFramework client = CuratorFrameworkFactory.newClient(
            "127.0.0.1:2181",
            new ExponentialBackoffRetry(1000, 3)
        );
        client.start();

        // 创建信号量（最多5个许可）
        InterProcessSemaphoreV2 semaphore = new InterProcessSemaphoreV2(client, "/locks/semaphore", 5);

        try {
            // 获取1个许可
            Lease lease = semaphore.acquire();

            logger.info("获取许可成功，执行业务逻辑");

            // 执行业务
            Thread.sleep(5000);

            // 释放许可
            semaphore.returnLease(lease);
            logger.info("释放许可");

        } catch (Exception e) {
            logger.error("信号量异常", e);
        }

        client.close();
        */
    }
}
