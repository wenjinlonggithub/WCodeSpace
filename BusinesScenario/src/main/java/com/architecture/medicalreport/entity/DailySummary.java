package com.architecture.medicalreport.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日汇总报表实体
 *
 * @author Medical Report System
 * @since 2025-01-16
 */
@Data
public class DailySummary {

    /** 主键ID */
    private Long id;

    /** 报表日期 */
    private LocalDate reportDate;

    /** 科室ID */
    private String departmentId;

    /** 科室名称 */
    private String departmentName;

    /** 门诊人次 */
    private Integer outpatientCount;

    /** 住院人次 */
    private Integer inpatientCount;

    /** 总收入 */
    private BigDecimal totalRevenue;

    /** 门诊收入 */
    private BigDecimal outpatientRevenue;

    /** 住院收入 */
    private BigDecimal inpatientRevenue;

    /** 药品收入 */
    private BigDecimal drugRevenue;

    /** 检查收入 */
    private BigDecimal examRevenue;

    /** 治疗收入 */
    private BigDecimal treatmentRevenue;

    /** 医保支付金额 */
    private BigDecimal insuranceAmount;

    /** 自费金额 */
    private BigDecimal selfAmount;

    /** 门诊均次费用 */
    private BigDecimal avgOutpatientCost;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
