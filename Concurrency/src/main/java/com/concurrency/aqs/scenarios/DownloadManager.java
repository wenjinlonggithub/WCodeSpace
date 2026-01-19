package com.concurrency.aqs.scenarios;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 基于AQS实现的下载管理器
 * 控制并发下载数量，支持任务队列和优先级
 *
 * 功能：
 * - 限制最大并发下载数
 * - 支持任务排队
 * - 支持下载进度跟踪
 * - 支持任务取消
 */
public class DownloadManager {

    private final Semaphore semaphore;
    private final int maxConcurrent;
    private final ConcurrentHashMap<String, DownloadTask> tasks;
    private final AtomicInteger taskIdGenerator;

    public DownloadManager(int maxConcurrent) {
        if (maxConcurrent <= 0) {
            throw new IllegalArgumentException("maxConcurrent must be positive");
        }
        this.maxConcurrent = maxConcurrent;
        this.semaphore = new Semaphore(maxConcurrent);
        this.tasks = new ConcurrentHashMap<>();
        this.taskIdGenerator = new AtomicInteger(0);
    }

    /**
     * 自定义信号量
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

            int getPermits() {
                return getState();
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

        int availablePermits() {
            return sync.getPermits();
        }
    }

    /**
     * 下载任务
     */
    public static class DownloadTask {
        private final String id;
        private final String url;
        private final String fileName;
        private volatile TaskStatus status;
        private volatile int progress;
        private volatile long startTime;
        private volatile long endTime;
        private volatile String error;

        public DownloadTask(String id, String url, String fileName) {
            this.id = id;
            this.url = url;
            this.fileName = fileName;
            this.status = TaskStatus.PENDING;
            this.progress = 0;
        }

        public String getId() { return id; }
        public String getUrl() { return url; }
        public String getFileName() { return fileName; }
        public TaskStatus getStatus() { return status; }
        public int getProgress() { return progress; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public String getError() { return error; }

        void setStatus(TaskStatus status) { this.status = status; }
        void setProgress(int progress) { this.progress = progress; }
        void setStartTime(long startTime) { this.startTime = startTime; }
        void setEndTime(long endTime) { this.endTime = endTime; }
        void setError(String error) { this.error = error; }
    }

    /**
     * 任务状态
     */
    public enum TaskStatus {
        PENDING,      // 等待中
        DOWNLOADING,  // 下载中
        COMPLETED,    // 已完成
        FAILED,       // 失败
        CANCELLED     // 已取消
    }

    /**
     * 提交下载任务
     */
    public String submitDownload(String url, String fileName) {
        String taskId = "task-" + taskIdGenerator.incrementAndGet();
        DownloadTask task = new DownloadTask(taskId, url, fileName);
        tasks.put(taskId, task);

        // 异步执行下载
        new Thread(() -> executeDownload(task), "Download-" + taskId).start();

        return taskId;
    }

