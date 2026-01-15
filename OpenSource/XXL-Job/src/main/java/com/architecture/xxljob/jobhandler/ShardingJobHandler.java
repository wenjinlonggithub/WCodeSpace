package com.architecture.xxljob.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 分片广播任务处理器
 *
 * <p>分片广播是XXL-Job的重要特性,适用于大数据量任务的并行处理。
 *
 * <p>核心概念:
 * <ul>
 *   <li>shardIndex: 当前分片序号(从0开始)</li>
 *   <li>shardTotal: 总分片数(执行器数量)</li>
 *   <li>每个执行器处理属于自己分片的数据</li>
 * </ul>
 *
 * <p>应用场景:
 * <ul>
 *   <li>大数据量数据同步</li>
 *   <li>批量数据处理</li>
 *   <li>分布式计算任务</li>
 * </ul>
 *
 * @author Architecture Learning
 */
@Component
public class ShardingJobHandler {

    private static final Logger logger = LoggerFactory.getLogger(ShardingJobHandler.class);

    /**
     * 基础分片任务示例
     *
     * <p>演示如何使用分片参数进行数据分片处理。
     *
     * <p>假设场景:
     * <ul>
     *   <li>有3个执行器(节点)</li>
     *   <li>需要处理10000条数据</li>
     *   <li>节点0处理: id % 3 == 0 的数据</li>
     *   <li>节点1处理: id % 3 == 1 的数据</li>
     *   <li>节点2处理: id % 3 == 2 的数据</li>
     * </ul>
     *
     * <p>调度配置:
     * <pre>
     * JobHandler: shardingJobHandler
     * 路由策略: 分片广播
     * </pre>
     */
    @XxlJob("shardingJobHandler")
    public void shardingJobHandler() {
        // 获取分片参数
        int shardIndex = XxlJobHelper.getShardIndex();  // 当前分片序号
        int shardTotal = XxlJobHelper.getShardTotal();  // 总分片数

        XxlJobHelper.log("分片广播任务开始执行");
        XxlJobHelper.log("分片参数 - 当前分片: {}, 总分片数: {}", shardIndex, shardTotal);

        // 模拟获取数据
        List<Integer> allData = generateMockData(10000);

        int processedCount = 0;

        // 处理属于当前分片的数据
        for (Integer dataId : allData) {
            // 根据ID取模判断是否属于当前分片
            if (dataId % shardTotal == shardIndex) {
                processData(dataId);
                processedCount++;
            }
        }

        XxlJobHelper.log("分片任务完成,处理数据量: {}", processedCount);
        XxlJobHelper.handleSuccess(String.format("成功处理%d条数据", processedCount));
    }

    /**
     * 分页分片任务示例
     *
     * <p>更高效的分片方式,通过分页查询实现数据分片,避免一次性加载所有数据。
     *
     * <p>分片策略:
     * <ul>
     *   <li>假设3个执行器,每页100条数据</li>
     *   <li>执行器0处理: 0,3,6,9... 页</li>
     *   <li>执行器1处理: 1,4,7,10... 页</li>
     *   <li>执行器2处理: 2,5,8,11... 页</li>
     * </ul>
     */
    @XxlJob("shardingPageJobHandler")
    public void shardingPageJobHandler() {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        XxlJobHelper.log("分页分片任务开始");
        XxlJobHelper.log("分片参数 - 当前分片: {}, 总分片数: {}", shardIndex, shardTotal);

        int pageSize = 100;
        int pageNo = shardIndex;  // 从分片索引开始
        int totalProcessed = 0;

        while (true) {
            // 分页查询数据
            List<DataItem> dataPage = queryDataByPage(pageNo, pageSize);

            if (dataPage.isEmpty()) {
                XxlJobHelper.log("第{}页无数据,分片任务结束", pageNo);
                break;
            }

            XxlJobHelper.log("处理第{}页数据,数据量: {}", pageNo, dataPage.size());

            // 处理当前页数据
            for (DataItem item : dataPage) {
                processDataItem(item);
                totalProcessed++;
            }

            // 下一页(跳过其他分片的页)
            pageNo += shardTotal;
        }

        XxlJobHelper.log("分页分片任务完成,总处理量: {}", totalProcessed);
        XxlJobHelper.handleSuccess(String.format("成功处理%d条数据", totalProcessed));
    }

