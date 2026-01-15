package com.architecture.core.registry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Dubbo 服务注册与发现机制示例
 *
 * 支持的注册中心：
 * 1. Zookeeper：Apache 官方推荐
 * 2. Nacos：阿里巴巴推荐
 * 3. Redis：高性能缓存
 * 4. Multicast：组播，无需第三方服务
 * 5. Simple：简单注册中心，仅供测试使用
 *
 * 核心原理：
 * - 服务提供者启动时向注册中心注册服务
 * - 服务消费者启动时向注册中心订阅服务
 * - 注册中心推送服务变更通知
 * - 支持自动注册和手动注册
 *
 * 目录结构（Zookeeper）：
 * /dubbo
 *   /com.example.UserService
 *     /providers
 *       dubbo://192.168.1.1:20880/com.example.UserService?version=1.0.0
 *       dubbo://192.168.1.2:20880/com.example.UserService?version=1.0.0
 *     /consumers
 *       consumer://192.168.1.100/com.example.UserService?version=1.0.0
 *     /configurators
 *     /routers
 */
public class RegistryDemo {

    /**
     * URL 类（简化版）
     */
    static class URL {
        private String protocol;
        private String host;
        private int port;
        private String path;
        private Map<String, String> parameters;

        public URL(String protocol, String host, int port, String path) {
            this.protocol = protocol;
            this.host = host;
            this.port = port;
            this.path = path;
            this.parameters = new HashMap<>();
        }

        public URL addParameter(String key, String value) {
            parameters.put(key, value);
            return this;
        }

        public String getParameter(String key) {
            return parameters.get(key);
        }

        public String toFullString() {
            StringBuilder sb = new StringBuilder();
            sb.append(protocol).append("://").append(host).append(":").append(port);
            sb.append("/").append(path);
            if (!parameters.isEmpty()) {
                sb.append("?");
                parameters.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
            }
            return sb.toString();
        }

        public String getServiceKey() {
            return path;
        }
    }

    /**
     * 通知监听器
     */
    interface NotifyListener {
        void notify(List<URL> urls);
    }

    /**
     * 注册中心接口
     */
    interface Registry {
        /**
         * 注册服务
         */
        void register(URL url);

        /**
         * 取消注册
         */
        void unregister(URL url);

        /**
         * 订阅服务
         */
        void subscribe(URL url, NotifyListener listener);

        /**
         * 取消订阅
         */
        void unsubscribe(URL url, NotifyListener listener);

        /**
         * 查询服务列表
         */
        List<URL> lookup(URL url);
    }

    /**
     * 简单注册中心实现（内存版）
     */
    static class SimpleRegistry implements Registry {
        // 服务名 -> URL列表
        private Map<String, List<URL>> registeredServices = new ConcurrentHashMap<>();

        // 服务名 -> 监听器列表
        private Map<String, List<NotifyListener>> subscribers = new ConcurrentHashMap<>();

        @Override
        public void register(URL url) {
            String serviceKey = url.getServiceKey();
            System.out.println("[注册中心] 注册服务: " + serviceKey);
            System.out.println("[注册中心] URL: " + url.toFullString());

            registeredServices.computeIfAbsent(serviceKey, k -> new CopyOnWriteArrayList<>())
                .add(url);

            // 通知所有订阅者
            notifySubscribers(serviceKey);
        }

        @Override
        public void unregister(URL url) {
            String serviceKey = url.getServiceKey();
            System.out.println("[注册中心] 取消注册: " + serviceKey);

            List<URL> urls = registeredServices.get(serviceKey);
            if (urls != null) {
                urls.remove(url);
                notifySubscribers(serviceKey);
            }
        }

        @Override
        public void subscribe(URL url, NotifyListener listener) {
            String serviceKey = url.getServiceKey();
            System.out.println("[注册中心] 订阅服务: " + serviceKey);

            subscribers.computeIfAbsent(serviceKey, k -> new CopyOnWriteArrayList<>())
                .add(listener);

            // 立即通知当前服务列表
            List<URL> urls = registeredServices.getOrDefault(serviceKey, Collections.emptyList());
            listener.notify(new ArrayList<>(urls));
        }

        @Override
        public void unsubscribe(URL url, NotifyListener listener) {
            String serviceKey = url.getServiceKey();
            System.out.println("[注册中心] 取消订阅: " + serviceKey);

            List<NotifyListener> listeners = subscribers.get(serviceKey);
            if (listeners != null) {
                listeners.remove(listener);
            }
        }

