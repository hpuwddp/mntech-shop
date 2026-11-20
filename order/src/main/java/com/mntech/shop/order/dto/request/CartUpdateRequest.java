package com.mntech.shop.order.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CartUpdateRequest {
    private String userId;

    private List<CartItemUpdate> items;

    @Data
    public static class CartItemUpdate {
        private String productId;
        private String skuId;
        private Integer quantity;
        private Boolean selected;
    }
}
