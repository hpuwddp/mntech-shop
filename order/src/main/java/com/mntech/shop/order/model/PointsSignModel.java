package com.mntech.shop.order.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户签到记录表（同一用户同一天只能签一次）
 */
@Data
@TableName("points_sign")
public class PointsSignModel {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("user_id")
    private String userId;

    /** 签到日期（服务器时区） */
    @TableField("sign_date")
    private LocalDate signDate;

    /** 本次签到获得积分（含连续奖励） */
    @TableField("points")
    private Integer points;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}