        @Override
        public List<URL> lookup(URL url) {
            String serviceKey = url.getServiceKey();
            return new ArrayList<>(
                registeredServices.getOrDefault(serviceKey, Collections.emptyList())
            );
        }

        private void notifySubscribers(String serviceKey) {
            List<NotifyListener> listeners = subscribers.get(serviceKey);
            if (listeners == null || listeners.isEmpty()) {
                return;
            }

            List<URL> urls = registeredServices.getOrDefault(serviceKey, Collections.emptyList());
            System.out.println("[注册中心] 推送服务变更通知: " + serviceKey + ", 提供者数量: " + urls.size());

            for (NotifyListener listener : listeners) {
                listener.notify(new ArrayList<>(urls));
            }
        }
    }

    /**
     * Zookeeper 注册中心实现（简化版）
     */
    static class ZookeeperRegistry implements Registry {
        private String zkAddress;
        private Map<String, List<URL>> cache = new ConcurrentHashMap<>();
        private Map<String, List<NotifyListener>> listeners = new ConcurrentHashMap<>();

        public ZookeeperRegistry(String zkAddress) {
            this.zkAddress = zkAddress;
            System.out.println("[Zookeeper] 连接到: " + zkAddress);
        }

        @Override
        public void register(URL url) {
            String path = toZkPath(url, "providers");
            System.out.println("[Zookeeper] 创建临时节点: " + path);

            // 实际实现中会调用 Zookeeper API 创建临时节点
            // zkClient.create(path, url.toFullString(), CreateMode.EPHEMERAL);

            cache.computeIfAbsent(url.getServiceKey(), k -> new CopyOnWriteArrayList<>())
                .add(url);
        }

        @Override
        public void unregister(URL url) {
            String path = toZkPath(url, "providers");
            System.out.println("[Zookeeper] 删除节点: " + path);

            // zkClient.delete(path);

            List<URL> urls = cache.get(url.getServiceKey());
            if (urls != null) {
                urls.remove(url);
            }
        }

        @Override
        public void subscribe(URL url, NotifyListener listener) {
            String serviceKey = url.getServiceKey();
            String path = "/dubbo/" + serviceKey + "/providers";
            System.out.println("[Zookeeper] 订阅路径: " + path);

            listeners.computeIfAbsent(serviceKey, k -> new CopyOnWriteArrayList<>())
                .add(listener);

            // 实际实现中会监听 Zookeeper 节点变化
            // zkClient.subscribeChildChanges(path, (parentPath, currentChildren) -> {
            //     List<URL> urls = toURLs(currentChildren);
            //     listener.notify(urls);
            // });

            // 模拟初始通知
            List<URL> urls = cache.getOrDefault(serviceKey, Collections.emptyList());
            listener.notify(new ArrayList<>(urls));
        }

        @Override
        public void unsubscribe(URL url, NotifyListener listener) {
            String serviceKey = url.getServiceKey();
            List<NotifyListener> list = listeners.get(serviceKey);
            if (list != null) {
                list.remove(listener);
            }
        }

        @Override
        public List<URL> lookup(URL url) {
            String path = "/dubbo/" + url.getServiceKey() + "/providers";
            System.out.println("[Zookeeper] 查询节点: " + path);

            // 实际实现中会查询 Zookeeper
            // List<String> children = zkClient.getChildren(path);
            // return toURLs(children);

            return new ArrayList<>(
                cache.getOrDefault(url.getServiceKey(), Collections.emptyList())
            );
        }

        private String toZkPath(URL url, String category) {
            return "/dubbo/" + url.getServiceKey() + "/" + category + "/" +
                   url.protocol + "%3A%2F%2F" + url.host + "%3A" + url.port;
        }
    }

    /**
     * 服务提供者
     */
    static class ServiceProvider {
        private Registry registry;
        private URL serviceURL;

        public ServiceProvider(Registry registry, String host, int port, String serviceName) {
            this.registry = registry;
            this.serviceURL = new URL("dubbo", host, port, serviceName)
                .addParameter("version", "1.0.0")
                .addParameter("methods", "sayHello,sayGoodbye")
                .addParameter("timeout", "3000");
        }

