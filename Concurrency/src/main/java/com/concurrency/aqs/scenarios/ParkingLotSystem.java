package com.concurrency.aqs.scenarios;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 基于AQS实现的停车场管理系统
 * 演示AQS在资源管理和状态控制中的应用
 *
 * 功能：
 * - 车位数量限制
 * - 车辆进出管理
 * - 停车时长统计
 * - 车位状态查询
 */
public class ParkingLotSystem {

    private final Semaphore semaphore;
    private final int totalSpots;
    private final ConcurrentHashMap<String, ParkingRecord> records;
    private final AtomicInteger spotIdGenerator;

    public ParkingLotSystem(int totalSpots) {
        if (totalSpots <= 0) {
            throw new IllegalArgumentException("totalSpots must be positive");
        }
        this.totalSpots = totalSpots;
        this.semaphore = new Semaphore(totalSpots);
        this.records = new ConcurrentHashMap<>();
        this.spotIdGenerator = new AtomicInteger(0);
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
     * 停车记录
     */
    public static class ParkingRecord {
        private final String vehicleId;
        private final int spotNumber;
        private final long entryTime;
        private volatile long exitTime;
        private volatile boolean active;

        public ParkingRecord(String vehicleId, int spotNumber) {
            this.vehicleId = vehicleId;
            this.spotNumber = spotNumber;
            this.entryTime = System.currentTimeMillis();
            this.active = true;
        }

        public String getVehicleId() { return vehicleId; }
        public int getSpotNumber() { return spotNumber; }
        public long getEntryTime() { return entryTime; }
        public long getExitTime() { return exitTime; }
        public boolean isActive() { return active; }

        public long getDuration() {
            if (active) {
                return System.currentTimeMillis() - entryTime;
            }
            return exitTime - entryTime;
        }

        void setExitTime(long exitTime) {
            this.exitTime = exitTime;
            this.active = false;
        }
    }

    /**
     * 车辆进入停车场（阻塞）
     */
    public ParkingRecord enter(String vehicleId) throws InterruptedException {
        semaphore.acquire();
        int spotNumber = spotIdGenerator.incrementAndGet();
        ParkingRecord record = new ParkingRecord(vehicleId, spotNumber);
        records.put(vehicleId, record);
        return record;
    }

    /**
     * 车辆进入停车场（带超时）
     */
    public ParkingRecord enter(String vehicleId, long timeout, TimeUnit unit)
            throws InterruptedException {
        if (semaphore.tryAcquire(timeout, unit)) {
            int spotNumber = spotIdGenerator.incrementAndGet();
            ParkingRecord record = new ParkingRecord(vehicleId, spotNumber);
            records.put(vehicleId, record);
            return record;
        }
        return null;
    }

    /**
     * 车辆离开停车场
     */
    public boolean exit(String vehicleId) {
        ParkingRecord record = records.get(vehicleId);
        if (record != null && record.isActive()) {
            record.setExitTime(System.currentTimeMillis());
            semaphore.release();
            return true;
        }
        return false;
    }

    /**
     * 获取停车记录
     */
    public ParkingRecord getRecord(String vehicleId) {
        return records.get(vehicleId);
    }

    /**
     * 获取所有活跃记录
     */
    public List<ParkingRecord> getActiveRecords() {
        List<ParkingRecord> active = new ArrayList<>();
        for (ParkingRecord record : records.values()) {
            if (record.isActive()) {
                active.add(record);
            }
        }
        return active;
    }

    /**
     * 获取可用车位数
     */
    public int getAvailableSpots() {
        return semaphore.availablePermits();
    }

    /**
     * 获取已占用车位数
     */
    public int getOccupiedSpots() {
        return totalSpots - semaphore.availablePermits();
    }

    /**
     * 获取总车位数
     */
    public int getTotalSpots() {
        return totalSpots;
    }

    /**
     * 是否已满
     */
    public boolean isFull() {
        return semaphore.availablePermits() == 0;
    }

    /**
     * 计算停车费用（简单示例：每小时10元）
     */
    public double calculateFee(String vehicleId) {
        ParkingRecord record = records.get(vehicleId);
        if (record == null) {
            return 0.0;
        }
        long durationMillis = record.getDuration();
        long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
        if (hours == 0) {
            hours = 1; // 不足1小时按1小时计算
        }
        return hours * 10.0;
    }

    /**
     * 测试示例
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 停车场管理系统测试 ===\n");

        ParkingLotSystem parkingLot = new ParkingLotSystem(5);

        // 测试1：基本进出操作
        System.out.println("1. 基本进出操作（总车位: 5）");
        System.out.println("   可用车位: " + parkingLot.getAvailableSpots());

        ParkingRecord car1 = parkingLot.enter("京A12345");
        System.out.println("   车辆 京A12345 进入，车位号: " + car1.getSpotNumber());
        System.out.println("   可用车位: " + parkingLot.getAvailableSpots());

        ParkingRecord car2 = parkingLot.enter("京B67890");
        System.out.println("   车辆 京B67890 进入，车位号: " + car2.getSpotNumber());
        System.out.println("   可用车位: " + parkingLot.getAvailableSpots());

        // 测试2：并发进入
        System.out.println("\n2. 并发进入测试（8辆车竞争5个车位）");
        Thread[] threads = new Thread[8];
        for (int i = 0; i < 8; i++) {
            final String vehicleId = "车" + (i + 1);
            threads[i] = new Thread(() -> {
                try {
                    System.out.println("   " + vehicleId + " 尝试进入...");
                    ParkingRecord record = parkingLot.enter(vehicleId, 2, TimeUnit.SECONDS);
                    if (record != null) {
                        System.out.println("   " + vehicleId + " 成功进入，车位: " +
                                         record.getSpotNumber() +
                                         " (剩余: " + parkingLot.getAvailableSpots() + ")");
                        Thread.sleep((long) (Math.random() * 2000));
                        parkingLot.exit(vehicleId);
                        System.out.println("   " + vehicleId + " 离开 (剩余: " +
                                         parkingLot.getAvailableSpots() + ")");
                    } else {
                        System.out.println("   " + vehicleId + " 进入超时，停车场已满");
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

        // 测试3：停车时长和费用
        System.out.println("\n3. 停车时长和费用测试");
        ParkingLotSystem lot2 = new ParkingLotSystem(3);

        ParkingRecord testCar = lot2.enter("测试车001");
        System.out.println("   车辆进入，车位: " + testCar.getSpotNumber());
        System.out.println("   等待2秒模拟停车...");
        Thread.sleep(2000);

        long duration = testCar.getDuration();
        double fee = lot2.calculateFee("测试车001");
        System.out.println("   停车时长: " + duration + "ms");
        System.out.println("   停车费用: " + fee + "元");

        lot2.exit("测试车001");
        System.out.println("   车辆离开");

        // 测试4：实时监控
        System.out.println("\n4. 实时监控测试");
        ParkingLotSystem lot3 = new ParkingLotSystem(4);

        // 启动监控线程
        Thread monitor = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    Thread.sleep(500);
                    System.out.println("\n   --- 停车场状态 ---");
                    System.out.println("   总车位: " + lot3.getTotalSpots());
                    System.out.println("   已占用: " + lot3.getOccupiedSpots());
                    System.out.println("   可用: " + lot3.getAvailableSpots());
                    System.out.println("   是否已满: " + lot3.isFull());

                    List<ParkingRecord> active = lot3.getActiveRecords();
                    if (!active.isEmpty()) {
                        System.out.println("   当前停车车辆:");
                        for (ParkingRecord record : active) {
                            System.out.printf("     %s - 车位%d - 已停%dms%n",
                                record.getVehicleId(),
                                record.getSpotNumber(),
                                record.getDuration()
                            );
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Monitor");
        monitor.start();

        // 模拟车辆进出
        Thread[] vehicles = new Thread[6];
        for (int i = 0; i < 6; i++) {
            final String vid = "监控车" + (i + 1);
            vehicles[i] = new Thread(() -> {
                try {
                    Thread.sleep((long) (Math.random() * 1000));
                    ParkingRecord rec = lot3.enter(vid, 3, TimeUnit.SECONDS);
                    if (rec != null) {
                        Thread.sleep((long) (Math.random() * 3000));
                        lot3.exit(vid);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Vehicle-" + i);
            vehicles[i].start();
        }

        for (Thread vehicle : vehicles) {
            vehicle.join();
        }
        monitor.join();

        System.out.println("\n测试完成！");
    }
}
