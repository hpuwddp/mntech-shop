package com.mntech.shop.order.dto.request;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CartAddRequest {
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    @NotBlank(message = "产品ID不能为空")
    private String productId;

    private String skuId;

    @NotBlank(message = "产品名称不能为空")
    private String productName;

    private String skuName;
    private String productImage;

    @NotNull(message = "价格不能为空")
    private Double price;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;

    @NotNull(message = "库存不能为空")
    private Integer stock;
}
