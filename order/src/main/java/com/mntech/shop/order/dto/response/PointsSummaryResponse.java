package com.mntech.shop.order.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 积分汇总
 */
@Data
@Builder
public class PointsSummaryResponse {

    /** 累计总积分 */
    private Integer totalPoints;

    /** 今日是否已签到 */
    private Boolean todaySigned;

    /** 连续签到天数（已签今日则含今日） */
    private Integer consecutiveDays;

    /** 最近签到日期（yyyy-MM-dd，未签到为 null） */
    private String lastSignDate;

    /** 每日签到积分 */
    private Integer dailyPoints;

    /** 连续满 7 天奖励积分 */
    private Integer weekBonus;
}