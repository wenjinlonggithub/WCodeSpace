package com.architecture.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Dubbo 动态代理机制示例
 *
 * Dubbo 支持两种动态代理方式：
 * 1. JDK 动态代理：基于接口
 * 2. Javassist/ByteBuddy 字节码生成：性能更好
 *
 * 核心原理：
 * - 服务消费者通过代理对象调用远程服务
 * - 代理对象负责网络通信、序列化、负载均衡等
 * - 代理工厂根据配置选择合适的代理实现
 *
 * 工作流程：
 * 1. 创建代理对象
 * 2. 拦截方法调用
 * 3. 构建 Invocation 对象
 * 4. 执行远程调用
 * 5. 返回结果
 */
public class ProxyDemo {

    /**
     * 服务接口
     */
    public interface HelloService {
        String sayHello(String name);
        String sayGoodbye(String name);
    }

    /**
     * 实际的远程调用处理器
     */
    static class RpcInvocationHandler implements InvocationHandler {
        private String url;
        private Class<?> interfaceClass;

        public RpcInvocationHandler(Class<?> interfaceClass, String url) {
            this.interfaceClass = interfaceClass;
            this.url = url;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 处理 Object 类的方法
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }

            // 构建 RPC 调用信息
            String methodName = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] arguments = args;

            System.out.println("\n=== 执行远程调用 ===");
            System.out.println("接口: " + interfaceClass.getName());
            System.out.println("方法: " + methodName);
            System.out.println("参数: " + java.util.Arrays.toString(arguments));
            System.out.println("目标地址: " + url);

            // 1. 序列化参数
            byte[] serializedArgs = serialize(arguments);
            System.out.println("1. 序列化参数完成");

            // 2. 网络传输（模拟）
            System.out.println("2. 通过网络发送请求到: " + url);
            byte[] response = sendRequest(url, interfaceClass.getName(), methodName, serializedArgs);

            // 3. 反序列化结果
            Object result = deserialize(response, method.getReturnType());
            System.out.println("3. 反序列化结果完成");
            System.out.println("4. 返回结果: " + result);

