package com.architecture.batchprocess.repository;

import com.architecture.batchprocess.model.DataRecord;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据访问层（模拟）
 * 实际项目中应该使用JPA或MyBatis
 */
public class DataRepository {

    // 模拟数据库存储
    private final Map<Long, DataRecord> dataStore = new ConcurrentHashMap<>();

    /**
     * 初始化测试数据
     */
    public void initTestData(int count) {
        System.out.println("初始化 " + count + " 条测试数据...");
        for (long i = 1; i <= count; i++) {
            DataRecord record = new DataRecord(
                i,
                "BIZ_" + i,
                new BigDecimal("100.00")
            );
            dataStore.put(i, record);
        }
        System.out.println("测试数据初始化完成");
    }

    /**
     * 分页查询：ID大于指定值
     */
    public List<DataRecord> findByIdGreaterThan(Long lastId, int pageSize) {
        return dataStore.values().stream()
            .filter(record -> record.getId() > lastId)
            .sorted((r1, r2) -> r1.getId().compareTo(r2.getId()))
            .limit(pageSize)
            .collect(Collectors.toList());
    }

    /**
     * 批量更新
     */
    public void batchUpdate(List<DataRecord> records) {
        for (DataRecord record : records) {
            record.setUpdatedAt(new Date());
            dataStore.put(record.getId(), record);
        }
    }

    /**
     * 根据ID查询
     */
    public DataRecord findById(Long id) {
        return dataStore.get(id);
    }

    /**
     * 统计总数
     */
    public long count() {
        return dataStore.size();
    }

    /**
     * 统计成功数量
     */
    public long countByStatus(String status) {
        return dataStore.values().stream()
            .filter(record -> status.equals(record.getStatus()))
            .count();
    }

    /**
     * 清空数据
     */
    public void clear() {
        dataStore.clear();
    }
}
