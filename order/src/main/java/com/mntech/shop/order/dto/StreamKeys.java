package com.mntech.shop.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

public final class StreamKeys  {
    public static final String INVENTORY_STREAM = "seckill:inventory:stream";
    public static final String INVENTORY_GROUP  = "inventory-group";
    public static final String INVENTORY_DLQ    = "seckill:inventory:dlq"; // 死信流
    public static final int    MAX_LEN          = 10000; // 流最大长度，防止无限增长
    private StreamKeys() {}
}
