package com.architecture.eventdriven;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件总线 - 事件驱动架构的核心
 * 负责事件的发布和订阅
 */
public class EventBus {

    // 存储事件类型和处理器的映射
    private final Map<Class<?>, List<EventHandler<?>>> handlers = new ConcurrentHashMap<>();

    /**
     * 订阅事件
     */
    public <T> void subscribe(Class<T> eventType, EventHandler<T> handler) {
        handlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
        System.out.println("[EventBus] 订阅事件: " + eventType.getSimpleName());
    }

    /**
     * 发布事件
     */
    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        System.out.println("\n[EventBus] 发布事件: " + event.getClass().getSimpleName());
        System.out.println("事件内容: " + event);

        List<EventHandler<?>> eventHandlers = handlers.get(event.getClass());
        if (eventHandlers != null) {
            for (EventHandler<?> handler : eventHandlers) {
                try {
                    ((EventHandler<T>) handler).handle(event);
                } catch (Exception e) {
                    System.err.println("[EventBus] 处理事件失败: " + e.getMessage());
                }
            }
        }
    }
}
