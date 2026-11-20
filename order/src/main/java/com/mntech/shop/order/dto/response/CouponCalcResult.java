package com.mntech.shop.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 优惠券抵扣计算结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponCalcResult {
    /** 实际抵扣金额 */
    private BigDecimal discountAmount;
    /** 优惠券名称 */
    private String couponName;
    /** 优惠券定义ID（coupon.id） */
    private String couponId;
    /** 用户优惠券ID（user_coupon.id） */
    private String userCouponId;
}