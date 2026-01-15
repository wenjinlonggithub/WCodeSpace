package com.architecture.xxljob.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 任务生命周期管理示例
 *
 * <p>演示XXL-Job任务的完整生命周期管理,包括:
 * <ul>
 *   <li>任务初始化</li>
 *   <li>任务执行</li>
 *   <li>任务销毁</li>
 *   <li>任务中断</li>
 * </ul>
 *
 * @author Architecture Learning
 */
@Component
public class LifecycleJobHandler {

    private static final Logger logger = LoggerFactory.getLogger(LifecycleJobHandler.class);

    /**
     * 生命周期完整示例
     *
     * <p>演示任务的完整生命周期:
     * <ul>
     *   <li>初始化: 准备资源</li>
     *   <li>执行: 业务逻辑</li>
     *   <li>清理: 释放资源</li>
     *   <li>异常处理: 记录并上报</li>
     * </ul>
     */
    @XxlJob("lifecycleJobHandler")
    public void lifecycleJobHandler() {
        XxlJobHelper.log("========== 任务开始 ==========");

        // 任务上下文信息
        long jobId = XxlJobHelper.getJobId();
        String jobParam = XxlJobHelper.getJobParam();
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        XxlJobHelper.log("任务ID: {}", jobId);
        XxlJobHelper.log("任务参数: {}", jobParam);
        XxlJobHelper.log("分片信息: {}/{}", shardIndex, shardTotal);

        try {
            // 1. 初始化阶段
            XxlJobHelper.log(">>> 初始化阶段");
            initialize();

            // 2. 执行阶段
            XxlJobHelper.log(">>> 执行阶段");
            execute(jobParam);

            // 3. 完成阶段
            XxlJobHelper.log(">>> 完成阶段");
            XxlJobHelper.handleSuccess("任务执行成功");

        } catch (Exception e) {
            // 异常处理
            XxlJobHelper.log(">>> 异常处理阶段");
            logger.error("任务执行异常", e);
            XxlJobHelper.handleFail("任务执行失败: " + e.getMessage());

        } finally {
            // 清理资源
            XxlJobHelper.log(">>> 清理阶段");
            cleanup();
            XxlJobHelper.log("========== 任务结束 ==========");
        }
    }

