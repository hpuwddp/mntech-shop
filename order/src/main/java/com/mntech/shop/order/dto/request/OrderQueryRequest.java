package com.mntech.shop.order.dto.request;

import lombok.Data;

@Data
public class OrderQueryRequest {
    private Integer page = 1;        // 当前页码，默认1
    private Integer size = 20;       // 每页条数，默认20
    private String userId;
    private String status;           // 订单状态筛选
}
