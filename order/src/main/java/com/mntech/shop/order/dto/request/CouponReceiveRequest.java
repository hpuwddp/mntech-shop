package com.mntech.shop.order.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CouponReceiveRequest {
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @NotBlank(message = "优惠券ID不能为空")
    private String couponId;
}
