package com.mntech.shop.order.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class GroupBuyCreateRequest {

    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @NotBlank(message = "团购商品ID不能为空")
    private String groupBuyProductId;

    @NotNull(message = "购买数量不能为空")
    @Positive(message = "购买数量必须大于0")
    private Integer quantity;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;
}