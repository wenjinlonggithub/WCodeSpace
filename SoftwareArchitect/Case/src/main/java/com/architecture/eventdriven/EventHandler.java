package com.architecture.eventdriven;

/**
 * 事件处理器接口
 */
@FunctionalInterface
public interface EventHandler<T> {

    /**
     * 处理事件
     */
    void handle(T event);
}
