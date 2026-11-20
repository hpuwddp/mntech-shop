package com.mntech.shop.order.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class SeckillBuyRequest {

    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @NotBlank(message = "秒杀商品ID不能为空")
    private String seckillProductId;

    @NotNull(message = "购买数量不能为空")
    @Positive(message = "购买数量必须大于0")
    private Integer quantity;

    @NotBlank(message = "秒杀活动ID不能为空")
    private String activityId;
}