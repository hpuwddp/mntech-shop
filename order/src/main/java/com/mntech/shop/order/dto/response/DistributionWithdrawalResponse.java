package com.mntech.shop.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class DistributionWithdrawalResponse {

    private String id;

    /** 提现金额 */
    private BigDecimal amount;

    /** 状态：PENDING/APPROVED/REJECTED */
    private String status;

    /** 状态说明 */
    private String statusDesc;

    /** 申请时间 */
    private LocalDateTime applyTime;

    /** 审核时间 */
    private LocalDateTime reviewTime;

    /** 审核人 */
    private String reviewer;

    /** 备注 */
    private String remark;

    /** 打款状态：NONE/PENDING/SUCCESS/FAILED */
    private String payoutStatus;

    /** 打款状态说明 */
    private String payoutStatusDesc;

    /** 打款时间 */
    private LocalDateTime payoutTime;

    /** 打款流水号 */
    private String payoutNo;

    /** 打款失败原因 */
    private String failReason;
}