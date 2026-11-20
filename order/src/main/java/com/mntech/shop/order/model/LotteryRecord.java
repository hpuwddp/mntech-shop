package com.mntech.shop.order.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 抽奖记录实体
 */
@Data
@TableName("lottery_record")
public class LotteryRecord {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("user_id")
    private String userId;

    @TableField("prize_id")
    private String prizeId;

    @TableField("prize_name")
    private String prizeName;

    @TableField("prize_level")
    private Integer prizeLevel;

    @TableField("prize_image")
    private String prizeImage;

    @TableField("status")
    private String status;  // PENDING-待支付, PAID-已支付, CANCELLED-已取消, REFUND-已退款

    @TableField("is_win")
    private Boolean isWin;

    @TableField("remark")
    private String remark;
    @TableField("receiver_name")
    private String receiverName;

    @TableField("receiver_phone")
    private String receiverPhone;

    @TableField("receiver_address")
    private String receiverAddress;
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
