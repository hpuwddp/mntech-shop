package com.mntech.shop.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SkuStatus {
    ACTIVE("有效"),
    INACTIVE("无效"),
    DELETED("已删除");

    private final String description;
}
