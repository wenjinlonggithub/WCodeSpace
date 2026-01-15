package com.architecture.xxljob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * XXL-Job执行器应用启动类
 *
 * <p>本应用是XXL-Job分布式任务调度平台的执行器端示例,展示了XXL-Job的核心功能和最佳实践。
 *
 * <p>主要功能:
 * <ul>
 *   <li>自动注册到调度中心</li>
 *   <li>接收并执行调度任务</li>
 *   <li>回调执行结果和日志</li>
 *   <li>支持任务分片、故障转移等高级特性</li>
 * </ul>
 *
 * @author Architecture Learning
 * @version 1.0
 */
@SpringBootApplication
public class XxlJobApplication {

    public static void main(String[] args) {
        SpringApplication.run(XxlJobApplication.class, args);
        System.out.println("========================================");
        System.out.println("XXL-Job Executor Started Successfully!");
        System.out.println("========================================");
    }
}
