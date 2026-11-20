package com.mntech.shop.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CommissionRecordResponse {

    private String id;

    /** 来源订单号 */
    private String orderNo;

    /** 分销层级 1/2/3 */
    private Integer level;

    /** 订单实付金额 */
    private BigDecimal orderAmount;

    /** 佣金比例 */
    private BigDecimal rate;

    /** 佣金金额 */
    private BigDecimal commissionAmount;

    /** 状态：PENDING/AVAILABLE/LOCKED/WITHDRAWN/CANCELLED */
    private String status;

    /** 状态说明 */
    private String statusDesc;

    /** 入账时间 */
    private LocalDateTime settledTime;

    /** 生成时间 */
    private LocalDateTime createdAt;
}