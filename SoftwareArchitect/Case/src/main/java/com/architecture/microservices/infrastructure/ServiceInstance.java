package com.architecture.microservices.infrastructure;

/**
 * 服务实例
 */
public class ServiceInstance {

    private final String instanceId;
    private final String serviceName;
    private final String address;
    private final int port;
    private boolean healthy = true;

    public ServiceInstance(String instanceId, String serviceName, String address, int port) {
        this.instanceId = instanceId;
        this.serviceName = serviceName;
        this.address = address;
        this.port = port;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }

    public String getUrl() {
        return "http://" + address + ":" + port;
    }
}
