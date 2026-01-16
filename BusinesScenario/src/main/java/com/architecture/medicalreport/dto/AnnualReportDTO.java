package com.architecture.medicalreport.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 年度报表DTO
 *
 * @author Medical Report System
 * @since 2025-01-16
 */
@Data
@Builder
public class AnnualReportDTO {

    /** 查询年份 */
    private Integer year;

    /** 总体概览 */
    private OverviewData overview;

    /** 月度趋势数据 */
    private List<MonthlyTrendData> monthlyTrend;

    /** 科室排名 */
    private List<DepartmentRankingData> departmentRanking;

    /** 医生工作量排名 */
    private List<DoctorWorkloadData> doctorWorkload;

    /**
     * 总体概览数据
     */
    @Data
    @Builder
    public static class OverviewData {
        /** 门诊总人次 */
        private Integer totalOutpatientCount;

        /** 住院总人次 */
        private Integer totalInpatientCount;

        /** 总收入 */
        private BigDecimal totalRevenue;

        /** 门诊收入 */
        private BigDecimal outpatientRevenue;

        /** 住院收入 */
        private BigDecimal inpatientRevenue;

        /** 药品收入 */
        private BigDecimal drugRevenue;

        /** 医保支付金额 */
        private BigDecimal insuranceAmount;

        /** 自费金额 */
        private BigDecimal selfAmount;

        /** 医保占比 */
        private BigDecimal insuranceRatio;

        /** 同比增长率 */
        private BigDecimal yearOverYearGrowth;
    }

    /**
     * 月度趋势数据
     */
    @Data
    @Builder
    public static class MonthlyTrendData {
        /** 月份 */
        private String month;

        /** 门诊人次 */
        private Integer outpatientCount;

        /** 住院人次 */
        private Integer inpatientCount;

        /** 总收入 */
        private BigDecimal totalRevenue;

        /** 环比增长率 */
        private BigDecimal monthOverMonthGrowth;
    }

    /**
     * 科室排名数据
     */
    @Data
    @Builder
    public static class DepartmentRankingData {
        /** 排名 */
        private Integer ranking;

        /** 科室ID */
        private String departmentId;

        /** 科室名称 */
        private String departmentName;

        /** 总收入 */
        private BigDecimal totalRevenue;

        /** 门诊人次 */
        private Integer outpatientCount;

        /** 住院人次 */
        private Integer inpatientCount;
    }

    /**
     * 医生工作量数据
     */
    @Data
    @Builder
    public static class DoctorWorkloadData {
        /** 排名 */
        private Integer ranking;

        /** 医生ID */
        private String doctorId;

        /** 医生姓名 */
        private String doctorName;

        /** 科室名称 */
        private String departmentName;

        /** 门诊人次 */
        private Integer outpatientCount;

        /** 创收金额 */
        private BigDecimal totalRevenue;
    }
}
