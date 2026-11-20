package com.mntech.shop.order.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户抽奖次数实体
 */
@Data
@TableName("user_lottery_quota")
public class UserLotteryQuota {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("user_id")
    private String userId;

    @TableField("total_quota")
    private Integer totalQuota;

    @TableField("used_quota")
    private Integer usedQuota;

    @TableField("remaining_quota")
    private Integer remainingQuota;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
