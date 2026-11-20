package com.mntech.shop.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyTeamBriefResponse {
    private String id;
    private String groupNo;
    private String activityId;
    private String groupBuyProductId;
    private String leaderUserId;
    private String leaderNickname;
    private String leaderAvatar;
    private Integer targetNum;
    private Integer currentNum;
    private String status;
    private String statusDesc;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private Long remainingSeconds;
    private String productName;
    private String productImage;
    private BigDecimal groupPrice;
}