package com.concurrency.aqs.enterprise;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 美团 - 外卖配送系统订单池
 *
 * 业务场景：
 * 美团外卖配送系统中，多个骑手并发读取订单池中的订单，抢单时需要独占写入。
 * 订单池需要支持高并发读取和精确的抢单控制。
 *
 * 技术方案：
 * - 使用ReadWriteLock实现订单池的读写分离
 * - 多个骑手并发读取订单（读锁）
 * - 抢单时独占写入（写锁）
 *
 * 业务价值：
 * - 提升订单分配效率，降低锁竞争
 * - 支持多骑手并发查看订单
 * - 保证抢单操作的原子性
 */
public class MeituanOrderPool {

    private final List<Order> availableOrders;
    private final List<Order> takenOrders;
    private final ReadWriteLock rwLock;

    public MeituanOrderPool() {
        this.availableOrders = new ArrayList<>();
        this.takenOrders = new CopyOnWriteArrayList<>();
        this.rwLock = new ReentrantReadWriteLock();
    }

    /**
     * 发布新订单
     */
    public void publishOrder(Order order) {
        rwLock.writeLock().lock();
        try {
            availableOrders.add(order);
            System.out.printf("[订单发布] 订单%s已发布 - %s (配送费: %.2f元)%n",
                    order.orderId, order.restaurant, order.deliveryFee);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * 骑手查看可用订单（读操作，支持并发）
     */
    public List<Order> viewAvailableOrders(String riderId) {
        rwLock.readLock().lock();
        try {
            System.out.printf("[查看订单] 骑手%s查看订单池 (当前%d个订单)%n",
                    riderId, availableOrders.size());
            return new ArrayList<>(availableOrders);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 骑手抢单（写操作，独占）
     */
    public boolean grabOrder(String riderId, String orderId) {
        rwLock.writeLock().lock();
        try {
            // 查找订单
            Order order = null;
            for (Order o : availableOrders) {
                if (o.orderId.equals(orderId)) {
                    order = o;
                    break;
                }
            }

            if (order == null) {
                System.out.printf("[抢单失败] 骑手%s - 订单%s已被抢走%n", riderId, orderId);
                return false;
            }

            // 抢单成功
            availableOrders.remove(order);
            order.riderId = riderId;
            order.grabTime = System.currentTimeMillis();
            takenOrders.add(order);

            System.out.printf("[抢单成功] 骑手%s抢到订单%s - 配送费%.2f元%n",
                    riderId, orderId, order.deliveryFee);

            return true;

        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * 完成配送
     */
    public boolean completeDelivery(String riderId, String orderId) {
        rwLock.writeLock().lock();
        try {
            for (Order order : takenOrders) {
                if (order.orderId.equals(orderId) && order.riderId.equals(riderId)) {
                    order.completed = true;
                    order.completeTime = System.currentTimeMillis();
                    long duration = order.completeTime - order.grabTime;

                    System.out.printf("[配送完成] 骑手%s完成订单%s - 耗时%d秒%n",
                            riderId, orderId, duration / 1000);
                    return true;
                }
            }
            return false;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * 获取统计信息
     */
    public PoolStats getStats() {
        rwLock.readLock().lock();
        try {
            int available = availableOrders.size();
            int taken = (int) takenOrders.stream().filter(o -> !o.completed).count();
            int completed = (int) takenOrders.stream().filter(o -> o.completed).count();

            return new PoolStats(available, taken, completed);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // 订单实体
    public static class Order {
        private final String orderId;
        private final String restaurant;
        private final String address;
        private final double deliveryFee;
        private String riderId;
        private long grabTime;
        private long completeTime;
        private boolean completed;

        public Order(String orderId, String restaurant, String address, double deliveryFee) {
            this.orderId = orderId;
            this.restaurant = restaurant;
            this.address = address;
            this.deliveryFee = deliveryFee;
        }

        public String getOrderId() {
            return orderId;
        }

        public double getDeliveryFee() {
            return deliveryFee;
        }
    }

    // 统计信息
    public static class PoolStats {
        private final int availableOrders;
        private final int takenOrders;
        private final int completedOrders;

        public PoolStats(int availableOrders, int takenOrders, int completedOrders) {
            this.availableOrders = availableOrders;
            this.takenOrders = takenOrders;
            this.completedOrders = completedOrders;
        }

        public void print() {
            System.out.println("\n=== 订单池统计 ===");
            System.out.printf("待抢订单: %d%n", availableOrders);
            System.out.printf("配送中: %d%n", takenOrders);
            System.out.printf("已完成: %d%n", completedOrders);
            System.out.printf("总订单数: %d%n", availableOrders + takenOrders + completedOrders);
        }
    }

    // 测试示例
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 美团外卖订单池演示 ===\n");

        MeituanOrderPool pool = new MeituanOrderPool();

        // 发布20个订单
        for (int i = 1; i <= 20; i++) {
            Order order = new Order(
                    "ORDER-" + i,
                    "餐厅" + i,
                    "地址" + i,
                    5 + Math.random() * 10
            );
            pool.publishOrder(order);
        }

        System.out.println();

        // 模拟10个骑手抢单
        Thread[] riders = new Thread[10];
        for (int i = 0; i < 10; i++) {
            final String riderId = "Rider-" + (i + 1);
            riders[i] = new Thread(() -> {
                try {
                    // 查看订单
                    List<Order> orders = pool.viewAvailableOrders(riderId);

                    if (!orders.isEmpty()) {
                        // 选择配送费最高的订单
                        Order bestOrder = orders.stream()
                                .max((o1, o2) -> Double.compare(o1.getDeliveryFee(), o2.getDeliveryFee()))
                                .orElse(null);

                        if (bestOrder != null) {
                            Thread.sleep((long) (Math.random() * 100));
                            boolean success = pool.grabOrder(riderId, bestOrder.getOrderId());

                            if (success) {
                                // 模拟配送
                                Thread.sleep((long) (1000 + Math.random() * 2000));
                                pool.completeDelivery(riderId, bestOrder.getOrderId());
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, riderId);
            riders[i].start();
        }

        // 等待所有骑手完成
        for (Thread rider : riders) {
            rider.join();
        }

        // 打印统计信息
        pool.getStats().print();
    }
}
