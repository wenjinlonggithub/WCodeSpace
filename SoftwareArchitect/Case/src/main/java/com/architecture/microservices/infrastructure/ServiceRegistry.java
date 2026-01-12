package com.architecture.microservices.infrastructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 服务注册中心
 *
 * 职责:
 * 1. 服务注册
 * 2. 服务发现
 * 3. 健康检查
 */
public class ServiceRegistry {

    private static ServiceRegistry instance;
    private final Map<String, List<ServiceInstance>> services = new ConcurrentHashMap<>();

    private ServiceRegistry() {
    }

    public static synchronized ServiceRegistry getInstance() {
        if (instance == null) {
            instance = new ServiceRegistry();
        }
        return instance;
    }

    /**
     * 注册服务
     */
    public void register(String serviceName, ServiceInstance instance) {
        services.computeIfAbsent(serviceName, k -> new ArrayList<>()).add(instance);
        System.out.println("  [Registry] 服务注册: " + serviceName +
                         " (实例: " + instance.getInstanceId() + ", 地址: " + instance.getAddress() + ")");
    }

    /**
     * 注销服务
     */
    public void deregister(String serviceName, String instanceId) {
        List<ServiceInstance> instances = services.get(serviceName);
        if (instances != null) {
            instances.removeIf(inst -> inst.getInstanceId().equals(instanceId));
            System.out.println("  [Registry] 服务注销: " + serviceName + " (实例: " + instanceId + ")");
        }
    }

    /**
     * 发现服务(负载均衡: 轮询)
     */
    public ServiceInstance discover(String serviceName) {
        List<ServiceInstance> instances = services.get(serviceName);
        if (instances == null || instances.isEmpty()) {
            return null;
        }

        // 简单轮询负载均衡
        ServiceInstance instance = instances.get(0);
        // 将第一个实例移到末尾
        instances.remove(0);
        instances.add(instance);

        return instance;
    }

    /**
     * 获取所有服务实例
     */
    public List<ServiceInstance> getInstances(String serviceName) {
        return services.getOrDefault(serviceName, new ArrayList<>());
    }

    /**
     * 打印已注册的服务
     */
    public void printRegisteredServices() {
        System.out.println("\n已注册服务:");
        services.forEach((serviceName, instances) -> {
            System.out.println("  " + serviceName + " (" + instances.size() + " 个实例)");
            instances.forEach(inst ->
                System.out.println("    - " + inst.getInstanceId() + " @ " + inst.getAddress())
            );
        });
    }
}
