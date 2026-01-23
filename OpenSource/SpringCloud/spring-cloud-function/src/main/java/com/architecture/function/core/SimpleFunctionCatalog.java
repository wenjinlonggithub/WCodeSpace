package com.architecture.function.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 简化版 FunctionCatalog 实现
 *
 * 模拟 Spring Cloud Function 的核心组件 FunctionCatalog
 * 负责管理和查找所有注册的函数
 *
 * 核心职责：
 * 1. 函数注册：存储 Function/Supplier/Consumer
 * 2. 函数查找：根据名称查找函数
 * 3. 函数组合：支持多个函数的链式调用
 */
public class SimpleFunctionCatalog {

    /**
     * 函数存储容器
     * Key: 函数名称
     * Value: 函数实例（可能是 Function/Supplier/Consumer）
     */
    private final Map<String, Object> functions = new ConcurrentHashMap<>();

    /**
     * 函数类型映射
     * Key: 函数名称
     * Value: 函数类型（FUNCTION/SUPPLIER/CONSUMER）
     */
    private final Map<String, FunctionType> functionTypes = new ConcurrentHashMap<>();

    /**
     * 注册 Function
     *
     * @param name 函数名称
     * @param function 函数实例
     */
    public <I, O> void registerFunction(String name, Function<I, O> function) {
        System.out.println("[FunctionCatalog] 注册 Function: " + name);
        functions.put(name, function);
        functionTypes.put(name, FunctionType.FUNCTION);
    }

    /**
     * 注册 Supplier
     *
     * @param name 函数名称
     * @param supplier 供应商实例
     */
    public <O> void registerSupplier(String name, Supplier<O> supplier) {
        System.out.println("[FunctionCatalog] 注册 Supplier: " + name);
        functions.put(name, supplier);
        functionTypes.put(name, FunctionType.SUPPLIER);
    }

    /**
     * 注册 Consumer
     *
     * @param name 函数名称
     * @param consumer 消费者实例
     */
    public <I> void registerConsumer(String name, Consumer<I> consumer) {
        System.out.println("[FunctionCatalog] 注册 Consumer: " + name);
        functions.put(name, consumer);
        functionTypes.put(name, FunctionType.CONSUMER);
    }

    /**
     * 查找函数
     *
     * 核心方法：根据函数名称查找对应的函数实例
     * 支持函数组合：使用 | 分隔多个函数名
     *
     * @param functionDefinition 函数定义，例如 "uppercase" 或 "uppercase|reverse"
     * @return 函数实例
     */
    @SuppressWarnings("unchecked")
    public <I, O> Function<I, O> lookup(String functionDefinition) {
        System.out.println("[FunctionCatalog] 查找函数: " + functionDefinition);

        // 检查是否包含组合符号 |
        if (functionDefinition.contains("|")) {
            return composeFunctions(functionDefinition);
        }

        // 单个函数查找
        Object function = functions.get(functionDefinition);
        if (function == null) {
            throw new IllegalArgumentException("函数不存在: " + functionDefinition);
        }

        FunctionType type = functionTypes.get(functionDefinition);
        if (type == FunctionType.FUNCTION) {
            return (Function<I, O>) function;
        } else {
            throw new IllegalArgumentException("不是 Function 类型: " + functionDefinition);
        }
    }

    /**
     * 查找 Supplier
     */
    @SuppressWarnings("unchecked")
    public <O> Supplier<O> lookupSupplier(String name) {
        System.out.println("[FunctionCatalog] 查找 Supplier: " + name);

        Object function = functions.get(name);
        if (function == null) {
            throw new IllegalArgumentException("函数不存在: " + name);
        }

        FunctionType type = functionTypes.get(name);
        if (type == FunctionType.SUPPLIER) {
            return (Supplier<O>) function;
        } else {
            throw new IllegalArgumentException("不是 Supplier 类型: " + name);
        }
    }

    /**
     * 查找 Consumer
     */
    @SuppressWarnings("unchecked")
    public <I> Consumer<I> lookupConsumer(String name) {
        System.out.println("[FunctionCatalog] 查找 Consumer: " + name);

        Object function = functions.get(name);
        if (function == null) {
            throw new IllegalArgumentException("函数不存在: " + name);
        }

        FunctionType type = functionTypes.get(name);
        if (type == FunctionType.CONSUMER) {
            return (Consumer<I>) function;
        } else {
            throw new IllegalArgumentException("不是 Consumer 类型: " + name);
        }
    }

    /**
     * 函数组合
     *
     * 核心算法：将多个函数组合成一个函数链
     * 例如：uppercase|reverse|trim
     *
     * 组合原理：
     * Function<A, B> f1 = a -> b
     * Function<B, C> f2 = b -> c
     * Function<A, C> composed = f1.andThen(f2)
     *
     * @param functionDefinition 函数定义，例如 "uppercase|reverse"
     * @return 组合后的函数
     */
    @SuppressWarnings("unchecked")
    private <I, O> Function<I, O> composeFunctions(String functionDefinition) {
        System.out.println("[FunctionCatalog] 开始组合函数: " + functionDefinition);

        String[] functionNames = functionDefinition.split("\\|");

        // 获取第一个函数作为起点
        Function<Object, Object> composedFunction =
            (Function<Object, Object>) lookup(functionNames[0].trim());

        // 逐个组合后续函数
        for (int i = 1; i < functionNames.length; i++) {
            String functionName = functionNames[i].trim();
            System.out.println("[FunctionCatalog] 组合函数: " + functionName);

            Function<Object, Object> nextFunction =
                (Function<Object, Object>) lookup(functionName);

            // 使用 andThen 方法组合函数
            composedFunction = composedFunction.andThen(nextFunction);
        }

        return (Function<I, O>) composedFunction;
    }

    /**
     * 获取所有已注册的函数名称
     */
    public Set<String> getFunctionNames() {
        return new HashSet<>(functions.keySet());
    }

    /**
     * 获取函数类型
     */
    public FunctionType getFunctionType(String name) {
        return functionTypes.get(name);
    }

    /**
     * 函数类型枚举
     */
    public enum FunctionType {
        FUNCTION,   // Function<I, O>
        SUPPLIER,   // Supplier<O>
        CONSUMER    // Consumer<I>
    }
}
