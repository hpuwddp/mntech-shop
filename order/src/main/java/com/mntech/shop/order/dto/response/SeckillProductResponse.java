package com.mntech.shop.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillProductResponse {
    private String id;
    private String activityId;
    private String productId;
    private String skuId;
    private String productName;
    private String skuName;
    private String imageUrl;
    private BigDecimal seckillPrice;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer remainingStock;
    private Integer perUserLimit;
    private Boolean isSoldOut;
}