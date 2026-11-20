package com.mntech.shop.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DistributionCenterResponse {

    /** 我的上级用户ID（无则 null） */
    private String parentUserId;

    /** 我的上级昵称（无则 null） */
    private String parentNickname;

    /** 可提现余额 */
    private BigDecimal availableAmount;

    /** 提现中金额 */
    private BigDecimal lockedAmount;

    /** 已提现金额 */
    private BigDecimal withdrawnAmount;

    /** 累计收益 */
    private BigDecimal totalEarned;

    /** 一级下线人数 */
    private Long level1Count;

    /** 二级下线人数 */
    private Long level2Count;

    /** 三级下线人数 */
    private Long level3Count;

    /** 最低提现金额 */
    private BigDecimal minWithdrawalAmount;

    /** 一级佣金比例（展示） */
    private BigDecimal level1Rate;

    /** 二级佣金比例（展示） */
    private BigDecimal level2Rate;

    /** 三级佣金比例（展示） */
    private BigDecimal level3Rate;
}