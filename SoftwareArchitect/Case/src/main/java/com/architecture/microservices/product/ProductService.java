package com.architecture.microservices.product;

import com.architecture.microservices.infrastructure.ServiceInstance;
import com.architecture.microservices.infrastructure.ServiceRegistry;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 产品服务
 *
 * 职责:
 * 1. 产品管理
 * 2. 库存管理
 * 3. 产品信息查询
 */
public class ProductService {

    private static ProductService instance;
    private final ServiceRegistry registry;
    private final Map<String, Product> products = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);
    private ServiceInstance serviceInstance;

    public ProductService(ServiceRegistry registry) {
        this.registry = registry;
        instance = this;
    }

    public static ProductService getInstance() {
        return instance;
    }

    /**
     * 启动服务并注册到注册中心
     */
    public void start() {
        serviceInstance = new ServiceInstance(
            "product-service-001",
            "product-service",
            "localhost",
            8082
        );

        registry.register("product-service", serviceInstance);
        System.out.println("✓ 产品服务已启动: " + serviceInstance.getUrl());
    }

    /**
     * 停止服务并从注册中心注销
     */
    public void stop() {
        if (serviceInstance != null) {
            registry.deregister("product-service", serviceInstance.getInstanceId());
            System.out.println("✓ 产品服务已停止");
        }
    }

    /**
     * 创建产品
     */
    public String createProduct(String requestBody) {
        System.out.println("  [ProductService] 处理创建产品请求");

        String productId = "P" + String.format("%03d", idGenerator.getAndIncrement());
        String name = extractValue(requestBody, "name");
        String priceStr = extractValue(requestBody, "price");
        BigDecimal price = new BigDecimal(priceStr);

        Product product = new Product(productId, name, price, 100); // 默认库存100
        products.put(productId, product);

        System.out.println("  [ProductService] 产品创建成功: " + productId);
        return String.format("{\"productId\":\"%s\",\"name\":\"%s\",\"price\":%s,\"stock\":%d}",
                           productId, name, price, 100);
    }

    /**
     * 查询产品
     */
    public String getProduct(String productId) {
        System.out.println("  [ProductService] 查询产品: " + productId);

        Product product = products.get(productId);
        if (product == null) {
            return "{\"error\":\"产品不存在\"}";
        }

        return String.format("{\"productId\":\"%s\",\"name\":\"%s\",\"price\":%s,\"stock\":%d}",
                           product.getId(), product.getName(), product.getPrice(), product.getStock());
    }

    /**
     * 检查库存(供其他服务调用)
     */
    public boolean checkStock(String productId, int quantity) {
        System.out.println("  [ProductService] 检查库存: " + productId + ", 数量: " + quantity);
        Product product = products.get(productId);
        return product != null && product.getStock() >= quantity;
    }

    /**
     * 扣减库存(供其他服务调用)
     */
    public void reduceStock(String productId, int quantity) {
        System.out.println("  [ProductService] 扣减库存: " + productId + ", 数量: " + quantity);
        Product product = products.get(productId);
        if (product != null) {
            product.setStock(product.getStock() - quantity);
        }
    }

    /**
     * 获取产品价格(供其他服务调用)
     */
    public BigDecimal getPrice(String productId) {
        Product product = products.get(productId);
        return product != null ? product.getPrice() : BigDecimal.ZERO;
    }

    private String extractValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start == -1) {
            // 尝试不带引号的值(数字)
            searchKey = "\"" + key + "\":";
            start = json.indexOf(searchKey);
            if (start == -1) return "";
            start += searchKey.length();
            int end = Math.min(
                json.indexOf(",", start) == -1 ? json.length() : json.indexOf(",", start),
                json.indexOf("}", start) == -1 ? json.length() : json.indexOf("}", start)
            );
            return json.substring(start, end).trim();
        }
        start += searchKey.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    /**
     * 产品实体
     */
    private static class Product {
        private final String id;
        private final String name;
        private final BigDecimal price;
        private int stock;

        public Product(String id, String name, BigDecimal price, int stock) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.stock = stock;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public int getStock() {
            return stock;
        }

        public void setStock(int stock) {
            this.stock = stock;
        }
    }
}
