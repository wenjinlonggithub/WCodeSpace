package com.architecture.designpattern.strategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 策略模式核心定义
 * 
 * 策略模式（Strategy Pattern）：
 * 定义一系列算法，把它们一个个封装起来，并且使它们可相互替换。
 * 本模式使得算法可独立于使用它的客户而变化。
 * 
 * 核心组成部分：
 * 1. Strategy（抽象策略）：定义所有具体策略的公共接口
 * 2. ConcreteStrategy（具体策略）：实现抽象策略定义的接口
 * 3. Context（上下文）：持有一个策略对象的引用，定义一个接口来让策略访问它的数据
 * 
 * 适用场景：
 * - 多个类只区别在表现行为不同，可以使用策略模式，在运行时动态选择具体要执行的行为
 * - 需要在不同情况下使用不同的算法，可以使用策略模式把算法的选择和算法的实现分离开来
 * - 系统中有很多类，它们的区别仅在于它们的行为，那么使用策略模式可以动态地让一个对象在许多行为中选择一种行为
 * - 一个系统需要动态地在几种算法中选择一种时，可将每个算法封装到策略类中
 */

/**
 * 基础策略接口
 * @param <T> 输入参数类型
 * @param <R> 返回结果类型
 */
@FunctionalInterface
interface Strategy<T, R> {
    /**
     * 执行策略算法
     * @param input 输入参数
     * @return 执行结果
     */
    R execute(T input);
    
    /**
     * 策略名称，默认返回类名
     * @return 策略名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * 策略描述
     * @return 策略描述
     */
    default String getDescription() {
        return "策略：" + getName();
    }
}

/**
 * 抽象策略基类，提供一些通用功能
 * @param <T> 输入参数类型
 * @param <R> 返回结果类型
 */
abstract class AbstractStrategy<T, R> implements Strategy<T, R> {
    
    private final String name;
    private final String description;
    
    protected AbstractStrategy(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    /**
     * 前置处理，子类可重写
     */
    protected void beforeExecute(T input) {
        // 默认空实现
    }
    
    /**
     * 后置处理，子类可重写
     */
    protected void afterExecute(T input, R result) {
        // 默认空实现
    }
    
    @Override
    public final R execute(T input) {
        beforeExecute(input);
        R result = doExecute(input);
        afterExecute(input, result);
        return result;
    }
    
    /**
     * 具体的策略执行逻辑，由子类实现
     */
    protected abstract R doExecute(T input);
}

/**
 * 策略上下文类
 * @param <T> 输入参数类型
 * @param <R> 返回结果类型
 */
class StrategyContext<T, R> {
    
    private Strategy<T, R> strategy;
    
    public StrategyContext(Strategy<T, R> strategy) {
        this.strategy = strategy;
    }
    
    /**
     * 设置策略
     */
    public void setStrategy(Strategy<T, R> strategy) {
        this.strategy = strategy;
    }
    
    /**
     * 获取当前策略
     */
    public Strategy<T, R> getStrategy() {
        return strategy;
    }
    
    /**
     * 执行策略
     */
    public R execute(T input) {
        if (strategy == null) {
            throw new IllegalStateException("策略未设置");
        }
        return strategy.execute(input);
    }
}

/**
 * 策略工厂和注册管理器
 * @param <T> 输入参数类型
 * @param <R> 返回结果类型
 */
class StrategyRegistry<T, R> {
    
    private final Map<String, Strategy<T, R>> strategies = new ConcurrentHashMap<>();
    private Strategy<T, R> defaultStrategy;
    
    /**
     * 注册策略
     */
    public void register(String name, Strategy<T, R> strategy) {
        strategies.put(name, strategy);
    }
    
    /**
     * 注册策略（使用策略自身的名称）
     */
    public void register(Strategy<T, R> strategy) {
        register(strategy.getName(), strategy);
    }
    
    /**
     * 获取策略
     */
    public Strategy<T, R> get(String name) {
        return strategies.get(name);
    }
    
    /**
     * 移除策略
     */
    public void remove(String name) {
        strategies.remove(name);
    }
    
    /**
     * 设置默认策略
     */
    public void setDefaultStrategy(Strategy<T, R> strategy) {
        this.defaultStrategy = strategy;
    }
    
    /**
     * 获取策略，如果不存在则返回默认策略
     */
    public Strategy<T, R> getOrDefault(String name) {
        return strategies.getOrDefault(name, defaultStrategy);
    }
    
    /**
     * 检查策略是否存在
     */
    public boolean contains(String name) {
        return strategies.containsKey(name);
    }
    
    /**
     * 获取所有策略名称
     */
    public java.util.Set<String> getAllNames() {
        return strategies.keySet();
    }
    
    /**
     * 清空所有策略
     */
    public void clear() {
        strategies.clear();
    }
}

/**
 * 链式策略执行器 - 支持多个策略按顺序执行
 * @param <T> 输入参数类型
 */
class StrategyChain<T> {
    
    private final java.util.List<Strategy<T, T>> strategies = new java.util.ArrayList<>();
    
    /**
     * 添加策略到链中
     */
    public StrategyChain<T> addStrategy(Strategy<T, T> strategy) {
        strategies.add(strategy);
        return this;
    }
    
    /**
     * 执行策略链
     */
    public T execute(T input) {
        T result = input;
        for (Strategy<T, T> strategy : strategies) {
            result = strategy.execute(result);
        }
        return result;
    }
    
    /**
     * 获取策略链长度
     */
    public int size() {
        return strategies.size();
    }
    
    /**
     * 清空策略链
     */
    public void clear() {
        strategies.clear();
    }
}

/**
 * 条件策略选择器 - 根据条件动态选择策略
 * @param <T> 输入参数类型
 * @param <R> 返回结果类型
 */
class ConditionalStrategySelector<T, R> {
    
    private final java.util.List<StrategyRule<T, R>> rules = new java.util.ArrayList<>();
    private Strategy<T, R> defaultStrategy;
    
    /**
     * 策略规则内部类
     */
    private static class StrategyRule<T, R> {
        private final Function<T, Boolean> condition;
        private final Strategy<T, R> strategy;
        
        public StrategyRule(Function<T, Boolean> condition, Strategy<T, R> strategy) {
            this.condition = condition;
            this.strategy = strategy;
        }
        
        public boolean matches(T input) {
            return condition.apply(input);
        }
        
        public Strategy<T, R> getStrategy() {
            return strategy;
        }
    }
    
    /**
     * 添加条件策略
     */
    public ConditionalStrategySelector<T, R> when(Function<T, Boolean> condition, Strategy<T, R> strategy) {
        rules.add(new StrategyRule<>(condition, strategy));
        return this;
    }
    
    /**
     * 设置默认策略
     */
    public ConditionalStrategySelector<T, R> otherwise(Strategy<T, R> strategy) {
        this.defaultStrategy = strategy;
        return this;
    }
    
    /**
     * 根据条件选择并执行策略
     */
    public R execute(T input) {
        for (StrategyRule<T, R> rule : rules) {
            if (rule.matches(input)) {
                return rule.getStrategy().execute(input);
            }
        }
        
        if (defaultStrategy != null) {
            return defaultStrategy.execute(input);
        }
        
        throw new IllegalStateException("没有找到匹配的策略，且未设置默认策略");
    }
}