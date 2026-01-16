package com.architecture.medicalreport.service;

import com.architecture.medicalreport.dto.AnnualReportDTO;
import com.architecture.medicalreport.dto.ReportQueryDTO;
import com.architecture.medicalreport.repository.ReportRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 报表数据聚合服务
 *
 * 核心功能:
 * 1. 聚合多维度数据
 * 2. 并行查询优化
 * 3. 数据计算和转换
 *
 * @author Medical Report System
 * @since 2025-01-16
 */
@Slf4j
@Service
public class ReportAggregationService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReportCacheService cacheService;

    /**
     * 查询年度报表 (多维度聚合)
     *
     * 性能优化:
     * 1. 并行查询4个维度数据
     * 2. 优先从缓存获取
     * 3. 分级查询: 概览 → 月度趋势 → 详细数据
     *
     * @param query 查询条件
     * @return 年度报表
     */
    public AnnualReportDTO queryAnnualReport(ReportQueryDTO query) {
        Integer year = query.getStartDate().getYear();
        String cacheKey = "annual_report:" + year;

        // 尝试从缓存获取
        AnnualReportDTO cached = cacheService.get(cacheKey);
        if (cached != null) {
            log.info("从缓存获取年度报表: year={}", year);
            return cached;
        }

        log.info("查询年度报表: year={}", year);

        // 并行查询4个维度
        CompletableFuture<AnnualReportDTO.OverviewData> overviewFuture =
                CompletableFuture.supplyAsync(() -> queryOverview(year));

        CompletableFuture<List<AnnualReportDTO.MonthlyTrendData>> trendFuture =
                CompletableFuture.supplyAsync(() -> queryMonthlyTrend(year));

        CompletableFuture<List<AnnualReportDTO.DepartmentRankingData>> deptFuture =
                CompletableFuture.supplyAsync(() -> queryDepartmentRanking(query.getStartDate(), query.getEndDate(), 10));

        CompletableFuture<List<AnnualReportDTO.DoctorWorkloadData>> doctorFuture =
                CompletableFuture.supplyAsync(() -> queryDoctorWorkload(query.getStartDate(), query.getEndDate(), 20));

        // 等待所有查询完成
        CompletableFuture.allOf(overviewFuture, trendFuture, deptFuture, doctorFuture).join();

        // 构建结果
        AnnualReportDTO result = AnnualReportDTO.builder()
                .year(year)
                .overview(overviewFuture.join())
                .monthlyTrend(trendFuture.join())
                .departmentRanking(deptFuture.join())
                .doctorWorkload(doctorFuture.join())
                .build();

        // 缓存结果 (1小时)
        cacheService.put(cacheKey, result, 3600);

        return result;
    }

    /**
     * 查询总体概览
     */
    private AnnualReportDTO.OverviewData queryOverview(Integer year) {
        Map<String, Object> data = reportRepository.queryAnnualOverview(year);

        BigDecimal totalRevenue = (BigDecimal) data.getOrDefault("total_revenue", BigDecimal.ZERO);
        BigDecimal insuranceAmount = (BigDecimal) data.getOrDefault("insurance_amount", BigDecimal.ZERO);

        // 计算医保占比
        BigDecimal insuranceRatio = BigDecimal.ZERO;
        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            insuranceRatio = insuranceAmount
                    .divide(totalRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return AnnualReportDTO.OverviewData.builder()
                .totalOutpatientCount((Integer) data.getOrDefault("outpatient_count", 0))
                .totalInpatientCount((Integer) data.getOrDefault("inpatient_count", 0))
                .totalRevenue(totalRevenue)
                .outpatientRevenue((BigDecimal) data.getOrDefault("outpatient_revenue", BigDecimal.ZERO))
                .inpatientRevenue((BigDecimal) data.getOrDefault("inpatient_revenue", BigDecimal.ZERO))
                .drugRevenue((BigDecimal) data.getOrDefault("drug_revenue", BigDecimal.ZERO))
                .insuranceAmount(insuranceAmount)
                .selfAmount((BigDecimal) data.getOrDefault("self_amount", BigDecimal.ZERO))
                .insuranceRatio(insuranceRatio)
                .yearOverYearGrowth((BigDecimal) data.getOrDefault("year_over_year_growth", BigDecimal.ZERO))
                .build();
    }

    /**
     * 查询月度趋势
     */
    private List<AnnualReportDTO.MonthlyTrendData> queryMonthlyTrend(Integer year) {
        List<Map<String, Object>> dataList = reportRepository.queryMonthlyTrend(year);

        return dataList.stream()
                .map(data -> AnnualReportDTO.MonthlyTrendData.builder()
                        .month((String) data.get("report_month"))
                        .outpatientCount((Integer) data.getOrDefault("outpatient_count", 0))
                        .inpatientCount((Integer) data.getOrDefault("inpatient_count", 0))
                        .totalRevenue((BigDecimal) data.getOrDefault("total_revenue", BigDecimal.ZERO))
                        .monthOverMonthGrowth((BigDecimal) data.getOrDefault("month_over_month_growth", BigDecimal.ZERO))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 查询科室排名
     */
    private List<AnnualReportDTO.DepartmentRankingData> queryDepartmentRanking(
            LocalDate startDate, LocalDate endDate, Integer topN) {

        List<Map<String, Object>> dataList = reportRepository.queryDepartmentRanking(startDate, endDate, topN);

        List<AnnualReportDTO.DepartmentRankingData> result = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> data = dataList.get(i);
            result.add(AnnualReportDTO.DepartmentRankingData.builder()
                    .ranking(i + 1)
                    .departmentId((String) data.get("department_id"))
                    .departmentName((String) data.get("department_name"))
                    .totalRevenue((BigDecimal) data.getOrDefault("total_revenue", BigDecimal.ZERO))
                    .outpatientCount((Integer) data.getOrDefault("outpatient_count", 0))
                    .inpatientCount((Integer) data.getOrDefault("inpatient_count", 0))
                    .build());
        }

        return result;
    }

    /**
     * 查询医生工作量
     */
    private List<AnnualReportDTO.DoctorWorkloadData> queryDoctorWorkload(
            LocalDate startDate, LocalDate endDate, Integer topN) {

        List<Map<String, Object>> dataList = reportRepository.queryDoctorWorkload(startDate, endDate, topN);

        List<AnnualReportDTO.DoctorWorkloadData> result = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> data = dataList.get(i);
            result.add(AnnualReportDTO.DoctorWorkloadData.builder()
                    .ranking(i + 1)
                    .doctorId((String) data.get("doctor_id"))
                    .doctorName((String) data.get("doctor_name"))
                    .departmentName((String) data.get("department_name"))
                    .outpatientCount((Integer) data.getOrDefault("outpatient_count", 0))
                    .totalRevenue((BigDecimal) data.getOrDefault("total_revenue", BigDecimal.ZERO))
                    .build());
        }

        return result;
    }
}
