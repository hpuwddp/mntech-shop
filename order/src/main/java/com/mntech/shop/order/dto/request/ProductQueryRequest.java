package com.mntech.shop.order.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductQueryRequest {

    private String name;


    private String brandId;


    private String categoryId;

    /**
     * 指定商品ID列表（优惠券指定商品适用），支持多个
     */
    private List<String> productIds;

    private String categoryName;

    private String status;


    private Boolean onShelf;


    private BigDecimal minPrice;


    private BigDecimal maxPrice;


    private Integer pageNum = 1;


    private Integer pageSize = 10;

    private String orderBy;

    private String orderDirection;
}
