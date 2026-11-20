package com.mntech.shop.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
private String orderNo;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private String couponId;
    private BigDecimal payAmount;
    private String couponName;
    private String status;
    private String statusDesc;
    private LocalDateTime payTime;
    private String transactionId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String payType;
    private String remark;
    private LocalDateTime createdAt;
    private String wl;
    private String wlCode;
    private List<OrderItemResponse> items;

    @Data
    @Builder
    public static class OrderItemResponse {
        private String productId;
        private String skuId;
        private String productName;
        private String skuName;
        private String productImage;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal totalPrice;
    }
}
