package com.architecture.medicalreport.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

/**
 * 报表查询DTO
 *
 * @author Medical Report System
 * @since 2025-01-16
 */
@Data
public class ReportQueryDTO {

    /** 开始日期 */
    private LocalDate startDate;

    /** 结束日期 */
    private LocalDate endDate;

    /** 科室ID列表 */
    private List<String> departmentIds;

    /** 医生ID列表 */
    private List<String> doctorIds;

    /** 报表类型 overview:概览 monthly:月度趋势 detail:详细数据 */
    private String reportType;

    /** 聚合维度 department:科室 doctor:医生 date:日期 */
    private String aggregateDimension;

    /** 分页页码 */
    private Integer pageNum = 1;

    /** 分页大小 */
    private Integer pageSize = 20;

    /** 排序字段 */
    private String orderBy;

    /** 排序方式 ASC:升序 DESC:降序 */
    private String orderDirection = "DESC";
}
