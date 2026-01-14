package com.architecture.aicoding.exception;

/**
 * OpenAI API异常
 * 用于封装OpenAI API调用过程中的各种错误
 */
public class OpenAIException extends RuntimeException {

    public OpenAIException(String message) {
        super(message);
    }

    public OpenAIException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenAIException(Throwable cause) {
        super(cause);
    }
}
