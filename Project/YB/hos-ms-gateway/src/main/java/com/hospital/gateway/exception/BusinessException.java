package com.hospital.gateway.exception;

/**
 * @author lvzeqiang
 * @date 2022/7/18 21:07
 * @description
 **/
public class BusinessException extends RuntimeException {
    public BusinessException() {
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
