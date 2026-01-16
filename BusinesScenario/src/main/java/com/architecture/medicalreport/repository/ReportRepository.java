package com.architecture.medicalreport.repository;

import com.architecture.medicalreport.dto.PageResult;
import com.architecture.medicalreport.dto.ReportQueryDTO;
import com.architecture.medicalreport.entity.DailySummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 报表数据访问层
 *
 * 性能优化策略:
 * 1. 使用分区表，查询时只扫描相关分区
 * 2. 优先查询汇总表，避免实时聚合
 * 3. 使用覆盖索引，减少回表
 * 4. 分页查询使用游标，避免深度分页
 *
 * @author Medical Report System
 * @since 2025-01-16
 */
@Mapper
public interface ReportRepository {

    /**
     * 查询日汇总数据 (分页)
     *
     * 使用场景: 查询月度/年度报表详情
     * 性能: 查询汇总表，索引覆盖，性能好
     *
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<DailySummary> queryDailySummaryPage(@Param("query") ReportQueryDTO query);

    /**
     * 查询日汇总数据 (流式查询)
     *
     * 使用场景: 大数据量导出
     * 性能: 使用流式查询，避免OOM
     *
     * @param query 查询条件
     * @param callback 回调函数，处理每批数据
     */
    void streamQueryDailySummary(
            @Param("query") ReportQueryDTO query,
            @Param("callback") StreamCallback<DailySummary> callback
    );

    /**
     * 查询年度总体概览
     *
     * SQL优化:
     * - 使用月汇总表聚合，而非日汇总表
     * - 覆盖索引: (report_month, department_id)
     *
     * @param year 年份
     * @return 总体数据
     */
    Map<String, Object> queryAnnualOverview(@Param("year") Integer year);

    /**
     * 查询月度趋势数据 (12个月)
     *
     * SQL优化:
     * - 查询月汇总表
     * - 按月份排序
     *
     * @param year 年份
     * @return 月度数据列表
     */
    List<Map<String, Object>> queryMonthlyTrend(@Param("year") Integer year);

    /**
     * 查询科室收入排名 (TOP N)
     *
     * SQL优化:
     * - 使用日汇总表GROUP BY
     * - LIMIT限制返回数量
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param topN 返回TOP N
     * @return 科室排名数据
     */
    List<Map<String, Object>> queryDepartmentRanking(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("topN") Integer topN
    );

    /**
     * 查询医生工作量排名
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param topN 返回TOP N
     * @return 医生工作量数据
     */
    List<Map<String, Object>> queryDoctorWorkload(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("topN") Integer topN
    );

    /**
     * 统计查询总数 (分页用)
     *
     * @param query 查询条件
     * @return 总数
     */
    Long countDailySummary(@Param("query") ReportQueryDTO query);

    /**
     * 流式查询回调接口
     */
    @FunctionalInterface
    interface StreamCallback<T> {
        /**
         * 处理每批数据
         *
         * @param batch 批次数据
         */
        void process(List<T> batch);
    }
}
