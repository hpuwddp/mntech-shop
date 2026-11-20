package com.mntech.shop.order.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 奖品实体
 */
@Data
@TableName("prize")
public class Prize {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField("name")
    private String name;

    @TableField("image")
    private String image;

    @TableField("description")
    private String description;

    @TableField("level")
    private Integer level;

    @TableField("probability")
    private BigDecimal probability;

    @TableField("total_quantity")
    private Integer totalQuantity;

    @TableField("remaining_quantity")
    private Integer remainingQuantity;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("status")
    private String status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
