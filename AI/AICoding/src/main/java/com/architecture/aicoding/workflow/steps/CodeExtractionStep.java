package com.architecture.aicoding.workflow.steps;

import com.architecture.aicoding.model.GeneratedCode;
import com.architecture.aicoding.workflow.WorkflowContext;
import com.architecture.aicoding.workflow.WorkflowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码提取步骤
 * 从AI响应中提取代码内容，识别类名和包名
 */
public class CodeExtractionStep implements WorkflowStep {
    private static final Logger logger = LoggerFactory.getLogger(CodeExtractionStep.class);

    // 匹配Markdown代码块的正则
    private static final Pattern CODE_BLOCK_PATTERN =
        Pattern.compile("```(?:java)?\\s*\\n([\\s\\S]*?)\\n```", Pattern.MULTILINE);

    // 匹配类名/接口名/枚举名的正则
    private static final Pattern CLASS_NAME_PATTERN =
        Pattern.compile("public\\s+(class|interface|enum|@interface)\\s+(\\w+)");

    // 匹配包名的正则
    private static final Pattern PACKAGE_PATTERN =
        Pattern.compile("package\\s+([a-zA-Z0-9_.]+)\\s*;");

    @Override
    public void execute(WorkflowContext context) {
        logger.info("开始提取代码");

        String rawResponse = context.getRawResponse();

        // 提取代码
        String code = extractCode(rawResponse);

        if (code == null || code.trim().isEmpty()) {
            throw new IllegalStateException("未能从响应中提取到有效代码");
        }

        // 提取类名
        String className = extractClassName(code);

        // 提取包名
        String packageName = extractPackageName(code);

        // 识别代码类型
        String codeType = extractCodeType(code);

        // 创建GeneratedCode对象
        GeneratedCode generatedCode = new GeneratedCode();
        generatedCode.setCode(code);
        generatedCode.setClassName(className);
        generatedCode.setPackageName(packageName);
        generatedCode.setCodeType(codeType);
        generatedCode.setLanguage("java");
        generatedCode.setLineCount(code.split("\n").length);

        context.setGeneratedCode(generatedCode);

        logger.info("代码提取成功 - 类名: {}, 包名: {}, 类型: {}, 行数: {}",
            className, packageName, codeType, generatedCode.getLineCount());

        logger.debug("提取的代码:\n{}", code);
    }

    /**
     * 从响应中提取代码
     */
    private String extractCode(String response) {
        // 尝试从Markdown代码块提取
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(response);
        if (matcher.find()) {
            logger.debug("从Markdown代码块提取代码");
            return matcher.group(1).trim();
        }

        // 如果没有代码块标记，检查是否整个响应就是代码
        if (response.contains("package ") || response.contains("public class") ||
            response.contains("public interface")) {
            logger.debug("响应本身就是代码");
            return response.trim();
        }

        logger.warn("未能识别代码格式，返回整个响应");
        return response.trim();
    }

    /**
     * 提取类名/接口名/枚举名
     */
    private String extractClassName(String code) {
        Pattern classPattern = Pattern.compile(
            "public\\s+(class|interface|enum|@interface)\\s+(\\w+)");
        Matcher matcher = classPattern.matcher(code);

        if (matcher.find()) {
            return matcher.group(2);
        }

        logger.warn("未能提取类名，使用默认值");
        return "UnknownClass";
    }

    /**
     * 提取包名
     */
    private String extractPackageName(String code) {
        Matcher matcher = PACKAGE_PATTERN.matcher(code);

        if (matcher.find()) {
            return matcher.group(1);
        }

        logger.debug("代码中没有包名声明");
        return null;
    }

    /**
     * 提取代码类型
     */
    private String extractCodeType(String code) {
        Matcher matcher = CLASS_NAME_PATTERN.matcher(code);

        if (matcher.find()) {
            String type = matcher.group(1);
            switch (type) {
                case "interface":
                    return "interface";
                case "enum":
                    return "enum";
                case "@interface":
                    return "annotation";
                default:
                    // 检查是否是抽象类
                    if (code.contains("abstract class")) {
                        return "abstract_class";
                    }
                    return "class";
            }
        }

        return "unknown";
    }

    @Override
    public String getStepName() {
        return "CodeExtraction";
    }
}
