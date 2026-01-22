package com.alibaba.nacos.discovery.model;

import lombok.Data;
import java.util.List;

/**
 * 服务信息模型
 *
 * @author architecture
 */
@Data
public class ServiceInfo {
    private String serviceName;
    private String port;
    private List<String> services;
}
