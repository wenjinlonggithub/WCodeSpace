package com.architecture.aicoding.model;

import lombok.Builder;
import lombok.Data;

/**
 * 项目上下文模型
 * 包含项目相关的环境信息
 */
@Data
@Builder
public class ProjectContext {

    /**
     * 包名
     */
    private String packageName;

    /**
     * Java版本
     */
    private String javaVersion;

    /**
     * 项目描述
     */
    private String projectDescription;

    /**
     * 依赖框架（如Spring Boot, MyBatis等）
     */
    private String frameworks;
}
