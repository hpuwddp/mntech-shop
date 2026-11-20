package com.mntech.shop.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CouponResponse {
    private String id;
    private String name;
    private String description;
    private String type;        // cash-满减, discount-折扣
    private BigDecimal value;   // 满减金额
    private BigDecimal discount; // 折扣值
    private BigDecimal minAmount; // 使用门槛
    private BigDecimal maxDiscount; // 最大折扣金额
private String minAmountText;   // 门槛文案
    private String applicableScope; // 适用范围 all-全场, category-指定分类, product-指定商品
    private String applicableIds;   // 适用分类/商品ID（逗号分隔）
    private String status;          // UNUSED-未使用, USED-已使用, EXPIRED-已过期
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime usedTime;
    private String couponCode;

    // 前端展示字段
    private String displayValue;    // 展示金额/折扣
    private String displayCondition; // 展示条件
    private String displayTime;     // 展示时间
private String color;           // 颜色
    private Boolean isExpired;      // 是否已过期
    private Boolean received;       // 当前用户是否已领取（领券中心用）
}
