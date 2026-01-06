package com.example.springdemo.lifecycle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 缓存服务
 * 
 * 演示InitializingBean接口的使用
 */
@Slf4j
@Service
public class CacheService implements InitializingBean {
    
    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();
    private final AtomicLong hitCount = new AtomicLong(0);
    private final AtomicLong missCount = new AtomicLong(0);
    private boolean ready = false;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("🗃️  缓存服务初始化...");
        
        // 预热缓存
        cache.put("system:startup-time", System.currentTimeMillis());
        cache.put("system:version", "1.0.0");
        
        ready = true;
        log.info("✅ 缓存服务初始化完成，预热数据: {} 条", cache.size());
    }
    
    public void put(String key, Object value) {
        if (!ready) {
            throw new IllegalStateException("缓存服务未就绪");
        }
        
        cache.put(key, value);
        log.info("📥 缓存存储: {} = {}", key, value);
    }
    
    public String get(String key) {
        if (!ready) {
            throw new IllegalStateException("缓存服务未就绪");
        }
        
        Object value = cache.get(key);
        if (value != null) {
            hitCount.incrementAndGet();
            log.info("🎯 缓存命中: {} = {}", key, value);
            return value.toString();
        } else {
            missCount.incrementAndGet();
            log.info("❌ 缓存未命中: {}", key);
            return null;
        }
    }
    
    public void clear() {
        cache.clear();
        log.info("🧹 缓存已清空");
    }
    
    public String getStats() {
        return String.format(
                "缓存统计 - 总数: %d, 命中: %d, 未命中: %d, 命中率: %.2f%%",
                cache.size(),
                hitCount.get(),
                missCount.get(),
                calculateHitRate()
        );
    }
    
    private double calculateHitRate() {
        long total = hitCount.get() + missCount.get();
        return total == 0 ? 0.0 : (double) hitCount.get() / total * 100;
    }
    
    public boolean isReady() {
        return ready;
    }
}