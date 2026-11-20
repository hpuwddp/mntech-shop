package com.mntech.shop.pojo.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkuStockUpdate {
    private String skuId;
    private Integer quantity;
    private Integer version;

}