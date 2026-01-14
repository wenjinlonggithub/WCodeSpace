package com.architecture.aicoding.exception;

/**
 * 代码验证异常
 * 用于封装代码验证过程中的各种错误
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }
}
