package com.architecture.aicoding.model;

import lombok.Data;

/**
 * 生成的代码模型
 * 封装AI生成的代码及其元信息
 */
@Data
public class GeneratedCode {

    /**
     * 代码内容
     */
    private String code;

    /**
     * 类名/接口名
     */
    private String className;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 代码是否有效（通过验证）
     */
    private boolean valid;

    /**
     * 包名
     */
    private String packageName;

    /**
     * 代码类型（class, interface, enum, abstract）
     */
    private String codeType;

    /**
     * 代码行数
     */
    private int lineCount;

    /**
     * 验证错误信息（如果验证失败）
     */
    private String validationError;

    public GeneratedCode() {
        this.language = "java";
        this.valid = true;
    }
}