        public void export() {
            System.out.println("\n=== 服务提供者启动 ===");
            registry.register(serviceURL);
            System.out.println("服务已注册: " + serviceURL.toFullString());
        }

        public void unexport() {
            System.out.println("\n=== 服务提供者停止 ===");
            registry.unregister(serviceURL);
            System.out.println("服务已注销");
        }
    }

    /**
     * 服务消费者
     */
    static class ServiceConsumer {
        private Registry registry;
        private String serviceName;
        private List<URL> providerURLs = new CopyOnWriteArrayList<>();

        public ServiceConsumer(Registry registry, String serviceName) {
            this.registry = registry;
            this.serviceName = serviceName;
        }

        public void subscribe() {
            System.out.println("\n=== 服务消费者启动 ===");

            URL subscribeURL = new URL("consumer", "192.168.1.100", 0, serviceName)
                .addParameter("version", "1.0.0");

            NotifyListener listener = new NotifyListener() {
                @Override
                public void notify(List<URL> urls) {
                    System.out.println("\n[消费者] 收到服务变更通知:");
                    providerURLs.clear();
                    providerURLs.addAll(urls);

                    if (urls.isEmpty()) {
                        System.out.println("  没有可用的服务提供者");
                    } else {
                        System.out.println("  可用的服务提供者:");
                        urls.forEach(url -> System.out.println("    - " + url.toFullString()));
                    }
                }
            };

            registry.subscribe(subscribeURL, listener);
        }

        public void invoke() {
            if (providerURLs.isEmpty()) {
                System.out.println("[消费者] 调用失败: 没有可用的服务提供者");
                return;
            }

            // 简单选择第一个提供者
            URL providerURL = providerURLs.get(0);
            System.out.println("[消费者] 调用服务: " + providerURL.host + ":" + providerURL.port);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("========== Dubbo 服务注册与发现示例 ==========\n");

        // 创建注册中心
        Registry registry = new SimpleRegistry();
        // 或使用 Zookeeper 注册中心
        // Registry registry = new ZookeeperRegistry("127.0.0.1:2181");

        String serviceName = "com.example.UserService";

        // 创建消费者并订阅
        ServiceConsumer consumer = new ServiceConsumer(registry, serviceName);
        consumer.subscribe();

        Thread.sleep(1000);

        // 启动第一个提供者
        ServiceProvider provider1 = new ServiceProvider(registry, "192.168.1.1", 20880, serviceName);
        provider1.export();

        Thread.sleep(1000);

        // 启动第二个提供者
        ServiceProvider provider2 = new ServiceProvider(registry, "192.168.1.2", 20881, serviceName);
        provider2.export();

        Thread.sleep(1000);

        // 消费者调用服务
        System.out.println("\n=== 消费者调用服务 ===");
        consumer.invoke();

        Thread.sleep(1000);

        // 停止第一个提供者
        provider1.unexport();

        Thread.sleep(1000);

        // 再次调用
        System.out.println("\n=== 消费者再次调用服务 ===");
        consumer.invoke();
    }
}

/**
 * Dubbo 注册中心总结：
 *
 * 1. 注册中心的作用：
 *    - 服务注册：提供者注册服务信息
 *    - 服务发现：消费者查询服务提供者
 *    - 动态感知：服务上下线实时通知
 *    - 负载均衡：提供多个服务提供者供选择
 *
 * 2. Zookeeper 特性：
 *    - 临时节点：提供者下线自动删除
 *    - 监听机制：Watcher 监听节点变化
 *    - 目录结构：树形结构存储服务信息
 *    - 高可用：集群部署保证可靠性
 *
 * 3. 注册流程：
 *    Provider: 启动 -> 注册 -> 等待请求
 *    Consumer: 启动 -> 订阅 -> 获取列表 -> 监听变化
 *
 * 4. 容错机制：
 *    - 注册失败自动重试
 *    - 本地缓存提供者列表
 *    - 注册中心不可用时使用缓存
 *    - 定期检查并更新
 *
 * 5. 配置方式：
 *    <dubbo:registry address="zookeeper://127.0.0.1:2181" />
 *    <dubbo:registry address="nacos://127.0.0.1:8848" />
 *    <dubbo:registry address="redis://127.0.0.1:6379" />
 *
 * 6. 最佳实践：
 *    - 使用集群部署注册中心
 *    - 配置合理的超时时间
 *    - 启用本地缓存
 *    - 监控注册中心状态
 */
