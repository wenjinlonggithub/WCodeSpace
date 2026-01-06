package com.architecture.middleware.cache;

import net.rubyeye.xmemcached.MemcachedClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemcachedExample {

    @Autowired
    private MemcachedClient memcachedClient;

    public void setCache(String key, Object value, int expireSeconds) throws Exception {
        memcachedClient.set(key, expireSeconds, value);
        System.out.println("Memcached set: " + key + " = " + value);
    }

    public Object getCache(String key) throws Exception {
        Object value = memcachedClient.get(key);
        System.out.println("Memcached get: " + key + " = " + value);
        return value;
    }

    public void deleteCache(String key) throws Exception {
        memcachedClient.delete(key);
        System.out.println("Memcached delete: " + key);
    }

    public void increment(String key, long delta) throws Exception {
        long result = memcachedClient.incr(key, delta);
        System.out.println("Memcached increment: " + key + " result = " + result);
    }

    public void decrement(String key, long delta) throws Exception {
        long result = memcachedClient.decr(key, delta);
        System.out.println("Memcached decrement: " + key + " result = " + result);
    }
}
