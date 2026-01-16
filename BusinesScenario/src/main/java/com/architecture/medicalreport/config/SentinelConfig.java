package com.architecture.medicalreport.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel限流熔断配置
 *
 * 限流规则:
 * 1. queryAnnualReport: QPS限制50/秒
 * 2. exportExcel: 线程数限制10个并发
 * 3. submitAsyncTask: QPS限制20/秒
 *
 * @author Medical Report System
 * @since 2025-01-16
 */
@Slf4j
@Configuration
public class SentinelConfig {

    @PostConstruct
    public void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 规则1: 年度报表查询 - QPS限流
        FlowRule queryRule = new FlowRule();
        queryRule.setResource("queryAnnualReport");
        queryRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        queryRule.setCount(50);  // 每秒50次
        rules.add(queryRule);

        // 规则2: Excel导出 - 线程数限流
        FlowRule exportRule = new FlowRule();
        exportRule.setResource("exportExcel");
        exportRule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        exportRule.setCount(10);  // 最多10个并发
        rules.add(exportRule);

        // 规则3: CSV导出 - 线程数限流
        FlowRule csvRule = new FlowRule();
        csvRule.setResource("exportCsv");
        csvRule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        csvRule.setCount(10);
        rules.add(csvRule);

        // 规则4: 异步任务提交 - QPS限流
        FlowRule taskRule = new FlowRule();
        taskRule.setResource("submitAsyncTask");
        taskRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        taskRule.setCount(20);  // 每秒20次
        rules.add(taskRule);

        FlowRuleManager.loadRules(rules);

        log.info("初始化Sentinel限流规则完成: rules={}", rules.size());
    }
}
