package com.architecture.aicoding.workflow;

import com.architecture.aicoding.model.CodeGenerationRequest;
import com.architecture.aicoding.model.CodeGenerationResponse;
import com.architecture.aicoding.model.GeneratedCode;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作流上下文
 * 在工作流各步骤间传递数据
 */
@Data
public class WorkflowContext {

    /**
     * 原始请求
     */
    private CodeGenerationRequest request;

    /**
     * 构建的Prompt
     */
    private String builtPrompt;

    /**
     * API原始响应
     */
    private String rawResponse;

    /**
     * 生成的代码
     */
    private GeneratedCode generatedCode;

    /**
     * 错误信息
     */
    private Exception error;

    /**
     * 元数据（存储各步骤的中间数据）
     */
    private Map<String, Object> metadata;

    /**
     * 开始时间
     */
    private long startTime;

    /**
     * 构造函数
     */
    public WorkflowContext(CodeGenerationRequest request) {
        this.request = request;
        this.metadata = new HashMap<>();
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 设置属性
     */
    public void setAttribute(String key, Object value) {
        metadata.put(key, value);
    }

    /**
     * 获取属性
     */
    public Object getAttribute(String key) {
        return metadata.get(key);
    }

    /**
     * 是否有错误
     */
    public boolean hasError() {
        return error != null;
    }

    /**
     * 构建响应对象
     */
    public CodeGenerationResponse buildResponse() {
        CodeGenerationResponse response = new CodeGenerationResponse();
        response.setSuccess(error == null);
        response.setGeneratedCode(generatedCode);
        response.setError(error != null ? error.getMessage() : null);
        response.setMetadata(metadata);

        // 计算耗时
        long duration = System.currentTimeMillis() - startTime;
        response.setDurationMs(duration);
        response.addMetadata("durationMs", duration);

        return response;
    }
}
