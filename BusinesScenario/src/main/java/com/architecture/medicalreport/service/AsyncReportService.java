package com.architecture.medicalreport.service;

import com.architecture.medicalreport.dto.AsyncTaskDTO;
import com.architecture.medicalreport.dto.ReportQueryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异步报表生成服务
 *
 * 使用场景:
 * 1. 大数据量报表生成(>10万条)
 * 2. 复杂聚合计算
 * 3. 导出Excel/PDF
 *
 * 实现方式:
 * - 使用Spring @Async异步执行
 * - 任务状态存储在Redis
 * - 生成的文件上传到OSS
 * - 通过WebSocket/轮询通知用户
 *
 * @author Medical Report System
 * @since 2025-01-16
 */
@Slf4j
@Service
public class AsyncReportService {

    @Autowired
    private ReportAggregationService aggregationService;

    @Autowired
    private ReportExportService exportService;

    /** 任务状态缓存 (实际应该用Redis) */
    private final Map<String, AsyncTaskDTO> taskCache = new ConcurrentHashMap<>();

    /**
     * 提交异步报表生成任务
     *
     * @param query 查询条件
     * @return 任务ID
     */
    public String submitTask(ReportQueryDTO query) {
        String taskId = UUID.randomUUID().toString();

        AsyncTaskDTO task = AsyncTaskDTO.builder()
                .taskId(taskId)
                .taskType("ANNUAL_REPORT")
                .status(AsyncTaskDTO.TaskStatus.PENDING)
                .progress(0)
                .createTime(LocalDateTime.now())
                .build();

        // 保存任务状态
        taskCache.put(taskId, task);

        // 异步执行任务
        generateReportAsync(taskId, query);

        log.info("提交异步报表任务: taskId={}, query={}", taskId, query);

        return taskId;
    }

    /**
     * 异步生成报表
     *
     * @param taskId 任务ID
     * @param query 查询条件
     */
    @Async("reportTaskExecutor")
    public void generateReportAsync(String taskId, ReportQueryDTO query) {
        AsyncTaskDTO task = taskCache.get(taskId);

        try {
            // 更新状态: 处理中
            task.setStatus(AsyncTaskDTO.TaskStatus.PROCESSING);
            task.setStartTime(LocalDateTime.now());
            task.setProgress(10);
            taskCache.put(taskId, task);

            // 1. 查询数据
            log.info("开始查询报表数据: taskId={}", taskId);
            var reportData = aggregationService.queryAnnualReport(query);
            task.setProgress(50);
            taskCache.put(taskId, task);

            // 2. 生成Excel
            log.info("开始生成Excel: taskId={}", taskId);
            String fileUrl = exportService.exportToExcel(reportData);
            task.setProgress(90);
            taskCache.put(taskId, task);

            // 3. 上传到OSS (模拟)
            log.info("上传文件到OSS: taskId={}, fileUrl={}", taskId, fileUrl);
            task.setProgress(100);
            task.setResultUrl(fileUrl);

            // 更新状态: 已完成
            task.setStatus(AsyncTaskDTO.TaskStatus.COMPLETED);
            task.setEndTime(LocalDateTime.now());
            taskCache.put(taskId, task);

            log.info("异步报表生成完成: taskId={}, cost={}ms",
                    taskId,
                    java.time.Duration.between(task.getStartTime(), task.getEndTime()).toMillis());

            // 发送通知 (WebSocket/Email/短信)
            sendNotification(taskId, task);

        } catch (Exception e) {
            log.error("异步报表生成失败: taskId={}", taskId, e);

            task.setStatus(AsyncTaskDTO.TaskStatus.FAILED);
            task.setErrorMsg(e.getMessage());
            task.setEndTime(LocalDateTime.now());
            taskCache.put(taskId, task);
        }
    }

    /**
     * 查询任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    public AsyncTaskDTO getTaskStatus(String taskId) {
        return taskCache.get(taskId);
    }

    /**
     * 下载报表文件
     *
     * @param taskId 任务ID
     * @param outputStream 输出流
     */
    public void downloadReport(String taskId, OutputStream outputStream) {
        AsyncTaskDTO task = taskCache.get(taskId);

        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }

        if (task.getStatus() != AsyncTaskDTO.TaskStatus.COMPLETED) {
            throw new IllegalStateException("任务未完成: " + task.getStatus());
        }

        // 从OSS下载文件并写入输出流
        // ossService.download(task.getResultUrl(), outputStream);

        log.info("下载报表文件: taskId={}, url={}", taskId, task.getResultUrl());
    }

    /**
     * 发送完成通知
     */
    private void sendNotification(String taskId, AsyncTaskDTO task) {
        // 方式1: WebSocket推送
        // webSocketService.send(userId, "报表已生成完成");

        // 方式2: 邮件通知
        // emailService.send(userEmail, "报表已生成", task.getResultUrl());

        // 方式3: 短信通知
        // smsService.send(userPhone, "报表已生成，请登录查看");

        log.info("发送完成通知: taskId={}", taskId);
    }
}
