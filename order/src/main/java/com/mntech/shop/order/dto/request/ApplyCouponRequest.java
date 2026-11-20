package com.mntech.shop.order.dto.request;

import lombok.Data;

/**
 * 给待支付订单应用/更换/清除优惠券
 * userCouponId 为空表示不使用优惠券（清除已选券）
 */
@Data
public class ApplyCouponRequest {
    private String userId;
    private String userCouponId;
}