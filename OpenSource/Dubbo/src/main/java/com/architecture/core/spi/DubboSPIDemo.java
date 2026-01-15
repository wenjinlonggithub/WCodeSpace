package com.architecture.core.spi;

import org.apache.dubbo.common.extension.ExtensionLoader;

/**
 * Dubbo SPI 机制示例
 *
 * Dubbo SPI 是对 Java SPI 的增强，具有以下特性：
 * 1. 按需加载：可以按需加载指定的实现类，而不是一次性加载所有实现
 * 2. 支持依赖注入：实现类可以依赖其他扩展点
 * 3. 支持 AOP：可以对扩展点进行包装
 * 4. 支持自适应扩展：可以在运行时根据 URL 参数动态选择扩展实现
 *
 * 核心原理：
 * - ExtensionLoader：扩展加载器，每个扩展点对应一个 ExtensionLoader 实例
 * - @SPI：标注在接口上，表示该接口是一个扩展点
 * - @Adaptive：自适应扩展，在运行时根据 URL 参数动态选择具体实现
 */
public class DubboSPIDemo {

    /**
     * 演示 Dubbo SPI 的基本使用
     */
    public static void main(String[] args) {
        // 1. 获取 Protocol 扩展点的 ExtensionLoader
        ExtensionLoader<org.apache.dubbo.rpc.Protocol> protocolLoader =
            ExtensionLoader.getExtensionLoader(org.apache.dubbo.rpc.Protocol.class);

        // 2. 获取默认实现
        org.apache.dubbo.rpc.Protocol defaultProtocol = protocolLoader.getDefaultExtension();
        System.out.println("默认协议: " + defaultProtocol.getClass().getName());

        // 3. 按名称获取指定实现
        org.apache.dubbo.rpc.Protocol dubboProtocol = protocolLoader.getExtension("dubbo");
        System.out.println("Dubbo 协议: " + dubboProtocol.getClass().getName());

        // 4. 获取自适应扩展（运行时动态选择）
        org.apache.dubbo.rpc.Protocol adaptiveProtocol = protocolLoader.getAdaptiveExtension();
        System.out.println("自适应协议: " + adaptiveProtocol.getClass().getName());

        // 5. 获取所有已加载的扩展名
        System.out.println("\n所有支持的协议扩展:");
        protocolLoader.getSupportedExtensions().forEach(System.out::println);
    }
}

/**
 * SPI 工作流程：
 *
 * 1. 加载配置文件
 *    - META-INF/dubbo/
 *    - META-INF/dubbo/internal/
 *    - META-INF/services/
 *
 * 2. 解析配置文件
 *    格式：key=value（实现类全限定名）
 *
 * 3. 实例化
 *    - 单例模式：同一个扩展名对应的实例全局唯一
 *    - 延迟加载：只有在真正使用时才会创建实例
 *
 * 4. 依赖注入
 *    通过 setter 方法注入其他扩展点依赖
 *
 * 5. AOP 包装
 *    如果存在包装类（构造函数参数为扩展点接口），则进行包装
 */
