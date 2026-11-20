package com.mntech.shop.order.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {
    private List<T> records;         // 数据列表
    private Long total;              // 总记录数
    private Integer page;            // 当前页码
    private Integer size;            // 每页条数
    private int pages;              // 总页数
    private Boolean hasNext;         // 是否有下一页
    private Boolean hasPrev;         // 是否有上一页
}
