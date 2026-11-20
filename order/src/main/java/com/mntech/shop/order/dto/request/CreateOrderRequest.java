package com.mntech.shop.order.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    private String orderNo;
    @NotEmpty(message = "订单明细不能为空")
    private List<OrderItemRequest> items;

private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private String payType = "wechat";

    /**
     * 用户优惠券ID（user_coupon.id），可空：不传表示不使用优惠券
     */
    private String userCouponId;

    @Data
    public static class OrderItemRequest {
        @NotBlank(message = "产品ID不能为空")
        private String productId;

        private String skuId;

        @NotBlank(message = "产品名称不能为空")
        private String productName;

        private String skuName;
        private String productImage;

        @NotNull(message = "数量不能为空")
        private Integer quantity;

        @NotNull(message = "单价不能为空")
        private BigDecimal price;
    }
}
