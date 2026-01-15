package com.architecture.config;

import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Method;
import org.springframework.context.annotation.Configuration;

/**
 * Dubbo 注解配置示例
 *
 * 使用注解方式配置 Dubbo 服务，比 XML 配置更简洁
 *
 * 启用方式：
 * 1. Spring Boot: @EnableDubbo 或 @DubboComponentScan
 * 2. Spring: <dubbo:annotation package="com.architecture"/>
 */
public class AnnotationConfig {

    /**
     * 服务提供者注解示例
     */
    @DubboService(
        // 服务版本
        version = "1.0.0",

        // 服务分组
        group = "default",

        // 超时时间（毫秒）
        timeout = 3000,

        // 重试次数
        retries = 0,

        // 负载均衡策略：random, roundrobin, leastactive, consistenthash
        loadbalance = "roundrobin",

        // 集群容错模式：failover, failfast, failsafe, failback, forking
        cluster = "failover",

        // 服务权重
        weight = 100,

        // 延迟暴露时间（毫秒）-1 表示 Spring 容器初始化完成后暴露
        delay = -1,

        // 协议名称
        protocol = "dubbo",

        // 服务注册
        register = true,

        // 服务端最大并发执行数
        executes = 200,

        // 方法级配置
        methods = {
            @Method(name = "getUserById", timeout = 1000, retries = 2),
            @Method(name = "createUser", timeout = 5000, retries = 0),
            @Method(name = "login", timeout = 2000, retries = 0)
        },

        // 参数校验
        validation = "true",

        // 过滤器
        filter = "echo,generic",

        // 监听器
        listener = "",

        // 是否异步
        async = false,

        // 是否启用 Token 验证
        token = "false",

        // 访问日志
        accesslog = "false"
    )
    public static class UserServiceAnnotated {
        // 服务实现...
    }

    /**
     * 服务消费者注解示例
     */
    public static class OrderServiceAnnotated {

        @DubboReference(
            // 服务版本
            version = "1.0.0",

            // 服务分组
            group = "default",

            // 超时时间
            timeout = 3000,

            // 重试次数
            retries = 2,

            // 启动时检查服务是否可用
            check = false,

            // 负载均衡策略
            loadbalance = "roundrobin",

            // 集群容错模式
            cluster = "failover",

            // 每个提供者的最大连接数
            connections = 100,

            // 是否延迟初始化
            lazy = false,

            // 客户端最大并发调用数
            actives = 0,

            // 方法级配置
            methods = {
                @Method(name = "getUserById", timeout = 1000, retries = 3),
                @Method(name = "login", timeout = 2000, retries = 0)
            },

            // 本地存根（客户端参数验证）
            stub = "",

            // Mock 降级
            mock = "",

            // 结果缓存: lru, threadlocal, jcache
            cache = "",

            // 是否异步调用
            async = false,

            // 异步调用发送超时
            sent = true,

            // 服务注册
            registry = {""}
        )
        private UserService userService;

        public void someMethod() {
            // 使用注入的服务
            userService.getUserById(1L);
        }
    }

    /**
     * 异步调用注解示例
     */
    public static class AsyncCallExample {

        @DubboReference(
            version = "1.0.0",
            methods = {
                @Method(name = "createOrder", async = true),
                @Method(name = "payOrder", async = true)
            }
        )
        private OrderService orderService;

        public void asyncCall() {
            // 异步调用，不会阻塞
            orderService.createOrder(null);

            // 获取异步调用结果
            // CompletableFuture<Long> future = RpcContext.getContext().getCompletableFuture();
            // future.whenComplete((result, exception) -> {
            //     if (exception != null) {
            //         // 处理异常
            //     } else {
            //         // 处理结果
            //     }
            // });
        }
    }

    /**
     * 泛化调用注解示例
     */
    public static class GenericCallExample {

        @DubboReference(
            interfaceName = "com.architecture.business.user.UserService",
            version = "1.0.0",
            generic = "true"
        )
        private GenericService genericService;

        public void genericCall() {
            // 泛化调用，不需要依赖服务接口
            // Object result = genericService.$invoke(
            //     "getUserById",
            //     new String[]{"java.lang.Long"},
            //     new Object[]{1L}
            // );
        }
    }

    /**
     * 服务分组注解示例
     */
    public static class GroupExample {

        // 指定服务分组
        @DubboReference(version = "1.0.0", group = "group1")
        private UserService userServiceGroup1;

        @DubboReference(version = "1.0.0", group = "group2")
        private UserService userServiceGroup2;

        // 分组聚合（调用所有分组的实现并合并结果）
        @DubboReference(version = "1.0.0", group = "*", merger = "true")
        private UserService userServiceMerge;
    }

    /**
     * 多版本注解示例
     */
    public static class VersionExample {

        @DubboReference(version = "1.0.0")
        private UserService userServiceV1;

        @DubboReference(version = "2.0.0")
        private UserService userServiceV2;
    }

    /**
     * 直连提供者注解示例（开发测试用）
     */
    public static class DirectCallExample {

        @DubboReference(
            version = "1.0.0",
            url = "dubbo://192.168.1.100:20880"
        )
        private UserService userService;
    }

    // 服务接口（占位）
    interface UserService {
        Object getUserById(Long userId);
        Long createUser(Object user);
        String login(String username, String password);
    }

    interface OrderService {
        Long createOrder(Object order);
        Object payOrder(Long orderId, String method);
    }

    interface GenericService {
        // Dubbo 提供的泛化接口
    }
}

/**
 * 注解配置说明：
 *
 * 1. @DubboService (服务提供者)
 *    - 标注在服务实现类上
 *    - 替代 XML 的 <dubbo:service>
 *    - 支持所有 dubbo:service 的属性
 *
 * 2. @DubboReference (服务消费者)
 *    - 标注在服务引用字段上
 *    - 替代 XML 的 <dubbo:reference>
 *    - 支持所有 dubbo:reference 的属性
 *
 * 3. @Method (方法级配置)
 *    - 配置具体方法的超时、重试等
 *    - 优先级高于接口级配置
 *
 * 4. 启用注解扫描：
 *    Spring Boot:
 *    @SpringBootApplication
 *    @EnableDubbo(scanBasePackages = "com.architecture")
 *    public class Application { }
 *
 *    Spring:
 *    <dubbo:annotation package="com.architecture"/>
 *
 * 5. 注解配置优先级：
 *    方法级 > 接口级 > 全局配置
 *    消费者配置 > 提供者配置
 *
 * 6. 常用属性：
 *    - version: 服务版本，用于版本管理
 *    - group: 服务分组，用于环境隔离
 *    - timeout: 超时时间，根据业务设置
 *    - retries: 重试次数，非幂等操作设为0
 *    - loadbalance: 负载均衡策略
 *    - cluster: 集群容错模式
 *    - check: 启动时检查，开发环境可设为false
 *
 * 7. 高级特性：
 *    - async: 异步调用
 *    - generic: 泛化调用
 *    - mock: 服务降级
 *    - cache: 结果缓存
 *    - validation: 参数校验
 *    - stub: 本地存根
 */
