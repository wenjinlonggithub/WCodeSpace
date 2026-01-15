package com.architecture.config;

import org.apache.dubbo.config.*;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;

import java.util.HashMap;
import java.util.Map;

/**
 * Dubbo API 配置示例
 *
 * 使用 Java API 方式配置 Dubbo 服务
 * 适用于：动态配置、编程式配置、不依赖 Spring 的场景
 */
public class ApiConfig {

    /**
     * 服务提供者 API 配置示例
     */
    public static class ProviderApiConfig {

        public static void main(String[] args) {
            // 1. 应用配置
            ApplicationConfig application = new ApplicationConfig();
            application.setName("dubbo-provider-api");
            application.setVersion("1.0.0");
            application.setOwner("architecture-team");
            application.setOrganization("example-company");

            // 应用参数
            Map<String, String> appParams = new HashMap<>();
            appParams.put("environment", "production");
            application.setParameters(appParams);

            // 2. 注册中心配置
            RegistryConfig registry = new RegistryConfig();
            registry.setProtocol("zookeeper");
            registry.setAddress("127.0.0.1:2181");
            registry.setTimeout(5000);
            registry.setSession(60000);

            // 3. 协议配置
            ProtocolConfig protocol = new ProtocolConfig();
            protocol.setName("dubbo");
            protocol.setPort(20880);
            protocol.setThreads(200);
            protocol.setThreadpool("fixed");
            protocol.setSerialization("hessian2");

            // 协议参数
            Map<String, String> protocolParams = new HashMap<>();
            protocolParams.put("heartbeat", "60000");
            protocolParams.put("payload", "8388608");
            protocol.setParameters(protocolParams);

            // 4. 提供者全局配置
            ProviderConfig provider = new ProviderConfig();
            provider.setTimeout(3000);
            provider.setRetries(0);
            provider.setLoadbalance("roundrobin");
            provider.setCluster("failover");
            provider.setVersion("1.0.0");
            provider.setGroup("default");

            // 5. 监控中心配置
            MonitorConfig monitor = new MonitorConfig();
            monitor.setProtocol("registry");

            // 6. 元数据配置
            MetadataReportConfig metadataReport = new MetadataReportConfig();
            metadataReport.setAddress("zookeeper://127.0.0.1:2181");

            // 7. 创建服务实例
            UserServiceImpl userServiceImpl = new UserServiceImpl();

            // 8. 服务配置
            ServiceConfig<UserService> service = new ServiceConfig<>();
            service.setApplication(application);
            service.setRegistry(registry);
            service.setProtocol(protocol);
            service.setProvider(provider);
            service.setMonitor(monitor);

            // 服务特定配置
            service.setInterface(UserService.class);
            service.setRef(userServiceImpl);
            service.setVersion("1.0.0");
            service.setGroup("default");
            service.setTimeout(3000);
            service.setRetries(0);
            service.setConnections(100);
            service.setExecutes(200);
            service.setActives(0);
            service.setDelay(0);

            // 方法级配置
            MethodConfig getUserByIdMethod = new MethodConfig();
            getUserByIdMethod.setName("getUserById");
            getUserByIdMethod.setTimeout(1000);
            getUserByIdMethod.setRetries(2);

            MethodConfig createUserMethod = new MethodConfig();
            createUserMethod.setName("createUser");
            createUserMethod.setTimeout(5000);
            createUserMethod.setRetries(0);

            // service.setMethods(Arrays.asList(getUserByIdMethod, createUserMethod));

            // 9. 使用 DubboBootstrap 启动（推荐）
            DubboBootstrap.getInstance()
                .application(application)
                .registry(registry)
                .protocol(protocol)
                .service(service)
                .start()
                .await();

            // 或者直接暴露服务（不推荐）
            // service.export();

            System.out.println("服务已启动...");
        }
    }

    /**
     * 服务消费者 API 配置示例
     */
    public static class ConsumerApiConfig {

