package com.okx.finance.common.util;

/**
 * 雪花算法ID生成器
 * 用于生成全局唯一、趋势递增的64位Long类型ID
 *
 * <p>算法说明：
 * 雪花算法（Snowflake）是Twitter开源的分布式ID生成算法
 * 生成的ID是一个64位的Long类型数字，结构如下：
 *
 * <pre>
 * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000000 00 - 000000000000
 * |   -------------------------------------------|   ----------   ------------
 * 1位  41位时间戳（毫秒级）                         10位机器ID    12位序列号
 * 符号位（始终为0）                                (支持1024台机器) (同一毫秒内支持4096个ID)
 * </pre>
 *
 * <p>特点：
 * 1. 全局唯一：不会产生重复ID
 * 2. 趋势递增：ID随时间递增，适合作为主键，提高数据库索引性能
 * 3. 高性能：本地生成，不依赖数据库或Redis，生成速度快
 * 4. 信息可解析：可以从ID中提取时间戳和机器ID
 *
 * <p>使用场景：
 * 1. 数据库主键ID（替代自增ID）
 * 2. 订单号、交易号生成
 * 3. 分布式系统中的唯一标识
 *
 * <p>注意事项：
 * 1. 需要确保不同机器的machineId不重复
 * 2. 依赖系统时钟，时钟回拨会导致生成失败
 * 3. 单机每毫秒最多生成4096个ID
 *
 * @author OKX Finance Team
 * @version 1.0
 */
public class SnowflakeIdGenerator {
    /**
     * 起始时间戳（2024-01-01 00:00:00 UTC）
     * 用于减小时间戳占用的位数
     * 该值固定后不应修改，否则会导致ID冲突
     */
    private static final long START_TIMESTAMP = 1704067200000L;

    /**
     * 序列号占用的位数（12位）
     * 同一毫秒内可以生成 2^12 = 4096 个ID
     */
    private static final long SEQUENCE_BIT = 12;

    /**
     * 机器ID占用的位数（10位）
     * 支持 2^10 = 1024 台机器
     * 可根据实际需求调整，例如5位机器ID+5位数据中心ID
     */
    private static final long MACHINE_BIT = 10;

    /**
     * 最大序列号（4095）
     * 计算方式：~(-1L << 12) = 4095
     */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    /**
     * 最大机器ID（1023）
     * 计算方式：~(-1L << 10) = 1023
     */
    private static final long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);

    /**
     * 机器ID左移位数（12位）
     * 机器ID需要左移12位（序列号占用12位）
     */
    private static final long MACHINE_LEFT = SEQUENCE_BIT;

    /**
     * 时间戳左移位数（22位）
     * 时间戳需要左移22位（机器ID占10位 + 序列号占12位）
     */
    private static final long TIMESTAMP_LEFT = SEQUENCE_BIT + MACHINE_BIT;

    /**
     * 机器ID（0-1023）
     * 不同机器应使用不同的ID，确保全局唯一性
     */
    private long machineId;

    /**
     * 当前毫秒内的序列号（0-4095）
     * 每毫秒从0开始递增
     */
    private long sequence = 0L;

    /**
     * 上一次生成ID的时间戳
     * 用于判断是否进入新的毫秒
     */
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     *
     * @param machineId 机器ID，取值范围 [0, 1023]
     * @throws IllegalArgumentException 如果machineId超出范围
     */
    public SnowflakeIdGenerator(long machineId) {
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("Machine ID can't be greater than " + MAX_MACHINE_NUM + " or less than 0");
        }
        this.machineId = machineId;
    }

    /**
     * 生成下一个ID
     *
     * <p>方法是线程安全的（使用synchronized）
     * 生成流程：
     * 1. 获取当前时间戳
     * 2. 如果在同一毫秒内，序列号+1
     * 3. 如果序列号溢出，等待下一毫秒
     * 4. 如果进入新的毫秒，序列号重置为0
     * 5. 组合时间戳、机器ID和序列号生成最终ID
     *
     * @return 64位长整型ID
     * @throws RuntimeException 如果检测到时钟回拨
     */
    public synchronized long nextId() {
        long currentTimestamp = getCurrentTimestamp();

        // 检测时钟回拨
        if (currentTimestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        // 同一毫秒内
        if (currentTimestamp == lastTimestamp) {
            // 序列号递增
            sequence = (sequence + 1) & MAX_SEQUENCE;

            // 序列号溢出，等待下一毫秒
            if (sequence == 0L) {
                currentTimestamp = getNextTimestamp();
            }
        } else {
            // 进入新的毫秒，序列号重置
            sequence = 0L;
        }

        // 更新时间戳
        lastTimestamp = currentTimestamp;

        // 组合各部分生成最终ID
        // 1. 时间戳部分：(当前时间戳 - 起始时间戳) << 22
        // 2. 机器ID部分：机器ID << 12
        // 3. 序列号部分：序列号
        // 使用位或运算组合
        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_LEFT)
                | (machineId << MACHINE_LEFT)
                | sequence;
    }

    /**
     * 获取当前时间戳（毫秒）
     *
     * @return 当前时间的毫秒级时间戳
     */
    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 等待下一毫秒
     *
     * <p>当同一毫秒内序列号用尽时调用
     * 通过循环等待直到进入下一毫秒
     *
     * @return 下一毫秒的时间戳
     */
    private long getNextTimestamp() {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }
}
