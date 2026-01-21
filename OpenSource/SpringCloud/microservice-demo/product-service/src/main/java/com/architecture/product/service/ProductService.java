package com.architecture.product.service;

import com.architecture.product.domain.Product;
import com.architecture.product.dto.ProductDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 商品服务实现类
 *
 * 核心业务逻辑:
 * 1. 商品信息查询: 提供给订单服务调用
 * 2. 商品列表查询: 支持分页和条件查询
 * 3. 商品管理: 创建、更新、删除商品
 *
 * 设计模式:
 * - DTO模式: 分离领域对象和传输对象
 * - 缓存模式: 使用内存缓存提高查询性能
 *
 * @author Architecture Team
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    // 模拟数据库存储
    private final Map<Long, Product> productDatabase = new ConcurrentHashMap<>();

    /**
     * 初始化测试数据
     *
     * @PostConstruct注解：
     * - 在Bean初始化完成后执行
     * - 依赖注入完成后调用
     * - 适合初始化资源、加载配置等操作
     */
    @PostConstruct
    public void initData() {
        log.info("初始化商品测试数据...");

        // 商品1: iPhone 15 Pro
        Product product1 = new Product();
        product1.setId(100L);
        product1.setName("iPhone 15 Pro");
        product1.setDescription("Apple最新旗舰手机，A17 Pro芯片，钛金属边框");
        product1.setPrice(new BigDecimal("7999.00"));
        product1.setCategory("手机");
        product1.setBrand("Apple");
        product1.setImageUrl("https://example.com/iphone15pro.jpg");
        product1.setStatus(1);
        product1.setCreatedAt(LocalDateTime.now());
        product1.setUpdatedAt(LocalDateTime.now());
        productDatabase.put(product1.getId(), product1);

        // 商品2: MacBook Pro
        Product product2 = new Product();
        product2.setId(101L);
        product2.setName("MacBook Pro 14\"");
        product2.setDescription("M3 Pro芯片，14英寸Liquid视网膜XDR显示屏");
        product2.setPrice(new BigDecimal("15999.00"));
        product2.setCategory("笔记本电脑");
        product2.setBrand("Apple");
        product2.setImageUrl("https://example.com/macbookpro.jpg");
        product2.setStatus(1);
        product2.setCreatedAt(LocalDateTime.now());
        product2.setUpdatedAt(LocalDateTime.now());
        productDatabase.put(product2.getId(), product2);

        // 商品3: AirPods Pro
        Product product3 = new Product();
        product3.setId(102L);
        product3.setName("AirPods Pro (第二代)");
        product3.setDescription("主动降噪，空间音频，无线充电盒");
        product3.setPrice(new BigDecimal("1899.00"));
        product3.setCategory("耳机");
        product3.setBrand("Apple");
        product3.setImageUrl("https://example.com/airpodspro.jpg");
        product3.setStatus(1);
        product3.setCreatedAt(LocalDateTime.now());
        product3.setUpdatedAt(LocalDateTime.now());
        productDatabase.put(product3.getId(), product3);

        log.info("商品测试数据初始化完成，共{}个商品", productDatabase.size());
    }

    /**
     * 根据ID查询商品
     *
     * 调用场景:
     * - 订单服务创建订单时查询商品价格
     * - 商品详情页展示
     *
     * 性能优化:
     * - 使用ConcurrentHashMap提高并发性能
     * - 可以添加Redis缓存进一步优化
     *
     * @param id 商品ID
     * @return 商品信息
     */
    public ProductDTO getProduct(Long id) {
        log.info("查询商品, productId={}", id);

        Product product = productDatabase.get(id);
        if (product == null) {
            log.warn("商品不存在, productId={}", id);
            return null;
        }

        return convertToDTO(product);
    }

    /**
     * 查询所有商品
     *
     * @return 商品列表
     */
    public List<ProductDTO> getAllProducts() {
        log.info("查询所有商品");

        List<ProductDTO> products = new ArrayList<>();
        for (Product product : productDatabase.values()) {
            if (product.getStatus() == 1) {  // 只返回上架商品
                products.add(convertToDTO(product));
            }
        }

        log.info("查询到{}个上架商品", products.size());
        return products;
    }

    /**
     * 根据分类查询商品
     *
     * @param category 商品分类
     * @return 商品列表
     */
    public List<ProductDTO> getProductsByCategory(String category) {
        log.info("根据分类查询商品, category={}", category);

        List<ProductDTO> products = new ArrayList<>();
        for (Product product : productDatabase.values()) {
            if (product.getStatus() == 1 && category.equals(product.getCategory())) {
                products.add(convertToDTO(product));
            }
        }

        log.info("查询到{}个商品", products.size());
        return products;
    }

    /**
     * 创建商品
     *
     * @param productDTO 商品信息
     * @return 创建后的商品信息
     */
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("创建商品, name={}", productDTO.getName());

        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);
        product.setId(generateId());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        productDatabase.put(product.getId(), product);
        log.info("商品创建成功, productId={}", product.getId());

        return convertToDTO(product);
    }

    /**
     * 更新商品信息
     *
     * @param id 商品ID
     * @param productDTO 商品信息
     * @return 更新后的商品信息
     */
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("更新商品, productId={}", id);

        Product product = productDatabase.get(id);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        BeanUtils.copyProperties(productDTO, product);
        product.setId(id);
        product.setUpdatedAt(LocalDateTime.now());

        productDatabase.put(id, product);
        log.info("商品更新成功, productId={}", id);

        return convertToDTO(product);
    }

    /**
     * 删除商品(逻辑删除)
     *
     * @param id 商品ID
     */
    public void deleteProduct(Long id) {
        log.info("删除商品, productId={}", id);

        Product product = productDatabase.get(id);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        // 逻辑删除：设置状态为下架
        product.setStatus(0);
        product.setUpdatedAt(LocalDateTime.now());

        log.info("商品删除成功, productId={}", id);
    }

    /**
     * Product实体转DTO
     *
     * @param product 商品实体
     * @return 商品DTO
     */
    private ProductDTO convertToDTO(Product product) {
        if (product == null) {
            return null;
        }
        ProductDTO dto = new ProductDTO();
        BeanUtils.copyProperties(product, dto);
        return dto;
    }

    /**
     * 生成商品ID
     * 实际生产环境应使用数据库自增或分布式ID生成器
     *
     * @return 商品ID
     */
    private Long generateId() {
        return System.currentTimeMillis();
    }
}
