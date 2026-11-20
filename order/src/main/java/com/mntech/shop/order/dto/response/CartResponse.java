package com.mntech.shop.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponse {
    private List<CartItemResponse> items;
    private Integer totalCount;
    private Integer selectedCount;
    private BigDecimal selectedTotalAmount;

    @Data
    @Builder
    public static class CartItemResponse {
        private String id;
        private String productId;
        private String skuId;
        private String productName;
        private String skuName;
        private String productImage;
        private BigDecimal price;
        private Integer quantity;
        private Integer stock;
        private Boolean selected;
        private BigDecimal totalPrice;
    }
}
