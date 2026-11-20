package com.mntech.shop.order.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 抽奖记录响应DTO
 */
@Data
public class LotteryRecordResponse {
    private String id;
    private String prizeName;
    private Integer prizeLevel;
    private String prizeLevelDesc;
    private String prizeImage;
    private Boolean isWin;
    private LocalDateTime createdAt;
    private String status;
}