    /**
     * 订单超时取消任务(实战示例)
     *
     * <p>场景: 每分钟检查并取消30分钟未支付的订单
     *
     * <p>使用分片广播提升处理效率:
     * <ul>
     *   <li>单节点: 处理1000订单需要30秒</li>
     *   <li>3节点分片: 每节点处理333订单,仅需10秒</li>
     * </ul>
     */
    @XxlJob("orderTimeoutCancelJob")
    public void orderTimeoutCancelJob() {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        XxlJobHelper.log("订单超时取消任务开始");
        XxlJobHelper.log("分片参数: {}/{}", shardIndex, shardTotal);

        int batchSize = 100;
        int pageNo = shardIndex;
        int cancelCount = 0;

        try {
            while (true) {
                // 查询超时订单(支持分片)
                List<Order> timeoutOrders = queryTimeoutOrders(pageNo, batchSize, shardTotal);

                if (timeoutOrders.isEmpty()) {
                    break;
                }

                XxlJobHelper.log("查询到{}笔超时订单", timeoutOrders.size());

                // 处理订单
                for (Order order : timeoutOrders) {
                    try {
                        boolean success = cancelOrder(order);
                        if (success) {
                            cancelCount++;
                            XxlJobHelper.log("取消订单成功: {}", order.getOrderNo());
                        }
                    } catch (Exception e) {
                        XxlJobHelper.log("取消订单失败: {}, 原因: {}",
                                order.getOrderNo(), e.getMessage());
                    }
                }

                pageNo += shardTotal;
            }

            XxlJobHelper.log("订单超时取消任务完成,共取消{}笔订单", cancelCount);
            XxlJobHelper.handleSuccess();

        } catch (Exception e) {
            XxlJobHelper.log("任务执行异常: {}", e.getMessage());
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    /**
     * 数据同步任务(实战示例)
     *
     * <p>场景: 将MySQL数据同步到Elasticsearch
     *
     * <p>分片优势:
     * <ul>
     *   <li>提升同步速度(并行处理)</li>
     *   <li>降低单点压力</li>
     *   <li>支持动态扩容</li>
     * </ul>
     */
    @XxlJob("dataSyncJob")
    public void dataSyncJob() {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        XxlJobHelper.log("数据同步任务开始");
        XxlJobHelper.log("分片参数: {}/{}", shardIndex, shardTotal);

        int batchSize = 500;
        int pageNo = shardIndex;
        int syncCount = 0;

        try {
            while (true) {
                // 查询待同步数据
                List<DataItem> dataList = queryPendingSyncData(pageNo, batchSize, shardTotal);

                if (dataList.isEmpty()) {
                    break;
                }

                // 批量同步到ES
                boolean success = batchSyncToES(dataList);
                if (success) {
                    syncCount += dataList.size();
                    XxlJobHelper.log("成功同步{}条数据", dataList.size());
                }

                pageNo += shardTotal;
            }

            XxlJobHelper.log("数据同步完成,总同步量: {}", syncCount);
            XxlJobHelper.handleSuccess();

        } catch (Exception e) {
            XxlJobHelper.log("同步失败: {}", e.getMessage());
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    // ==================== 模拟方法 ====================

    /**
     * 生成模拟数据
     */
    private List<Integer> generateMockData(int count) {
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            data.add(i);
        }
        return data;
    }

    /**
     * 处理单条数据
     */
    private void processData(Integer dataId) {
        // 模拟数据处理
        logger.debug("处理数据: {}", dataId);
    }

    /**
     * 分页查询数据
     */
    private List<DataItem> queryDataByPage(int pageNo, int pageSize) {
        // 模拟分页查询
        // 实际应从数据库查询: SELECT * FROM table LIMIT #{offset}, #{pageSize}
        List<DataItem> result = new ArrayList<>();

        // 模拟最多查询10页
        if (pageNo < 10) {
            for (int i = 0; i < pageSize; i++) {
                result.add(new DataItem((long) (pageNo * pageSize + i)));
            }
        }

        return result;
    }

    /**
     * 处理数据项
     */
    private void processDataItem(DataItem item) {
        logger.debug("处理数据项: {}", item.getId());
    }

    /**
     * 查询超时订单
     */
    private List<Order> queryTimeoutOrders(int pageNo, int pageSize, int shardTotal) {
        // 模拟查询超时订单
        // SQL: SELECT * FROM orders WHERE status = 'WAIT_PAY'
        //      AND create_time < NOW() - INTERVAL 30 MINUTE
        //      AND MOD(id, #{shardTotal}) = #{shardIndex}
        //      LIMIT #{pageSize}
        return new ArrayList<>();
    }

    /**
     * 取消订单
     */
    private boolean cancelOrder(Order order) {
        // 模拟取消订单逻辑
        return true;
    }

    /**
     * 查询待同步数据
     */
    private List<DataItem> queryPendingSyncData(int pageNo, int pageSize, int shardTotal) {
        // 模拟查询待同步数据
        return new ArrayList<>();
    }

    /**
     * 批量同步到ES
     */
    private boolean batchSyncToES(List<DataItem> dataList) {
        // 模拟批量同步
        return true;
    }

    // ==================== 内部类 ====================

    /**
     * 数据项
     */
    private static class DataItem {
        private Long id;

        public DataItem(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }

    /**
     * 订单实体
     */
    private static class Order {
        private Long id;
        private String orderNo;

        public Long getId() {
            return id;
        }

        public String getOrderNo() {
            return orderNo;
        }
    }
}
