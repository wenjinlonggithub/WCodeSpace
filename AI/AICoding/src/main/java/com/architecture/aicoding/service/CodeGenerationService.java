package com.architecture.aicoding.service;

import com.architecture.aicoding.model.CodeGenerationRequest;
import com.architecture.aicoding.model.CodeGenerationResponse;

/**
 * 代码生成服务接口
 * 提供代码生成的高层服务接口
 */
public interface CodeGenerationService {

    /**
     * 生成代码
     * @param request 代码生成请求
     * @return 代码生成响应
     */
    CodeGenerationResponse generateCode(CodeGenerationRequest request);

    /**
     * 获取服务名称
     * @return 服务名称
     */
    default String getServiceName() {
        return "CodeGenerationService";
    }
}
