package com.mntech.shop.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyMemberResponse {
    private String userId;
    private String nickname;
    private String avatar;
    private Boolean isLeader;
    private String orderId;
    private Integer quantity;
    private String orderStatus;
    private String orderStatusDesc;
    private LocalDateTime createdAt;
}