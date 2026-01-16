package com.architecture.medicalreport.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 分页结果DTO
 *
 * @author Medical Report System
 * @since 2025-01-16
 */
@Data
@Builder
public class PageResult<T> {

    /** 当前页码 */
    private Integer pageNum;

    /** 每页大小 */
    private Integer pageSize;

    /** 总记录数 */
    private Long total;

    /** 总页数 */
    private Integer pages;

    /** 数据列表 */
    private List<T> data;

    /** 是否有下一页 */
    private Boolean hasNext;

    /** 是否有上一页 */
    private Boolean hasPrevious;

    /**
     * 计算总页数
     */
    public static <T> PageResult<T> build(Integer pageNum, Integer pageSize, Long total, List<T> data) {
        int pages = (int) Math.ceil((double) total / pageSize);
        return PageResult.<T>builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(total)
                .pages(pages)
                .data(data)
                .hasNext(pageNum < pages)
                .hasPrevious(pageNum > 1)
                .build();
    }
}
