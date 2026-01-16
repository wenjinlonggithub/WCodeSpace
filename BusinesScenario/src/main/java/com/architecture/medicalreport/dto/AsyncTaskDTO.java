package com.architecture.medicalreport.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 异步任务DTO
 *
 * @author Medical Report System
 * @since 2025-01-16
 */
@Data
@Builder
public class AsyncTaskDTO {

    /** 任务ID */
    private String taskId;

    /** 任务类型 */
    private String taskType;

    /** 任务状态 PENDING:待处理 PROCESSING:处理中 COMPLETED:已完成 FAILED:失败 */
    private TaskStatus status;

    /** 进度百分比 0-100 */
    private Integer progress;

    /** 结果文件URL */
    private String resultUrl;

    /** 错误信息 */
    private String errorMsg;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 预计完成时间 */
    private LocalDateTime estimatedEndTime;

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        PENDING("待处理"),
        PROCESSING("处理中"),
        COMPLETED("已完成"),
        FAILED("失败");

        private final String description;

        TaskStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
