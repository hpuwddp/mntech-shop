package com.mntech.shop.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private String orderId;
    private String userId;
    private Long createTime;
    private String messageType; // CREATE, PAY, CANCEL
}
