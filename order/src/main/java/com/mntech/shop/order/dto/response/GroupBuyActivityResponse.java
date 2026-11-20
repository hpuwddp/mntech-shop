package com.mntech.shop.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyActivityResponse {
    private String id;
    private String name;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String statusDesc;
    private Integer productCount;
    private Integer totalStock;
    private Integer totalRemainingStock;
    private Boolean isOngoing;
    private Boolean isEnded;
    private Long remainingTime;
    private List<GroupBuyProductResponse> products;
}