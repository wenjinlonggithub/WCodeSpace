package com.architecture.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 熔断降级回退控制器
 *
 * 功能说明:
 * 当后端服务不可用时,返回友好的降级响应
 * 避免直接返回错误信息,提升用户体验
 *
 * 降级策略:
 * 1. 返回缓存数据(如果有)
 * 2. 返回默认值
 * 3. 返回友好提示信息
 *
 * @author Architecture Team
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> userFallback() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", HttpStatus.SERVICE_UNAVAILABLE.value());
        result.put("message", "用户服务暂时不可用,请稍后重试");
        result.put("data", null);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
    }

    @GetMapping("/order")
    public ResponseEntity<Map<String, Object>> orderFallback() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", HttpStatus.SERVICE_UNAVAILABLE.value());
        result.put("message", "订单服务暂时不可用,请稍后重试");
        result.put("data", null);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
    }

    @GetMapping("/{service}")
    public ResponseEntity<Map<String, Object>> defaultFallback(@PathVariable String service) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", HttpStatus.SERVICE_UNAVAILABLE.value());
        result.put("message", service + "服务暂时不可用,请稍后重试");
        result.put("data", null);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
    }
}
