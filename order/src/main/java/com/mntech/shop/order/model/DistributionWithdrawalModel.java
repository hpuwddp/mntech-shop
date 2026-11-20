package com.mntech.shop.order.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分销提现申请表
 * 状态：PENDING(待审核) / APPROVED(已通过) / REJECTED(已拒绝)
 */
@Data
@TableName("distribution_withdrawal")
public class DistributionWithdrawalModel {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 提现用户 */
    @TableField("user_id")
    private String userId;

    /** 提现金额 */
    @TableField("amount")
    private BigDecimal amount;

    /** 状态：PENDING/APPROVED/REJECTED */
    @TableField("status")
    private String status;

    /** 申请时间 */
    @TableField("apply_time")
    private LocalDateTime applyTime;

    /** 审核时间 */
    @TableField("review_time")
    private LocalDateTime reviewTime;

    /** 审核人 */
    @TableField("reviewer")
    private String reviewer;

    /** 备注 */
    @TableField("remark")
    private String remark;

    /** 打款状态：NONE(未打款)/PENDING(打款中)/SUCCESS(打款成功)/FAILED(打款失败) */
    @TableField("payout_status")
    private String payoutStatus;

    /** 打款时间 */
    @TableField("payout_time")
    private LocalDateTime payoutTime;

    /** 打款流水号（渠道返回） */
    @TableField("payout_no")
    private String payoutNo;

    /** 打款失败原因 */
    @TableField("fail_reason")
    private String failReason;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}