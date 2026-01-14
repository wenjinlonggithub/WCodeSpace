package com.architecture.aicoding.workflow;

/**
 * 工作流步骤接口
 * 定义工作流中每个步骤的基本行为
 */
public interface WorkflowStep {

    /**
     * 执行步骤
     * @param context 工作流上下文
     */
    void execute(WorkflowContext context);

    /**
     * 获取步骤名称
     * @return 步骤名称
     */
    String getStepName();
}
