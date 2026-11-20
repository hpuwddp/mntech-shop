package com.mntech.shop.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mntech.shop.order.dto.request.ProductCreateRequest;
import com.mntech.shop.order.dto.request.ProductQueryRequest;
import com.mntech.shop.order.dto.request.ProductUpdateRequest;
import com.mntech.shop.order.dto.request.SkuCreateRequest;
import com.mntech.shop.order.dto.response.ProductResponse;
import com.mntech.shop.order.dto.response.SkuResponse;
import com.mntech.shop.pojo.dto.response.PageResult;

import java.util.List;

public interface IProductService {
    /**
     * 创建产品
     */
    ProductResponse createProduct(ProductCreateRequest request) throws JsonProcessingException;

    /**
     * 更新产品
     */
    ProductResponse updateProduct(String id, ProductUpdateRequest request) throws JsonProcessingException;

    /**
     * 删除产品
     */
    void deleteProduct(String id, String operatorId);

    /**
     * 查询产品详情
     */
    ProductResponse getProduct(String id);

    /**
     * 分页查询产品
     */
    PageResult<ProductResponse> queryProducts(ProductQueryRequest request);

    /**
     * 查询上架产品
     */
    List<ProductResponse> getOnShelfProducts();

    /**
     * 查询促销产品
     */
    List<ProductResponse> getPromotionProducts();

    /**
     * 提交审核
     */
    ProductResponse submitForAudit(String id, String operatorId);

    /**
     * 审核通过
     */
    ProductResponse approveProduct(String id, String operatorId, String remark);

    /**
     * 审核驳回
     */
    ProductResponse rejectProduct(String id, String operatorId, String remark);

    /**
     * 上架
     */
    ProductResponse putOnShelf(String id, String operatorId);

    /**
     * 下架
     */
    ProductResponse putOffShelf(String id, String operatorId);

    /**
     * 扣减库存
     */
    ProductResponse reduceStock(String productId, Integer quantity);

    /**
     * 确认扣减库存(支付成功)
     */
    ProductResponse confirmReduceStock(String productId, Integer quantity);

    /**
     * 释放锁定库存(取消订单)
     */
    ProductResponse releaseLockedStock(String productId, Integer quantity);

    /**
     * 增加库存
     */
    ProductResponse increaseStock(String productId, Integer quantity);

    /**
     * 添加SKU
     */
    SkuResponse addSku(SkuCreateRequest request) throws JsonProcessingException;

    /**
     * 删除SKU
     */
    void deleteSku(String productId, String skuId);

    /**
     * 根据分类查询产品（含SKU）
     */
    PageResult<ProductResponse> getProductsByCategory(ProductQueryRequest request);
}
