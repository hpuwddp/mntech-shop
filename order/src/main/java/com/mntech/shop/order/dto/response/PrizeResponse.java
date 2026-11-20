package com.mntech.shop.order.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 奖品响应DTO
 */
@Data
public class PrizeResponse {
    private String id;
    private String name;
    private String image;
    private String description;
    private Integer level;
    private String levelDesc;
    private BigDecimal probability;
    private Integer remainingQuantity;
}
