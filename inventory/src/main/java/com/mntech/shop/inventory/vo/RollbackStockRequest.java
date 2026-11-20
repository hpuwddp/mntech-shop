package com.mntech.shop.inventory.vo;

import lombok.Data;

/**
 * 回滚库存请求
 */
@Data
public class RollbackStockRequest {
    private String productId;

    private String skuId;

    private Integer quantity;

    private String orderNo;
}