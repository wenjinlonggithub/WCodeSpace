package com.concurrency.aqs.enterprise;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 携程 - 酒店库存管理系统
 *
 * 业务场景：
 * 携程酒店预订系统需要精确控制房间库存，防止超订。
 * 支持预订、取消、释放等操作，保证库存准确性。
 *
 * 技术方案：
 * - 使用Semaphore表示可用房间数
 * - 预订时acquire，取消时release
 * - 支持超时预订和自动释放
 *
 * 业务价值：
 * - 防止超订，保证库存准确性
 * - 支持高并发预订场景
 * - 提供灵活的库存管理能力
 */
public class CtripHotelInventory {

    private final String hotelId;
    private final String hotelName;
    private final Map<String, RoomType> roomTypes;
    private final Map<String, Reservation> reservations;

    public CtripHotelInventory(String hotelId, String hotelName) {
        this.hotelId = hotelId;
        this.hotelName = hotelName;
        this.roomTypes = new ConcurrentHashMap<>();
        this.reservations = new ConcurrentHashMap<>();
    }

    /**
     * 添加房型
     */
    public void addRoomType(String roomTypeId, String roomTypeName, int totalRooms, double price) {
        RoomType roomType = new RoomType(roomTypeId, roomTypeName, totalRooms, price);
        roomTypes.put(roomTypeId, roomType);
        System.out.printf("[房型添加] %s - %s: %d间, %.2f元/晚%n",
                hotelName, roomTypeName, totalRooms, price);
    }

