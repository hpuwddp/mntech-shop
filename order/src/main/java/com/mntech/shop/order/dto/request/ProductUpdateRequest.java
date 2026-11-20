package com.mntech.shop.order.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data

public class ProductUpdateRequest {

    private String name;


    private String subtitle;


    private String description;


    private String brandId;

    private String categoryId;


    private BigDecimal originalPrice;

    private BigDecimal sellingPrice;


    private BigDecimal memberPrice;


    private BigDecimal promotionPrice;

    private LocalDateTime promotionStart;

    private LocalDateTime promotionEnd;


    private BigDecimal weight;


    private BigDecimal volume;


    //private String shippingTemplateId;


    private String deliveryTime;


    private String mainImage;


    private List<String> images;


    private List<String> tags;
}
