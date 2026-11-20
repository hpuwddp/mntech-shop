package com.mntech.shop.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一订单状态
 * 包括 普通订单
 * 秒杀订单
 */
@Getter
@AllArgsConstructor
public enum  OrderStatus {
    PENDING("待支付"),
    PAID("已支付"),
    SEND("已发货"),
    CANCELLED("已取消"),
    REFUND("已退款"),
    COMPLETED("完成"),
    DESCSTOCK("待扣减库存");
    private final String description;

    public String getDescription() {
        return description;
    }
}
