package com.architecture.middleware.cache;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.utils.AddrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemcachedConfig {

    @Value("${memcached.servers:localhost:11211}")
    private String servers;

    @Value("${memcached.pool.size:10}")
    private int poolSize;

    @Bean
    public MemcachedClient memcachedClient() throws Exception {
        MemcachedClientBuilder builder = new XMemcachedClientBuilder(
            AddrUtil.getAddresses(servers)
        );
        builder.setConnectionPoolSize(poolSize);
        return builder.build();
    }
}
