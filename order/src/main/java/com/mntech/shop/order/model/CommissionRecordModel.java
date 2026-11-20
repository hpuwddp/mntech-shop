package com.mntech.shop.order.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分销佣金记录表
 * 每笔订单按分销层级各生成一条（level 1/2/3）
 * 状态：PENDING(待入账) / AVAILABLE(可提现) / LOCKED(提现中) / WITHDRAWN(已提现) / CANCELLED(已作废)
 */
@Data
@TableName("commission_record")
public class CommissionRecordModel {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 来源订单号 */
    @TableField("order_no")
    private String orderNo;

    /** 下单人（买家） */
    @TableField("buyer_user_id")
    private String buyerUserId;

    /** 分销层级：1/2/3 */
    @TableField("level")
    private Integer level;

    /** 收益用户（上级） */
    @TableField("user_id")
    private String userId;

    /** 订单实付金额快照 */
    @TableField("order_amount")
    private BigDecimal orderAmount;

    /** 佣金比例（如 0.10 表示 10%） */
    @TableField("rate")
    private BigDecimal rate;

    /** 佣金金额 */
    @TableField("commission_amount")
    private BigDecimal commissionAmount;

    /** 状态：PENDING/AVAILABLE/LOCKED/WITHDRAWN/CANCELLED */
    @TableField("status")
    private String status;

    /** 提现单ID（LOCKED/WITHDRAWN 时关联） */
    @TableField("withdrawal_id")
    private String withdrawalId;

    /** 入账时间（确认收货） */
    @TableField("settled_time")
    private LocalDateTime settledTime;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}