package com.architecture.aicoding.workflow.steps;

import com.architecture.aicoding.workflow.WorkflowContext;
import com.architecture.aicoding.workflow.WorkflowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * 代码验证步骤
 * 验证生成代码的基本语法正确性
 */
public class CodeValidationStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(CodeValidationStep.class);

    @Override
    public void execute(WorkflowContext context) {
        if (!context.getRequest().isValidateCode()) {
            logger.info("跳过代码验证（用户配置）");
            return;
        }

        logger.info("开始验证代码");

        String code = context.getGeneratedCode().getCode();

        // 执行基本语法验证
        ValidationResult result = validateBasicSyntax(code);

        context.setAttribute("syntaxValid", result.isValid());
        context.getGeneratedCode().setValid(result.isValid());

        if (!result.isValid()) {
            logger.warn("代码验证失败: {}", result.getErrorMessage());
            context.getGeneratedCode().setValidationError(result.getErrorMessage());
        } else {
            logger.info("代码验证通过");
        }
    }

    /**
     * 验证基本语法
     */
    private ValidationResult validateBasicSyntax(String code) {
        // 1. 检查括号匹配
        if (!checkBracesBalance(code)) {
            return ValidationResult.error("代码中存在不匹配的花括号");
        }

        // 2. 检查是否有基本的类/接口定义
        if (!hasClassOrInterfaceDefinition(code)) {
            return ValidationResult.error("代码中缺少类或接口定义");
        }

        // 3. 检查是否有明显的语法错误
        if (hasSyntaxErrors(code)) {
            return ValidationResult.error("代码中存在明显的语法错误");
        }

        return ValidationResult.success();
    }

    /**
     * 检查花括号是否匹配
     */
    private boolean checkBracesBalance(String code) {
        int balance = 0;
        for (char c : code.toCharArray()) {
            if (c == '{') {
                balance++;
            } else if (c == '}') {
                balance--;
            }
            if (balance < 0) {
                return false; // 右括号多于左括号
            }
        }
        return balance == 0; // 必须完全匹配
    }

    /**
     * 检查是否有类或接口定义
     */
    private boolean hasClassOrInterfaceDefinition(String code) {
        Pattern pattern = Pattern.compile(
            "(public|private|protected)?\\s*(abstract)?\\s*(class|interface|enum)\\s+\\w+");
        return pattern.matcher(code).find();
    }

    /**
     * 检查明显的语法错误
     */
    private boolean hasSyntaxErrors(String code) {
        // 检查是否有未闭合的字符串
        int quoteCount = 0;
        boolean inEscape = false;
        for (char c : code.toCharArray()) {
            if (inEscape) {
                inEscape = false;
                continue;
            }
            if (c == '\\') {
                inEscape = true;
            } else if (c == '"') {
                quoteCount++;
            }
        }

        // 如果引号数量是奇数，说明有未闭合的字符串
        if (quoteCount % 2 != 0) {
            return true;
        }

        // 检查是否有未闭合的注释
        if (code.contains("/*") && !code.contains("*/")) {
            return true;
        }

        return false;
    }

    @Override
    public String getStepName() {
        return "CodeValidation";
    }

    /**
     * 验证结果内部类
     */
    private static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
