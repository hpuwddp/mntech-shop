package com.mntech.shop.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyProductResponse {
    private String id;
    private String activityId;
    private String productId;
    private String skuId;
    private BigDecimal groupPrice;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer remainingStock;
    private Integer perUserLimit;
    private Integer minPeople;
    private Integer groupHours;
    private Integer salesCount;
    private String productName;
    private String imageUrl;
    private Boolean isSoldOut;
    private String groupPriceDisplay;
    private String discountDisplay;
    private Integer availableStock;
    private List<GroupBuyTeamBriefResponse> ongoingTeams;
    /** 当前用户是否已参与该团购活动（存在进行中订单且团 ONGOING，含团长与成员） */
    private Boolean participated;
}