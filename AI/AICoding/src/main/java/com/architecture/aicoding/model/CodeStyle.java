package com.architecture.aicoding.model;

import lombok.Builder;
import lombok.Data;

/**
 * 代码风格模型
 * 定义生成代码的风格规范
 */
@Data
@Builder
public class CodeStyle {

    /**
     * 命名规范（如CamelCase, snake_case等）
     */
    private String namingConvention;

    /**
     * 注释风格（如JavaDoc, 行内注释等）
     */
    private String commentStyle;

    /**
     * 缩进方式（空格或制表符）
     */
    private String indentation;

    /**
     * 是否使用Lombok
     */
    private boolean useLombok;

    /**
     * 是否生成getter/setter
     */
    private boolean generateGetterSetter;
}
