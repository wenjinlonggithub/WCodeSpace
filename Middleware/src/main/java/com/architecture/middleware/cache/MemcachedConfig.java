package com.architecture.middleware.cache;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.utils.AddrUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemcachedConfig {

    @Bean
    public MemcachedClient memcachedClient() throws Exception {
        MemcachedClientBuilder builder = new XMemcachedClientBuilder(
            AddrUtil.getAddresses("localhost:11211")
        );
        builder.setConnectionPoolSize(10);
        return builder.build();
    }
}
