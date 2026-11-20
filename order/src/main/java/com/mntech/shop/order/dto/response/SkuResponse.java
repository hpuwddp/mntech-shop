package com.mntech.shop.order.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class SkuResponse {
    private String id;

    private String productId;

    private String skuCode;

    private String skuName;

    private Map<String, List<String>> attributes;

    private String attributeHash;

    private BigDecimal originalPrice;

    private BigDecimal sellingPrice;

    private BigDecimal currentPrice;

    private BigDecimal promotionPrice;

    private LocalDateTime promotionStart;

    private LocalDateTime promotionEnd;

    private Integer totalStock;

    private Integer availableStock;

    private Integer lockedStock;

    private Integer soldStock;

    private BigDecimal weight;

    private BigDecimal volume;
    private String imageUrl;

    private String status;

    private String statusDesc;

    private Integer salesCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private Integer version;
}
