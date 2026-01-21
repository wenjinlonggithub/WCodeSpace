package com.hospital.gateway.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author lvzeqiang
 * @date 2022/7/14 09:34
 * @description
 **/
@FeignClient(name = "passportFeign", url = "${pass.passport-domain}")
public interface PassportFeign {
    @GetMapping(value = "/oauth/check_token/simple?token={token}")
    String checkToken(@PathVariable("token") String token);
}
