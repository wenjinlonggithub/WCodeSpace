package com.architecture.aicoding.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 代码生成响应模型
 * 封装代码生成的结果和元数据
 */
@Data
public class CodeGenerationResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 生成的代码
     */
    private GeneratedCode generatedCode;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 元数据（token使用、耗时等）
     */
    private Map<String, Object> metadata;

    /**
     * 生成耗时（毫秒）
     */
    private Long durationMs;

    public CodeGenerationResponse() {
        this.metadata = new HashMap<>();
    }

    /**
     * 添加元数据
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    /**
     * 获取元数据
     */
    public Object getMetadata(String key) {
        return this.metadata.get(key);
    }
}
