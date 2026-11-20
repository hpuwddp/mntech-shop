package com.mntech.shop.inventory.vo;

import lombok.Data;

import java.util.List;

/**
 * 批量查询库存请求
 */
@Data
public class BatchStockRequest {
    /**
     * 商品ID列表（与skuIds二选一）
     */
    private List<String> productIds;

    /**
     * SKU ID列表（与productIds二选一）
     */
    private List<String> skuIds;
}