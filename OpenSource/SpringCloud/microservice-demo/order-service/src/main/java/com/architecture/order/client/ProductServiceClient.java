package com.architecture.order.client;

import com.architecture.order.dto.ProductDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 商品服务Feign客户端
 *
 * 熔断器原理:
 * 1. 关闭状态(CLOSED): 正常调用服务
 * 2. 打开状态(OPEN): 失败率超过阈值，所有请求直接降级
 * 3. 半开状态(HALF_OPEN): 尝试放行部分请求，检测服务是否恢复
 *
 * 配置参数:
 * - failure-rate-threshold: 失败率阈值(默认50%)
 * - wait-duration-in-open-state: 熔断器打开后等待时间
 * - sliding-window-size: 滑动窗口大小
 *
 * @author architecture
 */
@FeignClient(name = "product-service")
public interface ProductServiceClient {

    /**
     * 查询商品详情
     *
     * 熔断器配置:
     * - 名称: productService
     * - 降级方法: getProductFallback
     *
     * 熔断触发场景:
     * 1. 商品服务宕机
     * 2. 响应超时(超过readTimeout)
     * 3. 网络异常
     *
     * @param productId 商品ID
     * @return 商品信息
     */
    @GetMapping("/products/{id}")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    ProductDTO getProduct(@PathVariable("id") Long productId);

    /**
     * 降级方法
     *
     * 注意事项:
     * 1. 方法签名必须和原方法一致
     * 2. 需要额外增加一个Throwable参数
     *
     * @param productId 商品ID
     * @param throwable 异常信息
     * @return 默认商品信息
     */
    default ProductDTO getProductFallback(Long productId, Throwable throwable) {
        System.out.println("商品服务调用失败: " + throwable.getMessage());

        // 返回默认商品信息
        ProductDTO product = new ProductDTO();
        product.setId(productId);
        product.setName("商品服务暂时不可用");
        product.setPrice(java.math.BigDecimal.ZERO);
        return product;
    }

    /**
     * 批量查询商品
     *
     * 性能优化:
     * - 避免循环调用getProduct
     * - 一次请求获取多个商品信息
     * - 减少网络开销
     *
     * @param productIds 商品ID列表
     * @return 商品列表
     */
    @GetMapping("/products/batch")
    java.util.List<ProductDTO> batchGetProducts(
        @org.springframework.web.bind.annotation.RequestParam("ids") java.util.List<Long> productIds
    );
}
