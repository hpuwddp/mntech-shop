package com.mntech.shop.order.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分变动流水表（签到、连续奖励等明细）
 */
@Data
@TableName("points_record")
public class PointsRecordModel {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("user_id")
    private String userId;

    /** 类型：SIGN(每日签到)/SIGN_BONUS(连续签到奖励) */
    @TableField("change_type")
    private String changeType;

    /** 变动积分（正数） */
    @TableField("points")
    private Integer points;

    /** 备注 */
    @TableField("remark")
    private String remark;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}