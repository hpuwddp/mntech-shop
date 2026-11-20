package com.mntech.shop.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抽奖结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryResultResponse {
    private Boolean isWin;           // 是否中奖
    private String prizeId;          // 奖品ID
    private String prizeName;        // 奖品名称
    private String prizeImage;       // 奖品图片
    private Integer prizeLevel;      // 奖项等级
    private String prizeLevelDesc;   // 奖项等级描述
    private String message;          // 提示信息
    private Integer remainingQuota;  // 剩余抽奖次数
}