    /**
     * 任务中断示例
     *
     * <p>演示如何优雅地处理任务中断:
     * <ul>
     *   <li>检测中断信号</li>
     *   <li>保存处理进度</li>
     *   <li>清理资源</li>
     *   <li>返回中断状态</li>
     * </ul>
     */
    @XxlJob("interruptJobHandler")
    public void interruptJobHandler() {
        XxlJobHelper.log("可中断任务开始执行");

        try {
            // 模拟长时间运行的任务
            for (int i = 0; i < 100; i++) {
                // 检查是否收到中断信号
                if (Thread.currentThread().isInterrupted()) {
                    XxlJobHelper.log("收到中断信号,任务停止执行");
                    saveProgress(i);  // 保存当前进度
                    XxlJobHelper.handleFail("任务被中断,已保存进度: " + i);
                    return;
                }

                // 执行业务逻辑
                processStep(i);
                XxlJobHelper.log("完成步骤: {}/100", i + 1);

                // 模拟耗时操作
                TimeUnit.MILLISECONDS.sleep(500);
            }

            XxlJobHelper.log("任务正常完成");
            XxlJobHelper.handleSuccess();

        } catch (InterruptedException e) {
            XxlJobHelper.log("任务被中断");
            Thread.currentThread().interrupt();
            XxlJobHelper.handleFail("任务被中断");

        } catch (Exception e) {
            XxlJobHelper.log("任务执行异常: {}", e.getMessage());
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    /**
     * 任务超时处理示例
     *
     * <p>演示如何处理任务超时:
     * <ul>
     *   <li>设置超时时间</li>
     *   <li>监控执行时间</li>
     *   <li>超时后的处理策略</li>
     * </ul>
     */
    @XxlJob("timeoutJobHandler")
    public void timeoutJobHandler() {
        XxlJobHelper.log("超时处理任务开始");

        long startTime = System.currentTimeMillis();
        long timeout = 60 * 1000;  // 60秒超时

        try {
            int processedCount = 0;

            while (true) {
                // 检查是否超时
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed > timeout) {
                    XxlJobHelper.log("任务执行超时,已处理: {}", processedCount);
                    XxlJobHelper.handleSuccess(
                            String.format("超时退出,已处理%d条数据", processedCount)
                    );
                    return;
                }

                // 处理数据
                boolean hasMore = processData();
                if (!hasMore) {
                    XxlJobHelper.log("数据处理完成,处理量: {}", processedCount);
                    XxlJobHelper.handleSuccess();
                    return;
                }

                processedCount++;

                // 定期输出进度
                if (processedCount % 10 == 0) {
                    XxlJobHelper.log("已处理: {}, 耗时: {}ms", processedCount, elapsed);
                }
            }

        } catch (Exception e) {
            XxlJobHelper.log("任务执行异常: {}", e.getMessage());
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    /**
     * 任务重试示例
     *
     * <p>演示任务失败后的重试逻辑:
     * <ul>
     *   <li>失败重试策略</li>
     *   <li>重试次数限制</li>
     *   <li>指数退避算法</li>
     * </ul>
     */
    @XxlJob("retryJobHandler")
    public void retryJobHandler() {
        XxlJobHelper.log("重试任务开始");

        int maxRetry = 3;
        int retryCount = 0;

        while (retryCount < maxRetry) {
            try {
                // 执行可能失败的操作
                executeRiskyOperation();

                XxlJobHelper.log("任务执行成功");
                XxlJobHelper.handleSuccess();
                return;

            } catch (Exception e) {
                retryCount++;
                XxlJobHelper.log("第{}次执行失败: {}", retryCount, e.getMessage());

                if (retryCount >= maxRetry) {
                    XxlJobHelper.log("达到最大重试次数,任务失败");
                    XxlJobHelper.handleFail("重试" + maxRetry + "次后仍然失败");
                    return;
                }

                // 指数退避
                try {
                    long waitTime = (long) Math.pow(2, retryCount) * 1000;
                    XxlJobHelper.log("等待{}ms后重试...", waitTime);
                    TimeUnit.MILLISECONDS.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    XxlJobHelper.handleFail("重试被中断");
                    return;
                }
            }
        }
    }

    /**
     * 任务状态跟踪示例
     *
     * <p>演示如何跟踪和记录任务执行状态:
     * <ul>
     *   <li>记录开始时间</li>
     *   <li>记录处理进度</li>
     *   <li>记录执行结果</li>
     *   <li>统计执行指标</li>
     * </ul>
     */
    @XxlJob("statusTrackingJobHandler")
    public void statusTrackingJobHandler() {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        XxlJobHelper.log("任务开始时间: {}", startTime);

        int totalCount = 0;
        int successCount = 0;
        int failCount = 0;

        try {
            // 模拟处理100条数据
            for (int i = 0; i < 100; i++) {
                totalCount++;

                try {
                    processItem(i);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    XxlJobHelper.log("处理失败 #{}: {}", i, e.getMessage());
                }

                // 定期输出进度
                if ((i + 1) % 20 == 0) {
                    XxlJobHelper.log("进度: {}/100, 成功: {}, 失败: {}",
                            i + 1, successCount, failCount);
                }
            }

            // 计算执行时间
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // 输出统计信息
            XxlJobHelper.log("========== 执行统计 ==========");
            XxlJobHelper.log("总处理量: {}", totalCount);
            XxlJobHelper.log("成功数量: {}", successCount);
            XxlJobHelper.log("失败数量: {}", failCount);
            XxlJobHelper.log("成功率: {}%", (successCount * 100.0 / totalCount));
            XxlJobHelper.log("执行时长: {}ms", duration);
            XxlJobHelper.log("平均耗时: {}ms/条", (duration / totalCount));
            XxlJobHelper.log("===============================");

            XxlJobHelper.handleSuccess(
                    String.format("处理完成: 成功%d, 失败%d, 耗时%dms",
                            successCount, failCount, duration)
            );

        } catch (Exception e) {
            XxlJobHelper.log("任务执行异常: {}", e.getMessage());
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 初始化资源
     */
    private void initialize() {
        XxlJobHelper.log("初始化资源...");
        // 初始化数据库连接、缓存、配置等
    }

    /**
     * 执行业务逻辑
     */
    private void execute(String param) throws Exception {
        XxlJobHelper.log("执行业务逻辑,参数: {}", param);

        // 模拟业务处理
        for (int i = 0; i < 5; i++) {
            XxlJobHelper.log("处理步骤: {}", i + 1);
            TimeUnit.SECONDS.sleep(1);
        }
    }

    /**
     * 清理资源
     */
    private void cleanup() {
        XxlJobHelper.log("清理资源...");
        // 关闭连接、释放资源等
    }

    /**
     * 处理步骤
     */
    private void processStep(int step) {
        logger.debug("处理步骤: {}", step);
    }

    /**
     * 保存进度
     */
    private void saveProgress(int progress) {
        XxlJobHelper.log("保存进度: {}", progress);
        // 保存到数据库或缓存
    }

    /**
     * 处理数据
     */
    private boolean processData() throws Exception {
        // 模拟数据处理
        TimeUnit.MILLISECONDS.sleep(100);
        return Math.random() > 0.1;  // 90%的概率有更多数据
    }

    /**
     * 执行高风险操作
     */
    private void executeRiskyOperation() throws Exception {
        // 模拟可能失败的操作
        if (Math.random() < 0.7) {  // 70%失败率
            throw new Exception("操作失败");
        }
    }

    /**
     * 处理单个数据项
     */
    private void processItem(int index) throws Exception {
        // 模拟处理
        if (Math.random() < 0.05) {  // 5%失败率
            throw new Exception("处理失败");
        }
    }
}