    /**
     * 执行下载
     */
    private void executeDownload(DownloadTask task) {
        try {
            // 等待获取下载槽位
            semaphore.acquire();

            if (task.getStatus() == TaskStatus.CANCELLED) {
                return;
            }

            task.setStatus(TaskStatus.DOWNLOADING);
            task.setStartTime(System.currentTimeMillis());

            // 模拟下载过程
            simulateDownload(task);

            task.setStatus(TaskStatus.COMPLETED);
            task.setProgress(100);
            task.setEndTime(System.currentTimeMillis());

        } catch (InterruptedException e) {
            task.setStatus(TaskStatus.CANCELLED);
            task.setError("Download interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            task.setStatus(TaskStatus.FAILED);
            task.setError(e.getMessage());
            task.setEndTime(System.currentTimeMillis());
        } finally {
            semaphore.release();
        }
    }

    /**
     * 模拟下载过程
     */
    private void simulateDownload(DownloadTask task) throws InterruptedException {
        for (int i = 0; i <= 100; i += 10) {
            if (task.getStatus() == TaskStatus.CANCELLED) {
                throw new InterruptedException("Task cancelled");
            }
            task.setProgress(i);
            Thread.sleep(200); // 模拟下载耗时
        }
    }

    /**
     * 取消下载任务
     */
    public boolean cancelDownload(String taskId) {
        DownloadTask task = tasks.get(taskId);
        if (task != null && task.getStatus() == TaskStatus.PENDING) {
            task.setStatus(TaskStatus.CANCELLED);
            return true;
        }
        return false;
    }

    /**
     * 获取任务信息
     */
    public DownloadTask getTask(String taskId) {
        return tasks.get(taskId);
    }

    /**
     * 获取所有任务
     */
    public List<DownloadTask> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    /**
     * 获取当前下载数
     */
    public int getActiveDownloads() {
        return maxConcurrent - semaphore.availablePermits();
    }

    /**
     * 获取可用槽位
     */
    public int getAvailableSlots() {
        return semaphore.availablePermits();
    }

    /**
     * 测试示例
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 下载管理器测试 ===\n");

        DownloadManager manager = new DownloadManager(3);

        // 测试1：提交多个下载任务
        System.out.println("1. 提交下载任务（最大并发: 3）");
        List<String> taskIds = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            String taskId = manager.submitDownload(
                "http://example.com/file" + i + ".zip",
                "file" + i + ".zip"
            );
            taskIds.add(taskId);
            System.out.println("   提交任务: " + taskId);
        }

        // 测试2：监控下载进度
        System.out.println("\n2. 监控下载进度");
        Thread monitor = new Thread(() -> {
            try {
                for (int i = 0; i < 15; i++) {
                    Thread.sleep(500);
                    System.out.println("\n   --- 进度报告 ---");
                    System.out.println("   活跃下载: " + manager.getActiveDownloads() +
                                     " / 可用槽位: " + manager.getAvailableSlots());

                    for (DownloadTask task : manager.getAllTasks()) {
                        System.out.printf("   %s: %s [%d%%]%n",
                            task.getId(),
                            task.getStatus(),
                            task.getProgress()
                        );
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Monitor");
        monitor.start();

        // 等待所有任务完成
        Thread.sleep(8000);

        // 测试3：统计结果
        System.out.println("\n3. 下载统计");
        int completed = 0, failed = 0, cancelled = 0;
        long totalTime = 0;

        for (String taskId : taskIds) {
            DownloadTask task = manager.getTask(taskId);
            switch (task.getStatus()) {
                case COMPLETED:
                    completed++;
                    totalTime += (task.getEndTime() - task.getStartTime());
                    break;
                case FAILED:
                    failed++;
                    break;
                case CANCELLED:
                    cancelled++;
                    break;
            }
        }

        System.out.println("   总任务数: " + taskIds.size());
        System.out.println("   完成: " + completed);
        System.out.println("   失败: " + failed);
        System.out.println("   取消: " + cancelled);
        if (completed > 0) {
            System.out.println("   平均耗时: " + (totalTime / completed) + "ms");
        }

        // 测试4：任务取消
        System.out.println("\n4. 任务取消测试");
        DownloadManager manager2 = new DownloadManager(2);
        String task1 = manager2.submitDownload("http://example.com/large1.zip", "large1.zip");
        String task2 = manager2.submitDownload("http://example.com/large2.zip", "large2.zip");
        String task3 = manager2.submitDownload("http://example.com/large3.zip", "large3.zip");

        Thread.sleep(100);
        boolean cancelled3 = manager2.cancelDownload(task3);
        System.out.println("   取消任务3: " + (cancelled3 ? "成功" : "失败"));

        Thread.sleep(3000);
        System.out.println("   任务1状态: " + manager2.getTask(task1).getStatus());
        System.out.println("   任务2状态: " + manager2.getTask(task2).getStatus());
        System.out.println("   任务3状态: " + manager2.getTask(task3).getStatus());

        monitor.interrupt();
        System.out.println("\n测试完成！");
    }
}
