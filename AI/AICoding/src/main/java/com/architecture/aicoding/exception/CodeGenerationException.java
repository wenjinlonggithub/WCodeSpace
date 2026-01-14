package com.architecture.aicoding.exception;

/**
 * 代码生成异常
 * 用于封装代码生成流程中的各种错误
 */
public class CodeGenerationException extends RuntimeException {

    public CodeGenerationException(String message) {
        super(message);
    }

    public CodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodeGenerationException(Throwable cause) {
        super(cause);
    }
}
