package com.architecture;

/**
 * 大厂面试 - 业务场景疑难杂症解决方案
 *
 * 本项目整理了大厂面试中常见的8大核心场景及其技术实现方案
 *
 * 场景列表：
 * 1. 高并发秒杀系统      - com.architecture.seckill
 * 2. 分布式锁           - com.architecture.distributedlock
 * 3. 延迟队列           - com.architecture.delayqueue
 * 4. 限流算法           - com.architecture.ratelimit
 * 5. 接口幂等性         - com.architecture.idempotent
 * 6. 热点数据处理       - com.architecture.hotdata
 * 7. 分布式ID生成       - com.architecture.distributedid
 * 8. APP在线状态检测与智能推送 - com.architecture.apppush
 *
 * 每个场景包含：
 * - README.md：业务背景、问题分析、技术方案对比
 * - 完整的代码实现
 * - 可运行的示例
 */
public class App {

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("大厂面试 - 业务场景疑难杂症解决方案");
        System.out.println("=".repeat(80));
        System.out.println();

        System.out.println("场景一：高并发秒杀系统");
        System.out.println("  核心技术：Redis预减库存 + 消息队列异步削峰 + 乐观锁防超卖");
        System.out.println("  性能指标：QPS 10万+，零超卖");
        System.out.println("  代码位置：com.architecture.seckill");
        System.out.println();

        System.out.println("场景二：分布式锁");
        System.out.println("  核心技术：Redis SET NX EX + Redisson看门狗 + Zookeeper临时节点");
        System.out.println("  推荐方案：Redisson（简单高效） / Zookeeper（强一致性）");
        System.out.println("  代码位置：com.architecture.distributedlock");
        System.out.println();

        System.out.println("场景三：延迟队列");
        System.out.println("  核心技术：Redis Sorted Set + RabbitMQ死信队列 + 时间轮算法");
        System.out.println("  推荐方案：Redis Sorted Set（通用） / 时间轮（高性能）");
        System.out.println("  代码位置：com.architecture.delayqueue");
        System.out.println();

        System.out.println("场景四：限流算法");
        System.out.println("  核心技术：令牌桶 + 漏桶 + 滑动窗口 + Redis分布式限流");
        System.out.println("  推荐方案：令牌桶算法（允许突发流量）");
        System.out.println("  代码位置：com.architecture.ratelimit");
        System.out.println();

        System.out.println("场景五：接口幂等性设计");
        System.out.println("  核心技术：Token机制 + 分布式锁 + 唯一索引 + 状态机");
        System.out.println("  推荐方案：Token机制（前端重复提交） / 状态机（有状态流转）");
        System.out.println("  代码位置：com.architecture.idempotent");
        System.out.println();

        System.out.println("场景六：热点数据处理");
        System.out.println("  核心技术：多级缓存 + 互斥锁防击穿 + 热点数据永不过期");
        System.out.println("  性能提升：本地缓存QPS可达百万级");
        System.out.println("  代码位置：com.architecture.hotdata");
        System.out.println();

        System.out.println("场景七：分布式ID生成");
        System.out.println("  核心技术：雪花算法（Snowflake）");
        System.out.println("  性能指标：单机QPS 1000万，趋势递增，69年可用");
        System.out.println("  代码位置：com.architecture.distributedid");
        System.out.println();

        System.out.println("场景八：APP在线状态检测与智能推送");
        System.out.println("  核心技术：WebSocket长连接 + Redis状态存储 + 心跳机制");
        System.out.println("  推送策略：在线WebSocket推送，离线极光推送");
        System.out.println("  代码位置：com.architecture.apppush");
        System.out.println();

        System.out.println("=".repeat(80));
        System.out.println("详细文档请查看项目根目录的 README.md");
        System.out.println("每个场景目录下都有独立的 README.md 说明文档");
        System.out.println("=".repeat(80));
    }
}
