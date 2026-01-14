package com.architecture.aicoding.workflow;

import com.architecture.aicoding.model.CodeGenerationRequest;
import com.architecture.aicoding.model.CodeGenerationResponse;

/**
 * 代码生成工作流接口
 * 定义完整的代码生成流程
 */
public interface CodeGenerationWorkflow {

    /**
     * 执行代码生成工作流
     * @param request 代码生成请求
     * @return 代码生成响应
     */
    CodeGenerationResponse execute(CodeGenerationRequest request);

    /**
     * 获取工作流名称
     * @return 工作流名称
     */
    String getWorkflowName();
}
