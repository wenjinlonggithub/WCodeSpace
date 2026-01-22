package com.architecture.seata.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;

/**
 * 账户服务Feign客户端
 */
@FeignClient(name = "account-service", path = "/account")
public interface AccountClient {

    /**
     * 扣减账户余额
     */
    @PostMapping("/deduct")
    void deduct(@RequestParam("userId") Long userId,
                @RequestParam("money") BigDecimal money);
}
