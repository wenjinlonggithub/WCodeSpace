package com.architecture.medicalreport.controller;

import com.architecture.medicalreport.dto.AnnualReportDTO;
import com.architecture.medicalreport.dto.AsyncTaskDTO;
import com.architecture.medicalreport.dto.ReportQueryDTO;
import com.architecture.medicalreport.service.AsyncReportService;
import com.architecture.medicalreport.service.ReportAggregationService;
import com.architecture.medicalreport.service.ReportExportService;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 医疗报表Controller
 *
 * API设计:
 * - GET  /api/reports/annual         查询年度报表(同步)
 * - POST /api/reports/annual/async   提交异步生成任务
 * - GET  /api/reports/task/{taskId}  查询任务状态
 * - GET  /api/reports/export/excel   导出Excel
 * - GET  /api/reports/export/csv     导出CSV
 *
 * 安全措施:
 * - Sentinel限流熔断
 * - 参数校验
 * - 权限控制
 *
 * @author Medical Report System
 * @since 2025-01-16
 */
@Slf4j
@RestController
@RequestMapping("/api/reports")
public class MedicalReportController {

    @Autowired
    private ReportAggregationService aggregationService;

    @Autowired
    private AsyncReportService asyncReportService;

    @Autowired
    private ReportExportService exportService;

    /**
     * 查询年度报表 (同步接口)
     *
     * 限流规则:
     * - QPS限制: 50/秒
     * - 并发限制: 20个线程
     * - 熔断规则: 异常比例 > 50% 触发熔断
     *
     * 降级策略:
     * 1. 返回缓存数据
     * 2. 返回汇总数据
     * 3. 提示异步生成
     *
     * @param query 查询条件
     * @return 年度报表数据
     */
    @GetMapping("/annual")
    @SentinelResource(
            value = "queryAnnualReport",
            blockHandler = "handleBlock",
            fallback = "handleFallback"
    )
    public Result<AnnualReportDTO> queryAnnualReport(@Validated ReportQueryDTO query) {
        log.info("查询年度报表: query={}", query);

        long startTime = System.currentTimeMillis();

        try {
            AnnualReportDTO result = aggregationService.queryAnnualReport(query);

            long costTime = System.currentTimeMillis() - startTime;
            log.info("查询年度报表成功: cost={}ms", costTime);

            return Result.success(result);

        } catch (Exception e) {
            log.error("查询年度报表失败", e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 提交异步报表生成任务
     *
     * 使用场景:
     * - 数据量 > 10万条
     * - 查询耗时 > 5秒
     * - 导出文件
     *
     * @param query 查询条件
     * @return 任务ID
     */
    @PostMapping("/annual/async")
    @SentinelResource(value = "submitAsyncTask", blockHandler = "handleBlock")
    public Result<String> submitAsyncTask(@RequestBody @Validated ReportQueryDTO query) {
        log.info("提交异步报表任务: query={}", query);

        try {
            String taskId = asyncReportService.submitTask(query);
            return Result.success(taskId, "任务提交成功，请通过任务ID查询进度");

        } catch (Exception e) {
            log.error("提交异步任务失败", e);
            return Result.fail("任务提交失败: " + e.getMessage());
        }
    }

    /**
     * 查询异步任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    @GetMapping("/task/{taskId}")
    public Result<AsyncTaskDTO> getTaskStatus(@PathVariable String taskId) {
        log.info("查询任务状态: taskId={}", taskId);

        try {
            AsyncTaskDTO task = asyncReportService.getTaskStatus(taskId);

            if (task == null) {
                return Result.fail("任务不存在");
            }

            return Result.success(task);

        } catch (Exception e) {
            log.error("查询任务状态失败", e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 导出Excel (流式导出)
     *
     * 限流规则:
     * - 并发限制: 10个线程
     * - 超时时间: 5分钟
     *
     * @param query 查询条件
     * @param response HTTP响应
     */
    @GetMapping("/export/excel")
    @SentinelResource(
            value = "exportExcel",
            blockHandler = "handleExportBlock"
    )
    public void exportExcel(@Validated ReportQueryDTO query, HttpServletResponse response) {
        log.info("导出Excel: query={}", query);

        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=report_" + System.currentTimeMillis() + ".xlsx");

            // 流式导出
            exportService.streamExportToExcel(query, response.getOutputStream());

            log.info("Excel导出成功");

        } catch (IOException e) {
            log.error("Excel导出失败", e);
            throw new RuntimeException("Excel导出失败: " + e.getMessage());
        }
    }

    /**
     * 导出CSV (更快)
     *
     * @param query 查询条件
     * @param response HTTP响应
     */
    @GetMapping("/export/csv")
    @SentinelResource(value = "exportCsv", blockHandler = "handleExportBlock")
    public void exportCsv(@Validated ReportQueryDTO query, HttpServletResponse response) {
        log.info("导出CSV: query={}", query);

        try {
            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=report_" + System.currentTimeMillis() + ".csv");

            exportService.exportToCsv(query, response.getOutputStream());

            log.info("CSV导出成功");

        } catch (IOException e) {
            log.error("CSV导出失败", e);
            throw new RuntimeException("CSV导出失败: " + e.getMessage());
        }
    }

    // ======================== 限流降级处理 ========================

    /**
     * 查询接口限流处理
     */
    public Result<AnnualReportDTO> handleBlock(ReportQueryDTO query, BlockException ex) {
        log.warn("查询接口触发限流: query={}, exception={}", query, ex.getClass().getSimpleName());
        return Result.fail("查询过于频繁，请稍后再试");
    }

    /**
     * 查询接口异常降级
     */
    public Result<AnnualReportDTO> handleFallback(ReportQueryDTO query, Throwable ex) {
        log.error("查询接口异常降级: query={}", query, ex);

        // 降级策略: 返回提示信息
        return Result.fail("系统繁忙，请使用异步查询: POST /api/reports/annual/async");
    }

    /**
     * 导出接口限流处理
     */
    public void handleExportBlock(ReportQueryDTO query, HttpServletResponse response, BlockException ex)
            throws IOException {
        log.warn("导出接口触发限流: query={}", query);

        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write("{\"code\":429,\"message\":\"导出过于频繁，请稍后再试\"}");
    }

    // ======================== 内部类 ========================

    /**
     * 统一响应结果
     */
    @lombok.Data
    @lombok.Builder
    public static class Result<T> {
        /** 响应码 200:成功 其他:失败 */
        private Integer code;

        /** 响应消息 */
        private String message;

        /** 响应数据 */
        private T data;

        /** 时间戳 */
        private Long timestamp;

        public static <T> Result<T> success(T data) {
            return Result.<T>builder()
                    .code(200)
                    .message("success")
                    .data(data)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        public static <T> Result<T> success(T data, String message) {
            return Result.<T>builder()
                    .code(200)
                    .message(message)
                    .data(data)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }

        public static <T> Result<T> fail(String message) {
            return Result.<T>builder()
                    .code(500)
                    .message(message)
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }
}
