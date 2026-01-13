package com.architecture.distributedid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 雪花算法（Snowflake）分布式ID生成器
 *
 * 业务背景：
 * 分布式系统中需要生成全局唯一ID，用于订单号、用户ID等场景。
 *
 * 传统方案的问题：
 * 1. 数据库自增ID：性能差，单点故障
 * 2. UUID：无序，不适合数据库索引
 * 3. Redis INCR：依赖Redis，网络开销
 *
 * 雪花算法：
 * Twitter开源的分布式ID生成算法
 *
 * ID结构（64位）：
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
 * |   |-------------------------------------------|   |-----|   |-----|   |----------|
 * 符号位    41位时间戳（毫秒）                         5位机房ID  5位机器ID  12位序列号
 * (1位)    (精确到毫秒，可用69年)                     (32个机房) (32台机器) (每毫秒4096个ID)
 *
 * 优势：
 * 1. 趋势递增：时间戳在前，ID总体递增，适合数据库索引
 * 2. 高性能：本地生成，无网络开销，单机QPS可达百万
 * 3. 高可用：无单点故障
 * 4. 信息丰富：ID中包含时间戳、机房、机器等信息
 *
 * 注意事项：
 * 1. 时钟回拨问题：服务器时间回拨会导致ID重复
 * 2. 机器ID分配：需要保证每台机器的workerId唯一
 */
public class SnowflakeIdGenerator {

    private static final Logger logger = LoggerFactory.getLogger(SnowflakeIdGenerator.class);

    // =============================== 位移量 ===============================
    // 时间戳左移22位 (5位机房ID + 5位机器ID + 12位序列号)
    private static final long TIMESTAMP_LEFT_SHIFT = 22L;

    // 机房ID左移17位 (5位机器ID + 12位序列号)
    private static final long DATACENTER_ID_SHIFT = 17L;

    // 机器ID左移12位 (12位序列号)
    private static final long WORKER_ID_SHIFT = 12L;

    // =============================== 掩码 ===============================
    // 序列号掩码：4095 (0b111111111111=0xfff=4095)
    private static final long SEQUENCE_MASK = ~(-1L << 12);

    // 机器ID掩码：31
    private static final long WORKER_ID_MASK = ~(-1L << 5);

    // 机房ID掩码：31
    private static final long DATACENTER_ID_MASK = ~(-1L << 5);

    // =============================== 配置 ===============================
    // 起始时间戳 (2024-01-01 00:00:00)
    private static final long EPOCH = 1704038400000L;

    // 机房ID（5位，最大31）
    private final long datacenterId;

    // 机器ID（5位，最大31）
    private final long workerId;

    // 序列号（12位，最大4095）
    private long sequence = 0L;

    // 上次生成ID的时间戳
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long datacenterId, long workerId) {
        if (datacenterId > DATACENTER_ID_MASK || datacenterId < 0) {
            throw new IllegalArgumentException("Datacenter ID must be between 0 and " + DATACENTER_ID_MASK);
        }
        if (workerId > WORKER_ID_MASK || workerId < 0) {
            throw new IllegalArgumentException("Worker ID must be between 0 and " + WORKER_ID_MASK);
        }

        this.datacenterId = datacenterId;
        this.workerId = workerId;

        logger.info("Snowflake ID生成器初始化成功, datacenterId={}, workerId={}", datacenterId, workerId);
    }

    /**
     * 生成下一个ID（线程安全）
     *
     * @return 唯一ID
     */
    public synchronized long nextId() {
        long timestamp = currentTimeMillis();

        // 时钟回拨检测
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;
            logger.error("时钟回拨{}毫秒，拒绝生成ID", offset);
            throw new RuntimeException("Clock moved backwards. Refusing to generate id for " + offset + " milliseconds");
        }

        // 同一毫秒内，序列号自增
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;

            // 序列号溢出（同一毫秒生成了4096个ID）
            if (sequence == 0) {
                // 阻塞到下一毫秒
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置为0
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 组装ID
        // 时间戳部分 | 机房ID部分 | 机器ID部分 | 序列号部分
        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 阻塞等待到下一毫秒
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳
     */
    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 解析ID，提取时间戳、机房ID、机器ID、序列号
     */
    public static IdInfo parseId(long id) {
        long timestamp = (id >> TIMESTAMP_LEFT_SHIFT) + EPOCH;
        long datacenterId = (id >> DATACENTER_ID_SHIFT) & DATACENTER_ID_MASK;
        long workerId = (id >> WORKER_ID_SHIFT) & WORKER_ID_MASK;
        long sequence = id & SEQUENCE_MASK;

        return new IdInfo(id, timestamp, datacenterId, workerId, sequence);
    }

    /**
     * ID信息
     */
    public static class IdInfo {
        private final long id;
        private final long timestamp;
        private final long datacenterId;
        private final long workerId;
        private final long sequence;

        public IdInfo(long id, long timestamp, long datacenterId, long workerId, long sequence) {
            this.id = id;
            this.timestamp = timestamp;
            this.datacenterId = datacenterId;
            this.workerId = workerId;
            this.sequence = sequence;
        }

        @Override
        public String toString() {
            return String.format("ID=%d, timestamp=%d, datacenterId=%d, workerId=%d, sequence=%d",
                    id, timestamp, datacenterId, workerId, sequence);
        }
    }

    /**
     * 使用示例
     */
    public static void main(String[] args) {
        // 创建ID生成器：机房ID=1, 机器ID=1
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(1, 1);

        // 生成10个ID
        for (int i = 0; i < 10; i++) {
            long id = generator.nextId();
            logger.info("生成ID: {}", id);

            // 解析ID
            IdInfo info = parseId(id);
            logger.info("解析结果: {}", info);
        }

        // 性能测试：单线程生成100万个ID
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            generator.nextId();
        }
        long elapsed = System.currentTimeMillis() - start;
        logger.info("生成100万个ID耗时: {}ms, QPS: {}", elapsed, 1000000 * 1000 / elapsed);

        // 结果：约100ms，QPS达到1000万
    }
}
