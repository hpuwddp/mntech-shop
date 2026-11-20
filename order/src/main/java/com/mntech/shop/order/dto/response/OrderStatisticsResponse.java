package com.mntech.shop.order.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatisticsResponse {
    private Integer all;        // 全部订单
    private Integer pending;    // 待付款
    private Integer paid;       // 已支付（待发货）
    private Integer shipped;    // 待收货
    private Integer completed;  // 已完成（待评价）
    private Integer cancelled;  // 已取消
    private Integer refunded;   // 已退款
}
