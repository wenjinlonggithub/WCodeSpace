package com.architecture.function.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 核心组件配置类
 *
 * 配置 Spring Cloud Function 的核心组件
 * 确保各组件按正确的顺序初始化
 */
@Configuration
public class FunctionCoreConfiguration {

    /**
     * 创建 FunctionCatalog Bean
     *
     * 这是最核心的组件，必须最先创建
     * 其他组件都依赖于它
     */
    @Bean
    public SimpleFunctionCatalog functionCatalog() {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║     初始化 Spring Cloud Function 核心组件              ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println("[Configuration] 创建 FunctionCatalog Bean");
        return new SimpleFunctionCatalog();
    }

    /**
     * 创建 FunctionInvoker Bean
     *
     * 函数执行引擎，依赖于 FunctionCatalog
     */
    @Bean
    public FunctionInvoker functionInvoker(SimpleFunctionCatalog functionCatalog) {
        System.out.println("[Configuration] 创建 FunctionInvoker Bean");
        return new FunctionInvoker(functionCatalog);
    }

    /**
     * 创建 FunctionRegistrar Bean
     *
     * 自动注册器，依赖于 FunctionCatalog
     * 实现了 BeanPostProcessor，会在所有 Bean 初始化后自动执行
     */
    @Bean
    public FunctionRegistrar functionRegistrar(SimpleFunctionCatalog functionCatalog) {
        System.out.println("[Configuration] 创建 FunctionRegistrar Bean (BeanPostProcessor)");
        System.out.println("[Configuration] FunctionRegistrar 将自动扫描并注册所有函数 Bean");
        return new FunctionRegistrar(functionCatalog);
    }

    /**
     * 创建 FunctionController Bean
     *
     * Web 适配器，依赖于 FunctionInvoker
     * 提供 HTTP 端点来调用函数
     */
    @Bean
    public FunctionController functionController(FunctionInvoker functionInvoker) {
        System.out.println("[Configuration] 创建 FunctionController Bean");
        System.out.println("[Configuration] HTTP 端点已准备就绪");
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║     Spring Cloud Function 核心组件初始化完成           ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
        return new FunctionController(functionInvoker);
    }
}
