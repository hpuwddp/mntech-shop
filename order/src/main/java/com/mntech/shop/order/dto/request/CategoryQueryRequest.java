package com.mntech.shop.order.dto.request;

import lombok.Data;

@Data
public class CategoryQueryRequest {
    private String name;

    private String code;


    private String status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
