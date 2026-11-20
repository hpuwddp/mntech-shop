package com.mntech.shop.inventory.vo;

import lombok.Data;

/**
 * 扣减库存请求
 */
@Data
public class DeductStockRequest {
    private String productId;

    private String skuId;

    private Integer quantity;

    private String orderNo;
}

