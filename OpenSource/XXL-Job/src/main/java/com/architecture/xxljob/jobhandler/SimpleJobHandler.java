package com.architecture.xxljob.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 简单任务处理器示例
 *
 * <p>展示XXL-Job最基本的任务处理方式,包括:
 * <ul>
 *   <li>任务注册和配置</li>
 *   <li>参数获取</li>
 *   <li>日志记录</li>
 *   <li>执行结果设置</li>
 * </ul>
 *
 * <p>使用方式:
 * <ol>
 *   <li>在调度中心新建任务</li>
 *   <li>JobHandler设置为: simpleJobHandler</li>
 *   <li>配置Cron表达式</li>
 *   <li>启动任务</li>
 * </ol>
 *
 * @author Architecture Learning
 */
@Component
public class SimpleJobHandler {

    private static final Logger logger = LoggerFactory.getLogger(SimpleJobHandler.class);

    /**
     * 简单任务示例
     *
     * <p>这是一个最基本的XXL-Job任务示例,演示了:
     * <ul>
     *   <li>使用@XxlJob注解注册任务</li>
     *   <li>获取任务参数</li>
     *   <li>记录任务执行日志</li>
     *   <li>设置任务执行结果</li>
     * </ul>
     *
     * <p>调度配置示例:
     * <pre>
     * 任务名称: 简单任务示例
     * Cron表达式: 0/30 * * * * ?  (每30秒执行一次)
     * JobHandler: simpleJobHandler
     * 路由策略: 轮询
     * 阻塞策略: 单机串行
     * </pre>
     *
     * @throws Exception 任务执行异常
     */
    @XxlJob("simpleJobHandler")
    public void simpleJobHandler() throws Exception {
        // 记录任务开始日志
        XxlJobHelper.log("XXL-JOB, 简单任务开始执行");

        // 获取任务参数
        String param = XxlJobHelper.getJobParam();
        XxlJobHelper.log("任务参数: {}", param);

        // 获取任务ID
        long jobId = XxlJobHelper.getJobId();
        XxlJobHelper.log("任务ID: {}", jobId);

        // 模拟业务逻辑
        for (int i = 0; i < 5; i++) {
            XxlJobHelper.log("执行任务步骤 - {}", i + 1);
            TimeUnit.SECONDS.sleep(2);
        }

        // 设置任务执行结果
        XxlJobHelper.log("任务执行完成");
        XxlJobHelper.handleSuccess("任务执行成功,处理了5个步骤");
    }

    /**
     * 带参数的任务示例
     *
     * <p>演示如何接收和解析任务参数,支持:
     * <ul>
     *   <li>简单字符串参数</li>
     *   <li>JSON格式参数</li>
     *   <li>键值对参数</li>
     * </ul>
     *
     * <p>调度配置:
     * <pre>
     * JobHandler: paramJobHandler
     * 任务参数: {"type":"sync","count":100}
     * </pre>
     */
    @XxlJob("paramJobHandler")
    public void paramJobHandler() {
        XxlJobHelper.log("带参数任务开始执行");

        try {
            // 获取参数
            String param = XxlJobHelper.getJobParam();
            XxlJobHelper.log("接收到的参数: {}", param);

            if (param == null || param.trim().isEmpty()) {
                XxlJobHelper.log("未传入参数,使用默认配置");
                processWithDefault();
            } else {
                // 解析参数并处理
                processWithParam(param);
            }

            XxlJobHelper.handleSuccess();

        } catch (Exception e) {
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail("任务执行失败: " + e.getMessage());
        }
    }

    /**
     * 异常处理示例
     *
     * <p>演示如何处理任务执行过程中的异常:
     * <ul>
     *   <li>捕获异常并记录日志</li>
     *   <li>区分业务异常和系统异常</li>
     *   <li>设置失败状态触发重试或报警</li>
     * </ul>
     */
    @XxlJob("exceptionJobHandler")
    public void exceptionJobHandler() {
        XxlJobHelper.log("异常处理示例任务开始");

        try {
            // 模拟业务逻辑
            doBusinessLogic();

            XxlJobHelper.log("业务逻辑执行成功");
            XxlJobHelper.handleSuccess();

        } catch (BusinessException e) {
            // 业务异常,记录日志但不标记为失败
            XxlJobHelper.log("业务异常(可忽略): {}", e.getMessage());
            XxlJobHelper.handleSuccess("业务异常已处理");

        } catch (Exception e) {
            // 系统异常,标记为失败
            logger.error("系统异常", e);
            XxlJobHelper.log("系统异常: {}", e.getMessage());
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    /**
     * 使用默认配置处理
     */
    private void processWithDefault() {
        XxlJobHelper.log("使用默认配置处理任务");
        // 默认处理逻辑
    }

    /**
     * 根据参数处理
     */
    private void processWithParam(String param) {
        XxlJobHelper.log("根据参数处理: {}", param);
        // 参数化处理逻辑
    }

    /**
     * 模拟业务逻辑
     */
    private void doBusinessLogic() throws Exception {
        // 模拟业务处理
        XxlJobHelper.log("执行业务逻辑...");
    }

    /**
     * 业务异常类
     */
    private static class BusinessException extends Exception {
        public BusinessException(String message) {
            super(message);
        }
    }
}
