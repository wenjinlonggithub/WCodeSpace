package com.okx.finance.common.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一响应结果类
 * 封装所有API接口的返回数据，提供统一的响应格式
 *
 * <p>响应格式：
 * <pre>
 * {
 *   "code": 200,           // 状态码：200-成功，其他-失败
 *   "message": "success",  // 提示信息
 *   "data": {...}          // 返回数据（泛型）
 * }
 * </pre>
 *
 * <p>状态码约定：
 * - 200：成功
 * - 400：请求参数错误
 * - 401：未授权（Token无效或过期）
 * - 403：无权限
 * - 404：资源不存在
 * - 500：服务器内部错误
 *
 * <p>使用示例：
 * <pre>
 * // 成功返回
 * return Result.success(user);
 *
 * // 失败返回
 * return Result.error("用户不存在");
 * return Result.error(401, "Token已过期");
 * </pre>
 *
 * @param <T> 返回数据的类型
 * @author OKX Finance Team
 * @version 1.0
 */
@Data
public class Result<T> implements Serializable {
    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     * 200表示成功，其他值表示失败
     * 具体含义参考HTTP状态码
     */
    private Integer code;

    /**
     * 提示信息
     * 成功时为"success"或业务相关的提示
     * 失败时为具体的错误原因
     */
    private String message;

    /**
     * 返回数据
     * 泛型类型，可以是任何类型的数据
     * 成功时包含具体的业务数据
     * 失败时通常为null
     */
    private T data;

    /**
     * 成功响应（带数据）
     * 创建一个表示成功的响应对象，并携带数据
     *
     * @param data 要返回的数据
     * @param <T>  数据类型
     * @return Result对象，code=200，message="success"
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 失败响应（默认状态码500）
     * 创建一个表示失败的响应对象，使用默认状态码500
     *
     * @param message 错误信息
     * @param <T>     数据类型
     * @return Result对象，code=500，data=null
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败响应（指定状态码）
     * 创建一个表示失败的响应对象，可以指定具体的状态码
     *
     * @param code    状态码（如401、403、404等）
     * @param message 错误信息
     * @param <T>     数据类型
     * @return Result对象，指定code，data=null
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
