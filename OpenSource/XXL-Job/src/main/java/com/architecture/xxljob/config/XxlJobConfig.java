package com.architecture.xxljob.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * XXL-Job执行器配置
 *
 * <p>配置XXL-Job执行器的核心参数,包括:
 * <ul>
 *   <li>调度中心地址</li>
 *   <li>执行器名称和地址</li>
 *   <li>日志路径和保留天数</li>
 *   <li>访问令牌</li>
 * </ul>
 *
 * <p>配置示例:
 * <pre>
 * xxl:
 *   job:
 *     admin:
 *       addresses: http://localhost:8080/xxl-job-admin
 *     executor:
 *       appname: xxl-job-executor-sample
 *       port: 9999
 *       logpath: /data/applogs/xxl-job/jobhandler
 *       logretentiondays: 30
 * </pre>
 *
 * @author Architecture Learning
 */
@Configuration
public class XxlJobConfig {

    private static final Logger logger = LoggerFactory.getLogger(XxlJobConfig.class);

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Value("${xxl.job.executor.appname}")
    private String appname;

    @Value("${xxl.job.executor.address:}")
    private String address;

    @Value("${xxl.job.executor.ip:}")
    private String ip;

    @Value("${xxl.job.executor.port:9999}")
    private int port;

    @Value("${xxl.job.accessToken:}")
    private String accessToken;

    @Value("${xxl.job.executor.logpath:}")
    private String logPath;

    @Value("${xxl.job.executor.logretentiondays:30}")
    private int logRetentionDays;

    /**
     * 配置XXL-Job执行器
     *
     * <p>执行器负责:
     * <ul>
     *   <li>启动内嵌Server接收调度请求</li>
     *   <li>自动注册到调度中心</li>
     *   <li>执行任务并回调结果</li>
     *   <li>管理任务日志</li>
     * </ul>
     *
     * @return XxlJobSpringExecutor实例
     */
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        logger.info(">>>>>>>>>>> xxl-job config init.");

        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();

        // 设置调度中心地址
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);

        // 设置执行器信息
        xxlJobSpringExecutor.setAppname(appname);
        xxlJobSpringExecutor.setAddress(address);
        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(port);

        // 设置访问令牌
        xxlJobSpringExecutor.setAccessToken(accessToken);

        // 设置日志路径
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);

        return xxlJobSpringExecutor;
    }
}
