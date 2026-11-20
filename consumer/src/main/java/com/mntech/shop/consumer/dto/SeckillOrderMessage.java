package com.mntech.shop.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String orderId;
    private String userId;
    private String seckillProductId;
    private String activityId;
    private Integer quantity;
    private Long createTime;
    private String messageType; // CHECK, PAY, CANCEL
}