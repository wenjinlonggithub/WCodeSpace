package com.architecture.function.core;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 函数注册器（模拟实现）
 *
 * 模拟 Spring Cloud Function 的自动注册机制
 * 实现 BeanPostProcessor 接口，在 Bean 初始化后自动注册函数
 *
 * 核心原理：
 * 1. Spring 容器初始化 Bean 时，会调用所有 BeanPostProcessor 的方法
 * 2. 在 postProcessAfterInitialization 方法中检查 Bean 类型
 * 3. 如果是 Function/Supplier/Consumer 类型，则自动注册到 FunctionCatalog
 *
 * 执行流程：
 * Spring 容器启动 → 创建 Bean → BeanPostProcessor.postProcessAfterInitialization
 * → 检查类型 → 注册到 FunctionCatalog
 */
@Component
public class FunctionRegistrar implements BeanPostProcessor {

    private final SimpleFunctionCatalog functionCatalog;

    public FunctionRegistrar(SimpleFunctionCatalog functionCatalog) {
        this.functionCatalog = functionCatalog;
    }

    /**
     * Bean 初始化后的处理
     *
     * Spring 会在每个 Bean 初始化完成后调用此方法
     * 这是自动注册函数的关键入口
     *
     * @param bean Bean 实例
     * @param beanName Bean 名称
     * @return Bean 实例（不做修改，原样返回）
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("[FunctionRegistrar] 检查 Bean: " + beanName + " (" + bean.getClass().getSimpleName() + ")");

        // 检查是否是函数式接口类型
        if (bean instanceof Function) {
            registerFunctionBean(beanName, (Function<?, ?>) bean);
        } else if (bean instanceof Supplier) {
            registerSupplierBean(beanName, (Supplier<?>) bean);
        } else if (bean instanceof Consumer) {
            registerConsumerBean(beanName, (Consumer<?>) bean);
        }

        return bean;
    }

    /**
     * 注册 Function 类型的 Bean
     */
    private void registerFunctionBean(String beanName, Function<?, ?> function) {
        System.out.println("[FunctionRegistrar] ✓ 发现 Function Bean: " + beanName);
        functionCatalog.registerFunction(beanName, function);
    }

    /**
     * 注册 Supplier 类型的 Bean
     */
    private void registerSupplierBean(String beanName, Supplier<?> supplier) {
        System.out.println("[FunctionRegistrar] ✓ 发现 Supplier Bean: " + beanName);
        functionCatalog.registerSupplier(beanName, supplier);
    }

    /**
     * 注册 Consumer 类型的 Bean
     */
    private void registerConsumerBean(String beanName, Consumer<?> consumer) {
        System.out.println("[FunctionRegistrar] ✓ 发现 Consumer Bean: " + beanName);
        functionCatalog.registerConsumer(beanName, consumer);
    }
}
