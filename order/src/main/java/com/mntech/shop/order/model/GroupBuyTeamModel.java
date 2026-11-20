package com.mntech.shop.order.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("group_buy_team")
public class GroupBuyTeamModel {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("group_no")
    private String groupNo;

    @TableField("activity_id")
    private String activityId;

    @TableField("group_buy_product_id")
    private String groupBuyProductId;

    @TableField("leader_user_id")
    private String leaderUserId;

    @TableField("current_num")
    private Integer currentNum;

    @TableField("target_num")
    private Integer targetNum;

    @TableField("status")
    private String status; // ONGOING(进行中), SUCCESS(已成团), FAILED(未成团), CLOSED(已关闭)

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}