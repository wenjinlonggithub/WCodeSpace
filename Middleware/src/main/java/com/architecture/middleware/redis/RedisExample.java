package com.architecture.middleware.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisExample {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void stringOperations() {
        stringRedisTemplate.opsForValue().set("key1", "value1");
        String value = stringRedisTemplate.opsForValue().get("key1");
        System.out.println("String value: " + value);

        stringRedisTemplate.opsForValue().set("key2", "value2", 60, TimeUnit.SECONDS);
    }

    public void hashOperations() {
        stringRedisTemplate.opsForHash().put("user:1", "name", "张三");
        stringRedisTemplate.opsForHash().put("user:1", "age", "25");
        
        Object name = stringRedisTemplate.opsForHash().get("user:1", "name");
        System.out.println("Hash name: " + name);
    }

    public void listOperations() {
        stringRedisTemplate.opsForList().rightPush("queue", "task1");
        stringRedisTemplate.opsForList().rightPush("queue", "task2");
        stringRedisTemplate.opsForList().rightPush("queue", "task3");
        
        String task = stringRedisTemplate.opsForList().leftPop("queue");
        System.out.println("List task: " + task);
    }

    public void setOperations() {
        stringRedisTemplate.opsForSet().add("tags", "java", "redis", "spring");
        
        Boolean isMember = stringRedisTemplate.opsForSet().isMember("tags", "java");
        System.out.println("Is member: " + isMember);
    }

    public void zsetOperations() {
        stringRedisTemplate.opsForZSet().add("ranking", "player1", 100);
        stringRedisTemplate.opsForZSet().add("ranking", "player2", 200);
        stringRedisTemplate.opsForZSet().add("ranking", "player3", 150);
        
        Double score = stringRedisTemplate.opsForZSet().score("ranking", "player2");
        System.out.println("ZSet score: " + score);
    }

    public void distributedLock() {
        String lockKey = "lock:resource";
        Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 10, TimeUnit.SECONDS);
        
        if (Boolean.TRUE.equals(acquired)) {
            try {
                System.out.println("Lock acquired, processing...");
            } finally {
                stringRedisTemplate.delete(lockKey);
            }
        }
    }
}
