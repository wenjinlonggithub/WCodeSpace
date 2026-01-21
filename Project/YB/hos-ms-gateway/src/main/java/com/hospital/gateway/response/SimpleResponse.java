package com.hospital.gateway.response;

import com.hospital.gateway.enums.ResultCodeEnum;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SimpleResponse<T> extends BaseResponse {

    private T data;

    private static <T> SimpleResponse<T> build(int code, String message, T data) {
        SimpleResponse<T> ret = new SimpleResponse<T>();
        ret.setCode(code);
        ret.setMsg(message);
        ret.setData(data);
        return ret;
    }

    public static <T> SimpleResponse<T> succeed() {
        return (SimpleResponse<T>) build(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMsg(), (Object)null);
    }

    public static <T> SimpleResponse<T> succeed(T data) {
        return data instanceof String ? build(ResultCodeEnum.SUCCESS.getCode(), data.toString(), data) : build(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMsg(), data);
    }

    public static <T> SimpleResponse<T> fail() {
        return (SimpleResponse<T>) build(ResultCodeEnum.FAILED.getCode(), ResultCodeEnum.FAILED.getMsg(), (Object)null);
    }

    public static <T> SimpleResponse<T> fail(String message) {
        return (SimpleResponse<T>) build(ResultCodeEnum.FAILED.getCode(), message, (Object)null);
    }

    public static <T> SimpleResponse<T> fail(int code, String message) {
        return (SimpleResponse<T>) build(code, message, (Object)null);
    }

    public static <T> SimpleResponse<T> fail(ResponseCode responseCode) {
        return (SimpleResponse<T>) build(responseCode.getCode(), responseCode.getMsg(), (Object)null);
    }

}
