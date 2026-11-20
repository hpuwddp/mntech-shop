package com.mntech.shop.order.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CouponUseRequest {
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @NotBlank(message = "用户优惠券ID不能为空")
    private String userCouponId;

    @NotBlank(message = "订单号不能为空")
    private String orderNo;
}
