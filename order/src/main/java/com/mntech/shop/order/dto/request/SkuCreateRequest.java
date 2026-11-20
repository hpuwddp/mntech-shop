package com.mntech.shop.order.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class SkuCreateRequest {
    @NotBlank(message = "产品ID不能为空")
    private String productId;

    @NotBlank(message = "SKU编码不能为空")
    private String skuCode;


    private String skuName;

    @NotNull(message = "SKU属性不能为空")
    private Map<String, String> attributes;

    @NotNull(message = "销售价不能为空")
    @Positive(message = "销售价必须大于0")
    private BigDecimal sellingPrice;

    @NotNull(message = "库存不能为空")
    @PositiveOrZero(message = "库存不能为负数")
    private Integer totalStock;
}
