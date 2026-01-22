package com.architecture.seata.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 库存服务Feign客户端
 */
@FeignClient(name = "storage-service", path = "/storage")
public interface StorageClient {

    /**
     * 扣减库存
     */
    @PostMapping("/deduct")
    void deduct(@RequestParam("productId") Long productId,
                @RequestParam("count") Integer count);
}
