package com.architecture.consistency.saga;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saga上下文
 * 在Saga执行过程中传递数据
 */
@Data
public class SagaContext {

    /**
     * Saga实例ID
     */
    private String sagaId;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 上下文数据
     */
    private Map<String, Object> data = new ConcurrentHashMap<>();

    /**
     * Saga状态
     */
    private SagaStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public SagaContext(String sagaId, String businessId) {
        this.sagaId = sagaId;
        this.businessId = businessId;
        this.status = SagaStatus.RUNNING;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 设置数据
     */
    public void put(String key, Object value) {
        data.put(key, value);
    }

    /**
     * 获取数据
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    /**
     * 获取数据（带默认值）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    /**
     * Saga状态枚举
     */
    public enum SagaStatus {
        RUNNING("运行中"),
        COMPLETED("已完成"),
        COMPENSATING("补偿中"),
        COMPENSATED("已补偿"),
        FAILED("失败");

        private final String desc;

        SagaStatus(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}