            return result;
        }

        private byte[] serialize(Object[] args) {
            // 实际实现中会使用 Hessian、Protobuf、Kryo 等序列化框架
            return new byte[0];
        }

        private byte[] sendRequest(String url, String interfaceName, String methodName, byte[] args) {
            // 实际实现中会使用 Netty 进行网络通信
            // 模拟远程调用返回
            return ("Hello from " + url).getBytes();
        }

        private Object deserialize(byte[] data, Class<?> returnType) {
            // 实际实现中会使用对应的反序列化框架
            if (returnType == String.class) {
                return new String(data);
            }
            return null;
        }
    }

    /**
     * 代理工厂
     */
    static class ProxyFactory {
        /**
         * 创建 JDK 动态代理
         */
        @SuppressWarnings("unchecked")
        public static <T> T createJdkProxy(Class<T> interfaceClass, String url) {
            InvocationHandler handler = new RpcInvocationHandler(interfaceClass, url);

            return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                handler
            );
        }

        /**
         * 创建 Javassist 代理（简化版，实际使用字节码生成）
         */
        public static <T> T createJavassistProxy(Class<T> interfaceClass, String url) {
            // Javassist 动态生成字节码，性能比 JDK 代理更好
            // 这里简化为使用 JDK 代理
            System.out.println("使用 Javassist 代理（简化为 JDK 代理）");
            return createJdkProxy(interfaceClass, url);
        }
    }

    /**
     * Dubbo Invoker 接口模拟
     */
    interface Invoker<T> {
        Class<T> getInterface();
        Result invoke(Invocation invocation) throws Exception;
    }

    /**
     * 调用信息
     */
    static class Invocation {
        private String methodName;
        private Class<?>[] parameterTypes;
        private Object[] arguments;

        public Invocation(String methodName, Class<?>[] parameterTypes, Object[] arguments) {
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
            this.arguments = arguments;
        }

        public String getMethodName() {
            return methodName;
        }

        public Class<?>[] getParameterTypes() {
            return parameterTypes;
        }

        public Object[] getArguments() {
            return arguments;
        }
    }

    /**
     * 调用结果
     */
    static class Result {
        private Object value;
        private Throwable exception;

        public Result(Object value) {
            this.value = value;
        }

        public Result(Throwable exception) {
            this.exception = exception;
        }

        public Object getValue() {
            return value;
        }

        public boolean hasException() {
            return exception != null;
        }

        public Throwable getException() {
            return exception;
        }
    }

    /**
     * Dubbo Invoker 实现
     */
    static class DubboInvoker<T> implements Invoker<T> {
        private Class<T> interfaceClass;
        private String url;

        public DubboInvoker(Class<T> interfaceClass, String url) {
            this.interfaceClass = interfaceClass;
            this.url = url;
        }

        @Override
        public Class<T> getInterface() {
            return interfaceClass;
        }

        @Override
        public Result invoke(Invocation invocation) throws Exception {
            try {
                System.out.println("\n[DubboInvoker] 执行远程调用");
                System.out.println("[DubboInvoker] URL: " + url);
                System.out.println("[DubboInvoker] 方法: " + invocation.getMethodName());

                // 1. 构建请求
                // 2. 编码（序列化）
                // 3. 网络传输
                // 4. 解码（反序列化）
                // 5. 返回结果

                Object result = "远程调用结果: " + invocation.getArguments()[0];
                return new Result(result);
            } catch (Exception e) {
                return new Result(e);
            }
        }
    }

    /**
     * Dubbo 代理工厂实现
     */
    static class DubboProxyFactory {
        @SuppressWarnings("unchecked")
        public static <T> T getProxy(Invoker<T> invoker) {
            Class<T> interfaceClass = invoker.getInterface();

            InvocationHandler handler = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (method.getDeclaringClass() == Object.class) {
                        return method.invoke(invoker, args);
                    }

                    Invocation invocation = new Invocation(
                        method.getName(),
                        method.getParameterTypes(),
                        args
                    );

                    Result result = invoker.invoke(invocation);

                    if (result.hasException()) {
                        throw result.getException();
                    }

                    return result.getValue();
                }
            };

            return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                handler
            );
        }
    }

    public static void main(String[] args) {
        System.out.println("========== Dubbo 动态代理示例 ==========\n");

        // 方式一：简单的 JDK 代理
        System.out.println("【方式一】简单的 JDK 动态代理");
        System.out.println("----------------------------------------");
        HelloService proxy1 = ProxyFactory.createJdkProxy(
            HelloService.class,
            "dubbo://192.168.1.1:20880"
        );
        String result1 = proxy1.sayHello("Dubbo");
        System.out.println("最终结果: " + result1);

        // 方式二：Dubbo 风格的代理
        System.out.println("\n\n【方式二】Dubbo 风格的代理（Invoker + Proxy）");
        System.out.println("----------------------------------------");
        DubboInvoker<HelloService> invoker = new DubboInvoker<>(
            HelloService.class,
            "dubbo://192.168.1.2:20880"
        );
        HelloService proxy2 = DubboProxyFactory.getProxy(invoker);
        String result2 = proxy2.sayHello("World");
        System.out.println("最终结果: " + result2);

        // 代理对象的特性
        System.out.println("\n\n【代理对象的特性】");
        System.out.println("----------------------------------------");
        System.out.println("代理对象类名: " + proxy1.getClass().getName());
        System.out.println("是否实现接口: " + (proxy1 instanceof HelloService));
        System.out.println("类加载器: " + proxy1.getClass().getClassLoader());
    }
}

/**
 * Dubbo 代理机制总结：
 *
 * 1. 代理类型选择：
 *    - JDK 动态代理：适用于接口代理，JDK 自带
 *    - Javassist：字节码生成，性能更好，Dubbo 默认选择
 *    - ByteBuddy：新版本支持，性能最好
 *
 * 2. 代理流程：
 *    Consumer 调用 -> 代理对象 -> Invoker -> Filter链 -> 负载均衡
 *    -> 网络传输 -> Provider Invoker -> 实际服务实现
 *
 * 3. 关键组件：
 *    - ProxyFactory：代理工厂，负责创建代理对象
 *    - Invoker：调用器，封装了服务调用的所有信息
 *    - Invocation：调用信息，包含方法名、参数等
 *    - Result：调用结果，包含返回值或异常
 *
 * 4. 性能优化：
 *    - 使用字节码生成避免反射开销
 *    - 缓存代理对象
 *    - 异步调用支持
 *
 * 5. 扩展性：
 *    - 通过 SPI 机制支持自定义代理实现
 *    - 支持多种序列化协议
 *    - 支持自定义 Filter 进行拦截处理
 */
