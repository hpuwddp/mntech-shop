package com.mntech.shop.order.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分销上下级关系表
 * 每个用户最新一条上级记录，可随时更换（换绑即修改 parent_id）
 */
@Data
@TableName("distribution_relation")
public class DistributionRelationModel {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 当前用户（下级） */
    @TableField("user_id")
    private String userId;

    /** 上级（推荐人） */
    @TableField("parent_id")
    private String parentId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}