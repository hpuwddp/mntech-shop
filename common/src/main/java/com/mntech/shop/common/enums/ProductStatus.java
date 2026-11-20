package com.mntech.shop.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStatus {
    DRAFT("草稿"),
    PENDING("待审核"),
    APPROVED("已审核"),
    REJECTED("已驳回"),
    ON_SHELF("已上架"),
    OFF_SHELF("已下架"),
    DELETED("已删除");

    private final String description;
}