        public static void main(String[] args) {
            // 1. 应用配置
            ApplicationConfig application = new ApplicationConfig();
            application.setName("dubbo-consumer-api");

            // 2. 注册中心配置
            RegistryConfig registry = new RegistryConfig();
            registry.setAddress("zookeeper://127.0.0.1:2181");
            registry.setTimeout(5000);

            // 3. 消费者全局配置
            ConsumerConfig consumer = new ConsumerConfig();
            consumer.setTimeout(3000);
            consumer.setRetries(2);
            consumer.setCheck(false);
            consumer.setLoadbalance("roundrobin");
            consumer.setCluster("failover");

            // 4. 引用配置
            ReferenceConfig<UserService> reference = new ReferenceConfig<>();
            reference.setApplication(application);
            reference.setRegistry(registry);
            reference.setConsumer(consumer);

            // 引用特定配置
            reference.setInterface(UserService.class);
            reference.setVersion("1.0.0");
            reference.setGroup("default");
            reference.setTimeout(3000);
            reference.setRetries(2);
            reference.setCheck(false);
            reference.setConnections(100);
            reference.setLazy(false);

            // 方法级配置
            MethodConfig getUserMethod = new MethodConfig();
            getUserMethod.setName("getUserById");
            getUserMethod.setTimeout(1000);
            getUserMethod.setRetries(3);

            // reference.setMethods(Collections.singletonList(getUserMethod));

            // 5. 使用 DubboBootstrap 启动（推荐）
            DubboBootstrap.getInstance()
                .application(application)
                .registry(registry)
                .reference(reference)
                .start();

            // 6. 获取服务代理
            UserService userService = reference.get();

            // 7. 调用服务
            try {
                User user = userService.getUserById(1L);
                System.out.println("查询结果: " + user);
            } catch (Exception e) {
                System.err.println("调用失败: " + e.getMessage());
            }

            // 8. 销毁服务
            // reference.destroy();
            // DubboBootstrap.getInstance().stop();
        }
    }

    /**
     * 多注册中心配置示例
     */
    public static class MultiRegistryConfig {

        public static void main(String[] args) {
            ApplicationConfig application = new ApplicationConfig();
            application.setName("dubbo-multi-registry");

            // 配置多个注册中心
            RegistryConfig zkRegistry = new RegistryConfig();
            zkRegistry.setId("zkRegistry");
            zkRegistry.setAddress("zookeeper://127.0.0.1:2181");

            RegistryConfig nacosRegistry = new RegistryConfig();
            nacosRegistry.setId("nacosRegistry");
            nacosRegistry.setAddress("nacos://127.0.0.1:8848");

            // 服务只注册到指定的注册中心
            ServiceConfig<UserService> service = new ServiceConfig<>();
            service.setInterface(UserService.class);
            // service.setRegistries(Arrays.asList(zkRegistry, nacosRegistry));

            DubboBootstrap.getInstance()
                .application(application)
                .registry(zkRegistry)
                .registry(nacosRegistry)
                .service(service)
                .start();
        }
    }

    /**
     * 多协议配置示例
     */
    public static class MultiProtocolConfig {

        public static void main(String[] args) {
            ApplicationConfig application = new ApplicationConfig();
            application.setName("dubbo-multi-protocol");

            RegistryConfig registry = new RegistryConfig();
            registry.setAddress("zookeeper://127.0.0.1:2181");

            // Dubbo 协议
            ProtocolConfig dubboProtocol = new ProtocolConfig();
            dubboProtocol.setName("dubbo");
            dubboProtocol.setPort(20880);

            // REST 协议
            ProtocolConfig restProtocol = new ProtocolConfig();
            restProtocol.setName("rest");
            restProtocol.setPort(8080);
            restProtocol.setServer("netty");

            // 服务使用多种协议暴露
            ServiceConfig<UserService> service = new ServiceConfig<>();
            service.setInterface(UserService.class);
            // service.setProtocols(Arrays.asList(dubboProtocol, restProtocol));

            DubboBootstrap.getInstance()
                .application(application)
                .registry(registry)
                .protocol(dubboProtocol)
                .protocol(restProtocol)
                .service(service)
                .start();
        }
    }

