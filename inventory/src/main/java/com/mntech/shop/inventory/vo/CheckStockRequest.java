package com.mntech.shop.inventory.vo;

import lombok.Data;

/**
 * 检查库存请求
 */
@Data
public class CheckStockRequest {
    private String productId;

    private String skuId;

    private Integer quantity;
}
