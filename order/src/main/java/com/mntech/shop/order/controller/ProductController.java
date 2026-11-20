package com.mntech.shop.order.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mntech.shop.order.dto.request.*;
import com.mntech.shop.order.dto.response.ProductResponse;
import com.mntech.shop.order.dto.response.SkuResponse;
import com.mntech.shop.order.service.IProductService;
import com.mntech.shop.pojo.dto.response.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final IProductService productService;

    /**
     * 创建产品
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) throws JsonProcessingException {
        request.setCreatedBy("system"); // 实际从SecurityContext获取
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 更新产品
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable String id,
                                                         @Valid @RequestBody ProductUpdateRequest request) throws JsonProcessingException {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除产品
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id, "system");
        return ResponseEntity.noContent().build();
    }

    /**
     * 查询产品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String id) {
        ProductResponse response = productService.getProduct(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据分类查询产品（含SKU）
     * POST /api/products/by-category
     */
    @PostMapping("/by-category")
    public ResponseEntity<PageResult<ProductResponse>> getProductsByCategory(
            @Valid @RequestBody ProductQueryRequest request) {
        PageResult<ProductResponse> result = productService.getProductsByCategory(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 分页查询产品
     */
    @PostMapping("/query")
    public ResponseEntity<PageResult<ProductResponse>> queryProducts(@RequestBody ProductQueryRequest request) {
        PageResult<ProductResponse> result = productService.queryProducts(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询上架产品
     */
    @GetMapping("/on-shelf")
    public ResponseEntity<List<ProductResponse>> getOnShelfProducts() {
        List<ProductResponse> products = productService.getOnShelfProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * 查询促销产品
     */
    @GetMapping("/promotion")
    public ResponseEntity<List<ProductResponse>> getPromotionProducts() {
        List<ProductResponse> products = productService.getPromotionProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * 提交审核
     */
    @PostMapping("/{id}/submit-audit")
    public ResponseEntity<ProductResponse> submitForAudit(@PathVariable String id) {
        ProductResponse response = productService.submitForAudit(id, "system");
        return ResponseEntity.ok(response);
    }

    /**
     * 审核通过
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<ProductResponse> approveProduct(@PathVariable String id, String remark) {
        ProductResponse response = productService.approveProduct(id, "system", remark);
        return ResponseEntity.ok(response);
    }

    /**
     * 审核驳回
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<ProductResponse> rejectProduct(@PathVariable String id, @RequestParam String remark) {
        ProductResponse response = productService.rejectProduct(id, "system", remark);
        return ResponseEntity.ok(response);
    }

    /**
     * 上架
     */
    @PostMapping("/{id}/on-shelf")
    public ResponseEntity<ProductResponse> putOnShelf(@PathVariable String id) {
        ProductResponse response = productService.putOnShelf(id, "system");
        return ResponseEntity.ok(response);
    }

    /**
     * 下架
     */
    @PostMapping("/{id}/off-shelf")
    public ResponseEntity<ProductResponse> putOffShelf(@PathVariable String id) {
        ProductResponse response = productService.putOffShelf(id, "system");
        return ResponseEntity.ok(response);
    }

    /**
     * 扣减库存
     */
    @PostMapping("/reduce-stock")
    public ResponseEntity<ProductResponse> reduceStock(
            @Valid @RequestBody ReduceStockRequest request) {
        ProductResponse response = productService.reduceStock(request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(response);
    }

    /**
     * 增加库存
     */
    @PostMapping("/{id}/increase-stock")
    public ResponseEntity<ProductResponse> increaseStock(@PathVariable String id, @RequestParam Integer quantity) {
        ProductResponse response = productService.increaseStock(id, quantity);
        return ResponseEntity.ok(response);
    }

    /**
     * 添加SKU
     */
    @PostMapping("/skus")
    public ResponseEntity<SkuResponse> addSku(@Valid @RequestBody SkuCreateRequest request) throws JsonProcessingException {
        SkuResponse response = productService.addSku(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 删除SKU
     */
    @DeleteMapping("/{productId}/skus/{skuId}")
    public ResponseEntity<Void> deleteSku(@PathVariable String productId, @PathVariable String skuId) {
        productService.deleteSku(productId, skuId);
        return ResponseEntity.noContent().build();
    }
}