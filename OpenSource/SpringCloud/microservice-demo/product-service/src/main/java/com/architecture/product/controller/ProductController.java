package com.architecture.product.controller;

import com.architecture.product.dto.ProductDTO;
import com.architecture.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 *
 * 提供商品相关的REST接口:
 * - GET /products/{id}: 根据ID查询商品(供Order Service调用)
 * - GET /products: 查询所有商品
 * - GET /products/category/{category}: 根据分类查询
 * - POST /products: 创建商品
 * - PUT /products/{id}: 更新商品
 * - DELETE /products/{id}: 删除商品
 *
 * REST风格设计:
 * - 使用HTTP方法表达操作: GET(查询), POST(创建), PUT(更新), DELETE(删除)
 * - 资源路径清晰: /products/{id}
 * - 统一返回格式
 *
 * @author Architecture Team
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 根据ID查询商品
     *
     * 此接口会被Order Service通过Feign调用
     * 调用链路: Order Service → Feign Client → Product Service
     *
     * @param id 商品ID
     * @return 商品信息
     */
    @GetMapping("/{id}")
    public ProductDTO getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    /**
     * 查询所有商品
     *
     * @return 商品列表
     */
    @GetMapping
    public List<ProductDTO> getAllProducts() {
        return productService.getAllProducts();
    }

    /**
     * 根据分类查询商品
     *
     * @param category 商品分类
     * @return 商品列表
     */
    @GetMapping("/category/{category}")
    public List<ProductDTO> getProductsByCategory(@PathVariable String category) {
        return productService.getProductsByCategory(category);
    }

    /**
     * 创建商品
     *
     * @param productDTO 商品信息
     * @return 创建后的商品信息
     */
    @PostMapping
    public ProductDTO createProduct(@RequestBody ProductDTO productDTO) {
        return productService.createProduct(productDTO);
    }

    /**
     * 更新商品
     *
     * @param id 商品ID
     * @param productDTO 商品信息
     * @return 更新后的商品信息
     */
    @PutMapping("/{id}")
    public ProductDTO updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        return productService.updateProduct(id, productDTO);
    }

    /**
     * 删除商品
     *
     * @param id 商品ID
     */
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}