    /**
     * 预订房间（核心方法）
     */
    public String bookRoom(String roomTypeId, String guestName, LocalDate checkIn,
                           LocalDate checkOut, long timeoutMs) {
        RoomType roomType = roomTypes.get(roomTypeId);
        if (roomType == null) {
            System.err.printf("[预订失败] 房型%s不存在%n", roomTypeId);
            return null;
        }

        // 尝试获取房间（带超时）
        try {
            boolean acquired = roomType.inventory.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
            if (!acquired) {
                System.err.printf("[预订失败] %s - %s房间已满%n", guestName, roomType.name);
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }

        // 生成预订单
        String reservationId = generateReservationId();
        Reservation reservation = new Reservation(
                reservationId,
                roomTypeId,
                guestName,
                checkIn,
                checkOut,
                roomType.price,
                System.currentTimeMillis()
        );

        reservations.put(reservationId, reservation);

        int available = roomType.inventory.availablePermits();
        System.out.printf("[预订成功] %s预订%s - 订单号: %s (剩余%d间)%n",
                guestName, roomType.name, reservationId, available);

        return reservationId;
    }

    /**
     * 取消预订
     */
    public boolean cancelReservation(String reservationId) {
        Reservation reservation = reservations.remove(reservationId);
        if (reservation == null) {
            System.err.printf("[取消失败] 订单%s不存在%n", reservationId);
            return false;
        }

        RoomType roomType = roomTypes.get(reservation.roomTypeId);
        if (roomType == null) {
            return false;
        }

        // 释放房间
        roomType.inventory.release();

        int available = roomType.inventory.availablePermits();
        System.out.printf("[取消成功] 订单%s已取消 - %s (当前可用%d间)%n",
                reservationId, roomType.name, available);

        return true;
    }

    /**
     * 批量预订（团队预订）
     */
    public boolean bookRoomsBatch(String roomTypeId, String groupName, int roomCount,
                                  LocalDate checkIn, LocalDate checkOut) {
        RoomType roomType = roomTypes.get(roomTypeId);
        if (roomType == null) {
            System.err.printf("[团队预订失败] 房型%s不存在%n", roomTypeId);
            return false;
        }

        // 尝试获取多个房间
        boolean acquired = roomType.inventory.tryAcquire(roomCount);
        if (!acquired) {
            System.err.printf("[团队预订失败] %s - %s房间不足 (需要%d间)%n",
                    groupName, roomType.name, roomCount);
            return false;
        }

        System.out.printf("[团队预订成功] %s预订%d间%s (剩余%d间)%n",
                groupName, roomCount, roomType.name, roomType.inventory.availablePermits());

        return true;
    }

    /**
     * 查询房型可用性
     */
    public RoomAvailability checkAvailability(String roomTypeId) {
        RoomType roomType = roomTypes.get(roomTypeId);
        if (roomType == null) {
            return null;
        }

        int available = roomType.inventory.availablePermits();
        int booked = roomType.totalRooms - available;
        double occupancyRate = (double) booked / roomType.totalRooms * 100;

        return new RoomAvailability(
                roomType.id,
                roomType.name,
                roomType.totalRooms,
                available,
                booked,
                occupancyRate,
                roomType.price
        );
    }

    /**
     * 获取酒店统计
     */
    public HotelStats getStats() {
        int totalRooms = 0;
        int availableRooms = 0;
        int totalReservations = reservations.size();

        for (RoomType roomType : roomTypes.values()) {
            totalRooms += roomType.totalRooms;
            availableRooms += roomType.inventory.availablePermits();
        }

        int bookedRooms = totalRooms - availableRooms;
        double occupancyRate = totalRooms > 0 ? (double) bookedRooms / totalRooms * 100 : 0;

        return new HotelStats(hotelId, hotelName, totalRooms, availableRooms,
                bookedRooms, occupancyRate, totalReservations);
    }

    private String generateReservationId() {
        return "RES" + System.currentTimeMillis() + (int) (Math.random() * 1000);
    }

    // 房型
    static class RoomType {
        private final String id;
        private final String name;
        private final int totalRooms;
        private final double price;
        private final Semaphore inventory;

        public RoomType(String id, String name, int totalRooms, double price) {
            this.id = id;
            this.name = name;
            this.totalRooms = totalRooms;
            this.price = price;
            this.inventory = new Semaphore(totalRooms);
        }
    }

    // 预订单
    static class Reservation {
        private final String id;
        private final String roomTypeId;
        private final String guestName;
        private final LocalDate checkIn;
        private final LocalDate checkOut;
        private final double price;
        private final long createTime;

        public Reservation(String id, String roomTypeId, String guestName,
                           LocalDate checkIn, LocalDate checkOut, double price, long createTime) {
            this.id = id;
            this.roomTypeId = roomTypeId;
            this.guestName = guestName;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.price = price;
            this.createTime = createTime;
        }
    }

    // 房型可用性
    public static class RoomAvailability {
        private final String roomTypeId;
        private final String roomTypeName;
        private final int totalRooms;
        private final int availableRooms;
        private final int bookedRooms;
        private final double occupancyRate;
        private final double price;

        public RoomAvailability(String roomTypeId, String roomTypeName, int totalRooms,
                                int availableRooms, int bookedRooms, double occupancyRate, double price) {
            this.roomTypeId = roomTypeId;
            this.roomTypeName = roomTypeName;
            this.totalRooms = totalRooms;
            this.availableRooms = availableRooms;
            this.bookedRooms = bookedRooms;
            this.occupancyRate = occupancyRate;
            this.price = price;
        }

        @Override
        public String toString() {
            return String.format("%s: 总计%d间, 可用%d间, 已订%d间, 入住率%.1f%%, %.2f元/晚",
                    roomTypeName, totalRooms, availableRooms, bookedRooms, occupancyRate, price);
        }
    }

    // 酒店统计
    public static class HotelStats {
        private final String hotelId;
        private final String hotelName;
        private final int totalRooms;
        private final int availableRooms;
        private final int bookedRooms;
        private final double occupancyRate;
        private final int totalReservations;

        public HotelStats(String hotelId, String hotelName, int totalRooms,
                          int availableRooms, int bookedRooms, double occupancyRate, int totalReservations) {
            this.hotelId = hotelId;
            this.hotelName = hotelName;
            this.totalRooms = totalRooms;
            this.availableRooms = availableRooms;
            this.bookedRooms = bookedRooms;
            this.occupancyRate = occupancyRate;
            this.totalReservations = totalReservations;
        }

        public void print() {
            System.out.println("\n=== 酒店统计 ===");
            System.out.printf("酒店: %s (ID: %s)%n", hotelName, hotelId);
            System.out.printf("总房间数: %d%n", totalRooms);
            System.out.printf("可用房间: %d%n", availableRooms);
            System.out.printf("已订房间: %d%n", bookedRooms);
            System.out.printf("入住率: %.1f%%%n", occupancyRate);
            System.out.printf("预订单数: %d%n", totalReservations);
        }
    }

    // 测试示例
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 携程酒店库存管理系统演示 ===\n");

        CtripHotelInventory hotel = new CtripHotelInventory("H001", "携程大酒店");

        // 添加房型
        hotel.addRoomType("RT001", "豪华大床房", 10, 688.00);
        hotel.addRoomType("RT002", "商务双床房", 15, 588.00);
        hotel.addRoomType("RT003", "总统套房", 2, 2888.00);

        System.out.println();

        // 模拟30个用户并发预订
        Thread[] threads = new Thread[30];
        for (int i = 0; i < 30; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                String guestName = "客人" + (index + 1);
                String roomTypeId = "RT00" + (index % 3 + 1);
                LocalDate checkIn = LocalDate.now().plusDays(1);
                LocalDate checkOut = LocalDate.now().plusDays(3);

                hotel.bookRoom(roomTypeId, guestName, checkIn, checkOut, 1000);
            }, "BookingThread-" + i);
            threads[i].start();
        }

        // 等待所有预订完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 查询房型可用性
        System.out.println("\n=== 房型可用性 ===");
        System.out.println(hotel.checkAvailability("RT001"));
        System.out.println(hotel.checkAvailability("RT002"));
        System.out.println(hotel.checkAvailability("RT003"));

        // 打印酒店统计
        hotel.getStats().print();

        // 测试团队预订
        System.out.println("\n=== 团队预订 ===");
        hotel.bookRoomsBatch("RT002", "XX公司团队", 5,
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(7));

        hotel.getStats().print();
    }
}
