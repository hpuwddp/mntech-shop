package com.mntech.shop.order.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ProductResponse {
    private String id;

    private String spuCode;


    private String name;


    private String subtitle;


    private String description;


    private String brandId;


    private String brandName;


    private String categoryId;


    private String categoryName;


    private BigDecimal originalPrice;


    private BigDecimal sellingPrice;


    private BigDecimal currentPrice;

    private BigDecimal memberPrice;


    private BigDecimal promotionPrice;


    private LocalDateTime promotionStart;


    private LocalDateTime promotionEnd;


    private Integer totalStock;


    private Integer availableStock;


    private Integer lockedStock;


    private Integer soldStock;


    private String status;


    private String statusDesc;


    private String auditStatus;


    private String auditStatusDesc;


    private String auditRemark;


    private String auditorId;


    private LocalDateTime auditTime;


    private Boolean hasSku;


    private Map<String, List<String>> skuTemplate;


    private List<SkuResponse> skus;


    private BigDecimal weight;


    private BigDecimal volume;


    //private String shippingTemplateId;


    private String deliveryTime;


    private String mainImage;


    private List<String> images;


    private String videoUrl;


    private List<String> tags;


    private Integer salesCount;


    private Integer viewCount;


    private Integer favoriteCount;


    private Integer reviewCount;


    private BigDecimal rating;


    private String createdBy;


    private LocalDateTime createdAt;


    private String updatedBy;

    private LocalDateTime updatedAt;


    private Integer version;
}
