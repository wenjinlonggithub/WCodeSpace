package com.hospital.gateway.filter.log;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author lvzeqiang
 * @date 2022/7/5 14:36
 * @description
 **/
@Data
public class LogObj {
    /**
     * 访问实例
     */
    private String targetServer;
    /**
     * 请求路径
     */
    private String path;
    /**
     * 请求方法
     */
    private String method;
    /**
     * 协议
     */
    private String schema;
    /**
     * 请求参数
     */
    private String params;
    /**
     * 响应体
     */
    private String responseData;
    /**
     * 请求ip
     */
    private String ip;
    /**
     * 请求时间
     */
    private LocalDateTime requestTime;
    /**
     * 响应时间
     */
    private LocalDateTime responseTime;
    /**
     * 执行时间
     */
    private long executeTime;
    private String appId;
}
