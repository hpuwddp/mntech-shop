package com.mntech.shop.order.dto;

import lombok.Data;

@Data
public class SeckillOrderStats {
    private Integer totalOrders;
    private Integer pendingOrders;
    private Integer paidOrders;
    private Integer cancelledOrders;
    private Integer totalSales;
}