    /**
     * 直连提供者配置示例（开发测试用）
     */
    public static class DirectConnectionConfig {

        public static void main(String[] args) {
            ApplicationConfig application = new ApplicationConfig();
            application.setName("dubbo-direct-connection");

            // 直连配置
            ReferenceConfig<UserService> reference = new ReferenceConfig<>();
            reference.setApplication(application);
            reference.setInterface(UserService.class);

            // 直接指定服务提供者地址，不通过注册中心
            reference.setUrl("dubbo://192.168.1.100:20880");

            DubboBootstrap.getInstance()
                .application(application)
                .reference(reference)
                .start();

            UserService userService = reference.get();
            System.out.println("直连调用: " + userService.getUserById(1L));
        }
    }

    /**
     * 泛化调用配置示例
     */
    public static class GenericCallConfig {

        public static void main(String[] args) {
            ApplicationConfig application = new ApplicationConfig();
            application.setName("dubbo-generic-call");

            RegistryConfig registry = new RegistryConfig();
            registry.setAddress("zookeeper://127.0.0.1:2181");

            // 泛化引用配置
            ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
            reference.setApplication(application);
            reference.setRegistry(registry);
            reference.setInterface("com.architecture.business.user.UserService");
            reference.setGeneric("true");

            DubboBootstrap.getInstance()
                .application(application)
                .registry(registry)
                .reference(reference)
                .start();

            // 获取泛化服务
            GenericService genericService = reference.get();

            // 泛化调用
            // Object result = genericService.$invoke(
            //     "getUserById",
            //     new String[]{"java.lang.Long"},
            //     new Object[]{1L}
            // );
            // System.out.println("泛化调用结果: " + result);
        }
    }

    // 服务接口和实现（占位）
    interface UserService {
        User getUserById(Long userId);
    }

    static class User {
        private Long id;
        private String username;

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }
    }

    static class UserServiceImpl implements UserService {
        @Override
        public User getUserById(Long userId) {
            User user = new User();
            // user.id = userId;
            // user.username = "User" + userId;
            return user;
        }
    }

    interface GenericService {
        // Dubbo 提供的泛化接口
    }
}

/**
 * API 配置说明：
 *
 * 1. 核心配置类：
 *    - ApplicationConfig: 应用配置
 *    - RegistryConfig: 注册中心配置
 *    - ProtocolConfig: 协议配置
 *    - ServiceConfig: 服务提供者配置
 *    - ReferenceConfig: 服务消费者配置
 *    - ProviderConfig: 提供者全局配置
 *    - ConsumerConfig: 消费者全局配置
 *    - MonitorConfig: 监控中心配置
 *    - MethodConfig: 方法级配置
 *
 * 2. DubboBootstrap（推荐）：
 *    - 统一的启动入口
 *    - 支持多服务、多注册中心、多协议
 *    - 优雅停机
 *    - 使用单例模式
 *
 * 3. 配置优先级：
 *    方法级 > 接口级 > 全局配置
 *    消费者 > 提供者
 *
 * 4. 适用场景：
 *    - 不依赖 Spring 的应用
 *    - 动态配置服务
 *    - 编程式配置
 *    - 测试场景
 *
 * 5. 优势：
 *    - 灵活性高
 *    - 可以动态添加/移除服务
 *    - 便于单元测试
 *    - 完全控制配置
 *
 * 6. 劣势：
 *    - 代码量较多
 *    - 配置分散
 *    - 不如声明式配置直观
 *
 * 7. 最佳实践：
 *    - 使用 DubboBootstrap 统一管理
 *    - 提取公共配置为方法
 *    - 使用配置文件 + API 结合
 *    - 注意资源释放（destroy）
 */
